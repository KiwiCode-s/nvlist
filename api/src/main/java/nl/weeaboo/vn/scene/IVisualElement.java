package nl.weeaboo.vn.scene;

import java.io.Serializable;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.core.IDestructible;
import nl.weeaboo.vn.core.IRenderEnv;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.scene.signal.ISignalHandler;

public interface IVisualElement extends Serializable, IDestructible, ISignalHandler {

    void draw(IDrawBuffer drawBuffer);

    IVisualGroup getParent();
    void setParent(IVisualGroup visualGroup);

	short getZ();

	boolean isVisible();

	/**
     * @return The axis-aligned bounding box for this visual element, relative to the origin of its parent.
     */
    Rect2D getVisualBounds();

    /**
     * Returns information about the rendering environment.
     */
    IRenderEnv getRenderEnv();

}