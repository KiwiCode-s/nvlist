package nl.weeaboo.vn.script.impl.lib;

import java.util.Collection;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import nl.weeaboo.common.Checks;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.ETextAttribute;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.script.impl.lua.LuaConvertUtil;

final class BasicTagHandler extends VarArgFunction {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(BasicTagHandler.class);

    @Override
    public Varargs invoke(Varargs args) {
        String tagId = args.tojstring(1);
        LuaTable table = args.opttable(2, new LuaTable());

        Tag tag = Tag.fromTagId(tagId);
        if (tag == null) {
            LOG.warn("Unsupported tag id: {}", tagId);
            return NONE;
        }

        TextStyle style = handleTag(tag, table);
        if (style == null) {
            return NONE;
        }

        return varargsOf(NIL, LuajavaLib.toUserdata(style, TextStyle.class));
    }

    private TextStyle handleTag(Tag tag, LuaTable table) {
        int n = 1;

        switch (tag) {
        case B: return newStyle(ETextAttribute.FONT_STYLE, valueOf("bold"));
        case I: return newStyle(ETextAttribute.FONT_STYLE, valueOf("italic"));
        case U: return newStyle(ETextAttribute.UNDERLINE, valueOf(true));
        case FONT: return newStyle(ETextAttribute.FONT_NAME, table.rawget(n));
        case COLOR: return newStyle(ETextAttribute.COLOR, table.rawget(n));
        case SIZE: return newStyle(ETextAttribute.FONT_SIZE, table.rawget(n));
        case SPEED: return newStyle(ETextAttribute.SPEED, table.rawget(n));
        case ALIGN: return newStyle(ETextAttribute.ALIGN, table.rawget(n));
        case CENTER: return newStyle(ETextAttribute.ALIGN, valueOf("center"));
        default:
            LOG.warn("No implementation for tag: {}", tag.getTagId());
            return null;
        }
    }

    private static TextStyle newStyle(ETextAttribute attribute, LuaValue luaValue) {
        MutableTextStyle mts = new MutableTextStyle();
        Object javaValue = LuaConvertUtil.parseTextAttribute(attribute, luaValue);
        if (javaValue != null) {
            mts.setProperty(attribute, javaValue);
        }
        return mts.immutableCopy();
    }

    public static Collection<String> getSupportedTags() {
        ImmutableSet.Builder<String> tagIds = ImmutableSet.builder();
        for (Tag tag : Tag.values()) {
            tagIds.add(tag.getTagId());
        }
        return tagIds.build();
    }

    private enum Tag {
        B("b"),
        I("i"),
        U("u"),
        FONT("font"),
        COLOR("color"),
        SIZE("size"),
        SPEED("speed"),
        ALIGN("align"),
        CENTER("center");

        private final String tagId;

        private Tag(String tagId) {
            this.tagId = Checks.checkNotNull(tagId);
        }

        public String getTagId() {
            return tagId;
        }

        public static Tag fromTagId(String tagId) {
            for (Tag tag : values()) {
                if (tag.tagId.equals(tagId)) {
                    return tag;
                }
            }
            return null;
        }
    }

}