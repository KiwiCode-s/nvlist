package nl.weeaboo.vn.impl.sound;

import nl.weeaboo.vn.impl.sound.INativeAudio;

public class MockNativeAudio implements INativeAudio {

    private static final long serialVersionUID = 1L;

    private int loopsLeft;
    private boolean paused;
    private double volume;

    @Override
    public void play(int loops) {
        loopsLeft = loops;
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void stop(int fadeOutMillis) {
        loopsLeft = 0;
        paused = false;
    }

    @Override
    public boolean isPlaying() {
        return loopsLeft != 0;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public int getLoopsLeft() {
        return loopsLeft;
    }

    @Override
    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getVolume() {
        return volume;
    }

}