package com.homeclimatecontrol.xbee;

public class TestPortProvider {

    /**
     * Generic test port.
     */
    private final static String XBEE_TEST_PORT ="XBEE_TEST_PORT";

    /**
     * Test port for tests where the coordinator firmware is expected.
     */
    private final static String XBEE_COORDINATOR_TEST_PORT ="XBEE_COORDINATOR_TEST_PORT";

    public static String getTestPort() {
        return getTestPort(XBEE_TEST_PORT);
    }

    public static String getCoordinatorTestPort() {
        return getTestPort(XBEE_COORDINATOR_TEST_PORT);
    }

    public static String getTestPort(String portVariable) {
        String port = System.getenv(portVariable);
        if (port == null) {
            throw new IllegalStateException("Define environment variable " + portVariable + "=<your-safe-test-port> to run this test");
        }
        return port;
    }
}
