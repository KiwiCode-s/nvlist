package nl.weeaboo.vn.scene;

import java.io.Serializable;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.core.IChangeListener;
import nl.weeaboo.vn.render.IDrawBuffer;

public interface IRenderable extends Serializable {

    /**
     * Called when this renderable is attached to a drawable in the scene.
     *
     * @param cl The change listener to add.
     */
    void onAttached(IChangeListener cl);

    /**
     * Called when this renderable is detached from the scene. This method can be used to clean up any native
     * resources.
     *
     * @param cl The change listener to remove.
     */
    void onDetached(IChangeListener cl);

    /**
     * @return The intrinsic width for this renderable.
     */
    double getNativeWidth();

    /**
     * @return The intrinsic height for this renderable.
     */
    double getNativeHeight();

	/**
     * @return The axis-aligned bounding box for this renderable element.
     */
    Rect2D getVisualBounds();

    /**
     * Renders to the given draw buffer
     */
    void render(IDrawable parentComponent, Area2D bounds, IDrawBuffer drawBuffer);

}