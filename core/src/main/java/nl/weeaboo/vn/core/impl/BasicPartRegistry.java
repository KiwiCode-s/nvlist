package nl.weeaboo.vn.core.impl;

import nl.weeaboo.entity.PartRegistry;
import nl.weeaboo.entity.PartType;
import nl.weeaboo.vn.core.IButtonPart;
import nl.weeaboo.vn.core.IInputListener;
import nl.weeaboo.vn.image.impl.ImagePart;
import nl.weeaboo.vn.script.impl.ScriptPart;
import nl.weeaboo.vn.sound.impl.SoundPart;
import nl.weeaboo.vn.text.impl.TextPart;

public class BasicPartRegistry extends PartRegistry {

	private static final long serialVersionUID = 1L;

    public final PartType<ScriptPart> script;
    public final PartType<? extends IInputListener> input;

    public final PartType<DrawablePart> drawable;
	public final PartType<TransformablePart> transformable;
    public final PartType<ImagePart> image;
    public final PartType<? extends IButtonPart> button;
    // public final PartType<? extends IButtonDrawablePart> buttonDrawable;
    public final PartType<TextPart> text;

    public final PartType<SoundPart> sound;

	public BasicPartRegistry() {
        script = register("script", ScriptPart.class);
        input = register("input", InputHandlerPart.class);

        drawable = register("drawable", DrawablePart.class);
		transformable = register("transformable", TransformablePart.class);
        image = register("image", ImagePart.class);
        button = register("button", ButtonPart.class);
        //buttonDrawable = register("buttonDrawable", ButtonDrawable.class);
        text = register("text", TextPart.class);

        sound = register("sound", SoundPart.class);
	}

}