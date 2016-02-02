package nl.weeaboo.vn.image.impl;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.core.IInterpolator;
import nl.weeaboo.vn.core.impl.AlignUtil;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.impl.BitmapTweenConfig.ControlImage;
import nl.weeaboo.vn.image.impl.BitmapTweenConfig.InputTexture;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.render.impl.TriangleGrid;
import nl.weeaboo.vn.render.impl.TriangleGrid.TextureWrap;
import nl.weeaboo.vn.scene.IDrawable;
import nl.weeaboo.vn.scene.impl.AnimatedRenderable;

public abstract class BitmapTweenRenderer extends AnimatedRenderable {

    private static final long serialVersionUID = ImageImpl.serialVersionUID;

    private static final int INTERPOLATOR_MAX = 255;

    private final IImageModule imageModule;
    private final BitmapTweenConfig config;

    private Area2D baseUV = ITexture.DEFAULT_UV;

    // --- Initialized in prepare() ---
    private Dim remapTextureSize;
    private ITexture remapTexture;
    private TriangleGrid grid;

    public BitmapTweenRenderer(IImageModule imageModule, BitmapTweenConfig config) {
        this.imageModule = imageModule;
        this.config = config;
    }

    @Override
    protected void disposeResources() {
        super.disposeResources();

        disposeRemapTexture();
        grid = null;
    }

    private void disposeRemapTexture() {
        remapTexture = null;
    }

    @Override
    protected void prepareResources() {
        super.prepareResources();

        double width = getWidth();
        double height = getHeight();

        ControlImage controlImage = config.getControlImage();

        // Create remap texture
        remapTextureSize = new Dim(INTERPOLATOR_MAX + 1, 1);

        // Create geometry
        InputTexture tex0 = config.getStartTexture();
        Rect2D bounds0 = tex0.getBounds();
        TextureWrap wrap0 = TextureWrap.CLAMP;

        InputTexture tex1 = config.getEndTexture();
        Rect2D bounds1 = tex1.getBounds();
        TextureWrap wrap1 = TextureWrap.CLAMP;

        Rect2D controlBounds = controlImage.getBounds(bounds0, bounds1);
        Area2D controlTexUV;
        TextureWrap controlWrap;

        ITexture controlTex = controlImage.getTexture();
        Rect2D b = AlignUtil.getAlignedBounds(controlTex, 0, 0);
        Area2D uv = controlTex.getUV();
        if (controlImage.isTile()) {
            controlTexUV = Area2D.of(uv.x, uv.y, uv.w * width / b.w, uv.h * height / b.h);
            controlWrap = TextureWrap.REPEAT_BOTH;
        } else {
            double sx = width / b.w, sy = height / b.h;
            double w, h;
            if (sx >= sy) {
                w = uv.w;
                h = sy / sx * uv.h;
            } else {
                h = uv.h;
                w = sx / sy * uv.w;
            }
            controlTexUV = Area2D.of(uv.x + (1 - w) / 2, uv.y + (1 - h) / 2, w, h);
            controlWrap = TextureWrap.CLAMP;
        }

        grid = TriangleGrid.layout3(
                bounds0.toArea2D(), tex0.getUV(baseUV), wrap0,
                bounds1.toArea2D(), tex1.getUV(baseUV), wrap1,
                controlBounds.toArea2D(), controlTexUV, controlWrap);

        updateRemapTex(); // Needs to be called here, we don't know if update() will be called before draw()
    }

    private boolean updateRemapTex() {
        double i1 = INTERPOLATOR_MAX * getNormalizedTime() * (1 + config.getRange());
        double i0 = i1 - INTERPOLATOR_MAX * config.getRange();

        int w = remapTextureSize.w;
        int h = remapTextureSize.h;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.Intensity);
        try {
            ByteBuffer buf = pixmap.getPixels();

            IInterpolator interpolator = config.getInterpolator();
            for (int n = 0; n < w * h; n++) {
                byte value;
                if (n <= i0) {
                    // Fully visible
                    value = (byte)INTERPOLATOR_MAX;
                } else if (n >= i1) {
                    // Not visible yet
                    value = (byte)0;
                } else {
                    // Value between i0 and i1; partially visible
                    // f is 1.0 at i0, 0.0 at i1
                    double f = (i1 - n) / (i1 - i0);
                    value = (byte)(INTERPOLATOR_MAX * interpolator.remap((float)f));
                }
                buf.put(n, value);
            }

            disposeRemapTexture();
            remapTexture = imageModule.createTexture(PixelTextureData.fromPixmap(pixmap), 1, 1);
        } finally {
            pixmap.dispose();
        }

        return true;
    }

    protected ITexture getStartTexture() {
        return config.getStartTexture().getTexture();
    }
    protected ITexture getEndTexture() {
        return config.getEndTexture().getTexture();
    }
    protected ITexture getControlTexture() {
        return config.getControlImage().getTexture();
    }
    protected ITexture getRemapTexture() {
        return remapTexture;
    }

    @Override
    public double getNativeWidth() {
        return getControlTexture().getWidth();
    }

    @Override
    public double getNativeHeight() {
        return getControlTexture().getHeight();
    }

    @Override
    protected final void render(IDrawable parent, Area2D bounds, IDrawBuffer drawBuffer) {
        render(drawBuffer, grid, bounds.x, bounds.y);
    }

    protected abstract void render(IDrawBuffer drawBuffer, TriangleGrid grid, double x, double y);

}