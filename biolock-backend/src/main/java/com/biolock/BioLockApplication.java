package com.biolock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.opencv.core.Core;

@SpringBootApplication
public class BioLockApplication {

    static {
        try {
            // Load from your DLL path
            System.load("C:\\opencv\\build\\java\\x64\\opencv_java460.dll");
            System.out.println("✅ OpenCV loaded successfully!");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("⚠️ Failed to load from path: " + e.getMessage());
            try {
                // Try global fallback
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                System.out.println("✅ OpenCV loaded via system library fallback.");
            } catch (Exception ex) {
                System.err.println("❌ OpenCV could not be loaded at all!");
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BioLockApplication.class, args);
    }
}
