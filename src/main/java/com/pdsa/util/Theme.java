package com.pdsa.util;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared theme helper — call {@link #apply(Stage, Scene)} from every GameNApp
 * and RecordsView to attach the stylesheet, load fonts, and set the stage icon.
 */
public final class Theme {

    private static final AtomicBoolean fontsLoaded = new AtomicBoolean(false);

    private Theme() {}

    /**
     * Attaches the shared stylesheet to {@code scene}, loads custom fonts
     * (idempotent), and sets the app icon on {@code stage}.
     */
    public static void apply(Stage stage, Scene scene) {
        loadFonts();

        String css = Theme.class.getResource("/com/pdsa/style.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        try (InputStream icon = Theme.class.getResourceAsStream("/com/pdsa/icon.png")) {
            if (icon != null) {
                stage.getIcons().add(new Image(icon));
            }
        } catch (Exception ignored) {}
    }

    /** Loads custom fonts once per JVM run. */
    public static void loadFonts() {
        if (fontsLoaded.compareAndSet(false, true)) {
            loadFont("/com/pdsa/fonts/PressStart2P-Regular.ttf");
            loadFont("/com/pdsa/fonts/VT323-Regular.ttf");
            loadFont("/com/pdsa/fonts/Outfit-Variable.ttf");
        }
    }

    private static void loadFont(String path) {
        try (InputStream in = Theme.class.getResourceAsStream(path)) {
            if (in != null) {
                Font.loadFont(in, 12);
            } else {
                System.err.println("[Theme] Font not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("[Theme] Could not load font " + path + ": " + e.getMessage());
        }
    }
}
