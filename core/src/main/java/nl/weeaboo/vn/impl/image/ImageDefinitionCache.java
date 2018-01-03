package nl.weeaboo.vn.impl.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nl.weeaboo.common.Checks;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.FileSystemUtil;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.image.desc.IImageDefinition;
import nl.weeaboo.vn.impl.image.desc.ImageDefinitionIO;

public final class ImageDefinitionCache {

    private static final Logger LOG = LoggerFactory.getLogger(ImageDefinitionCache.class);

    private final IFileSystem fileSystem;

    private final Map<FilePath, IImageDefinition> cache = Maps.newHashMap();
    private final Set<FilePath> seenFolders = Sets.newHashSet();

    public ImageDefinitionCache(IFileSystem fileSystem) {
        this.fileSystem = Checks.checkNotNull(fileSystem);
    }

    /**
     * @return Returns the image definition for the given path, or {@code null} if the given image has no
     *         definition.
     */
    @CheckForNull
    public IImageDefinition getImageDef(FilePath path) {
        IImageDefinition imageDef = cache.get(path);
        if (imageDef != null) {
            return imageDef;
        }

        // Check if we need to load the image definition file for this folder
        FilePath folder = MoreObjects.firstNonNull(path.getParent(), FilePath.empty());
        if (seenFolders.add(folder)) {
            try {
                loadJson(folder);

                // Re-attempt to find an image def (may still fail)
                imageDef = cache.get(path);
            } catch (FileNotFoundException fnfe) {
                // Ignore
            } catch (IOException e) {
                LOG.warn("Error loading image definitions", e);
            }
        }
        return imageDef;
    }

    private void loadJson(FilePath folder) throws IOException {
        FilePath jsonPath = folder.resolve(IImageDefinition.IMG_DEF_FILE);
        String json = FileSystemUtil.readString(fileSystem, jsonPath);

        // Load JSON and add image definitions to the cache
        for (IImageDefinition imageDef : ImageDefinitionIO.deserialize(json)) {
            cache.put(folder.resolve(imageDef.getFilename()), imageDef);
        }
    }
}
