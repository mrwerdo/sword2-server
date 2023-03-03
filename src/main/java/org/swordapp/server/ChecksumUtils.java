package org.swordapp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that holds Checksum related methods.
 */
public final class ChecksumUtils {

    private static Logger log = LoggerFactory.getLogger(ChecksumUtils.class);
    private static MessageDigest md;
    private static final int LARGE_BUFFER = 1024 * 1024; // bytes = 1MB
    private static final int SMALL_BUFFER = 1024; // bytes = 1KB
    
    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("Cannot load MD5 digest from this JVM.", e);
        }
    }
    
    // Utility class - hiding default constructor.
    private ChecksumUtils() { }

    /**
     * Generate a hash for the data present in the input stream, before copying it to the output stream.
     * The hash is returned as a String representation. The digest algorithm is MD5, see {@link #md}.
     * 
     * @param input The InputStream to checksum.
     * @param output The OutputStream to copy the input to.
     * @return A string hash of the input stream data.
     * @throws IOException
     *         If there is an error accessing the streams.
     */
    public static String hashAndCopy(final InputStream input, final OutputStream output) throws IOException {
        if (md == null) {
            throw new IllegalStateException("Cannot generate hash value as the digest is not available.");
        }
        // make sure we have a nice and fresh digester
        md.reset();
        
        try (
            DigestOutputStream digestStream = new DigestOutputStream(output, md);
            ReadableByteChannel in = Channels.newChannel(input);
            WritableByteChannel out = Channels.newChannel(digestStream);
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(LARGE_BUFFER);
        
            while (in.read(buffer) != -1) {
                // The read() call leaves the buffer in "fill mode". To prepare
                // to write bytes from the bufferwe have to put it in "drain mode"
                // by flipping it: setting limit to position and position to zero
                buffer.flip();
                // Push the data to the MD5 digester & towards file stream
                out.write(buffer);
                // Compact the buffer by discarding bytes that were written,
                // and shifting any remaining bytes. This method also
                // prepares the buffer for the next call to read() by setting the
                // position to the limit and the limit to the buffer capacity.
                buffer.compact();
            }
        }
    
        // return the digest as a String representation
        return formatAsHex(md.digest());
    }
    
    /**
     * Generate a hash for a String
     * The hash is returned as a String representation. The digest algorithm is MD5, see {@link #md}.
     */
    public static String hash(final String s) {
        if (md == null) {
            throw new IllegalStateException("Cannot generate hash value as the digest is not available.");
        }
        // make sure we have a nice and fresh digester
        md.reset();
        return formatAsHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    }
    
    private static String formatAsHex(final byte[] digest) {
        // MD5 = 32 chars. SHA-1 = 40 chars.
        return String.format("%032x", new BigInteger(1, digest));
    }
}
