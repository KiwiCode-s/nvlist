package nl.weeaboo.vn.impl.scene;

import java.io.Serializable;

import nl.weeaboo.vn.scene.IButton;
import nl.weeaboo.vn.scene.IImageDrawable;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.ITextDrawable;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.text.ILoadingFontStore;

public class ComponentFactory implements Serializable {

    private static final long serialVersionUID = SceneImpl.serialVersionUID;

    /**
     * Creates a new image drawable and adds it to the specified layer.
     */
    public IImageDrawable createImage(ILayer layer) {
        ImageDrawable image = new ImageDrawable();
        layer.add(image);
        return image;
    }

    /**
     * Creates a new text drawable and adds it to the specified layer.
     */
    public ITextDrawable createText(ILayer layer, ILoadingFontStore fontStore) {
        TextDrawable textDrawable = new TextDrawable(fontStore);
        textDrawable.setSize(layer.getWidth(), layer.getHeight());
        layer.add(textDrawable);
        return textDrawable;
    }

    /**
     * Creates a new button drawable and adds it to the specified layer.
     */
    public IButton createButton(ILayer layer, IScriptContext scriptContext, ILoadingFontStore fontStore) {
        Button button = new Button(scriptContext.getEventDispatcher(), fontStore);
        layer.add(button);
        return button;
    }

}
