package nl.weeaboo.vn.buildtools.optimizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

import nl.weeaboo.vn.buildtools.project.NvlistProjectConnection;
import nl.weeaboo.vn.buildtools.project.ProjectFolderConfig;
import nl.weeaboo.vn.impl.save.JsonUtil;

public final class OptimizerConfigJsonTest extends OptimizerTest {

    @Test(expected = RuntimeException.class)
    public void testParseEmpty() {
        OptimizerConfigJson config = load("optimizer-config-empty.json");
        config.openProject(); // Attempting to use an invalid config will just throw an exception
    }

    @Test
    public void testParseFull() {
        OptimizerConfigJson config = load("optimizer-config-full.json");
        Assert.assertEquals("projectFolder", config.projectFolder);
        Assert.assertEquals("buildToolsFolder", config.buildToolsFolder);
        Assert.assertEquals(Arrays.asList("1x2", "3x4"), config.targetResolutions);
        Assert.assertEquals(Arrays.asList("a*", "b.png"), config.exclude);
    }

    /**
     * Load config and use it to create an {@link OptimizerContext}.
     */
    @Test
    public void testCreateContext() {
        OptimizerConfigJson config = load("optimizer-config-full.json");

        // Set folders to temp folders supplied by parent class of this test
        ProjectFolderConfig folderConfig = context.getProject().getFolderConfig();
        config.projectFolder = folderConfig.getProjectFolder().toString();
        config.buildToolsFolder = folderConfig.getBuildToolsFolder().toString();

        // Create the context
        NvlistProjectConnection projectConnection = config.openProject();
        config.createContext(projectConnection, context.getMainConfig().getOutputFolder());
    }

    private OptimizerConfigJson load(String resourceName) {
        try {
            String json = Resources.toString(getClass().getResource(resourceName), StandardCharsets.UTF_8);
            return JsonUtil.fromJson(OptimizerConfigJson.class, json);
        } catch (IOException ioe) {
            throw new AssertionError("Error loading config: " + resourceName, ioe);
        }
    }

}
