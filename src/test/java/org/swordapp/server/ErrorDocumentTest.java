package org.swordapp.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDocumentTest {
    
    SwordConfiguration swordConfiguration = new SwordConfigurationDefault();
    
    @Test
    void getStatus() {
    }
    
    @Test
    void writeTo() throws IOException, SwordServerException {
        // given
        ErrorDocument doc = new ErrorDocument(UriRegistry.ERROR_BAD_REQUEST, -1,
                                     "My summary", "My verbose error message");
        StringWriter writer = new StringWriter();
        String expectedOutput = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n" +
            "<sword:error href=\"http://purl.org/net/sword/error/ErrorBadRequest\" xmlns:sword=\"http://purl.org/net/sword/terms/\">" +
            "<atom:title xmlns:atom=\"http://www.w3.org/2005/Atom\">ERROR</atom:title>" +
            "<atom:updated xmlns:atom=\"http://www.w3.org/2005/Atom\">" + DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)) + "</atom:updated>" +
            "<atom:generator uri=\"http://www.swordapp.org/\" version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\"/>" +
            "<sword:treatment>Processing failed</sword:treatment>" +
            "<atom:summary xmlns:atom=\"http://www.w3.org/2005/Atom\">My summary</atom:summary>" +
            "<sword:verboseDescription>My verbose error message</sword:verboseDescription></sword:error>\r\n";
        
        // when
        doc.writeTo(writer, swordConfiguration);
        
        // then
        assertEquals(expectedOutput, writer.toString());
    }
}