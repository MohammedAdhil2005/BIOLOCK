package com.biolock.service;

import org.opencv.core.*;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.Base64;
import java.util.Arrays;

public class RetinaMatcher {

    // Path to Haarcascade for eye detection
    private static final String EYE_CASCADE_PATH = "haarcascades/haarcascade_eye.xml";
    private static final CascadeClassifier eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);

    // ✅ Detect and crop the eye region
    private static Mat detectAndCropEye(Mat img) {
        if (img.channels() == 3) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        }

        MatOfRect eyes = new MatOfRect();
        eyeDetector.detectMultiScale(img, eyes, 1.1, 5, 0, new Size(30, 30), new Size());

        if (eyes.toArray().length == 0) {
            System.out.println("❌ No eyes detected.");
            return new Mat(); // empty Mat
        }

        // Pick the first detected eye (you can improve this by picking the largest one)
        Rect eyeRect = eyes.toArray()[0];
        return new Mat(img, eyeRect);
    }

    // ✅ CLAHE for lighting normalization
    private static Mat applyCLAHE(Mat img) {
        if (img.channels() == 3) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        }
        Imgproc.resize(img, img, new Size(300, 300)); // consistent size
        var clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(2.0);
        Mat result = new Mat();
        clahe.apply(img, result);
        return result;
    }

    public static boolean match(String username, String base64Image) {
        try {
            // Decode incoming image from Base64
            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
            Mat inputImage = Imgcodecs.imdecode(new MatOfByte(decodedBytes), Imgcodecs.IMREAD_COLOR);

            if (inputImage.empty()) {
                System.out.println("❌ Input image is empty or unreadable.");
                return false;
            }

            // Load stored retina image
            String storedPath = "images/" + username + ".png";
            Mat storedImage = Imgcodecs.imread(storedPath, Imgcodecs.IMREAD_COLOR);

            if (storedImage.empty()) {
                System.out.println("❌ Stored retina image not found: " + storedPath);
                return false;
            }

            // ✅ Detect and crop eyes
            inputImage = detectAndCropEye(inputImage);
            storedImage = detectAndCropEye(storedImage);

            if (inputImage.empty() || storedImage.empty()) {
                System.out.println("❌ Could not find eye region in one or both images.");
                return false;
            }

            // ✅ Apply CLAHE
            inputImage = applyCLAHE(inputImage);
            storedImage = applyCLAHE(storedImage);

            // ORB Feature Detection
            ORB orb = ORB.create();
            MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
            Mat descriptors1 = new Mat();
            orb.detectAndCompute(inputImage, new Mat(), keypoints1, descriptors1);

            MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
            Mat descriptors2 = new Mat();
            orb.detectAndCompute(storedImage, new Mat(), keypoints2, descriptors2);

            if (descriptors1.empty() || descriptors2.empty()) {
                System.out.println("❌ No keypoints detected in one or both images.");
                return false;
            }

            // Match descriptors
            BFMatcher matcher = BFMatcher.create(Core.NORM_HAMMING, true);
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors1, descriptors2, matches);

            DMatch[] allMatches = matches.toArray();
            Arrays.sort(allMatches, (m1, m2) -> Float.compare(m1.distance, m2.distance));

            long goodMatches = Arrays.stream(allMatches)
                    .limit(30)
                    .filter(m -> m.distance < 50)
                    .count();

            System.out.println("🔍 Matching user: " + username);
            System.out.println("✅ Total Matches: " + matches.size().height);
            System.out.println("🎯 Good Matches (<40 distance from top 30): " + goodMatches);

            return goodMatches >= 10; // adjust threshold as needed

        } catch (Exception e) {
            System.out.println("🔥 Exception during retina matching: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
