package nl.weeaboo.vn;

import java.awt.Color;
import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.google.common.collect.Iterables;
import com.google.common.io.BaseEncoding;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.gdx.gl.GdxBitmapTweenRenderer;
import nl.weeaboo.gdx.scene2d.Scene2dEnv;
import nl.weeaboo.styledtext.EFontStyle;
import nl.weeaboo.styledtext.MutableStyledText;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.core.IContext;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.IInput;
import nl.weeaboo.vn.core.INovel;
import nl.weeaboo.vn.core.IRenderEnv;
import nl.weeaboo.vn.core.InitException;
import nl.weeaboo.vn.core.KeyCode;
import nl.weeaboo.vn.core.ResourceLoadInfo;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.INinePatch.EArea;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.ITextureRenderer;
import nl.weeaboo.vn.image.impl.BitmapTweenConfig;
import nl.weeaboo.vn.image.impl.BitmapTweenConfig.ControlImage;
import nl.weeaboo.vn.image.impl.NinePatchRenderer;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.save.SaveFormatException;
import nl.weeaboo.vn.save.impl.SaveParams;
import nl.weeaboo.vn.scene.IButton;
import nl.weeaboo.vn.scene.IImageDrawable;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.IRenderable;
import nl.weeaboo.vn.scene.IScreen;
import nl.weeaboo.vn.scene.ITextDrawable;
import nl.weeaboo.vn.scene.ITransformable;
import nl.weeaboo.vn.scene.impl.EntityHelper;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.lua.LuaConsole;
import nl.weeaboo.vn.sound.ISound;
import nl.weeaboo.vn.sound.ISoundModule;
import nl.weeaboo.vn.sound.SoundType;

final class DebugControls {

    private static final Logger LOG = LoggerFactory.getLogger(DebugControls.class);

    private final LuaConsole luaConsole;

    public DebugControls(Scene2dEnv sceneEnv) {
        this.luaConsole = new LuaConsole(sceneEnv);
    }

    public void update(INovel novel, IInput input) {
        IEnvironment env = novel.getEnv();
        IRenderEnv renderEnv = env.getRenderEnv();

        IContext activeContext = Iterables.get(env.getContextManager().getActiveContexts(), 0);
        IScriptContext scriptContext = null;
        IScreen screen = null;
        if (activeContext != null) {
            scriptContext = activeContext.getScriptContext();
            screen = activeContext.getScreen();
        }

        boolean alt = input.isPressed(KeyCode.ALT_LEFT, true);

        // Reset
        if (input.consumePress(KeyCode.F5)) {
            try {
                novel.restart();
            } catch (InitException e) {
                LOG.error("Fatal error during restart", e);
            }
        }

        // Save/load
        ISaveModule saveModule = env.getSaveModule();
        int slot = saveModule.getQuickSaveSlot(99);
        if (input.consumePress(KeyCode.PLUS)) {
            LOG.debug("Save");
            SaveParams saveParams = new SaveParams();
            try {
                saveModule.save(novel, slot, saveParams, null);
            } catch (SaveFormatException e) {
                LOG.warn("Save error", e);
            } catch (IOException e) {
                LOG.warn("Save error", e);
            }
        } else if (input.consumePress(KeyCode.MINUS)) {
            LOG.debug("Load");
            try {
                saveModule.load(novel, slot, null);
            } catch (SaveFormatException e) {
                LOG.warn("Load error", e);
            } catch (IOException e) {
                LOG.warn("Load error", e);
            }
        }

        // Fullscreen toggle
        if (alt && input.consumePress(KeyCode.ENTER)) {
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(renderEnv.getWidth(), renderEnv.getHeight());
            }
        }

        // Image
        IImageModule imageModule = env.getImageModule();
        if (screen != null && alt && input.consumePress(KeyCode.I)) {
            createImage(screen.getRootLayer(), imageModule);
        }
        if (screen != null && alt && input.consumePress(KeyCode.J)) {
            for (int n = 0; n < 100; n++) {
                createImage(screen.getRootLayer(), imageModule);
            }
        }
        if (screen != null && alt && input.consumePress(KeyCode.K)) {
            createBitmapTweenImage(screen.getRootLayer(), imageModule);
        }
        if (screen != null && alt && input.consumePress(KeyCode.N)) {
            createNinePatchImage(screen.getRootLayer(), imageModule);
        }

        // Text
        if (screen != null && alt && input.consumePress(KeyCode.T)) {
            createText(screen.getRootLayer());
        }
        if (screen != null && alt && input.consumePress(KeyCode.Y)) {
            createLongText(screen.getRootLayer());
        }

        // Button
        if (screen != null && alt && input.consumePress(KeyCode.B)) {
            createButton(screen.getRootLayer(), scriptContext);
        }

        // Music
        ISoundModule soundModule = env.getSoundModule();
        if (alt && input.consumePress(KeyCode.PERIOD)) {
            soundModule.getSoundController().stopAll();
        }
        if (alt && input.consumePress(KeyCode.M)) {
            try {
                ISound sound = soundModule.createSound(SoundType.MUSIC,
                        new ResourceLoadInfo("music.ogg"));
                sound.start(-1);
            } catch (IOException e) {
                LOG.warn("Audio error", e);
            }
        }

        // Lua console
        luaConsole.setActiveContext(activeContext);
        if (input.consumePress(KeyCode.F1)) {
            luaConsole.setVisible(!luaConsole.isVisible());
        }
    }

    public void update(ITransformable transformable, IInput input) {
        IRenderable renderer = null;
        if (transformable instanceof IImageDrawable) {
            IImageDrawable image = (IImageDrawable)transformable;
            renderer = image.getRenderer();
        }

        if (input.isPressed(KeyCode.CONTROL_LEFT, true)) {
            if (input.isPressed(KeyCode.LEFT, false)) transformable.rotate(4);
            if (input.isPressed(KeyCode.RIGHT, false)) transformable.rotate(-4);
        } else if (input.isPressed(KeyCode.SHIFT_LEFT, true)) {
            if (input.isPressed(KeyCode.UP, false)) transformable.scale(1, 8 / 9.);
            if (input.isPressed(KeyCode.DOWN, false)) transformable.scale(1, 1.125);
            if (input.isPressed(KeyCode.LEFT, false)) transformable.scale(8 / 9., 1);
            if (input.isPressed(KeyCode.RIGHT, false)) transformable.scale(1.125, 1);
        } else if (input.isPressed(KeyCode.ALT_LEFT, true)) {
            if (renderer instanceof ITextureRenderer) {
                ITextureRenderer texRenderer = (ITextureRenderer)renderer;
                if (input.isPressed(KeyCode.UP, false)) texRenderer.scrollUV(0, .05);
                if (input.isPressed(KeyCode.DOWN, false)) texRenderer.scrollUV(0, -.05);
                if (input.isPressed(KeyCode.LEFT, false)) texRenderer.scrollUV(.05, 0);
                if (input.isPressed(KeyCode.RIGHT, false)) texRenderer.scrollUV(-.05, 0);
            }
        } else {
            if (input.isPressed(KeyCode.UP, false)) transformable.translate(0, 5);
            if (input.isPressed(KeyCode.DOWN, false)) transformable.translate(0, -5);
            if (input.isPressed(KeyCode.LEFT, false)) transformable.translate(-5, 0);
            if (input.isPressed(KeyCode.RIGHT, false)) transformable.translate(5, 0);
        }
    }

    private static void createImage(ILayer layer, IImageModule imageModule) {
        IImageDrawable image = imageModule.createImage(layer);
        image.setPos(640, 360);
        image.setAlign(.5, .5);

        ITexture texture = imageModule.getTexture("test");
        image.setTexture(texture);
    }

    private void createNinePatchImage(ILayer layer, IImageModule imageModule) {
        IImageDrawable image = imageModule.createImage(layer);
        image.setPos(640, 360);

        ITexture texture = imageModule.getTexture("test");
        NinePatchRenderer renderer = new NinePatchRenderer();
        renderer.setInsets(Insets2D.of(50));
        for (EArea area : EArea.values()) {
            renderer.setTexture(area, texture);
        }
        renderer.setSize(400, 400);
        image.setRenderer(renderer, 5);
    }

    private static void createButton(ILayer layer, IScriptContext scriptContext) {
        EntityHelper entityHelper = new EntityHelper();
        IButton button = entityHelper.createButton(layer, scriptContext);
        button.setSize(100, 20);
        button.setText("Test");
        button.setPos(800, 200);
    }

    private static void createText(ILayer layer) {
        EntityHelper entityHelper = new EntityHelper();
        ITextDrawable text = entityHelper.createText(layer);

        text.setBounds(200, 200, 800, 200);
        text.setZ((short)-1000);
        text.setDefaultStyle(new TextStyle(null, 32));
        text.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        text.setVisibleText(0f);
    }

    private static void createLongText(ILayer layer) {
        EntityHelper entityHelper = new EntityHelper();
        ITextDrawable text = entityHelper.createText(layer);
        text.setBounds(0, 0, 1280, 720);

        MutableTextStyle mts = new MutableTextStyle();
        mts.setColor(0xFF404040);
        mts.setSpeed(10f);
        text.setDefaultStyle(mts.immutableCopy());

        Random random = new Random();
        BaseEncoding encoder = BaseEncoding.base64().omitPadding();
        MutableStyledText mst = new MutableStyledText();
        for (int w = 0; w < 1000; w++) {
            byte[] bytes = new byte[3 + random.nextInt(5)];
            random.nextBytes(bytes);

            mts.setFontSize(16 + 16 * random.nextInt(2));

            switch (random.nextInt(10)) {
            case 0:
                mts.setFontStyle(EFontStyle.BOLD);
                break;
            case 1:
                mts.setFontStyle(EFontStyle.ITALIC);
                break;
            default:
                mts.setFontStyle(EFontStyle.PLAIN);
            }

            mts.setColor(0xFF000000 | Color.HSBtoRGB(random.nextFloat(), .8f, .9f));

            mst.append(new StyledText(encoder.encode(bytes), mts.immutableCopy()));
            mst.append(' ', null);
        }
        text.setText(mst.immutableCopy());
        text.setVisibleText(0f);
    }

    private static void createBitmapTweenImage(ILayer layer, IImageModule imageModule) {
        IImageDrawable image = imageModule.createImage(layer);
        image.setPos(640, 360);
        image.setAlign(.5, .5);

        ITexture texture = imageModule.getTexture("test");
        ITexture control = imageModule.getTexture("fade/shutter-right");

        BitmapTweenConfig config = new BitmapTweenConfig(600, new ControlImage(control, false));
        config.setStartTexture(texture);
        config.setEndTexture(null);

        image.setRenderer(new GdxBitmapTweenRenderer(imageModule, config));
        image.setSize(150, 75);
    }

}
