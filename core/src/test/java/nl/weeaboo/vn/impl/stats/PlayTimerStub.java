package nl.weeaboo.vn.impl.stats;

import java.io.IOException;

import nl.weeaboo.vn.core.Duration;
import nl.weeaboo.vn.save.IStorage;
import nl.weeaboo.vn.stats.IPlayTimer;

public class PlayTimerStub implements IPlayTimer {

    private static final long serialVersionUID = 1L;

    private long playTimeSeconds;

    @Override
    public void update() {
        playTimeSeconds++;
    }

    @Override
    public void load(IStorage storage) throws IOException {
        playTimeSeconds = storage.getLong(PlayTimer.KEY_TOTAL, playTimeSeconds);
    }

    @Override
    public void save(IStorage storage) throws IOException {
        storage.setLong(PlayTimer.KEY_TOTAL, playTimeSeconds);
    }

    @Override
    public Duration getTotalPlayTime() {
        return Duration.fromSeconds(playTimeSeconds);
    }

    @Override
    public Duration getIdleTime() {
        return Duration.ZERO;
    }

}
