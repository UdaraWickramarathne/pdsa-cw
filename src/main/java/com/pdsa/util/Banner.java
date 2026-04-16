package com.pdsa.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Inline notification banner — replaces modal {@code Alert} dialogs.
 *
 * <p>Usage in a controller: inject {@code @FXML HBox bannerBox} and
 * {@code @FXML Label bannerLabel} from FXML, then call:
 * <pre>{@code Banner.show(bannerBox, bannerLabel, Banner.Kind.ERROR, "message"); }</pre>
 */
public final class Banner {

    public enum Kind { ERROR, WARN, INFO }

    private Banner() {}

    /**
     * Shows the banner with the given message and kind. Auto-dismisses after 4 s.
     */
    public static void show(HBox bannerBox, Label bannerLabel, Kind kind, String message) {
        // Reset state
        bannerBox.getStyleClass().removeAll("error", "warn", "info");
        bannerLabel.getStyleClass().removeAll("error", "warn", "info");

        String styleKey = switch (kind) {
            case ERROR -> "error";
            case WARN  -> "warn";
            case INFO  -> "info";
        };
        bannerBox.getStyleClass().add(styleKey);
        bannerLabel.getStyleClass().add(styleKey);
        bannerLabel.setText(message);

        bannerBox.setVisible(true);
        bannerBox.setManaged(true);
        bannerBox.setOpacity(1.0);

        // Auto-dismiss
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> hide(bannerBox));
        pause.play();
    }

    /** Hides the banner with a fade-out. */
    public static void hide(HBox bannerBox) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), bannerBox);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            bannerBox.setVisible(false);
            bannerBox.setManaged(false);
            bannerBox.setOpacity(1.0);
        });
        ft.play();
    }

    /**
     * Builds a pre-styled banner HBox + label ready to be placed at the top
     * of a game's root VBox. Wire to FXML via {@code fx:id="bannerBox"} and
     * {@code fx:id="bannerLabel"} on the inner label, or use programmatically.
     */
    public static HBox buildBannerRow(Label bannerLabel) {
        HBox box = new HBox(8);
        box.getStyleClass().add("inline-banner");
        box.setVisible(false);
        box.setManaged(false);

        bannerLabel.getStyleClass().add("banner-label");
        HBox.setHgrow(bannerLabel, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().add("btn-ghost");
        close.setStyle("-fx-padding: 0 6 0 6; -fx-font-size: 11px;");
        close.setOnAction(e -> hide(box));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(bannerLabel, spacer, close);
        return box;
    }
}
