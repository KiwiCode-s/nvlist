package nl.weeaboo.vn.impl.sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.sound.ISound;
import nl.weeaboo.vn.sound.ISoundController;
import nl.weeaboo.vn.sound.SoundType;

final class SoundController implements ISoundController {

    private static final long serialVersionUID = SoundImpl.serialVersionUID;

    private static final Logger LOG = LoggerFactory.getLogger(SoundController.class);

    private final Map<SoundType, Double> masterVolume;
    private final Map<Integer, ISound> sounds = new HashMap<>();
    private final List<ISound> pausedList = new ArrayList<>();

    private boolean paused;

    SoundController() {
        masterVolume = new EnumMap<>(SoundType.class);
        for (SoundType type : SoundType.values()) {
            masterVolume.put(type, 1.0);
        }
    }

    @Override
    public void update() {
        checkSounds();
    }

    @Override
    public void checkSounds() {
        Iterator<Map.Entry<Integer, ISound>> itr = sounds.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Integer, ISound> entry = itr.next();
            ISound s = entry.getValue();
            s.update();
            if (s.isStopped()) {
                LOG.debug("Removing stopped sound from channel {}: {}", entry.getKey(), s);
                pausedList.remove(s);
                itr.remove();
            }
        }
    }

    @Override
    public void stopAll() {
        for (int channel : ImmutableList.copyOf(sounds.keySet())) {
            stop(channel);
        }
    }

    @Override
    public void stop(int channel) {
        stop(channel, -1);
    }

    @Override
    public void stop(int channel, int fadeOutFrames) {
        ISound sound = sounds.remove(channel);
        if (sound != null) {
            LOG.debug("Stop sound in channel {}: {}", channel, sound);
            sound.stop(fadeOutFrames);
        }

        checkSounds();
    }

    @Override
    public ISound get(int channel) {
        return sounds.get(channel);
    }

    @Override
    public double getMasterVolume(SoundType type) {
        Preconditions.checkNotNull(type);
        return masterVolume.get(type);
    }

    private Iterable<ISound> getSounds(SoundType type) {
        Collection<ISound> result = new ArrayList<>();
        for (ISound sound : sounds.values()) {
            if (sound.getSoundType() == type) {
                result.add(sound);
            }
        }
        return result;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public int getFreeChannel() {
        for (int n = MIN_CHANNEL; n <= MAX_CHANNEL; n++) {
            if (!sounds.containsKey(n)) {
                return n;
            }
        }
        throw new IllegalStateException("No free channels left");
    }

    @Override
    public void set(int channel, ISound sound) {
        Checks.checkState(!isPaused(), "Unable to add sounds while paused");
        Checks.checkRange(channel, "channel", MIN_CHANNEL, MAX_CHANNEL);

        stop(channel);

        if (sounds.containsKey(channel)) {
            throw new IllegalStateException("Attempt to overwrite an existing sound entry.");
        }

        double mvol = getMasterVolume(sound.getSoundType());
        sound.setMasterVolume(mvol);
        sounds.put(channel, sound);

        LOG.debug("Start sound in channel {}: {}", channel, sound);

        checkSounds();
    }

    @Override
    public void setMasterVolume(SoundType type, double vol) {
        vol = Doubles.constrainToRange(vol, 0.0, 1.0);

        masterVolume.put(type, vol);

        for (ISound sound : getSounds(type)) {
            sound.setMasterVolume(vol);
        }
    }

    @Override
    public void setPaused(boolean p) {
        paused = p;

        if (paused) {
            for (ISound sound : sounds.values()) {
                if (!sound.isPaused()) {
                    sound.pause();
                    pausedList.add(sound);
                }
            }
        } else {
            for (ISound sound : pausedList) {
                sound.resume();
            }
            pausedList.clear();
        }

        checkSounds();
    }

}
