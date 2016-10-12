package nl.weeaboo.vn.test;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.InMemoryFileSystem;
import nl.weeaboo.filesystem.MultiFileSystem;
import nl.weeaboo.gdx.HeadlessGdx;
import nl.weeaboo.gdx.res.GdxFileSystem;

public final class TestFileSystem {

    static {
        HeadlessGdx.init();
    }

    public static MultiFileSystem newInstance() {
        IFileSystem readFileSystem = new GdxFileSystem("", true);
        IFileSystem inMemoryFileSystem = new InMemoryFileSystem(false);
        return new MultiFileSystem(readFileSystem, inMemoryFileSystem);
    }

}