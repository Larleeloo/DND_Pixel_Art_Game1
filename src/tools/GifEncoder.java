package tools;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Simple GIF89a encoder for creating animated GIFs.
 * Supports multiple frames with configurable delay and looping.
 */
public class GifEncoder {
    private OutputStream out;
    private int width, height;
    private int delay = 100; // Delay between frames in milliseconds
    private boolean started = false;
    private int repeatCount = 0; // 0 = infinite loop

    public GifEncoder(OutputStream out) {
        this.out = out;
    }

    public void setDelay(int ms) {
        this.delay = ms;
    }

    public void setRepeat(int count) {
        this.repeatCount = count;
    }

    public void start(int width, int height) throws IOException {
        this.width = width;
        this.height = height;
        this.started = true;

        // GIF Header
        writeString("GIF89a");

        // Logical Screen Descriptor
        writeShort(width);
        writeShort(height);
        out.write(0xF7); // Global Color Table Flag, Color Resolution, Sort Flag, Size of GCT (256 colors)
        out.write(0);    // Background Color Index
        out.write(0);    // Pixel Aspect Ratio

        // Global Color Table (256 colors)
        writeGlobalColorTable();

        // Netscape extension for looping
        writeNetscapeExt();
    }

    private void writeGlobalColorTable() throws IOException {
        // Write 256-color palette (6x6x6 color cube + grayscale)
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    out.write(r * 51);
                    out.write(g * 51);
                    out.write(b * 51);
                }
            }
        }
        // Fill remaining with grayscale
        for (int i = 216; i < 256; i++) {
            int gray = (i - 216) * 6;
            out.write(gray);
            out.write(gray);
            out.write(gray);
        }
    }

    private void writeNetscapeExt() throws IOException {
        out.write(0x21); // Extension Introducer
        out.write(0xFF); // Application Extension
        out.write(11);   // Block Size
        writeString("NETSCAPE2.0");
        out.write(3);    // Sub-block Size
        out.write(1);    // Loop sub-block ID
        writeShort(repeatCount); // Loop count (0 = forever)
        out.write(0);    // Block Terminator
    }

    public void addFrame(BufferedImage image) throws IOException {
        if (!started) {
            start(image.getWidth(), image.getHeight());
        }

        // Graphic Control Extension
        out.write(0x21); // Extension Introducer
        out.write(0xF9); // Graphic Control Label
        out.write(4);    // Block Size
        out.write(0x04); // Disposal method (restore to background), no transparency
        writeShort(delay / 10); // Delay time in 1/100ths of a second
        out.write(0);    // Transparent Color Index
        out.write(0);    // Block Terminator

        // Image Descriptor
        out.write(0x2C); // Image Separator
        writeShort(0);   // Image Left Position
        writeShort(0);   // Image Top Position
        writeShort(width);
        writeShort(height);
        out.write(0);    // No local color table, not interlaced

        // Image Data
        writeImageData(image);
    }

    private void writeImageData(BufferedImage image) throws IOException {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        // Convert to indexed colors
        byte[] indexedPixels = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            indexedPixels[i] = (byte) findClosestColor(pixels[i]);
        }

        // LZW Minimum Code Size
        int lzwMinCodeSize = 8;
        out.write(lzwMinCodeSize);

        // LZW Compress
        LzwEncoder encoder = new LzwEncoder(width, height, indexedPixels, lzwMinCodeSize);
        encoder.init();
        encoder.encode(out);

        out.write(0); // Block Terminator
    }

    private int findClosestColor(int argb) {
        int a = (argb >> 24) & 0xFF;
        if (a < 128) return 0; // Transparent -> black

        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // Map to 6x6x6 color cube
        int ri = Math.min(5, r / 43);
        int gi = Math.min(5, g / 43);
        int bi = Math.min(5, b / 43);

        return ri * 36 + gi * 6 + bi;
    }

    public void finish() throws IOException {
        out.write(0x3B); // GIF Trailer
        out.flush();
    }

    private void writeString(String s) throws IOException {
        out.write(s.getBytes());
    }

    private void writeShort(int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    /**
     * Simple LZW encoder for GIF compression.
     */
    private static class LzwEncoder {
        private static final int EOF = -1;
        private int imgW, imgH;
        private byte[] pixels;
        private int initCodeSize;
        private int remaining;
        private int curPixel;

        private static final int BITS = 12;
        private static final int HSIZE = 5003;
        private int n_bits;
        private int maxbits = BITS;
        private int maxcode;
        private int maxmaxcode = 1 << BITS;
        private int[] htab = new int[HSIZE];
        private int[] codetab = new int[HSIZE];
        private int hsize = HSIZE;
        private int free_ent = 0;
        private boolean clear_flg = false;
        private int g_init_bits;
        private int ClearCode;
        private int EOFCode;

        private int cur_accum = 0;
        private int cur_bits = 0;
        private int masks[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F,
                0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

        private int a_count;
        private byte[] accum = new byte[256];

        LzwEncoder(int width, int height, byte[] pixels, int colorDepth) {
            imgW = width;
            imgH = height;
            this.pixels = pixels;
            initCodeSize = Math.max(2, colorDepth);
        }

        void encode(OutputStream os) throws IOException {
            compress(initCodeSize + 1, os);
        }

        private void compress(int init_bits, OutputStream outs) throws IOException {
            int fcode;
            int i;
            int c;
            int ent;
            int disp;
            int hsize_reg;
            int hshift;

            g_init_bits = init_bits;
            clear_flg = false;
            n_bits = g_init_bits;
            maxcode = maxCode(n_bits);

            ClearCode = 1 << (init_bits - 1);
            EOFCode = ClearCode + 1;
            free_ent = ClearCode + 2;

            a_count = 0;
            ent = nextPixel();

            hshift = 0;
            for (fcode = hsize; fcode < 65536; fcode *= 2)
                ++hshift;
            hshift = 8 - hshift;

            hsize_reg = hsize;
            clearHash(hsize_reg);

            output(ClearCode, outs);

            outer_loop: while ((c = nextPixel()) != EOF) {
                fcode = (c << maxbits) + ent;
                i = (c << hshift) ^ ent;

                if (htab[i] == fcode) {
                    ent = codetab[i];
                    continue;
                } else if (htab[i] >= 0) {
                    disp = hsize_reg - i;
                    if (i == 0)
                        disp = 1;
                    do {
                        if ((i -= disp) < 0)
                            i += hsize_reg;

                        if (htab[i] == fcode) {
                            ent = codetab[i];
                            continue outer_loop;
                        }
                    } while (htab[i] >= 0);
                }
                output(ent, outs);
                ent = c;
                if (free_ent < maxmaxcode) {
                    codetab[i] = free_ent++;
                    htab[i] = fcode;
                } else {
                    clearBlock(outs);
                }
            }
            output(ent, outs);
            output(EOFCode, outs);
        }

        private void clearBlock(OutputStream outs) throws IOException {
            clearHash(hsize);
            free_ent = ClearCode + 2;
            clear_flg = true;
            output(ClearCode, outs);
        }

        private void clearHash(int hsize) {
            for (int i = 0; i < hsize; ++i)
                htab[i] = -1;
        }

        private int maxCode(int n_bits) {
            return (1 << n_bits) - 1;
        }

        private int nextPixel() {
            if (remaining == 0)
                return EOF;
            --remaining;
            return pixels[curPixel++] & 0xff;
        }

        private void output(int code, OutputStream outs) throws IOException {
            cur_accum &= masks[cur_bits];

            if (cur_bits > 0)
                cur_accum |= (code << cur_bits);
            else
                cur_accum = code;

            cur_bits += n_bits;

            while (cur_bits >= 8) {
                charOut((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            if (free_ent > maxcode || clear_flg) {
                if (clear_flg) {
                    maxcode = maxCode(n_bits = g_init_bits);
                    clear_flg = false;
                } else {
                    ++n_bits;
                    if (n_bits == maxbits)
                        maxcode = maxmaxcode;
                    else
                        maxcode = maxCode(n_bits);
                }
            }

            if (code == EOFCode) {
                while (cur_bits > 0) {
                    charOut((byte) (cur_accum & 0xff), outs);
                    cur_accum >>= 8;
                    cur_bits -= 8;
                }
                flushChar(outs);
            }
        }

        private void charOut(byte c, OutputStream outs) throws IOException {
            accum[a_count++] = c;
            if (a_count >= 254)
                flushChar(outs);
        }

        private void flushChar(OutputStream outs) throws IOException {
            if (a_count > 0) {
                outs.write(a_count);
                outs.write(accum, 0, a_count);
                a_count = 0;
            }
        }

        void init() {
            remaining = imgW * imgH;
            curPixel = 0;
        }
    }
}
