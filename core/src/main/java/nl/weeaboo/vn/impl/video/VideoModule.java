package nl.weeaboo.vn.impl.video;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.weeaboo.common.Dim;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.MediaType;
import nl.weeaboo.vn.core.ResourceId;
import nl.weeaboo.vn.core.ResourceLoadInfo;
import nl.weeaboo.vn.impl.core.AbstractModule;
import nl.weeaboo.vn.impl.core.DefaultEnvironment;
import nl.weeaboo.vn.impl.image.ResolutionFolderSelector;
import nl.weeaboo.vn.impl.image.ResolutionFolderSelector.ResolutionPath;
import nl.weeaboo.vn.render.IRenderEnv;
import nl.weeaboo.vn.video.IVideo;
import nl.weeaboo.vn.video.IVideoModule;

/**
 * Sub-module for playing video.
 */
public class VideoModule extends AbstractModule implements IVideoModule {

    protected static final FilePath DEFAULT_VIDEO_FOLDER = FilePath.of("video/");

    private static final long serialVersionUID = VideoImpl.serialVersionUID;
    private static final Logger LOG = LoggerFactory.getLogger(VideoModule.class);

    protected final IEnvironment env;
    protected final VideoResourceLoader resourceLoader;
    private final INativeVideoFactory nativeVideoFactory;

    private @Nullable IVideo fullscreenMovie;
    private FilePath videoFolder = DEFAULT_VIDEO_FOLDER;
    private Dim videoResolution = Dim.EMPTY;

    public VideoModule(DefaultEnvironment env) {
        this(env, new VideoResourceLoader(env), new NativeVideoFactory());
    }

    public VideoModule(DefaultEnvironment env, VideoResourceLoader resourceLoader,
            INativeVideoFactory nativeVideoFactory) {

        this.env = env;
        this.resourceLoader = resourceLoader;
        this.nativeVideoFactory = nativeVideoFactory;

        IRenderEnv renderEnv = env.getRenderEnv();
        setVideoResolution(renderEnv.getVirtualSize());
    }

    @Override
    public @Nullable ResourceId resolveResource(FilePath filename) {
        return resourceLoader.resolveResource(filename);
    }

    @Override
    public IVideo movie(ResourceLoadInfo loadInfo) throws IOException {
        checkIfMovieFinished();

        Preconditions.checkState(fullscreenMovie == null, "A different movie is still playing");

        LOG.info("Attempt to play movie: videoFolder={}, path={}", videoFolder, loadInfo.getPath());

        IVideo newMovie = createVideo(loadInfo);
        fullscreenMovie = newMovie;

        newMovie.start();
        return newMovie;
    }

    private IVideo createVideo(ResourceLoadInfo loadInfo) throws FileNotFoundException {
        FilePath path = loadInfo.getPath();
        resourceLoader.checkRedundantFileExt(path);

        ResourceId resourceId = resourceLoader.resolveResource(path);
        if (resourceId == null) {
            LOG.warn("Unable to find video file: {}", path);
            throw new FileNotFoundException(path.toString());
        }

        resourceLoader.logLoad(resourceId, loadInfo);

        IRenderEnv renderEnv = env.getRenderEnv();
        INativeVideo video = nativeVideoFactory.createNativeVideo(resourceLoader, resourceId.getFilePath(), renderEnv);
        return new Video(resourceId.getFilePath(), video);
    }

    @Override
    public @Nullable IVideo getBlocking() {
        checkIfMovieFinished();
        return fullscreenMovie;
    }

    private void checkIfMovieFinished() {
        if (fullscreenMovie != null && fullscreenMovie.isStopped()) {
            fullscreenMovie = null;
        }
    }

    @Override
    public Collection<FilePath> getVideoFiles(FilePath folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    protected double getVideoScale() {
        IRenderEnv renderEnv = env.getRenderEnv();
        return Math.min(renderEnv.getWidth() / (double)videoResolution.w,
                renderEnv.getHeight() / (double)videoResolution.h);
    }

    @Override
    public final void setVideoResolution(Dim desiredSize) {
        ResolutionFolderSelector folderSelector = new ResolutionFolderSelector(env, MediaType.VIDEO);
        ResolutionPath selected = folderSelector.select(desiredSize);

        videoResolution = selected.resolution;
        resourceLoader.setResourceFolder(selected.folder);
    }

}
