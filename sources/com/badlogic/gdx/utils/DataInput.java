package com.badlogic.gdx.utils;

import com.twi.game.BuildConfig;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataInput extends DataInputStream {
    private char[] chars = new char[32];

    public DataInput(InputStream in) {
        super(in);
    }

    public int readInt(boolean optimizePositive) throws IOException {
        int b = read();
        int result = b & 127;
        if ((b & 128) != 0) {
            int b2 = read();
            result |= (b2 & 127) << 7;
            if ((b2 & 128) != 0) {
                int b3 = read();
                result |= (b3 & 127) << 14;
                if ((b3 & 128) != 0) {
                    int b4 = read();
                    result |= (b4 & 127) << 21;
                    if ((b4 & 128) != 0) {
                        result |= (read() & 127) << 28;
                    }
                }
            }
        }
        return optimizePositive ? result : (result >>> 1) ^ (-(result & 1));
    }

    public String readString() throws IOException {
        int charCount = readInt(true);
        if (charCount == 0) {
            return null;
        }
        if (charCount == 1) {
            return BuildConfig.FLAVOR;
        }
        int charCount2 = charCount - 1;
        if (this.chars.length < charCount2) {
            this.chars = new char[charCount2];
        }
        char[] chars2 = this.chars;
        int charIndex = 0;
        int b = 0;
        while (charIndex < charCount2) {
            b = read();
            if (b > 127) {
                break;
            }
            chars2[charIndex] = (char) b;
            charIndex++;
        }
        if (charIndex < charCount2) {
            readUtf8_slow(charCount2, charIndex, b);
        }
        return new String(chars2, 0, charCount2);
    }

    private void readUtf8_slow(int charCount, int charIndex, int b) throws IOException {
        char[] chars2 = this.chars;
        while (true) {
            switch (b >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars2[charIndex] = (char) b;
                    break;
                case 12:
                case 13:
                    chars2[charIndex] = (char) (((b & 31) << 6) | (read() & 63));
                    break;
                case 14:
                    chars2[charIndex] = (char) (((b & 15) << 12) | ((read() & 63) << 6) | (read() & 63));
                    break;
            }
            charIndex++;
            if (charIndex < charCount) {
                b = read() & 255;
            } else {
                return;
            }
        }
    }
}
