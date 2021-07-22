package org.swordapp.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDocumentTest {
    
    SwordConfiguration swordConfiguration = new SwordTestConfiguration();
    
    @Test
    @DisplayName("HTTP Status code generation - default code")
    void getStatus_default() {
        // given
        int expectedStatusCode = HttpServletResponse.SC_BAD_REQUEST;
        ErrorDocument doc = new ErrorDocument(null);
        //when
        int receivedStatus = doc.getStatus();
        //then
        assertEquals(expectedStatusCode, receivedStatus);
    }
    
    @Test
    @DisplayName("HTTP Status code generation - code given")
    void getStatus_codeGiven() {
        // given
        int statusCode = HttpServletResponse.SC_PRECONDITION_FAILED;
        ErrorDocument doc = new ErrorDocument(UriRegistry.ERROR_BAD_REQUEST, statusCode);
        //when
        int receivedStatus = doc.getStatus();
        //then
        assertEquals(statusCode, receivedStatus);
    }
    
    static Stream<Arguments> codeMapProvider() {
        return ErrorDocument.ERROR_CODES.entrySet().stream()
            .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }
    @ParameterizedTest
    @MethodSource(value = "codeMapProvider")
    @DisplayName("HTTP Status code generation - code from error map")
    void getStatus_codeFromMap(String errorUri, Integer expectedCode) {
        // given
        ErrorDocument doc = new ErrorDocument(errorUri);
        //when
        int receivedStatus = doc.getStatus();
        //then
        assertEquals(expectedCode, receivedStatus);
    }
    
    @Test
    @DisplayName("XML generation and serialization")
    void writeTo() throws IOException, SwordServerException {
        // given
        ErrorDocument doc = new ErrorDocument(UriRegistry.ERROR_BAD_REQUEST, -1,
                                     "My summary", "My verbose error message");
        StringWriter writer = new StringWriter();
        String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<sword:error xmlns=\"http://www.w3.org/2005/Atom\" href=\"http://purl.org/net/sword/error/ErrorBadRequest\" xmlns:sword=\"http://purl.org/net/sword/terms/\">\n" +
            "  <title>ERROR</title>\n" +
            "  <updated>" + doc.getDateUpdated() + "</updated>\n" +
            "  <generator uri=\"http://www.swordapp.org/\" version=\"2.0\">swordtest@example.org</generator>\n" +
            "  <summary>My summary</summary>\n" +
            "  <link href=\"https://example.org\" rel=\"alternate\" type=\"text/html\"/>\n" +
            "  <sword:treatment>Processing failed</sword:treatment>\n" +
            "  <sword:verboseDescription>My verbose error message</sword:verboseDescription>\n" +
            "</sword:error>\n";
        
        // when
        doc.writeTo(writer, swordConfiguration);
        
        // then
        assertEquals(expectedOutput, writer.toString());
    }
}