package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common;

import java.io.UnsupportedEncodingException;

public final class ZMQUtil {
    private static final String LOSSLESS_CHARSET = "ISO-8859-1";

    private ZMQUtil() {
        throw new UnsupportedOperationException("No instance of this class is allowed.");
    }

    public static String encode(byte[] b) {
        return encode(b, LOSSLESS_CHARSET);
    }

    private static String encode(byte[] b, String charset) {
        try {
            return new String(b, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decode(String s) {
        return decode(s, LOSSLESS_CHARSET);
    }

    private static byte[] decode(String s, String charset) {
        try {
            return s.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
