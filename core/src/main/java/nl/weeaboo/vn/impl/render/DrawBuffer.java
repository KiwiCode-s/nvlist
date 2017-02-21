package nl.weeaboo.vn.impl.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Sort;
import com.google.common.collect.ImmutableList;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.styledtext.layout.ITextLayout;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.IWritableScreenshot;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.render.IDrawTransform;
import nl.weeaboo.vn.render.IRenderLogic;

public final class DrawBuffer implements IDrawBuffer {

    private final IntArray layerStarts = new IntArray();
    private final Array<BaseRenderCommand> commands = Array.of(BaseRenderCommand.class);

    @Override
    public void reset() {
        layerStarts.clear();
        commands.clear();
    }

    @Override
    public int reserveLayerIds(int count) {
        int firstId = layerStarts.size;
        for (int n = 0; n < count; n++) {
            layerStarts.add(-1);
        }
        return firstId;
    }

    @Override
    public void startLayer(int layerId, short z, Rect2D bounds) {
        if (layerId < 0 || layerId >= layerStarts.size) {
            throw new IllegalArgumentException("The given layerId hasn't been reserved yet: " + layerId);
        }

        if (layerId == 0 && commands.size == 0) {
            draw(new LayerRenderCommand(layerId, z, bounds));
        }

        layerStarts.set(layerId, commands.size);
    }

    @Override
    public void drawQuad(IDrawTransform dt, int argb, ITexture tex, Area2D bounds, Area2D uv) {
        draw(new QuadRenderCommand(dt.getZ(), dt.isClipEnabled(), dt.getBlendMode(), argb, tex,
                dt.getTransform(), bounds, uv));
    }

    @Override
    public void screenshot(IWritableScreenshot ss, boolean clip) {
        draw(new ScreenshotRenderCommand(ss, clip));
    }

    @Override
    public void drawLayer(int layerId, short z, Rect2D layerBounds) {
        draw(new LayerRenderCommand(layerId, z, layerBounds));
    }

    @Override
    public void drawText(IDrawTransform dt, double dx, double dy, ITextLayout textLayout,
            double visibleGlyphs) {
        draw(new TextRenderCommand(dt, dx, dy, textLayout, visibleGlyphs));
    }

    @Override
    public void drawCustom(IDrawTransform dt, int argb, IRenderLogic renderLogic) {
        draw(new CustomRenderCommand(dt.getZ(), dt.isClipEnabled(), dt.getBlendMode(), argb,
                dt.getTransform(), renderLogic));
    }

    /** Adds a draw command to the draw buffer. */
    public void draw(BaseRenderCommand cmd) {
        commands.add(cmd);
    }

    /** Returns the draw command for the root layer, or {@code null} if no such command exists. */
    public LayerRenderCommand getRootLayerCommand() {
        if (layerStarts.size == 0) {
            return null;
        }
        return (LayerRenderCommand)commands.get(0);
    }

    private int getLayerStart(int layerId) {
        return layerStarts.get(layerId);
    }

    private int getLayerEnd(int layerId) {
        int nextId = layerId + 1;
        if (nextId < layerStarts.size && layerStarts.get(nextId) >= 0) {
            return layerStarts.get(nextId);
        } else {
            return commands.size;
        }
    }

    /**
     * Returns the render commands for the requested layer, or an empty collection if nothing is stored for that layer
     * (even if the layer doesn't exist).
     */
    public List<? extends BaseRenderCommand> getLayerCommands(int layerId) {
        int start = getLayerStart(layerId);
        int end = getLayerEnd(layerId);
        if (end <= start) {
            return Collections.emptyList();
        }
        Sort.instance().sort(commands.items, start, end);
        return Arrays.asList(commands.items).subList(start, end);
    }

    /**
     * Returns a snapshot of all buffered draw commands.
     */
    public ImmutableList<RenderCommand> getCommands() {
        return ImmutableList.<RenderCommand>copyOf(commands);
    }

}
