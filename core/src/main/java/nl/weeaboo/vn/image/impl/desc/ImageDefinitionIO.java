package nl.weeaboo.vn.image.impl.desc;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.filesystem.FileCollectOptions;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.FileSystemUtil;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.image.desc.GLScaleFilter;
import nl.weeaboo.vn.image.desc.GLTilingMode;
import nl.weeaboo.vn.image.desc.IImageDefinition;
import nl.weeaboo.vn.image.desc.IImageSubRect;
import nl.weeaboo.vn.image.impl.desc.ImageDefinitionFileJson.ImageDefinitionJson;
import nl.weeaboo.vn.image.impl.desc.ImageDefinitionFileJson.ImageSubRectJson;
import nl.weeaboo.vn.save.SaveFormatException;
import nl.weeaboo.vn.save.impl.JsonUtil;

/** Helper class for reading/writing {@link ImageDefinition} objects */
public final class ImageDefinitionIO {

    private static final Logger LOG = LoggerFactory.getLogger(ImageDefinitionIO.class);
    private static final String VERSION = "1.0";

	private ImageDefinitionIO() {
	}

	public static Map<FilePath, IImageDefinition> fromFileManager(IFileSystem fileSystem, FilePath rootFolder)
			throws IOException, SaveFormatException
	{
	    Map<FilePath, IImageDefinition> result = Maps.newHashMap();

	    FileCollectOptions opts = new FileCollectOptions();
	    opts.collectFiles = false;
	    opts.collectFolders = true;
		for (FilePath folder : fileSystem.getFiles(opts)) {
			FilePath path = folder.resolve("img.json");
			if (!fileSystem.getFileExists(path)) {
			    continue;
			}

			for (ImageDefinition imageDef : deserialize(FileSystemUtil.readString(fileSystem, path))) {
			    FilePath relPath = rootFolder.relativize(imageDef.getFile());
			    result.put(relPath, imageDef);
			}
		}
	    return result;
	}

	public static String serialize(Collection<ImageDefinition> imageDefs) {
	    ImageDefinitionFileJson fileJson = new ImageDefinitionFileJson();
	    fileJson.images = new ImageDefinitionJson[imageDefs.size()];
	    int t = 0;
	    for (ImageDefinition imageDef : imageDefs) {
	        fileJson.images[t++] = encodeJson(imageDef);
	    }
	    return JsonUtil.toJson(fileJson);
	}

	public static Collection<ImageDefinition> deserialize(String string) throws SaveFormatException {
	    ImageDefinitionFileJson fileJson = JsonUtil.fromJson(ImageDefinitionFileJson.class, string);
	    if (!VERSION.equals(fileJson.version)) {
	        throw new SaveFormatException("Expected " + VERSION + ", was " + fileJson.version);
	    }

	    List<ImageDefinition> result = Lists.newArrayList();
	    for (ImageDefinitionJson imageDefJson : fileJson.images) {
	        try {
	            result.add(decodeJson(imageDefJson));
	        } catch (RuntimeException re) {
	            LOG.error("Invalid image definition: {}", imageDefJson.file, re);
	        }
	    }
	    return result;
	}

	private static ImageDefinitionJson encodeJson(ImageDefinition imageDef) {
	    ImageDefinitionJson imageDefJson = new ImageDefinitionJson();
	    imageDefJson.file = imageDef.getFile().toString();
        imageDefJson.width = imageDef.getSize().w;
        imageDefJson.height = imageDef.getSize().h;
        imageDefJson.minFilter = imageDef.getMinifyFilter().toString();
        imageDefJson.magFilter = imageDef.getMagnifyFilter().toString();
        imageDefJson.wrapX = imageDef.getTilingModeX().toString();
        imageDefJson.wrapY = imageDef.getTilingModeY().toString();

        List<ImageSubRectJson> subRects = Lists.newArrayList();
        for (IImageSubRect subRect : imageDef.getSubRects()) {
            subRects.add(encodeJson(subRect));
        }
        imageDefJson.subRects = subRects.toArray(new ImageSubRectJson[0]);

        return imageDefJson;
	}

    private static ImageSubRectJson encodeJson(IImageSubRect subRect) {
        ImageSubRectJson subRectJson = new ImageSubRectJson();
        subRectJson.id = subRect.getId();
        subRectJson.rect = encodeJson(subRect.getRect());
        return subRectJson;
    }

    private static int[] encodeJson(Rect rect) {
        return new int[] {rect.x, rect.y, rect.w, rect.h};
    }

    private static ImageDefinition decodeJson(ImageDefinitionJson imageDefJson) {
        FilePath file = FilePath.of(imageDefJson.file);
        Dim size = Dim.of(imageDefJson.width, imageDefJson.height);
        GLScaleFilter minf = parseScaleFilter(imageDefJson.minFilter);
        GLScaleFilter magf = parseScaleFilter(imageDefJson.magFilter);
        GLTilingMode wrapX = parseTilingMode(imageDefJson.wrapX);
        GLTilingMode wrapY = parseTilingMode(imageDefJson.wrapY);

        List<ImageSubRect> subRects = Lists.newArrayList();
        if (imageDefJson.subRects != null) {
            for (ImageSubRectJson subRectJson : imageDefJson.subRects) {
                subRects.add(parseSubRect(subRectJson));
            }
        }
        return new ImageDefinition(file, size, minf, magf, wrapX, wrapY, subRects);
    }

    private static GLScaleFilter parseScaleFilter(String filterString) {
        if (Strings.isNullOrEmpty(filterString)) {
            return GLScaleFilter.DEFAULT;
        }
        return GLScaleFilter.fromString(filterString);
    }

    private static GLTilingMode parseTilingMode(String tilingModeString) {
        if (Strings.isNullOrEmpty(tilingModeString)) {
            return GLTilingMode.DEFAULT;
        }
        return GLTilingMode.fromString(tilingModeString);
    }

    private static ImageSubRect parseSubRect(ImageSubRectJson subRectJson) {
        return new ImageSubRect(subRectJson.id, parseRect(subRectJson.rect));
    }

    private static Rect parseRect(int[] rect) {
        return Rect.of(rect[0], rect[1], rect[2], rect[3]);
    }

}