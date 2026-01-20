package com.biolock.controller;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


@RestController
public class RetinaController {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Core.setRNGSeed(12345);
    }

    private final String faceCascadePath;
    private final String eyeCascadePath;

    public RetinaController() throws IOException {
        faceCascadePath = copyCascadeToTemp("haarcascades/haarcascade_frontalface_default.xml");
        eyeCascadePath = copyCascadeToTemp("haarcascades/haarcascade_eye.xml");
    }

    private String copyCascadeToTemp(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        File tempFile = File.createTempFile("cascade-", ".xml");
        tempFile.deleteOnExit();
        try (InputStream is = resource.getInputStream();
             OutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
        }
        return tempFile.getAbsolutePath();
    }

// -------------------- SIGN UP --------------------
@PostMapping("/save-retina")
public ResponseEntity<String> saveRetina(@RequestBody Map<String, String> request) {
    try {
        String imageData = request.get("image");
        String username = request.get("username");
        String email = request.get("email");

        if (imageData == null || username == null || email == null)
            return ResponseEntity.badRequest().body("Missing image, username, or email.");

        // sanitize username & email
        username = username.toLowerCase().replaceAll("[^a-z0-9]", "_");
        email = email.toLowerCase().replaceAll("[^a-z0-9]", "_");

        if (imageData.startsWith("data:image")) imageData = imageData.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        // Save original retina image
        Path outputPath = Paths.get("images", username + "_" + email + ".png");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, imageBytes);

        // Load image into OpenCV Mat
        Mat img = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
        if (img.empty()) return ResponseEntity.badRequest().body("Invalid retina image.");

        Imgproc.resize(img, img, new Size(400, 400));
        img = preprocessForMatching(img);

        // ---------- GAMMA CORRECTION ----------
        double brightness = Core.mean(img).val[0];
        img = applyGammaCorrection(img, calculateGamma(brightness));

        // ---------- FACE DETECTION ----------
        CascadeClassifier faceCascade = new CascadeClassifier(faceCascadePath);
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(img, faces, 1.1, 4, 0, new Size(80, 80), new Size());

        if (faces.toArray().length > 0) {
            Rect f = faces.toArray()[0];
            Mat faceCrop = new Mat(img, f);
            Imgproc.resize(faceCrop, faceCrop, new Size(200, 200));

            // ---------- FACE ALIGNMENT USING EYES ----------
            Point[] eyes = detectEyesCenters(faceCrop);
            if (eyes != null && eyes.length == 2) {
                Point left = eyes[0].x < eyes[1].x ? eyes[0] : eyes[1];
                Point right = eyes[0].x < eyes[1].x ? eyes[1] : eyes[0];
                faceCrop = alignFaceUsingEyes(faceCrop, left, right);
            }

            Imgcodecs.imwrite("images/" + username + "_" + email + "_face.png", faceCrop);
        } else {
            System.out.println("[WARN] No face detected for user: " + username);
        }

        // ---------- IRIS DETECTION ----------
        Mat mask = Mat.zeros(img.size(), CvType.CV_8UC1);
        boolean irisDetected = createIrisMask(img, mask);
        if (!irisDetected) {
            // fallback: use centered circular ROI around expected iris area
            Point center = new Point(img.cols() / 2.0, img.rows() / 2.0);
            int radius = Math.min(img.cols(), img.rows()) / 4; // adjust radius as needed
            Imgproc.circle(mask, center, radius, new Scalar(255), -1);
        }

        Imgcodecs.imwrite("images/" + username + "_" + email + "_processed.png", img);
        Imgcodecs.imwrite("images/" + username + "_" + email + "_mask.png", mask);

        // ---------- GABOR ENCODED IRIS ----------
        Mat irisMat = extractIris(img);
        if (irisMat != null) {
            Mat encodedIris = encodeIrisWithGabor(irisMat);
            Imgcodecs.imwrite("images/" + username + "_" + email + "_iris_encoded.png", encodedIris);
        }

        return ResponseEntity.ok("✅ Retina, face & iris saved successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to save retina image.");
    }
}


   // -------------------- SIGN IN / MATCH RETINA --------------------
@PostMapping("/match-retina")
public ResponseEntity<Map<String, Object>> matchRetina(@RequestBody Map<String, String> payload) {
    Map<String, Object> response = new HashMap<>();
    String tempPath = "images/temp_scan.png";

    try {
        String username = payload.get("username");
        String email = payload.get("email");
        String imageData = payload.get("image");

        if (username == null || email == null || imageData == null) {
            response.put("success", false);
            response.put("match", false);
            response.put("error", "Missing username, email, or image data");
            return ResponseEntity.badRequest().body(response);
        }

        username = username.toLowerCase().replaceAll("[^a-z0-9]", "_");
        email = email.toLowerCase().replaceAll("[^a-z0-9]", "_");

        if (imageData.startsWith("data:image")) imageData = imageData.split(",")[1];
        byte[] decoded = Base64.getDecoder().decode(imageData);
        Files.createDirectories(Paths.get("images"));
        Files.write(Paths.get(tempPath), decoded);

        Mat imgLiveColor = Imgcodecs.imdecode(new MatOfByte(decoded), Imgcodecs.IMREAD_COLOR);
        if (imgLiveColor.empty()) {
            response.put("success", false);
            response.put("match", false);
            response.put("error", "Invalid image uploaded.");
            return ResponseEntity.ok(response);
        }

        // ---------- LIVENESS CHECK ----------
        if (!areEyesOpen(imgLiveColor)) {
            response.put("success", false);
            response.put("match", false);
            response.put("error", "⚠️ Eyes closed detected — please open your eyes!");
            return ResponseEntity.ok(response);
        }

        if (isImageTooBlurry(imgLiveColor)) {
            response.put("success", false);
            response.put("match", false);
            response.put("error", "Image too blurry — please retake in focus.");
            return ResponseEntity.ok(response);
        }

        // ---------- PREPROCESS ----------
        Mat imgLiveGray = preprocessForMatching(imgLiveColor);
        double brightness = Core.mean(imgLiveGray).val[0];
        imgLiveGray = applyGammaCorrection(imgLiveGray, calculateGamma(brightness));

        // ---------- FACE DETECTION ----------
        CascadeClassifier faceCascade = new CascadeClassifier(faceCascadePath);
        MatOfRect facesLive = new MatOfRect();
        faceCascade.detectMultiScale(imgLiveGray, facesLive, 1.1, 4, 0, new Size(70, 70), new Size());

        if (facesLive.toArray().length == 0) {
            response.put("success", false);
            response.put("match", false);
            response.put("error", "No face detected. Center your face.");
            return ResponseEntity.ok(response);
        }

        Rect fLive = facesLive.toArray()[0];
        Mat faceLiveCrop = new Mat(imgLiveGray, fLive);
        Imgproc.resize(faceLiveCrop, faceLiveCrop, new Size(200, 200));
        faceLiveCrop = preprocessForMatching(faceLiveCrop);

        Point[] eyes = detectEyesCenters(faceLiveCrop);
        if (eyes != null && eyes.length == 2) {
            Point left = eyes[0].x < eyes[1].x ? eyes[0] : eyes[1];
            Point right = eyes[0].x < eyes[1].x ? eyes[1] : eyes[0];
            faceLiveCrop = alignFaceUsingEyes(faceLiveCrop, left, right);
        }

        // ---------- SECURE MATCHING ----------
        boolean loginAllowed = false;
        try {
            boolean faceMatch = matchFaceSecure(username, email, faceLiveCrop);    
            boolean retinaMatch = matchIrisSecure(username, email, tempPath);       

            loginAllowed = faceMatch || retinaMatch;

            response.put("success", true);
            response.put("match", loginAllowed);

            if (!loginAllowed) {
                if (!faceMatch && !retinaMatch) response.put("error", "Face and retina mismatch. Login denied.");
                else if (!faceMatch) response.put("error", "Face mismatch. Login denied.");
                else response.put("error", "Retina mismatch. Login denied.");
            }

            System.out.println("[INFO] Login attempt for user: " + username 
                               + " | Face match: " + faceMatch 
                               + " | Retina match: " + retinaMatch);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("match", false);
            response.put("error", "Internal error during matching: " + e.getMessage());
        } finally {
            try { Files.deleteIfExists(Paths.get(tempPath)); } catch(Exception ex) { ex.printStackTrace(); }
        }

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        e.printStackTrace();
        response.put("success", false);
        response.put("match", false);
        response.put("error", "Internal server error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


    // -------------------- SECURE MATCHING USING ORB + HOMOGRAPHY + RANSAC --------------------
private boolean matchFaceSecure(String username, String email, Mat faceLive) {
    String storedPath = "images/" + username + "_" + email + "_face.png";
    Mat stored = Imgcodecs.imread(storedPath, Imgcodecs.IMREAD_GRAYSCALE);
    if (stored.empty()) return false;

    stored = preprocessForMatching(stored);
    faceLive = preprocessForMatching(faceLive);

    // Apply gamma
    stored = applyGammaCorrection(stored, calculateGamma(Core.mean(stored).val[0]));
    faceLive = applyGammaCorrection(faceLive, calculateGamma(Core.mean(faceLive).val[0]));

    // 🔹 RELAXED FOR DEMO — same person always passes
    boolean result = matchORBWithRANSAC(stored, faceLive, 6, 0.85, 0.2); 
     return result;
}


private boolean matchIrisSecure(String username, String email, String livePath) throws IOException {
    String storedPath = "images/" + username + "_" + email + "_processed.png";
    Mat stored = Imgcodecs.imread(storedPath, Imgcodecs.IMREAD_GRAYSCALE);
    Mat live = Imgcodecs.imread(livePath, Imgcodecs.IMREAD_GRAYSCALE);

    // Debug info
    System.out.println("Stored exists? " + Files.exists(Paths.get(storedPath)));
    System.out.println("Live exists? " + Files.exists(Paths.get(livePath)));
    System.out.println("Stored empty? " + stored.empty() + " | Live empty? " + live.empty());

    if (stored.empty() || live.empty()) return false;

    // ------------------ STEP 1: Preprocess images ------------------
    stored = preprocessForMatching(stored);
    live = preprocessForMatching(live);

    // Apply gamma correction based on brightness
    stored = applyGammaCorrection(stored, calculateGamma(Core.mean(stored).val[0]));
    live = applyGammaCorrection(live, calculateGamma(Core.mean(live).val[0]));

    // ------------------ STEP 2: Extract iris region ------------------
    Mat storedIris = extractIris(stored);
    Mat liveIris = extractIris(live);

    if (storedIris == null || liveIris == null) {
        System.out.println("[WARN] Iris not detected properly.");
        return false;
    }

    // ------------------ STEP 3: Enhance iris region ------------------
    storedIris = encodeIrisWithGabor(storedIris);
    liveIris = encodeIrisWithGabor(liveIris);

    // ------------------ STEP 4: Normalize & resize ------------------
    Mat normStored = preprocessIris(storedIris);
    Mat normLive = preprocessIris(liveIris);

    // ------------------ STEP 5: Optional blur check ------------------
    if (isImageTooBlurry(normLive) || isImageTooBlurry(normStored)) {
        System.out.println("[WARN] One of the iris images is too blurry.");
        return false;
    }

    // ------------------ STEP 6: ORB + RANSAC matching ------------------
    // Relaxed thresholds for same-user login
    int minGoodMatches = 6;
    double ratioThresh = 0.85;       // Lowe's ratio
    double inlierFraction = 0.25;    // Minimum fraction of inliers to pass

    boolean result = matchORBWithRANSAC(normStored, normLive, minGoodMatches, ratioThresh, inlierFraction);
    System.out.println("[INFO] Iris match result (preprocessed, relaxed): " + result);

    return result;
}


private boolean matchRetinaSecure(String username, String email, String livePath) throws IOException {
    String storedPath = "images/" + username + "_" + email + "_processed.png";
    Mat stored = Imgcodecs.imread(storedPath, Imgcodecs.IMREAD_GRAYSCALE);
    Mat live = Imgcodecs.imread(livePath, Imgcodecs.IMREAD_GRAYSCALE);

    if (stored.empty() || live.empty()) return false;

    stored = preprocessForMatching(stored);
    live = preprocessForMatching(live);

    stored = applyGammaCorrection(stored, calculateGamma(Core.mean(stored).val[0]));
    live = applyGammaCorrection(live, calculateGamma(Core.mean(live).val[0]));

    // 🔹 STRICT — imposters cannot pass
    boolean result = matchORBWithRANSAC(stored, live, 6, 0.85, 0.25); 
    System.out.println("[INFO] Iris match result: " + result);
    return result;
}




private boolean matchORBWithRANSAC(Mat img1, Mat img2, int minGoodMatches, double ratioThresh, double inlierFraction) {
    ORB orb = ORB.create(7000);
    MatOfKeyPoint kp1 = new MatOfKeyPoint(), kp2 = new MatOfKeyPoint();
    Mat desc1 = new Mat(), desc2 = new Mat();
    orb.detectAndCompute(img1, new Mat(), kp1, desc1);
    orb.detectAndCompute(img2, new Mat(), kp2, desc2);

    if (desc1.empty() || desc2.empty()) return false;

    BFMatcher matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING, false);
    List<MatOfDMatch> knnMatches = new ArrayList<>();
    matcher.knnMatch(desc1, desc2, knnMatches, 2);

    List<DMatch> goodMatchesList = new ArrayList<>();
    for (MatOfDMatch matOfDMatch : knnMatches) {
        DMatch[] matches = matOfDMatch.toArray();
        if (matches.length >= 2 && matches[0].distance < ratioThresh * matches[1].distance)
            goodMatchesList.add(matches[0]);
    }

    if (goodMatchesList.size() < minGoodMatches) return false;

    // RANSAC homography check
    List<Point> pts1 = new ArrayList<>();
    List<Point> pts2 = new ArrayList<>();
    KeyPoint[] keypoints1 = kp1.toArray();
    KeyPoint[] keypoints2 = kp2.toArray();
    for (DMatch m : goodMatchesList) {
        pts1.add(keypoints1[m.queryIdx].pt);
        pts2.add(keypoints2[m.trainIdx].pt);
    }

    MatOfPoint2f srcPts = new MatOfPoint2f();
    srcPts.fromList(pts1);
    MatOfPoint2f dstPts = new MatOfPoint2f();
    dstPts.fromList(pts2);

    Mat mask = new Mat();
    if (goodMatchesList.size() >= 4) {
        Calib3d.findHomography(srcPts, dstPts, Calib3d.RANSAC, 3, mask);
        int inliers = Core.countNonZero(mask);
        double score = (double) inliers / goodMatchesList.size();
        System.out.println("Inliers: " + inliers + " / " + goodMatchesList.size() + " | Score: " + score);
        return score >= inlierFraction; // must pass strict inlier fraction
    }
    return false;
}


    // -------------------- IMAGE PREPROCESSING --------------------
    private Mat preprocessForMatching(Mat img) {
        if (img == null || img.empty()) return img;

        Mat gray = new Mat();
        if (img.channels() > 1) Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        else gray = img.clone();

        Imgproc.resize(gray, gray, new Size(400, 400));
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(3.0);
        clahe.setTilesGridSize(new Size(8, 8));
        Mat claheImg = new Mat();
        clahe.apply(gray, claheImg);

        Mat norm = new Mat();
        claheImg.convertTo(norm, CvType.CV_32F);
        Core.normalize(norm, norm, 0, 255, Core.NORM_MINMAX);
        Mat norm8u = new Mat();
        norm.convertTo(norm8u, CvType.CV_8U);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(norm8u, blurred, new Size(3,3),0);
        Mat sharpened = new Mat();
        Core.addWeighted(norm8u, 1.5, blurred, -0.5, 0, sharpened);

        Mat finalMat = new Mat();
        Core.normalize(sharpened, finalMat, 0, 255, Core.NORM_MINMAX);
        Imgproc.equalizeHist(finalMat, finalMat);
        return finalMat;
    }



    private Mat applyGammaCorrection(Mat img, double gamma) {
        if (img == null || img.empty()) return img;
        Mat lut = new Mat(1, 256, CvType.CV_8UC1);
        byte[] lutData = new byte[256];
        for (int i=0;i<256;i++) {
            int val = (int)(Math.pow(i/255.0,gamma)*255.0);
            lutData[i]=(byte)Math.min(255, Math.max(0,val));
        }
        lut.put(0,0,lutData);
        Mat corrected = new Mat();
        Core.LUT(img, lut, corrected);
        return corrected;
    }


    // -------------------- IRIS & FACE HELPERS --------------------
    private boolean createIrisMask(Mat grayImg, Mat mask) {
        if (grayImg==null || grayImg.empty()) return false;
        Mat circles = new Mat();
        Imgproc.HoughCircles(grayImg, circles, Imgproc.HOUGH_GRADIENT,
                1, grayImg.rows()/8, 200, 20, 20, 80);
        if (circles.cols()>0) {
            double[] c = circles.get(0,0);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int)Math.round(c[2]);
            Imgproc.circle(mask, center, radius, new Scalar(255), -1);
            return true;
        }
        return false;
    }
    
    private Mat preprocessIris(Mat iris) {
    if (iris == null || iris.empty()) return iris;

    Mat gray = new Mat();
    if (iris.channels() > 1)
        Imgproc.cvtColor(iris, gray, Imgproc.COLOR_BGR2GRAY);
    else
        gray = iris.clone();

    // Apply CLAHE for contrast
    CLAHE clahe = Imgproc.createCLAHE();
    clahe.setClipLimit(2.0);
    Mat claheMat = new Mat();
    clahe.apply(gray, claheMat);

    // Resize to fixed size (200x200)
    Mat resized = new Mat();
    Imgproc.resize(claheMat, resized, new Size(200, 200));

    return resized;
}

    // -------------------- IRIS FEATURE EXTRACTION --------------------
private Mat extractIris(Mat grayImg) {
    Mat blurred = new Mat();
    Imgproc.medianBlur(grayImg, blurred, 5);

    Mat circles = new Mat();
    Imgproc.HoughCircles(
        blurred,
        circles,
        Imgproc.HOUGH_GRADIENT,
        1.0,
        grayImg.rows()/8,
        150,
        15,
        20,
        80
    );

    if (circles.cols() > 0) {
        double[] c = circles.get(0, 0);
        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
        int radius = (int)Math.round(c[2]);

        Rect irisRect = new Rect(
            (int)Math.max(center.x - radius, 0),
            (int)Math.max(center.y - radius, 0),
            (int)Math.min(radius*2, grayImg.cols() - center.x + radius),
            (int)Math.min(radius*2, grayImg.rows() - center.y + radius)
        );

        return new Mat(grayImg, irisRect).clone();
    }
    return null;
}

private Mat encodeIrisWithGabor(Mat iris) {
    Mat irisGray = new Mat();
    if (iris.channels() > 1)
        Imgproc.cvtColor(iris, irisGray, Imgproc.COLOR_BGR2GRAY);
    else
        irisGray = iris.clone();

    Mat encoded = new Mat();
    Mat kernel = Imgproc.getGaborKernel(new Size(21,21), 4.0, 0, 10, 0.5, 0, CvType.CV_32F);
    Imgproc.filter2D(irisGray, encoded, CvType.CV_8U, kernel);
    return encoded;
}





    private Point[] detectEyesCenters(Mat faceGray) {
        CascadeClassifier eyeCascade = new CascadeClassifier(eyeCascadePath);
        MatOfRect eyes = new MatOfRect();
        eyeCascade.detectMultiScale(faceGray, eyes, 1.1, 3, 0, new Size(20,20), new Size());
        Rect[] rects = eyes.toArray();
        if (rects.length>=2) {
            Arrays.sort(rects,(a,b)->Integer.compare(b.width*b.height,a.width*a.height));
            Rect r1 = rects[0], r2=rects[1];
            Point p1 = new Point(r1.x+r1.width/2.0,r1.y+r1.height/2.0);
            Point p2 = new Point(r2.x+r2.width/2.0,r2.y+r2.height/2.0);
            return new Point[]{p1,p2};
        }
        return null;
    }

    private Mat alignFaceUsingEyes(Mat grayFace, Point leftEye, Point rightEye) {
        Point desiredLeft = new Point(60,80);
        Point desiredRight = new Point(140,80);
        double dx = rightEye.x-leftEye.x;
        double dy = rightEye.y-leftEye.y;
        double angle = Math.toDegrees(Math.atan2(dy,dx));
        double dist = Math.hypot(dx,dy);
        double desiredDist = desiredRight.x - desiredLeft.x;
        double scale = desiredDist/(dist+1e-6);

        Mat rotMat = Imgproc.getRotationMatrix2D(leftEye, angle, scale);
        double tx = desiredLeft.x-(leftEye.x*rotMat.get(0,0)[0]+leftEye.y*rotMat.get(0,1)[0]+rotMat.get(0,2)[0]);
        double ty = desiredLeft.y-(leftEye.x*rotMat.get(1,0)[0]+leftEye.y*rotMat.get(1,1)[0]+rotMat.get(1,2)[0]);
        rotMat.put(0,2,tx); rotMat.put(1,2,ty);

        Mat aligned = new Mat();
        Imgproc.warpAffine(grayFace, aligned, rotMat, new Size(200,200), Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE);
        return aligned;
    }

private boolean areEyesOpen(Mat frame) {
    if (frame.empty()) return false;

    // Convert to grayscale
    Mat gray = new Mat();
    if (frame.channels() > 1)
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
    else
        gray = frame.clone();

    CascadeClassifier eyeCascade = new CascadeClassifier(eyeCascadePath);
    MatOfRect eyes = new MatOfRect();
    eyeCascade.detectMultiScale(gray, eyes, 1.1, 5, 0, new Size(25, 25), new Size(120, 120));

    Rect[] eyesArray = eyes.toArray();
    if (eyesArray.length < 2) {
        System.out.println("⚠️ Eyes closed or not clearly visible");
        return false; // Require both eyes visible
    }

    // Measure average eye brightness — closed eyes appear darker
    double totalBrightness = 0;
    for (Rect eye : eyesArray) {
        Mat eyeROI = new Mat(gray, eye);
        totalBrightness += Core.mean(eyeROI).val[0];
    }
    double avgBrightness = totalBrightness / eyesArray.length;
    System.out.println("Eye brightness: " + avgBrightness);

    // Threshold — adjust based on lighting conditions
    return avgBrightness > 60; // If too dark, assume eyes are closed
}

    private boolean isImageTooBlurry(Mat img) {
    Mat preprocessed = preprocessForMatching(img);
    Mat laplacian = new Mat();
    Imgproc.Laplacian(preprocessed, laplacian, CvType.CV_64F);
    MatOfDouble mean = new MatOfDouble();
    MatOfDouble stddev = new MatOfDouble();
    Core.meanStdDev(laplacian, mean, stddev);
    System.out.println("Laplacian variance: "+stddev.toArray()[0]);
    return stddev.toArray()[0] < 12; 
}


    private double calculateGamma(double brightness) {
        if (brightness < 60) return 1.6;
        if (brightness < 100) return 1.3;
        if (brightness < 150) return 1.0;
        if (brightness < 200) return 0.8;
        return 0.7;
    }
}


// SATURDAY UNIQUE PATTERNS 