package nl.weeaboo.gdx.res;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import nl.weeaboo.common.Checks;
import nl.weeaboo.filesystem.FileCollectOptions;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.ZipFileArchive;

public final class DesktopGdxFileSystem extends GdxFileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DesktopGdxFileSystem.class);

    private final String arcBaseName;
    private final InternalGdxFileSystem internalFileSystem;

    // Cached file archives. Archives are searched in order.
    private transient ImmutableList<ZipFileArchive> cachedFileArchives;

    public DesktopGdxFileSystem() {
        this("res/", "res");
    }

    /**
     * @param arcBaseName Base name for file archives. The final archive file names are created from the
     *        following pattern: {@code $(baseName).nvl}, {@code $(baseName)2.nvl}, ...
     */
    public DesktopGdxFileSystem(String internalFilePrefix, String arcBaseName) {
        super(true);

        this.arcBaseName = Checks.checkNotNull(arcBaseName);

        internalFileSystem = new InternalGdxFileSystem(internalFilePrefix);
    }

    @Override
    public FileHandle resolve(String subPath) {
        return new DesktopFileHandle(FilePath.of(subPath));
    }

    private IFileSystem resolveFileSystem(FilePath path) {
        // Try to resolve the path as a regular file first
        if (internalFileSystem.exists(path)) {
            return internalFileSystem;
        }

        // If the path can't be resolved to a regular file, check the ZIP archives
        for (ZipFileArchive arc : getFileArchives()) {
            if (arc.getFileExists(path)) {
                return arc;
            }
        }

        // Use internal filesystem for invalid file handles
        return internalFileSystem;
    }

    private Collection<ZipFileArchive> getFileArchives() {
        if (cachedFileArchives == null) {
            ImmutableList.Builder<ZipFileArchive> archives = ImmutableList.builder();

            for (int index = 1; index < 99; index++) {
                String name = arcBaseName + (index > 1 ? index : "") + ".nvl";
                File file = Gdx.files.internal(name).file();
                if (!file.isFile()) {
                    // Abort at the first miss
                    LOG.info("Stopped opening archives: {} (file={})", name, file);
                    break;
                }

                ZipFileArchive arc = new ZipFileArchive();
                try {
                    arc.open(file);

                    LOG.info("Opened archive: {} (file={})", name, file);
                    archives.add(arc);
                } catch (IOException e) {
                    LOG.warn("Error opening archive: {} (file={})", name, file);
                    arc.close();
                }
            }

            cachedFileArchives = archives.build();
        }
        return cachedFileArchives;
    }

    private Set<FilePath> getChildren(FilePath path) {
        FileCollectOptions collectOpts = FileCollectOptions.folders(path);
        collectOpts.recursive = false;

        Set<FilePath> result = Sets.newHashSet();
        try {
            Iterables.addAll(result, internalFileSystem.getFiles(collectOpts));
        } catch (IOException e) {
            LOG.warn("Error retrieving file list {}({})", internalFileSystem, path);
        }
        for (ZipFileArchive arc : getFileArchives()) {
            try {
                Iterables.addAll(result, arc.getFiles(collectOpts));
            } catch (IOException e) {
                LOG.warn("Error retrieving file list {}({})", arc, path);
            }
        }
        return result;
    }

    private final class DesktopFileHandle extends NonFileGdxFileHandle {

        private final FilePath path;

        public DesktopFileHandle(FilePath path) {
            super(path.toString(), FileType.Internal);

            this.path = Checks.checkNotNull(path);
        }

        private IFileSystem resolveFileSystem() {
            return DesktopGdxFileSystem.this.resolveFileSystem(path);
        }

        @Override
        public InputStream read() {
            try {
                return resolveFileSystem().openInputStream(path);
            } catch (IOException e) {
                throw gdxException(e);
            }
        }

        @Override
        public FileHandle child(String name) {
            return new DesktopFileHandle(path.resolve(name));
        }

        @Override
        public FileHandle parent() {
            FilePath parent = MoreObjects.firstNonNull(path.getParent(), FilePath.empty());
            return new DesktopFileHandle(parent);
        }

        @Override
        public FileHandle[] list() {
            Set<FilePath> paths = getChildren(path);

            FileHandle[] handles = new FileHandle[paths.size()];
            int t = 0;
            for (FilePath path : paths) {
                handles[t++] = new DesktopFileHandle(path);
            }
            return handles;
        }

        @Override
        public boolean isDirectory() {
            // TODO: This isn't entirely correct -> paths without an ending slash may still be folders
            return path.isFolder();
        }

        @Override
        public long length() {
            try {
                return resolveFileSystem().getFileSize(path);
            } catch (IOException e) {
                LOG.debug("Unable to determine file size for ({})", path);
                return 0L;
            }
        }

        @Override
        public boolean exists() {
            return resolveFileSystem().getFileExists(path);
        }

    }
}