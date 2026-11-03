package com.ykul.file_converter.converter;

import com.ykul.file_converter.converter.impl.ImageToPdfConverter;
import com.ykul.file_converter.converter.impl.TxtToPdfConverter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterTest {

    @Test
    void shouldConvertTxtToPdf() throws Exception {

        TxtToPdfConverter converter = new TxtToPdfConverter();
        String text = "Тест";
        ByteArrayInputStream input = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        byte[] pdfBytes = converter.convert(input);

        assertThat(pdfBytes).isNotEmpty();
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void shouldConvertImageToPdf() throws Exception {
        ImageToPdfConverter converter = new ImageToPdfConverter();

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        ByteArrayInputStream input = new ByteArrayInputStream(baos.toByteArray());

        byte[] pdfBytes = converter.convert(input);

        assertThat(pdfBytes).isNotEmpty();
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void shouldThrowExceptionForBrokenImage() {
        ImageToPdfConverter converter = new ImageToPdfConverter();
        ByteArrayInputStream brokenStream = new ByteArrayInputStream("Не пикча".getBytes());

        assertThatThrownBy(() -> converter.convert(brokenStream))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Не удалось прочитать изображение");
    }
}