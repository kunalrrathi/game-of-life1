package org.gameoflife;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Random;
import java.util.function.IntConsumer;

import static utilities.SpinSoundGenerator.playSpinSound;

public class SpinnerController {

    private final StackPane spinnerContainer;
    private double currentAngle = 0;

    public SpinnerController(StackPane spinnerContainer) {
        this.spinnerContainer = spinnerContainer;
    }

    public void spin(IntConsumer callback) {

        int steps = 4; //new Random().nextInt(10) + 1;

        spinnerContainer.setDisable(true);

        double anglePerStep = 36;
        double wheelAngle = (steps - 1) * anglePerStep;

        double arrowOffset = 180;
        double baseOffset = 90;

        // exact landing angle for chosen number
        double landingAngle =
                arrowOffset + wheelAngle - baseOffset;

        // normalize current position to 0..360
        double normalizedCurrent =
                ((currentAngle % 360) + 360) % 360;

        // how much more clockwise needed
        double delta = landingAngle - normalizedCurrent;

        if (delta < 0) {
            delta += 360;
        }

        // always add full spins
        double spinTurns = 360 * (4 + new Random().nextInt(3));

        double targetAngle = currentAngle + spinTurns + delta;

        RotateTransition rotate =
                new RotateTransition(Duration.seconds(2), spinnerContainer);

        rotate.setFromAngle(currentAngle);
        rotate.setToAngle(targetAngle);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        playSpinSound();

        rotate.setOnFinished(e -> {

            currentAngle = targetAngle; // save new position

            spinnerContainer.setDisable(false);

            callback.accept(steps);
        });

        rotate.play();
    }
}