package nl.weeaboo.vn.impl.sound;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.junit.Assert;

import nl.weeaboo.vn.sound.ISound;
import nl.weeaboo.vn.sound.ISoundController;
import nl.weeaboo.vn.sound.SoundType;

class MockSoundController implements ISoundController {

    private static final long serialVersionUID = 1L;

    private final AtomicInteger updateCount = new AtomicInteger();
    private final AtomicInteger stopAllCount = new AtomicInteger();

    private final Map<SoundType, Double> masterVolume = new EnumMap<>(SoundType.class);

    @Override
    public void update() {
        updateCount.incrementAndGet();
    }

    void consumeUpdateCount(int expected) {
        Assert.assertEquals(expected, updateCount.getAndSet(0));
    }

    @Override
    public void stopAll() {
        stopAllCount.incrementAndGet();
    }

    void consumeStopAllCount(int expected) {
        Assert.assertEquals(expected, stopAllCount.getAndSet(0));
    }

    @Override
    public void stop(int channel) {
    }

    @Override
    public void stop(int channel, int fadeOutMillis) {
    }

    @Override
    public @Nullable ISound get(int channel) {
        return null;
    }

    @Override
    public double getMasterVolume(SoundType type) {
        return masterVolume.getOrDefault(type, 1.0);
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public int getFreeChannel() {
        return 123;
    }

    @Override
    public void set(int channel, ISound sound) {
    }

    @Override
    public void setMasterVolume(SoundType type, double volume) {
        masterVolume.put(type, volume);
    }

    @Override
    public void setPaused(boolean p) {
    }

}