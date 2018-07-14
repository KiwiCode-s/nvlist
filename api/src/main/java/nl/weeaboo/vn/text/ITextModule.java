package nl.weeaboo.vn.text;

import nl.weeaboo.vn.core.IModule;
import nl.weeaboo.vn.core.IResourceResolver;

public interface ITextModule extends IModule {

    /** Returns the global text log. */
    ITextLog getTextLog();

    IResourceResolver getFontLoader();

    ILoadingFontStore getFontStore();

}
