package nl.weeaboo.vn.gdx.graphics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public final class PngUtil {

    private PngUtil() {
    }

    /**
     * @see Pixmap#Pixmap(byte[], int, int)
     * @throws IOException If an internal error occurs in the PNG encoder.
     */
    public static byte[] encodePng(Pixmap pixmap) throws IOException {
        PixmapIO.PNG encoder = new PixmapIO.PNG();
        encoder.setFlipY(false);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            encoder.write(bout, pixmap);
        } finally {
            bout.close();
            encoder.dispose();
        }

        return bout.toByteArray();
    }
}