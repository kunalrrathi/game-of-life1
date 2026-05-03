package utilities;

import javafx.scene.media.AudioClip;

public class SpinSoundGenerator {

    public static void playSpinSound() {
        AudioClip clip = new AudioClip(SpinSoundGenerator.class.getResource("/org/gameoflife/spin.mp3").toExternalForm());
        clip.play();
    }
}