package nl.weeaboo.vn.impl.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.weeaboo.vn.core.INotifier;

/**
 * Implementation of {@link INotifier} which writes messages to the application log.
 */
public final class LoggerNotifier implements INotifier {

    private static final Logger LOG = LoggerFactory.getLogger(LoggerNotifier.class);

    @Override
    public void message(String message) {
        LOG.info(message);
    }

}
