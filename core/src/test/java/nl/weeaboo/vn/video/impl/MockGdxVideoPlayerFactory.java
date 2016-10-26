package nl.weeaboo.vn.video.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerInitException;

import nl.weeaboo.filesystem.FilePath;

final class MockGdxVideoPlayerFactory implements IGdxVideoPlayerFactory {

    @Override
    public FileHandle resolveFileHandle(FilePath filePath) {
        return Gdx.files.internal(filePath.toString());
    }

    @Override
    public VideoPlayer createVideoPlayer() throws VideoPlayerInitException {
        return new MockGdxVideoPlayer();
    }

}
