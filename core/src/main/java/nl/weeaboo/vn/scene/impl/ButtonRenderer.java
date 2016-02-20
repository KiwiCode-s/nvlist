package nl.weeaboo.vn.scene.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.styledtext.ETextAlign;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.vn.core.IEventListener;
import nl.weeaboo.vn.image.INinePatch;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.impl.NinePatch;
import nl.weeaboo.vn.image.impl.NinePatchRenderer;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.scene.ButtonViewState;
import nl.weeaboo.vn.scene.IButtonRenderer;
import nl.weeaboo.vn.scene.IDrawable;
import nl.weeaboo.vn.text.IText;
import nl.weeaboo.vn.text.impl.TextRenderer;

public class ButtonRenderer extends AbstractRenderable implements IButtonRenderer {

    private static final long serialVersionUID = SceneImpl.serialVersionUID;

    private final NinePatchRenderer background = new NinePatchRenderer();
    private final TextRenderer textRenderer = new TextRenderer();

    private final Map<ButtonViewState, INinePatch> textures = Maps.newEnumMap(ButtonViewState.class);

    private ButtonViewState currentViewState = ButtonViewState.DEFAULT;

    public ButtonRenderer() {
        MutableTextStyle mts = new MutableTextStyle(IText.DEFAULT_STYLE);
        mts.setAlign(ETextAlign.CENTER);
        textRenderer.setDefaultStyle(mts.immutableCopy());

        textures.put(ButtonViewState.DEFAULT, new NinePatch());
    }

    @Override
    public void onAttached(IEventListener listener) {
        background.onAttached(listener);
        textRenderer.onAttached(listener);

        super.onAttached(listener);
    }

    @Override
    public void onDetached(IEventListener listener) {
        background.onDetached(listener);
        textRenderer.onDetached(listener);

        super.onDetached(listener);
    }

    protected void invalidateBackground() {
        INinePatch patch = textures.get(currentViewState);
        if (patch == null) {
            patch = textures.get(ButtonViewState.DEFAULT);
        }

        if (patch != null) {
            background.set(patch);
        }
    }

    @Override
    protected void render(IDrawBuffer drawBuffer, IDrawable parent, Area2D bounds) {
        background.render(drawBuffer, parent, bounds);

        // TODO #14: Vertical-align text
        textRenderer.render(drawBuffer, parent, bounds);
    }

    @Override
    public double getNativeWidth() {
        return Math.max(background.getWidth(), textRenderer.getWidth());
    }

    @Override
    public double getNativeHeight() {
        return Math.max(background.getHeight(), textRenderer.getHeight());
    }

    @Override
    public void setSize(double w, double h) {
        background.setSize(w, h);
        textRenderer.setSize(w, h);

        super.setSize(w, h);
    }

    @Override
    public StyledText getText() {
        return textRenderer.getText();
    }

    @Override
    public void setText(StyledText stext) {
        textRenderer.setText(stext);
    }

    @Override
    public void setTexture(ButtonViewState viewState, ITexture tex) {
        setTexture(viewState, new NinePatch(tex));
    }

    @Override
    public void setTexture(ButtonViewState viewState, INinePatch tex) {
        if (tex == null) {
            textures.remove(viewState);
        } else {
            textures.put(viewState, tex);
        }

        invalidateBackground();
    }

    @Override
    public void setViewState(ButtonViewState state) {
        currentViewState = state;

        invalidateBackground();
    }

}
