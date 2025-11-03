package com.gymapp.app.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QRUtil {
    public static byte[] generateQrPng(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(img, "png", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }

    public static Image imageFromBytes(byte[] pngBytes) {
        return new Image(new ByteArrayInputStream(pngBytes));
    }

    public static String decodeQr(BufferedImage bufferedImage) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            return new MultiFormatReader().decode(bitmap).getText();
        } catch (Exception e) {
            return null;
        }
    }
}
