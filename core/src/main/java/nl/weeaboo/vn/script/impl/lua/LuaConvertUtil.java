package nl.weeaboo.vn.script.impl.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.styledtext.ETextAttribute;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyleParseException;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.core.ResourceLoadInfo;
import nl.weeaboo.vn.core.impl.ContextUtil;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.IScreenshot;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.IScreen;
import nl.weeaboo.vn.script.ScriptException;

public final class LuaConvertUtil {

    public static ILayer getLayerArg(Varargs args, int index) throws ScriptException {
        if (args.isuserdata(index)) {
            ILayer layer = args.touserdata(index, ILayer.class);
            if (layer != null) {
                return layer;
            }
        } else if (args.isnil(index)) {
            return null;
        }
        throw new ScriptException("Invalid layer arg: " + args.tojstring(1));
    }

    private static IScreen getCurrentScreen() throws ScriptException {
        IScreen currentScreen = ContextUtil.getCurrentScreen();
        if (currentScreen == null) {
            throw new ScriptException("No screen active");
        }
        return currentScreen;
    }

    public static ILayer getActiveLayer() throws ScriptException {
        return getCurrentScreen().getActiveLayer();
    }

    public static ILayer getRootLayer() throws ScriptException {
        return getCurrentScreen().getRootLayer();
    }

    public static ITexture getTextureArg(IImageModule imageModule, Varargs args, int index)
            throws ScriptException {

        if (args.isstring(index)) {
            // Texture filename
            ResourceLoadInfo loadInfo = LuaScriptUtil.createLoadInfo(args.tojstring(index));
            return imageModule.getTexture(loadInfo, false);
        } else if (args.isuserdata(index)) {
            // Texture or screenshot object
            Object obj = args.touserdata(index);
            if (obj instanceof ITexture) {
                return (ITexture)obj;
            } else if (obj instanceof IScreenshot) {
                IScreenshot ss = (IScreenshot)obj;
                if (!ss.isAvailable()) {
                    throw new ScriptException("Screenshot data isn't available yet");
                }
                return imageModule.createTexture(ss);
            } else {
                throw new ScriptException("Invalid arguments");
            }
        } else if (!args.isnil(index)) {
            throw new ScriptException("Invalid arguments");
        }
        return null;
    }

    public static TextStyle getTextStyleArg(LuaValue val) throws ScriptException {
        TextStyle ts = val.touserdata(TextStyle.class);
        if (ts != null) {
            // The value is already a TextStyle
            return ts;
        }

        if (val.isstring()) {
            try {
                return TextStyle.fromString(val.tojstring());
            } catch (StyleParseException e) {
                throw new ScriptException("Unable to parse text style: " + val.tojstring(), e);
            }
        } else if (val.istable()) {
            LuaTable table = val.checktable();

            MutableTextStyle mts = new MutableTextStyle();
            for (LuaValue key : table.keys()) {
                ETextAttribute attribute = ETextAttribute.fromId(key.toString());
                if (attribute == null) {
                    continue;
                }

                Object javaValue = parseTextAttribute(attribute, table.get(key));
                if (javaValue != null) {
                    mts.setProperty(attribute, javaValue);
                }
            }
            return mts.immutableCopy();
        } else {
            return TextStyle.defaultInstance();
        }
    }

    public static Object parseTextAttribute(ETextAttribute attribute, LuaValue luaValue) {
        if (luaValue.isstring()) {
            return attribute.valueFromString(luaValue.tojstring());
        } else {
            return CoerceLuaToJava.coerceArg(luaValue, attribute.getType());
        }
    }

}