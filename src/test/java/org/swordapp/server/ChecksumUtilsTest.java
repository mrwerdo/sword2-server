package org.swordapp.server;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChecksumUtilsTest {
    
    @Test
    void hashAndCopy() throws IOException {
        // given
        String subject = "Hello my name is Ana.";
        String expectedHash = "03163f0452b2b82e1af36dec2f68a1af";
        
        Path input = Files.createTempFile("test-in",null);
        Path output = Files.createTempFile("test-out",null);
        FileWriter inWriter = new FileWriter(input.toFile());
        inWriter.write(subject);
        inWriter.close();
        
        // when
        String md5 = "";
        try (
            InputStream in = new FileInputStream(input.toFile());
            OutputStream out = new FileOutputStream(output.toFile());
        ) {
           md5 = ChecksumUtils.hashAndCopy(in, out);
        }
        
        // then
        assertEquals(expectedHash, md5);
        String outContent = Files.readString(output);
        assertEquals(subject, outContent);
    }
    
    @Test
    void hash() {
        // given
        String subject = "Hello my name is Ana.";
        String expectedHash = "03163f0452b2b82e1af36dec2f68a1af";
        
        // when & then
        assertEquals(expectedHash, ChecksumUtils.hash(subject));
    }
}
