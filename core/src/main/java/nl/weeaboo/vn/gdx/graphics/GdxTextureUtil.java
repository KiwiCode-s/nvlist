package nl.weeaboo.vn.gdx.graphics;

import javax.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import nl.weeaboo.common.Area;
import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.desc.GLScaleFilter;
import nl.weeaboo.vn.image.desc.GLTilingMode;
import nl.weeaboo.vn.impl.image.GdxTexture;
import nl.weeaboo.vn.impl.render.RenderLog;

/**
 * Various functions related to GDX texture objects.
 */
public final class GdxTextureUtil {

    private GdxTextureUtil() {
    }

    /**
     * Returns the backing libGDX texture for the given {@link ITexture}, or {@code null} if unable to extract a valid
     * texture.
     */
    public static @Nullable Texture getTexture(ITexture tex) {
        if (tex instanceof GdxTexture) {
            return ((GdxTexture)tex).getTexture();
        } else {
            return null;
        }
    }

    /**
     * @see #getTexture(ITexture)
     */
    public static @Nullable TextureRegion getTextureRegion(ITexture tex) {
        return getTextureRegion(tex, ITexture.DEFAULT_UV);
    }

    /**
     * Returns a sub-region of the backing libGDX texture for the given {@link ITexture}, or {@code null} if unable to
     * extract a valid texture.
     */
    public static @Nullable TextureRegion getTextureRegion(ITexture tex, Area2D uv) {
        if (tex instanceof GdxTexture) {
            return ((GdxTexture)tex).getTextureRegion(uv);
        } else {
            return null;
        }
    }

    /**
     * Binds a texture to OpenGL. If no backing libGDX texture can be resolved, binds {@code 0} as the active texture.
     * @see #getTexture(ITexture)
     */
    public static void bindTexture(int texUnit, @Nullable ITexture tex) {
        Texture texture = null;
        if (tex != null) {
            texture = GdxTextureUtil.getTexture(tex);
            if (texture == null) {
                RenderLog.warn("Skip drawing quad; backing texture is null: {}", tex);
            }
        }

        if (texture == null) {
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + texUnit);
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        } else {
            texture.bind(texUnit);
        }
    }

    /**
     * Creates a sub-region for the given libGDX texture.
     * @see #newGdxTextureRegion(Texture, Area)
     */
    public static TextureRegion newGdxTextureRegion(Texture texture) {
        return newGdxTextureRegion(texture, Area.of(0, 0, texture.getWidth(), texture.getHeight()));
    }

    /**
     * Creates a sub-region for the given libGDX texture.
     */
    public static TextureRegion newGdxTextureRegion(Texture texture, Area subRect) {
        TextureRegion region = new TextureRegion(texture, subRect.x, subRect.y, subRect.w, subRect.h);
        region.flip(false, true);
        return region;
    }

    /**
     * Applies NVList-wide default values for the texture's filter/wrap settings.
     */
    public static void setDefaultTextureParams(Texture texture) {
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
    }

    /**
     * Converts {@link GLScaleFilter} to its equivalent {@link TextureFilter}.
     */
    public static TextureFilter toGdxFilter(GLScaleFilter filter) {
        switch (filter) {
        case NEAREST: return TextureFilter.Nearest;
        case NEAREST_MIPMAP: return TextureFilter.MipMapNearestLinear;
        case LINEAR: return TextureFilter.Linear;
        case LINEAR_MIPMAP: return TextureFilter.MipMapLinearLinear;
        }

        throw new IllegalArgumentException("Unsupported filter type: " + filter);
    }

    /**
     * Converts {@link GLTilingMode} to its equivalent {@link TextureWrap}.
     */
    public static TextureWrap toGdxWrap(GLTilingMode mode) {
        switch (mode) {
        case CLAMP: return TextureWrap.ClampToEdge;
        case REPEAT: return TextureWrap.Repeat;
        }

        throw new IllegalArgumentException("Unsupported tiling mode: " + mode);
    }

    /**
     * Returns an estimate of the RAM usage of the given texture.
     */
    public static int estimateMemoryUseBytes(Texture tex) {
        int pixelCount = tex.getWidth() * tex.getHeight();
        int bpp = PixmapUtil.getBitsPerPixel(tex.getTextureData().getFormat());
        return pixelCount * bpp / 8;
    }

}
