package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class PdfUtilsTest
{
    @Test
    public void testIsBase64StringNotValidPdf() throws Exception
    {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());

        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));

        String base64encodedString = new String(encoded, StandardCharsets.US_ASCII);

        assertFalse(PdfUtils.isBase64StringPDF(base64encodedString));
    }


    @Test
    public void testIsBase64StringValidPdf() throws Exception
    {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());

        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));

        String base64encodedString = new String(encoded, StandardCharsets.US_ASCII);

        assertTrue(PdfUtils.isBase64StringPDF(base64encodedString));
    }
}