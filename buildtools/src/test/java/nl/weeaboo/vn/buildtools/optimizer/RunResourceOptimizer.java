package nl.weeaboo.vn.buildtools.optimizer;

import java.io.File;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;

import nl.weeaboo.vn.buildtools.project.NvlistProjectConnection;
import nl.weeaboo.vn.buildtools.project.ProjectFolderConfig;
import nl.weeaboo.vn.impl.InitConfig;

final class RunResourceOptimizer {

    /**
     * Test runner for the image optimizer pipeline.
     */
    public static void main(String[] args) {
        InitConfig.init();
        Lwjgl3NativesLoader.load();

        File dstFolder = new File("tmp");
        dstFolder.mkdirs();

        // NVList root project
        ProjectFolderConfig folderConfig = new ProjectFolderConfig(new File(""), new File(""));
        try (NvlistProjectConnection connection = NvlistProjectConnection.openProject(folderConfig)) {
            ResourceOptimizerConfig optimizerConfig = new ResourceOptimizerConfig(dstFolder);

            ResourceOptimizer optimizer = new ResourceOptimizer();
            optimizer.optimizeResources(connection, optimizerConfig);
        }
    }

}