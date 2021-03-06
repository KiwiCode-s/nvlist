package nl.weeaboo.vn.gdx.graphics.jng;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Dim;
import nl.weeaboo.vn.gdx.graphics.jng.JngHeader.AlphaSettings;
import nl.weeaboo.vn.gdx.graphics.jng.JngHeader.ColorSettings;

/**
 * Writes JNG images.
 */
public final class JngWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JngWriter.class);

    private Dim size;

    private @Nullable byte[] colorBytes;
    private int colorComponents;
    private int colorSampleDepth;

    private @Nullable byte[] alphaBytes;
    private JngAlphaType alphaType;

    public JngWriter() {
        resetImageState();
    }

    /**
     * Reset all image-specific state.
     */
    public final void resetImageState() {
        size = Dim.EMPTY;

        colorBytes = null;
        colorComponents = 0;
        colorSampleDepth = 8;

        alphaBytes = null;
        alphaType = JngAlphaType.PNG;
    }

    private JngColorType getOutputColorType() {
        if (alphaBytes == null) {
            if (colorComponents == 1) {
                return JngColorType.GRAY;
            } else {
                return JngColorType.COLOR;
            }
        } else {
            if (colorComponents == 1) {
                return JngColorType.GRAY_ALPHA;
            } else {
                return JngColorType.COLOR_ALPHA;
            }
        }
    }

    /**
     * Sets the color (RGB) part of the image.
     *
     * @param jpegData A JPEG encoded image
     * @throws IOException If an error occurs while attempting to parse the JPEG data.
     * @see #setAlphaInput(byte[])
     */
    public void setColorInput(byte[] jpegData) throws IOException {
        Checks.checkArgument(JpegHelper.isJpeg(jpegData), "Color input must be a JPEG file");

        DataInputStream din = new DataInputStream(new ByteArrayInputStream(jpegData));

        // Skip JPEG magic (already checked that the jpegData start with the expected value)
        din.readFully(new byte[JpegHelper.JPEG_MAGIC.length]);

        while (true) {
            byte preMarkerByte = din.readByte();
            if (preMarkerByte != (byte)0xFF) {
                throw new IOException(String.format("Marker not preceeded by 0xFF: 0x%02x", preMarkerByte));
            }

            int marker = din.readUnsignedByte();

            int length = din.readUnsignedShort();
            if (marker >= 0xc0 && marker <= 0xcf) {
                colorSampleDepth = din.readUnsignedByte();
                int h = din.readUnsignedShort();
                int w = din.readUnsignedShort();
                size = Dim.of(w, h);
                colorComponents = din.readUnsignedByte();
                break;
            } else {
                JngInputUtil.forceSkip(din, length - 2); // Skip segment
            }
        }

        colorBytes = jpegData;
    }

    /**
     * Sets the alpha (transparency) part of the image. The color input must be set first.
     *
     * @param imageData A JPEG or PNG encoded image
     * @throws IllegalStateException When this method is called before the color input is set.
     * @throws IllegalArgumentException If the passed image is incompatible because it has the wrong
     *         dimensions, or an invalid/unsupported encoding.
     * @throws IOException If an error occurs while attempting to parse the image data.
     * @see #setColorInput(byte[])
     */
    public void setAlphaInput(byte[] imageData) throws IOException {
        if (colorBytes == null) {
            throw new IllegalStateException("Must set color input before alpha");
        }

        if (PngHelper.isPng(imageData)) {
            ByteBuffer buf = ByteBuffer.wrap(imageData);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.position(buf.position() + 16); // Skip until width
            int width = buf.getInt();
            int height = buf.getInt();
            Dim alphaSize = Dim.of(width, height);

            Checks.checkArgument(alphaSize.equals(size),
                    "Alpha image size (" + alphaSize + ") != color size (" + size + ")");

            int bitDepth = buf.get() & 0xFF;
            if (bitDepth > 8) {
                LOG.debug("Alpha input with bitDepth={} found, result image may use a lower bit depth",
                        bitDepth);
            }

            int colorType = buf.get() & 0xFF;
            Checks.checkArgument(colorType == PngColorType.GRAYSCALE.toInt(),
                    "Non-grayscale color type: " + colorType);

            alphaBytes = PngHelper.readIDAT(new ByteArrayInputStream(imageData));
            alphaType = JngAlphaType.PNG;
        } else if (JpegHelper.isJpeg(imageData)) {
            alphaBytes = imageData;

            // TODO: Check dimensions

            alphaType = JngAlphaType.JPEG;
        } else {
            throw new IllegalArgumentException("Alpha input was an invalid file type (must be PNG or JPEG)");
        }
    }

    /**
     * @see #write(DataOutput)
     * @throws IOException If an I/O error occurs while writing the file.
     */
    public void write(OutputStream out) throws IOException {
        write(out instanceof DataOutput ? (DataOutput)out : new DataOutputStream(out));
    }

    /**
     * Writes the encoded JNG file to the supplied output. The color/image data
     *
     * @throws IOException If an I/O error occurs while writing the file.
     */
    public void write(DataOutput dout) throws IOException {
        Checks.checkState(colorBytes != null, "Color input must be set first");

        JngColorType outputColorType = getOutputColorType();

        ColorSettings color = new ColorSettings();
        color.colorType = outputColorType;
        color.sampleDepth = colorSampleDepth;

        AlphaSettings alpha = new AlphaSettings();
        alpha.compressionMethod = alphaType;

        JngHeader header = new JngHeader(size, color, alpha);
        header.write(dout);

        if (outputColorType.hasAlpha()) {
            switch (alphaType) {
            case PNG: {
                writeChunk(dout, JngConstants.CHUNK_IDAT, alphaBytes);
            } break;
            case JPEG: {
                writeChunk(dout, JngConstants.CHUNK_JDAA, alphaBytes);
            } break;
            default:
                throw new IllegalStateException("Invalid alphaType: " + alphaType);
            }
        }

        writeChunk(dout, JngConstants.CHUNK_JDAT, colorBytes);

        dout.write(PngHelper.IEND);
    }

    static void writeChunk(DataOutput dout, int chunkType, byte[] data) throws IOException {
        dout.writeInt(data.length);

        CRC32 crc = new CRC32();

        crc.update((chunkType >> 24) & 0xFF);
        crc.update((chunkType >> 16) & 0xFF);
        crc.update((chunkType >> 8 ) & 0xFF);
        crc.update( chunkType        & 0xFF);
        dout.writeInt(chunkType);

        crc.update(data, 0, data.length);
        dout.write(data, 0, data.length);
        dout.writeInt((int)crc.getValue());
    }

}
