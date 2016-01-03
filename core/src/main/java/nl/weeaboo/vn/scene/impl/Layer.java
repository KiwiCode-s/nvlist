package nl.weeaboo.vn.scene.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.image.IScreenshotBuffer;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.render.impl.ScreenshotBuffer;
import nl.weeaboo.vn.scene.IDrawable;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.IVisualElement;

public class Layer extends VisualGroup implements ILayer {

    private static final long serialVersionUID = SceneImpl.serialVersionUID;
    private static final Logger LOG = LoggerFactory.getLogger(Layer.class);

    private final BoundsHelper boundsHelper = new BoundsHelper();
    private final ScreenshotBuffer screenshotBuffer = new ScreenshotBuffer();

    protected Layer() {
    }

    public Layer(ILayer parent) {
        super(parent);
    }

    public Layer createSubLayer() {
        Layer subLayer = new Layer(this);
        add(subLayer);
        LOG.debug("Sub-layer created: {}, parent={}", subLayer, this);
        return subLayer;
    }

    @Override
    public void add(IDrawable d) {
        super.add(d);
    }

    protected boolean isElementVisible(IVisualElement elem) {
        if (elem instanceof IDrawable) {
            IDrawable d = (IDrawable)elem;
            if (!d.isVisible(.001)) {
                // Drawable is not visible
                return false;
            }
            if (d.isClipEnabled()) {
                // If clipping is off, bounds checks can be skipped
                return true;
            }
        } else {
            if (!elem.isVisible()) {
                // Visual element is not visible
                return false;
            }
        }

        // Check if the drawable's bounds lie within the layer bounds.
        // Remember: Drawable coordinates are relative to the coordinates of their parent layer.
        final Rect2D r = getBounds();
        return elem.getVisualBounds().intersects(0, 0, r.w, r.h);
    }

    @Override
    public void draw(IDrawBuffer drawBuffer) {
        draw(drawBuffer, drawBuffer.reserveLayerIds(1));
    }

    private void draw(IDrawBuffer drawBuffer, int layerId) {
        if (!isVisible()) {
            return;
        }

        drawBuffer.startLayer(layerId, this);

        List<Layer> subLayers = ImmutableList.copyOf(getSubLayers());

        // Render children (except sub-layers)
        for (IVisualElement child : getChildren()) {
            if (child instanceof ILayer || child.isDestroyed()) {
                continue;
            }

            if (isElementVisible(child)) {
                child.draw(drawBuffer);
            }
        }

        // Add render commands for our sub-layers
        int baseSubLayerId = drawBuffer.reserveLayerIds(subLayers.size());
        for (int n = 0; n < subLayers.size(); n++) {
            ILayer subLayer = subLayers.get(n);
            if (!subLayer.isDestroyed() && subLayer.isVisible()) {
                drawBuffer.drawLayer(baseSubLayerId + n, subLayer);
            }
        }

        // Add screenshot render commands to the end of the list
        screenshotBuffer.flush(drawBuffer);

        // Recursively render the contents of our sub-layers
        for (int n = 0; n < subLayers.size(); n++) {
            Layer subLayer = subLayers.get(n);
            if (!subLayer.isDestroyed() && subLayer.isVisible()) {
                subLayer.draw(drawBuffer, baseSubLayerId + n);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Layer(%08x)", hashCode());
    }

    @Override
    public final double getX() {
        return boundsHelper.getX();
    }

    @Override
    public final double getY() {
        return boundsHelper.getY();
    }

    @Override
    public final double getWidth() {
        return boundsHelper.getWidth();
    }

    @Override
    public final double getHeight() {
        return boundsHelper.getHeight();
    }

    @Override
    public Rect2D getBounds() {
        return boundsHelper.getBounds();
    }

    @Override
    public boolean containsLayer(ILayer layer) {
        if (layer == null) {
            return false;
        }

        for (ILayer sub : getSubLayers()) {
            if (sub == layer || sub.containsLayer(layer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<? extends Layer> getSubLayers() {
        return Iterables.unmodifiableIterable(Iterables.filter(getChildren(), Layer.class));
    }

    @Override
    public IScreenshotBuffer getScreenshotBuffer() {
        return screenshotBuffer;
    }

    @Override
    public final void setX(double x) {
        setPos(x, getY());
    }

    @Override
    public final void setY(double y) {
        setPos(getX(), y);
    }

    @Override
    public final void setWidth(double w) {
        setSize(w, getHeight());
    }

    @Override
    public final void setHeight(double h) {
        setSize(getWidth(), h);
    }

    @Override
    public void setPos(double x, double y) {
        boundsHelper.setPos(x, y);
    }

    @Override
    public void setSize(double w, double h) {
        boundsHelper.setSize(w, h);
    }

    @Override
    public void setBounds(double x, double y, double w, double h) {
        setPos(x, y);
        setSize(w, h);
    }

}