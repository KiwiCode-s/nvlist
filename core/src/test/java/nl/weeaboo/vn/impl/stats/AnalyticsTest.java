package nl.weeaboo.vn.impl.stats;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.InMemoryFileSystem;
import nl.weeaboo.filesystem.SecureFileWriter;
import nl.weeaboo.vn.core.MediaType;
import nl.weeaboo.vn.core.ResourceId;
import nl.weeaboo.vn.core.ResourceLoadInfo;
import nl.weeaboo.vn.impl.core.Destructibles;
import nl.weeaboo.vn.impl.core.TestEnvironment;

public final class AnalyticsTest {

    private static final ResourceId IMAGE = new ResourceId(MediaType.IMAGE, FilePath.of("image"));
    private static final ResourceId SOUND = new ResourceId(MediaType.SOUND, FilePath.of("sound"));

    private TestEnvironment env;
    private AnalyticsPreloaderStub preloader;
    private Analytics analytics;

    @Before
    public void before() {
        env = TestEnvironment.newInstance();
        preloader = new AnalyticsPreloaderStub();

        analytics = new Analytics(env, preloader);
    }

    @After
    public void after() {
        Destructibles.destroy(env);
    }

    @Test
    public void testSaveLoad() throws IOException {
        logResourceLoad(IMAGE, Arrays.asList("x.lvn:2"));
        logResourceLoad(SOUND, Arrays.asList("x.lvn:3"));

        FilePath savePath = FilePath.of("savePath");
        InMemoryFileSystem fileSystem = new InMemoryFileSystem(false);
        SecureFileWriter sfw = new SecureFileWriter(fileSystem);
        analytics.save(sfw, savePath);
        analytics.load(sfw, savePath);

        // Currently, only images are preloaded
        assertPreloads("x.lvn:1", IMAGE);
    }

    private void assertPreloads(String lvnFileLine, ResourceId... expectedPreloads) {
        analytics.handlePreloads(FileLine.fromString(lvnFileLine));
        Assert.assertEquals(Stream.of(expectedPreloads).map(ResourceId::getFilePath).collect(Collectors.toList()),
                preloader.consumePreloadedImages());
    }

    private void logResourceLoad(ResourceId id, List<String> stackTrace) {
        analytics.logResourceLoad(id, new ResourceLoadInfo(id.getType(), id.getFilePath(), stackTrace));
    }

}