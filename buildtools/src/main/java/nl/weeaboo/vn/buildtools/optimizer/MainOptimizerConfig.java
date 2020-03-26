package nl.weeaboo.vn.buildtools.optimizer;

import java.io.File;
import java.util.Objects;

/**
 * Global resource optimizer configuration not related to any particular resource type.
 */
public final class MainOptimizerConfig implements IOptimizerConfig {

    private final File outputFolder;

    public MainOptimizerConfig(File outputFolder) {
        this.outputFolder = Objects.requireNonNull(outputFolder);
    }

    /**
     * The output folder to where the optimized resources should be written.
     */
    public File getOutputFolder() {
        return outputFolder;
    }

}
