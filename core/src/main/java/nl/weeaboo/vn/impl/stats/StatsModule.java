package nl.weeaboo.vn.impl.stats;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.impl.core.AbstractModule;
import nl.weeaboo.vn.stats.IAnalytics;
import nl.weeaboo.vn.stats.IPlayTimer;
import nl.weeaboo.vn.stats.IResourceLoadLog;
import nl.weeaboo.vn.stats.IStatsModule;

public class StatsModule extends AbstractModule implements IStatsModule {

    private static final long serialVersionUID = StatsImpl.serialVersionUID;

    private final SeenLog seenLog;
    private final IResourceLoadLog resourceLoadLog;
    private final IPlayTimer playTimer;
    private final IAnalytics analytics;

    public StatsModule(IEnvironment env) {
        this(env, new PlayTimerStub());
    }

    public StatsModule(IEnvironment env, IPlayTimer playTimer) {
        seenLog = new SeenLog(env);
        resourceLoadLog = new ResourceLoadLog(seenLog);
        this.playTimer = Checks.checkNotNull(playTimer);
        analytics = new Analytics();
    }

    @Override
    public void update() {
        super.update();

        playTimer.update();
    }

    @Override
    public SeenLog getSeenLog() {
        return seenLog;
    }

    @Override
    public IResourceLoadLog getResourceLoadLog() {
        return resourceLoadLog;
    }

    @Override
    public IPlayTimer getPlayTimer() {
        return playTimer;
    }

    @Override
    public IAnalytics getAnalytics() {
        return analytics;
    }

}
