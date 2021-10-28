package com.rapplogic.xbee;

public class TestPortProvider {
    private final static String XBEE_TEST_PORT ="XBEE_TEST_PORT";
    public static String getTestPort() {
        String port = System.getenv(XBEE_TEST_PORT);
        if (port == null) {
            throw new IllegalStateException("Define environment variable XBEE_TEST_PORT=<your-safe-test-port> to run this test");
        }
        return port;
    }
}
