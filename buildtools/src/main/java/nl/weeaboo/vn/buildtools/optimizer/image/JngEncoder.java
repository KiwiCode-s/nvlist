package nl.weeaboo.vn.buildtools.optimizer.image;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;

import nl.weeaboo.vn.gdx.graphics.PixmapUtil;
import nl.weeaboo.vn.gdx.graphics.jng.JngWriter;

public class JngEncoder implements IImageEncoder {

    private final JngEncoderParams params = new JngEncoderParams();

    @Override
    public void encode(ImageWithDef image, File outputFile) throws IOException {
        Pixmap pixmap = image.getPixmap();

        DesktopJpegEncoder jpegEncoder = new DesktopJpegEncoder();
        DesktopPngEncoder pngEncoder = new DesktopPngEncoder();

        JngWriter writer = new JngWriter();

        // Set RGB color
        byte[] colorData = jpegEncoder.encodeJpeg(pixmap, params.getJpegQuality());
        writer.setColorInput(colorData);

        // Set alpha
        Pixmap alphaPixmap = extractAlpha(pixmap);
        byte[] alphaData;
        if (params.isAllowLossyAlpha()) {
            alphaData = jpegEncoder.encodeJpeg(alphaPixmap, params.getJpegAlphaQuality());
        } else {
            alphaData = pngEncoder.encodePng(alphaPixmap);
        }
        writer.setAlphaInput(alphaData);

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            writer.write(out);
        } finally {
            out.close();
        }
    }

    private static @Nullable Pixmap extractAlpha(Pixmap src) {
        Pixmap dst = PixmapUtil.newUninitializedPixmap(src.getWidth(), src.getHeight(), Format.Alpha);

        final ByteBuffer srcPixels = src.getPixels();
        final ByteBuffer dstPixels = dst.getPixels();

        switch (src.getFormat()) {
        case RGB565:
        case RGB888:
        case Intensity:
            // Format doesn't have alpha -- return an opaque image
            dst.setColor(Color.WHITE);
            dst.fill();
            break;
        case Alpha:
            // Format only has alpha, so just return a copy of the input
            PixmapUtil.copy(src, dst);
            break;
        case LuminanceAlpha: {
            final int limit = src.getWidth() * src.getHeight() * 2;
            for (int n = 0; n < limit; n += 2) {
                int a = srcPixels.get(n + 1) & 0xFF;
                dstPixels.put((byte)a);
            }
        } break;
        case RGBA4444: {
            // RGBA4444 is stored as shorts in native order (see Pixmap#getPixels)
            srcPixels.order(ByteOrder.nativeOrder());
            ShortBuffer pixels = srcPixels.asShortBuffer();

            final int limit = src.getWidth() * src.getHeight();
            for (int n = 0; n < limit; n++) {
                int a = pixels.get(n) & 0xF;
                dstPixels.put((byte)a);
            }
        } break;
        case RGBA8888: {
            final int limit = src.getWidth() * src.getHeight() * 4;
            for (int n = 0; n < limit; n += 4) {
                int a = srcPixels.get(n + 3) & 0xFF;
                dstPixels.put((byte)a);
            }
        } break;
        default:
            throw new IllegalArgumentException("Pixmap with unsupported format: " + src.getFormat());
        }

        dstPixels.flip();
        return dst;
    }

}
