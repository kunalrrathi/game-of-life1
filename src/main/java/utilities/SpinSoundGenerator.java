package utilities;

import javax.sound.sampled.*;

public class SpinSoundGenerator {

    public static void playSpinSound() {
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buf = new byte[1];

                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                // simulate spin ticks
                for (int i = 0; i < 30; i++) {

                    int duration = 20 + (i * 5); // slowing down

                    for (int j = 0; j < duration * (int)(sampleRate / 1000); j++) {
                        double angle = j / (sampleRate / 440) * 2.0 * Math.PI;
                        buf[0] = (byte)(Math.sin(angle) * 100);
                        sdl.write(buf, 0, 1);
                    }

                    Thread.sleep(10 + i * 5); // increasing delay (slowdown effect)
                }

                sdl.drain();
                sdl.stop();
                sdl.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}