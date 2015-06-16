package nl.weeaboo.vn.image.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Dim;
import nl.weeaboo.entity.Entity;
import nl.weeaboo.gdx.res.IResource;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.ILayer;
import nl.weeaboo.vn.core.IRenderEnv;
import nl.weeaboo.vn.core.impl.DefaultEnvironment;
import nl.weeaboo.vn.core.impl.EntityHelper;
import nl.weeaboo.vn.core.impl.ResourceLoader;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.IScreenshot;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.ITextureData;

public class ImageModule implements IImageModule {

    private static final long serialVersionUID = ImageImpl.serialVersionUID;
    private static final Logger LOG = LoggerFactory.getLogger(ImageModule.class);

    protected final IEnvironment env;
    protected final ResourceLoader resourceLoader;
    protected final EntityHelper entityHelper;

    private final TextureManager texManager;

    private Dim imageResolution;

    public ImageModule(DefaultEnvironment env, ResourceLoader resourceLoader, TextureManager texManager) {
        this.env = env;
        this.resourceLoader = resourceLoader;
        this.entityHelper = new EntityHelper(env.getPartRegistry());

        this.texManager = texManager;

        IRenderEnv renderEnv = env.getRenderEnv();
        imageResolution = renderEnv.getVirtualSize();
    }

    @Override
    public Entity createImage(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        entityHelper.addImageParts(e);
        return e;
    }

    @Override
    public Entity createTextDrawable(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        //TODO Add text parts
        return e;
    }

    @Override
    public Entity createButton(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        //TODO Add button parts
        return e;
    }

    @Override
    public ITexture getTexture(String filename, String[] callStack, boolean suppressErrors) {
        resourceLoader.checkRedundantFileExt(filename);

        String normalized = resourceLoader.normalizeFilename(filename);
        if (normalized == null) {
            if (!suppressErrors) {
                LOG.debug("Unable to find image file: " + filename);
            }
            return null;
        }

        // seenLog.addImage(filename); // TODO Enable seen log
        return getTextureNormalized(filename, normalized, callStack);
    }

    /**
     * Is called from {@link #getTexture(String, String[], boolean)}
     */
    protected ITexture getTextureNormalized(String filename, String normalized, String[] luaStack) {
        IResource<TextureRegion> tr = getTexRectNormalized(filename, normalized, luaStack);

        double scale = getImageScale();
        return texManager.newTexture(tr, scale, scale);
    }

    private IResource<TextureRegion> getTexRectNormalized(String filename, String normalized, String[] luaStack) {
        // TODO LVN-011 Track stack traces, log texture load times
        return texManager.getTexture(normalized);
    }

    @Override
    public ITexture createTexture(ITextureData texData, double sx, double sy) {
        return texManager.generateTexture((PixelTextureData)texData, sx, sy);
    }

    @Override
    public ITexture createTexture(IScreenshot ss) {
        IRenderEnv renderEnv = env.getRenderEnv();
        double sx = renderEnv.getWidth() / (double)ss.getScreenSize().w;
        double sy = renderEnv.getHeight() / (double)ss.getScreenSize().h;
        ITextureData pixels = ss.getPixels();
        return createTexture(pixels, sx, sy);
    }

    protected void onImageScaleChanged() {
    }
    
    @Override
    public Collection<String> getImageFiles(String folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    protected double getImageScale() {
        IRenderEnv renderEnv = env.getRenderEnv();
        return Math.min(renderEnv.getWidth() / (double)imageResolution.w,
                renderEnv.getHeight() / (double)imageResolution.h);
    }

    @Override
    public void setImageResolution(Dim size) {
        size = Checks.checkNotNull(size);
        if (!imageResolution.equals(size)) {
            imageResolution = size;
            onImageScaleChanged();
        }
    }

}
