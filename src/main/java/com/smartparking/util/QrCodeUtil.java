package com.smartparking.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Pure Java QR Code & UPI Payment URI utility.
 * Implements ISO/IEC 18004 QR Code Generation in pure Java with zero external dependencies.
 * Generates standards-compliant SVG QR codes and UPI payment strings.
 */
public final class QrCodeUtil {

    private QrCodeUtil() {
        // Prevent instantiation
    }

    /**
     * Builds an RFC-compliant UPI payment intent URI.
     *
     * @param vpa        Payee Virtual Payment Address (e.g. smartparking@upi)
     * @param payeeName  Payee display name (e.g. Smart Parking System)
     * @param amount     Amount payable in INR
     * @param bookingRef Booking reference identifier
     * @param note       Transaction note
     * @return UPI URI string (e.g. upi://pay?pa=...&pn=...&am=...&cu=INR&tn=...)
     */
    public static String buildUpiUri(String vpa, String payeeName, BigDecimal amount, String bookingRef, String note) {
        if (vpa == null || vpa.trim().isEmpty()) {
            vpa = "smartparking@upi";
        }
        if (payeeName == null || payeeName.trim().isEmpty()) {
            payeeName = "Smart Parking Management";
        }
        BigDecimal amt = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                ? amount.setScale(2, RoundingMode.HALF_UP)
                : new BigDecimal("1.00");

        String txNote = (note != null && !note.trim().isEmpty()) ? note : ("Parking Booking " + (bookingRef != null ? bookingRef : ""));

        try {
            return "upi://pay?pa=" + URLEncoder.encode(vpa.trim(), StandardCharsets.UTF_8.name()) +
                    "&pn=" + URLEncoder.encode(payeeName.trim(), StandardCharsets.UTF_8.name()) +
                    "&am=" + amt.toPlainString() +
                    "&cu=INR" +
                    "&tn=" + URLEncoder.encode(txNote.trim(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return "upi://pay?pa=" + vpa + "&pn=" + payeeName + "&am=" + amt.toPlainString() + "&cu=INR&tn=" + txNote;
        }
    }

    /**
     * Generates a self-contained SVG representation of a QR Code for the given content.
     *
     * @param text content to encode (e.g. UPI URI)
     * @param pixelSize SVG display width/height in pixels
     * @return SVG XML string
     */
    public static String generateQrSvg(String text, int pixelSize) {
        boolean[][] modules = encodeToMatrix(text);
        int matrixSize = modules.length;
        int quietZone = 4;
        int totalSize = matrixSize + (quietZone * 2);

        StringBuilder svg = new StringBuilder(2048);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 ")
           .append(totalSize).append(" ").append(totalSize).append("\" ")
           .append("width=\"").append(pixelSize).append("\" height=\"").append(pixelSize).append("\" ")
           .append("shape-rendering=\"crispEdges\">\n");

        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        svg.append("  <path fill=\"#111827\" d=\"");

        for (int r = 0; r < matrixSize; r++) {
            for (int c = 0; c < matrixSize; c++) {
                if (modules[r][c]) {
                    int x = c + quietZone;
                    int y = r + quietZone;
                    svg.append("M").append(x).append(",").append(y).append("h1v1h-1z ");
                }
            }
        }
        svg.append("\"/>\n</svg>");
        return svg.toString();
    }

    /**
     * Encodes arbitrary text into a 2D boolean matrix representing QR code modules.
     */
    public static boolean[][] encodeToMatrix(String text) {
        if (text == null) text = "";
        byte[] data = text.getBytes(StandardCharsets.UTF_8);

        // Find appropriate QR version (Version 1 to 10 with ECC Level M)
        int version = chooseVersion(data.length);
        int size = 17 + 4 * version;

        boolean[][] matrix = new boolean[size][size];
        boolean[][] isFunction = new boolean[size][size];

        // 1. Finder patterns
        drawFinderPattern(matrix, isFunction, 0, 0);
        drawFinderPattern(matrix, isFunction, size - 7, 0);
        drawFinderPattern(matrix, isFunction, 0, size - 7);

        // 2. Alignment patterns for Version >= 2
        int[] alignPositions = getAlignmentPatternPositions(version);
        for (int r : alignPositions) {
            for (int c : alignPositions) {
                if (!isFunction[r][c]) {
                    drawAlignmentPattern(matrix, isFunction, r - 2, c - 2);
                }
            }
        }

        // 3. Timing patterns
        for (int i = 0; i < size; i++) {
            if (!isFunction[6][i]) {
                matrix[6][i] = (i % 2 == 0);
                isFunction[6][i] = true;
            }
            if (!isFunction[i][6]) {
                matrix[i][6] = (i % 2 == 0);
                isFunction[i][6] = true;
            }
        }

        // 4. Dark module
        matrix[4 * version + 9][8] = true;
        isFunction[4 * version + 9][8] = true;

        // 5. Reserve format bits areas
        for (int i = 0; i < 9; i++) {
            isFunction[8][i] = true;
            isFunction[i][8] = true;
        }
        for (int i = 0; i < 8; i++) {
            isFunction[8][size - 1 - i] = true;
            isFunction[size - 1 - i][8] = true;
        }

        // 6. Encode data + error correction codewords
        byte[] allCodewords = encodeDataAndEcc(data, version);

        // 7. Place data bits into matrix
        int bitIndex = 0;
        int totalBits = allCodewords.length * 8;
        int right = size - 1;

        while (right > 0) {
            if (right == 6) right--; // Skip vertical timing pattern
            for (int vert = 0; vert < size; vert++) {
                for (int j = 0; j < 2; j++) {
                    int c = right - j;
                    boolean upward = ((right + 1) / 2 % 2 == 1);
                    int r = upward ? (size - 1 - vert) : vert;

                    if (!isFunction[r][c]) {
                        boolean bit = false;
                        if (bitIndex < totalBits) {
                            int byteIdx = bitIndex / 8;
                            int bitPos = 7 - (bitIndex % 8);
                            bit = ((allCodewords[byteIdx] >>> bitPos) & 1) == 1;
                            bitIndex++;
                        }
                        // Apply Mask Pattern 0: (row + col) % 2 == 0
                        boolean mask = ((r + c) % 2 == 0);
                        matrix[r][c] = (bit ^ mask);
                    }
                }
            }
            right -= 2;
        }

        // 8. Write format bits (ECC Level M = 00, Mask 0 = 000 -> 00000 -> BCH format bits 0x5412)
        int formatBits = 0x5412; // Standard format bits for ECC Level M, Mask 0
        writeFormatBits(matrix, size, formatBits);

        return matrix;
    }

    private static void writeFormatBits(boolean[][] matrix, int size, int formatBits) {
        for (int i = 0; i < 6; i++) {
            matrix[8][i] = ((formatBits >>> i) & 1) == 1;
        }
        matrix[8][7] = ((formatBits >>> 6) & 1) == 1;
        matrix[8][8] = ((formatBits >>> 7) & 1) == 1;
        matrix[7][8] = ((formatBits >>> 8) & 1) == 1;
        for (int i = 9; i < 15; i++) {
            matrix[14 - i][8] = ((formatBits >>> i) & 1) == 1;
        }

        for (int i = 0; i < 8; i++) {
            matrix[size - 1 - i][8] = ((formatBits >>> i) & 1) == 1;
        }
        for (int i = 8; i < 15; i++) {
            matrix[8][size - 15 + i] = ((formatBits >>> i) & 1) == 1;
        }
    }

    private static void drawFinderPattern(boolean[][] matrix, boolean[][] isFunc, int x, int y) {
        for (int r = -1; r <= 7; r++) {
            for (int c = -1; c <= 7; c++) {
                int mr = y + r;
                int mc = x + c;
                if (mr >= 0 && mr < matrix.length && mc >= 0 && mc < matrix.length) {
                    boolean val = (r >= 0 && r <= 6 && c >= 0 && c <= 6) &&
                            (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4));
                    matrix[mr][mc] = val;
                    isFunc[mr][mc] = true;
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] matrix, boolean[][] isFunc, int x, int y) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                int mr = y + r;
                int mc = x + c;
                boolean val = (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2));
                matrix[mr][mc] = val;
                isFunc[mr][mc] = true;
            }
        }
    }

    private static int chooseVersion(int byteLen) {
        // Capacities for Byte Mode under ECC Level M (Version 1 to 10)
        int[] capacities = {0, 14, 26, 42, 62, 84, 106, 122, 152, 180, 213};
        for (int v = 1; v <= 10; v++) {
            if (byteLen <= capacities[v]) {
                return v;
            }
        }
        return 10;
    }

    private static int[] getAlignmentPatternPositions(int version) {
        switch (version) {
            case 2: return new int[]{6, 18};
            case 3: return new int[]{6, 22};
            case 4: return new int[]{6, 26};
            case 5: return new int[]{6, 30};
            case 6: return new int[]{6, 34};
            case 7: return new int[]{6, 22, 38};
            case 8: return new int[]{6, 24, 42};
            case 9: return new int[]{6, 26, 46};
            case 10: return new int[]{6, 28, 50};
            default: return new int[]{};
        }
    }

    private static byte[] encodeDataAndEcc(byte[] data, int version) {
        // Table of total data codewords and ecc codewords for Version 1..10 under ECC Level M
        int[] totalDataCodewords = {0, 16, 28, 44, 64, 86, 108, 124, 154, 182, 216};
        int[] eccPerBlock =        {0, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26};
        int[] numBlocks =          {0,  1,  1,  1,  2,  2,  4,  4,  4,  5,  5};

        int dataCap = totalDataCodewords[version];
        byte[] buffer = new byte[dataCap];
        int bitPos = 0;

        // Mode: Byte mode (0100)
        buffer[0] = 0x40;
        bitPos = 4;

        // Character count indicator (8 bits for Vers 1-9, 16 for Vers 10+)
        int countBits = (version <= 9) ? 8 : 16;
        for (int i = countBits - 1; i >= 0; i--) {
            int bit = (data.length >>> i) & 1;
            buffer[bitPos / 8] |= (bit << (7 - (bitPos % 8)));
            bitPos++;
        }

        // Data payload
        for (byte b : data) {
            for (int i = 7; i >= 0; i--) {
                int bit = (b >>> i) & 1;
                buffer[bitPos / 8] |= (bit << (7 - (bitPos % 8)));
                bitPos++;
            }
        }

        // Terminator (up to 4 zero bits)
        int term = Math.min(4, (dataCap * 8) - bitPos);
        bitPos += term;

        // Round to byte boundary
        if (bitPos % 8 != 0) {
            bitPos += (8 - (bitPos % 8));
        }

        // Pad bytes (0xEC, 0x11)
        int bytePos = bitPos / 8;
        int pad = 0;
        while (bytePos < dataCap) {
            buffer[bytePos++] = (byte) ((pad % 2 == 0) ? 0xEC : 0x11);
            pad++;
        }

        // Reed-Solomon Error Correction computation
        int blocks = numBlocks[version];
        int eccLen = eccPerBlock[version];
        int dataPerBlock = dataCap / blocks;

        byte[][] dataBlocks = new byte[blocks][dataPerBlock];
        byte[][] eccBlocks = new byte[blocks][eccLen];

        int offset = 0;
        for (int b = 0; b < blocks; b++) {
            System.arraycopy(buffer, offset, dataBlocks[b], 0, dataPerBlock);
            offset += dataPerBlock;
            eccBlocks[b] = computeReedSolomonEcc(dataBlocks[b], eccLen);
        }

        // Interleave data codewords and then ECC codewords
        byte[] result = new byte[dataCap + (blocks * eccLen)];
        int outIdx = 0;
        for (int i = 0; i < dataPerBlock; i++) {
            for (int b = 0; b < blocks; b++) {
                result[outIdx++] = dataBlocks[b][i];
            }
        }
        for (int i = 0; i < eccLen; i++) {
            for (int b = 0; b < blocks; b++) {
                result[outIdx++] = eccBlocks[b][i];
            }
        }

        return result;
    }

    private static byte[] computeReedSolomonEcc(byte[] data, int eccLen) {
        int[] generator = getRsGeneratorPoly(eccLen);
        int[] info = new int[data.length + eccLen];
        for (int i = 0; i < data.length; i++) {
            info[i] = data[i] & 0xFF;
        }

        for (int i = 0; i < data.length; i++) {
            int lead = info[i];
            if (lead != 0) {
                for (int j = 0; j < generator.length; j++) {
                    info[i + j] ^= gfMultiply(generator[j], lead);
                }
            }
        }

        byte[] ecc = new byte[eccLen];
        for (int i = 0; i < eccLen; i++) {
            ecc[i] = (byte) info[data.length + i];
        }
        return ecc;
    }

    // Galois Field GF(2^8) math with primitive poly 0x11D (285)
    private static final int[] EXP_TABLE = new int[512];
    private static final int[] LOG_TABLE = new int[256];

    static {
        int val = 1;
        for (int i = 0; i < 255; i++) {
            EXP_TABLE[i] = val;
            LOG_TABLE[val] = i;
            val <<= 1;
            if ((val & 0x100) != 0) {
                val ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            EXP_TABLE[i] = EXP_TABLE[i - 255];
        }
    }

    private static int gfMultiply(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return EXP_TABLE[LOG_TABLE[a] + LOG_TABLE[b]];
    }

    private static int[] getRsGeneratorPoly(int degree) {
        int[] poly = {1};
        for (int i = 0; i < degree; i++) {
            int factor = EXP_TABLE[i];
            int[] next = new int[poly.length + 1];
            for (int j = 0; j < poly.length; j++) {
                next[j] ^= gfMultiply(poly[j], factor);
                next[j + 1] ^= poly[j];
            }
            poly = next;
        }
        return poly;
    }
}
