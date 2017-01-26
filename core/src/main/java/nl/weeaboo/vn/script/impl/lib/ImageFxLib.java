package nl.weeaboo.vn.script.impl.lib;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.gdx.graphics.GdxTextureUtil;
import nl.weeaboo.lua2.luajava.CoerceLuaToJava;
import nl.weeaboo.lua2.luajava.LuajavaLib;
import nl.weeaboo.lua2.vm.LuaNil;
import nl.weeaboo.lua2.vm.LuaString;
import nl.weeaboo.lua2.vm.LuaTable;
import nl.weeaboo.lua2.vm.LuaValue;
import nl.weeaboo.lua2.vm.Varargs;
import nl.weeaboo.vn.core.Direction;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.impl.ContextUtil;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.render.IOffscreenRenderTask;
import nl.weeaboo.vn.render.impl.fx.BlurTask;
import nl.weeaboo.vn.render.impl.fx.ColorMatrix;
import nl.weeaboo.vn.render.impl.fx.ColorMatrixTask;
import nl.weeaboo.vn.render.impl.fx.ImageCompositeConfig;
import nl.weeaboo.vn.render.impl.fx.ImageCompositeConfig.TextureEntry;
import nl.weeaboo.vn.render.impl.fx.ImageCompositeTask;
import nl.weeaboo.vn.scene.IScreen;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptFunction;
import nl.weeaboo.vn.script.impl.lua.LuaConvertUtil;

public class ImageFxLib extends LuaLib {

    private static final long serialVersionUID = 1L;

    // Precomputed constants for LuaString instances we sometimes need.
    private static final LuaValue S_TEX = LuaString.valueOf("tex");
    private static final LuaValue S_POS = LuaString.valueOf("pos");

    private final IEnvironment env;

    public ImageFxLib(IEnvironment env) {
        super("ImageFx");

        this.env = env;
    }

    @ScriptFunction
    public Varargs crop(Varargs args) throws ScriptException {
        IImageModule imageModule = env.getImageModule();

        ITexture tex = LuaConvertUtil.getTextureArg(imageModule, args.arg(1));
        if (tex == null) {
            return LuaNil.NIL;
        }

        double x = args.optdouble(2, 0.0);
        double y = args.optdouble(3, 0.0);
        double w = args.optdouble(4, tex.getWidth());
        double h = args.optdouble(5, tex.getHeight());

        TextureRegion cropped = GdxTextureUtil.getTextureRegion(tex, Area2D.of(x, y, w, h));
        if (cropped == null) {
            throw new ScriptException("Unsupported texture type: " + tex);
        }

        return LuajavaLib.toUserdata(cropped, ITexture.class);
    }

    private static Set<Direction> getDirectionsSet(Varargs args, int index, int defaultValue) {
        LuaValue val = args.arg(index);
        if (val.isboolean()) {
            if (val.toboolean()) {
                return EnumSet.of(Direction.CENTER);
            } else {
                return EnumSet.noneOf(Direction.class);
            }
        }

        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        int intValue = val.optint(defaultValue);
        // Each digit of the int value represents a direction. Iterate over the digits.
        while (intValue > 0) {
            int digit = intValue % 10;

            result.add(Direction.fromInt(digit));

            intValue /= 10;
        }
        return result;
    }

    private void addOffscreenRenderTask(IOffscreenRenderTask task) throws ScriptException {
        IScreen currentScreen = ContextUtil.getCurrentScreen();
        if (currentScreen == null) {
            throw new ScriptException("No screen is current");
        }
        currentScreen.getOffscreenRenderTaskBuffer().add(task);
    }

    @ScriptFunction
    public Varargs blur(Varargs args) throws ScriptException {
        IImageModule imageModule = env.getImageModule();

        ITexture tex = LuaConvertUtil.getTextureArg(imageModule, args.arg(1));
        if (tex == null) {
            return LuaNil.NIL;
        }
        double radius = args.optdouble(2, 8.0);
        Set<Direction> expandDirs = getDirectionsSet(args, 3, 2468);

        BlurTask task = new BlurTask(imageModule, tex, radius);
        task.setExpandDirs(expandDirs);
        addOffscreenRenderTask(task);
        return LuajavaLib.toUserdata(task, IOffscreenRenderTask.class);
    }

    @ScriptFunction
    public Varargs brighten(Varargs args) throws ScriptException {
        IImageModule imageModule = env.getImageModule();

        ITexture tex = LuaConvertUtil.getTextureArg(imageModule, args.arg(1));
        if (tex == null) {
            return LuaNil.NIL;
        }
        double add = args.optdouble(2, 0.5);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setOffsets(new double[] {add, add, add, 0.0});

        ColorMatrixTask task = new ColorMatrixTask(imageModule, tex, matrix);
        addOffscreenRenderTask(task);
        return LuajavaLib.toUserdata(task, IOffscreenRenderTask.class);
    }

    @ScriptFunction
    public Varargs colorMatrix(Varargs args) throws ScriptException {
        IImageModule imageModule = env.getImageModule();

        ITexture tex = LuaConvertUtil.getTextureArg(imageModule, args.arg(1));
        if (tex == null) {
            return LuaNil.NIL;
        }

        ColorMatrix matrix = new ColorMatrix();
        double[] offsets = new double[4];
        if (args.arg(2).istable()) {
            double[] arr = toDoubleArray(args.arg(2), 5);
            matrix.setRedFactors(arr);
            offsets[0] = arr[0];
        }
        if (args.arg(3).istable()) {
            double[] arr = toDoubleArray(args.arg(3), 5);
            matrix.setGreenFactors(arr);
            offsets[1] = arr[1];
        }
        if (args.arg(4).istable()) {
            double[] arr = toDoubleArray(args.arg(4), 5);
            matrix.setBlueFactors(arr);
            offsets[2] = arr[2];
        }
        if (args.arg(5).istable()) {
            double[] arr = toDoubleArray(args.arg(5), 5);
            matrix.setAlphaFactors(arr);
            offsets[3] = arr[3];
        }
        matrix.setOffsets(offsets);

        ColorMatrixTask task = new ColorMatrixTask(imageModule, tex, matrix);
        addOffscreenRenderTask(task);
        return LuajavaLib.toUserdata(task, IOffscreenRenderTask.class);
    }

    private static double[] toDoubleArray(LuaValue val, int length) {
        double[] values = CoerceLuaToJava.coerceArg(val, double[].class);
        return Arrays.copyOf(values, length);
    }

    /**
     * Expects a single argument of the form:
     *
     * <pre>
     * {
     *     {tex=myTexture1},
     *     {tex=myTexture2, pos={10, 10}},
     *     {tex=myTexture3}
     * }
     * </pre>
     *
     * <p>
     * The {@code pos} field is optional and assumed <code>{0, 0}</code> when omitted.
     */
    @ScriptFunction
    public Varargs composite(Varargs args) throws ScriptException {
        IImageModule imageModule = env.getImageModule();

        ImageCompositeConfig config = new ImageCompositeConfig();

        // Handle main table containing the compositing configuration
        LuaTable table = args.opttable(1, new LuaTable());
        LuaValue v;
        for (int n = 1; (v = table.get(n)) != LuaNil.NIL; n++) {
            // texture
            ITexture tex = LuaConvertUtil.getTextureArg(imageModule, v.get(S_TEX));
            TextureEntry entry = new TextureEntry(tex);

            // (optional) posistion
            LuaTable posT = v.get(S_POS).opttable(null);
            if (posT != null) {
                entry.setPos(posT.get(1).checkdouble(), posT.get(2).checkdouble());
            }

            // Add to config
            config.add(entry);
        }

        // (optional) explicit size
        final int w = args.toint(2);
        final int h = args.toint(3);
        if (w > 0 && h > 0) {
            config.setSize(w, h);
        }

        ImageCompositeTask task = new ImageCompositeTask(imageModule, config);
        addOffscreenRenderTask(task);
        return LuajavaLib.toUserdata(task, IOffscreenRenderTask.class);
    }

}
