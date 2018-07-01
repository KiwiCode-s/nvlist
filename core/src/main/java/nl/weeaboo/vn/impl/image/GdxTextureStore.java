package nl.weeaboo.vn.impl.image;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.vn.gdx.graphics.GdxTextureUtil;
import nl.weeaboo.vn.gdx.res.GdxFileSystem;
import nl.weeaboo.vn.gdx.res.IWeigher;
import nl.weeaboo.vn.gdx.res.LoadingResourceStore;
import nl.weeaboo.vn.gdx.res.ResourceStoreCache;
import nl.weeaboo.vn.gdx.res.ResourceStoreCacheConfig;
import nl.weeaboo.vn.image.desc.IImageDefinition;
import nl.weeaboo.vn.impl.core.StaticRef;

public final class GdxTextureStore extends LoadingResourceStore<Texture> {

    private final ImageDefinitionCache cachedImageDefs;

    public GdxTextureStore(StaticRef<GdxTextureStore> selfId, GdxFileSystem fileSystem) {
        super(selfId, Texture.class);

        cachedImageDefs = new ImageDefinitionCache(fileSystem);

        setCacheConfig(createCacheConfig());
    }

    private static ResourceStoreCacheConfig<Texture> createCacheConfig() {
        ResourceStoreCacheConfig<Texture> config = new ResourceStoreCacheConfig<>();
        // TODO: Make this configurable, and use a default value that's a multiple of the screen resolution
        config.setMaximumWeight(256_000_000);
        config.setWeigher(new IWeigher<Texture>() {
            @Override
            public int weigh(Texture tex) {
                return GdxTextureUtil.estimateMemoryUseBytes(tex);
            }
        });
        return config;
    }

    /**
     * Returns the image defintion corresponding to the specified image file, or {@code null} if the image
     * doesn't exist or doesn't have an image definition..
     */
    @CheckForNull
    public final IImageDefinition getImageDef(FilePath imagePath) {
        return cachedImageDefs.getImageDef(imagePath);
    }

    @Override
    @Nullable
    protected AssetLoaderParameters<Texture> getLoadParams(FilePath imagePath) {
        IImageDefinition imageDef = getImageDef(imagePath);
        if (imageDef == null) {
            return null;
        }

        TextureParameter params = new TextureParameter();

        params.minFilter = GdxTextureUtil.toGdxFilter(imageDef.getMinifyFilter());
        params.magFilter = GdxTextureUtil.toGdxFilter(imageDef.getMagnifyFilter());

        params.wrapU = GdxTextureUtil.toGdxWrap(imageDef.getTilingModeX());
        params.wrapV = GdxTextureUtil.toGdxWrap(imageDef.getTilingModeY());

        return params;
    }

    /**
     * Returns a string representation of the state of the internal texture load cache.
     */
    public String getCacheStatus() {
        ResourceStoreCache<Texture> cache = getCache();
        return StringUtil.formatRoot("%s/%s",
                StringUtil.formatMemoryAmount(cache.estimateWeight()),
                StringUtil.formatMemoryAmount(cache.getMaximumWeight()));
    }

}
