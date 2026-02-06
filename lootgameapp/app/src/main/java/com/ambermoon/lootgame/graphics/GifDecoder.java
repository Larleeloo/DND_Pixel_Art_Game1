package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom GIF decoder that extracts all frames with per-frame delays.
 * Android's BitmapFactory only gets the first frame, so we parse the
 * GIF89a binary format ourselves.
 */
public class GifDecoder {

    public static class GifResult {
        public List<Bitmap> frames = new ArrayList<>();
        public List<Integer> delays = new ArrayList<>(); // ms per frame
        public int width;
        public int height;
    }

    // GIF format constants
    private static final int MAX_STACK_SIZE = 4096;

    private int[] gct; // global color table
    private int gctSize;
    private int bgColor;
    private int bgIndex;
    private int pixelAspect;
    private int lctSize;
    private int[] lct; // local color table
    private int width;
    private int height;

    // Current frame info
    private int ix, iy, iw, ih; // frame position/size
    private boolean interlace;
    private boolean lctFlag;
    private int dispose;
    private int lastDispose;
    private boolean transparency;
    private int transIndex;
    private int delay; // ms

    private byte[] block = new byte[256];
    private int blockSize;

    private int[] image; // current frame pixels
    private int[] lastImage; // previous frame pixels
    private byte[] pixels; // current data block pixels

    private byte[] rawData;
    private int dataPos;

    public GifResult decode(InputStream is) {
        GifResult result = new GifResult();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            rawData = baos.toByteArray();
            dataPos = 0;

            // Read header
            String header = "";
            for (int i = 0; i < 6; i++) header += (char) readByte();
            if (!header.startsWith("GIF")) return result;

            readLogicalScreenDescriptor();
            if (gctSize > 0) {
                gct = readColorTable(gctSize);
            }

            result.width = width;
            result.height = height;

            boolean done = false;
            while (!done && dataPos < rawData.length) {
                int code = readByte();
                switch (code) {
                    case 0x2C: // Image descriptor
                        readImageDescriptor();
                        decodeImageData();
                        Bitmap frame = createFrame();
                        if (frame != null) {
                            result.frames.add(frame);
                            result.delays.add(Math.max(delay, 20)); // min 20ms
                        }
                        break;
                    case 0x21: // Extension
                        readExtension();
                        break;
                    case 0x3B: // Terminator
                        done = true;
                        break;
                    default:
                        done = true;
                        break;
                }
            }
        } catch (Exception e) {
            // Return whatever frames we got
        }
        return result;
    }

    private int readByte() {
        if (dataPos >= rawData.length) return 0;
        return rawData[dataPos++] & 0xFF;
    }

    private int readShort() {
        return readByte() | (readByte() << 8);
    }

    private void readLogicalScreenDescriptor() {
        width = readShort();
        height = readShort();
        int packed = readByte();
        gctSize = 2 << (packed & 7);
        bgIndex = readByte();
        pixelAspect = readByte();
        if ((packed & 0x80) != 0) {
            // GCT flag set
        } else {
            gctSize = 0;
        }
    }

    private int[] readColorTable(int ncolors) {
        int[] tab = new int[ncolors];
        for (int i = 0; i < ncolors; i++) {
            int r = readByte();
            int g = readByte();
            int b = readByte();
            tab[i] = Color.argb(255, r, g, b);
        }
        return tab;
    }

    private void readExtension() {
        int label = readByte();
        switch (label) {
            case 0xF9: // Graphic Control Extension
                readByte(); // block size (always 4)
                int packed = readByte();
                dispose = (packed & 0x1C) >> 2;
                transparency = (packed & 1) != 0;
                delay = readShort() * 10; // convert centiseconds to ms
                transIndex = readByte();
                readByte(); // terminator
                break;
            default:
                skipBlocks();
                break;
        }
    }

    private void skipBlocks() {
        int size;
        do {
            size = readByte();
            for (int i = 0; i < size; i++) readByte();
        } while (size > 0);
    }

    private void readImageDescriptor() {
        ix = readShort();
        iy = readShort();
        iw = readShort();
        ih = readShort();
        int packed = readByte();
        lctFlag = (packed & 0x80) != 0;
        interlace = (packed & 0x40) != 0;
        lctSize = 2 << (packed & 7);
        if (lctFlag) {
            lct = readColorTable(lctSize);
        }
    }

    private void decodeImageData() {
        int minCodeSize = readByte();
        int clearCode = 1 << minCodeSize;
        int eoiCode = clearCode + 1;
        int available = clearCode + 2;
        int codeSize = minCodeSize + 1;
        int codeMask = (1 << codeSize) - 1;
        int oldCode = -1;

        short[] prefix = new short[MAX_STACK_SIZE];
        byte[] suffix = new byte[MAX_STACK_SIZE];
        byte[] pixelStack = new byte[MAX_STACK_SIZE + 1];

        // Initialize string table
        for (int code = 0; code < clearCode; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }

        pixels = new byte[iw * ih];
        int pi = 0; // pixel index
        int top = 0; // stack top
        int bi = 0; // buffer index
        int datum = 0;
        int bits = 0;
        int first = 0;
        int count = 0;

        // Read data blocks
        ByteArrayOutputStream dataBlocks = new ByteArrayOutputStream();
        int blockSz;
        while ((blockSz = readByte()) > 0) {
            for (int i = 0; i < blockSz; i++) {
                dataBlocks.write(readByte());
            }
        }
        byte[] data = dataBlocks.toByteArray();
        int dataIdx = 0;

        while (pi < pixels.length && dataIdx < data.length) {
            // Need more bits
            while (bits < codeSize && dataIdx < data.length) {
                datum |= (data[dataIdx++] & 0xFF) << bits;
                bits += 8;
            }

            int code = datum & codeMask;
            datum >>= codeSize;
            bits -= codeSize;

            if (code == clearCode) {
                codeSize = minCodeSize + 1;
                codeMask = (1 << codeSize) - 1;
                available = clearCode + 2;
                oldCode = -1;
                continue;
            }

            if (code == eoiCode) break;

            if (oldCode == -1) {
                pixels[pi++] = suffix[code];
                oldCode = code;
                first = code;
                continue;
            }

            int inCode = code;
            if (code >= available) {
                pixelStack[top++] = (byte) first;
                code = oldCode;
            }

            while (code >= clearCode) {
                pixelStack[top++] = suffix[code];
                code = prefix[code];
            }
            first = suffix[code] & 0xFF;

            if (available >= MAX_STACK_SIZE) {
                // Table full
                pixels[pi++] = (byte) first;
                continue;
            }

            pixelStack[top++] = (byte) first;
            prefix[available] = (short) oldCode;
            suffix[available] = (byte) first;
            available++;

            if ((available & codeMask) == 0 && available < MAX_STACK_SIZE) {
                codeSize++;
                codeMask = (1 << codeSize) - 1;
            }

            oldCode = inCode;

            // Pop stack to pixels
            while (top > 0 && pi < pixels.length) {
                pixels[pi++] = pixelStack[--top];
            }
        }
    }

    private Bitmap createFrame() {
        if (image == null) {
            image = new int[width * height];
        }

        // Handle disposal from previous frame
        if (lastDispose == 2) {
            // Restore to background
            int c = transparency ? 0 : (gct != null ? gct[bgIndex] : 0);
            for (int i = 0; i < image.length; i++) image[i] = c;
        } else if (lastDispose == 3 && lastImage != null) {
            System.arraycopy(lastImage, 0, image, 0, image.length);
        }

        // Save current image if needed for restore
        if (dispose == 3) {
            lastImage = new int[image.length];
            System.arraycopy(image, 0, lastImage, 0, image.length);
        }

        int[] act = lctFlag ? lct : gct;
        if (act == null) return null;

        // Apply frame pixels
        int pass = 1, inc = 8, iline = 0;
        for (int i = 0; i < ih; i++) {
            int line = i;
            if (interlace) {
                if (iline >= ih) { pass++; switch(pass) { case 2: iline=4; break; case 3: iline=2; inc=4; break; case 4: iline=1; inc=2; break; } }
                line = iline;
                iline += inc;
            }
            line += iy;
            if (line < height) {
                int k = line * width;
                int dx = k + ix;
                int dlim = dx + iw;
                if (k + width < dlim) dlim = k + width;
                int sx = i * iw;
                while (dx < dlim) {
                    int index = pixels[sx++] & 0xFF;
                    if (!(transparency && index == transIndex)) {
                        if (index < act.length) {
                            image[dx] = act[index];
                        }
                    }
                    dx++;
                }
            }
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(image, 0, width, 0, 0, width, height);

        lastDispose = dispose;
        lctFlag = false;

        return bmp;
    }
}
