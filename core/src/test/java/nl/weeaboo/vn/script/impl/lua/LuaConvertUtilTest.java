package nl.weeaboo.vn.script.impl.lua;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import nl.weeaboo.lua2.lib.CoerceJavaToLua;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.impl.ImageModuleStub;
import nl.weeaboo.vn.image.impl.TestScreenshot;
import nl.weeaboo.vn.image.impl.TestTexture;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.impl.Layer;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.impl.lua.LuaConvertUtil;

public class LuaConvertUtilTest {

    private ImageModuleStub imageModule;

    @Before
    public void before() {
        LuaTestUtil.newRunState();

        imageModule = new ImageModuleStub();
    }

    @Test
    public void getLayerArg() throws ScriptException {
        final ILayer dummyLayer = new Layer(null);

        assertLayerArg(dummyLayer, 1);
        // Different indices work
        assertLayerArg(dummyLayer, 2);
        // Null values work
        assertLayerArg(null, 1);
        // Incompatible types throw an exception
        assertLayerArgException(Integer.valueOf(7), 1);
        assertLayerArgException(new Object(), 1);
    }

    @Test
    public void getTextureArg() throws ScriptException {
        final ITexture dummyTexture = new TestTexture();

        assertTextureArg(dummyTexture, 1);
        // Different indices work
        assertTextureArg(dummyTexture, 2);

        // Strings work
        assertTextureArgNotNull("filename.jpg", 1);
        // Lua considers numbers convertible to string
        assertTextureArgNotNull(7, 1);

        // Screenshots throw an exception if not available
        TestScreenshot screenshot = new TestScreenshot();
        assertTextureArgException(screenshot, 1);
        // Screenshots work if they are available
        screenshot.makeAvailable(10, 10);
        assertTextureArgNotNull(screenshot, 1);

        // Null works
        assertTextureArg(null, 1);

        // Incompatible types throw an exception
        assertTextureArgException(true, 1);
        assertTextureArgException(new Object(), 1);
    }

    private void assertTextureArgException(Object javaValue, int index) {
        try {
            assertTextureArg(javaValue, index);
            Assert.fail("Expected an exception");
        } catch (ScriptException se) {
            // Expected
        }
    }

    private void assertTextureArgNotNull(Object javaValue, int index) throws ScriptException {
        Varargs varargs = createVararg(javaValue, index);
        Assert.assertNotNull(LuaConvertUtil.getTextureArg(imageModule, varargs, index + 1));
    }

    private void assertTextureArg(Object javaValue, int index) throws ScriptException {
        Varargs varargs = createVararg(javaValue, index);
        Assert.assertEquals(javaValue, LuaConvertUtil.getTextureArg(imageModule, varargs, index + 1));
    }

    private void assertLayerArgException(Object javaValue, int index) {
        try {
            assertLayerArg(javaValue, index);
            Assert.fail("Expected an exception");
        } catch (ScriptException se) {
            // Expected
        }
    }

    private void assertLayerArg(Object javaValue, int index) throws ScriptException {
        Varargs varargs = createVararg(javaValue, index);
        Assert.assertEquals(javaValue, LuaConvertUtil.getLayerArg(varargs, index + 1));
    }

    public static Varargs createVararg(Object javaValue, int index) {
        LuaValue[] vals = new LuaValue[index + 1];
        Arrays.fill(vals, LuaValue.NIL);
        vals[index] = CoerceJavaToLua.coerce(javaValue);
        return LuaValue.varargsOf(vals);
    }

}