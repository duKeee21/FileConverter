package com.ykul.file_converter.converter.impl;

import com.ykul.file_converter.converter.Converter;
import com.ykul.file_converter.converter.FileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class ImageToPdfConverter implements Converter {

    @Override
    public boolean supports(FileType fileType) {
        return fileType.equals(FileType.JPG) || fileType.equals(FileType.PNG);
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        try (inputStream) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Не удалось прочитать изображение");
            }

            try (PDDocument document = new PDDocument()) {
                float width = image.getWidth();
                float height = image.getHeight();
                PDPage page = new PDPage(new PDRectangle(width, height));
                document.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.drawImage(pdImage, 0, 0, width, height);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                return out.toByteArray();
            }
        }
    }
}