package nl.weeaboo.vn.impl.save;

import java.io.IOException;

import nl.weeaboo.common.Dim;
import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.vn.save.SaveFormatException;
import nl.weeaboo.vn.save.ThumbnailInfo;

final class ThumbnailInfoJson {

    public String path;
    public int width;
    public int height;

    public static ThumbnailInfo decode(ThumbnailInfoJson json) throws SaveFormatException, IOException {
        try {
            return new ThumbnailInfo(FilePath.of(json.path), Dim.of(json.width, json.height));
        } catch (IllegalArgumentException iae) {
            throw new SaveFormatException("Invalid parameters", iae);
        }
    }

    public static ThumbnailInfoJson encode(ThumbnailInfo tInfo) {
        ThumbnailInfoJson json = new ThumbnailInfoJson();
        json.width = tInfo.getImageSize().w;
        json.height = tInfo.getImageSize().h;
        json.path = tInfo.getPath().toString();
        return json;
    }

}
