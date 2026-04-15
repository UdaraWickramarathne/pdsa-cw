package com.pdsa;

/**
 * Plain entry point that does NOT extend javafx.application.Application.
 * This is required when running JavaFX from a fat/uber JAR — without this wrapper
 * the JVM throws "JavaFX runtime components are missing" even when they are present.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
