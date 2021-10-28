package com.rapplogic.xbee;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeNotConnectedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests opening and closing connections to the radio
 *
 * @author andrew
 *
 */
class OpenCloseConnectionsTest {

	private final static Logger log = LogManager.getLogger(OpenCloseConnectionsTest.class);

	private final XBee xbee = new XBee();

	/**
	 * Test the functionality with the actual serial port.
	 *
	 * Enable this test only if you have the hardware connected.
	 */
	@Test
    @Disabled("Enable only if safe to use hardware is connected")
	void testSerial() throws XBeeException, IOException {

		// series 1 (VT: FIXME: series of what?)
		// String port = "/dev/tty.usbserial-A4004Rim";

		// series 2 (VT: FIXME: series of what?)
		// String port = "/dev/tty.usbserial-A6005v5M";

		// x86
		String port = "/dev/ttyUSB0";

		testOpenCloseConnections(port);
	}

	private void testOpenCloseConnections(String port) throws XBeeException, IOException {
		ThreadContext.push("testOpenClose(" + port + ")");

		try {

			testConnection(new XBeeSerialConnectionWrapper(port, 9600));

		} finally {
			ThreadContext.pop();
		}
	}

	private void testConnection(XBeeSerialConnectionWrapper connectionWrapper) throws XBeeException, IOException {

		log.info("opening connection");

		XBeeConnection connection = connectionWrapper.open();

		// first connect directly to end device and configure.  then comment out configureXXX methods and connect to coordinator
		xbee.initProviderConnection(connection);

        assertThat(xbee.isConnected()).isTrue();
        assertThatIllegalStateException()
                .isThrownBy(() -> {
                    // VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1
                    log.info("attempting duplicate open");
                    xbee.initProviderConnection(connection);
                }).withMessage("Cannot open new connection -- existing connection is still open.  Please close first");

		log.info("sending channel command");

		assertThat(xbee.sendSynchronous(new AtCommand("CH")).isError()).isFalse();

		log.info("closing connection");
		xbee.close();

		assertThat(xbee.isConnected()).isFalse();

        assertThatExceptionOfType(XBeeNotConnectedException.class).isThrownBy(() -> {
            // VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1
            log.info("sending at command, but we're disconnected");
            xbee.sendSynchronous(new AtCommand("CH"));
        });

		log.info("reconnecting");
		xbee.initProviderConnection(connectionWrapper.reopen());

        assertThat(xbee.sendSynchronous(new AtCommand("CH")).isError()).isFalse();

		log.info("closing conn");
		xbee.close();

        assertThatIllegalStateException()
                .isThrownBy(() -> {
                    // VT: FIXME: https://github.com/home-climate-control/xbee-api/issues/1
                    log.info("try duplicate close");
                    xbee.close();
                }).withMessage("XBee is not connected");
	}

	private static class XBeeSerialConnectionWrapper {

		public final String port;
		public final int baudRate;

		private final SerialPortConnection serial = new SerialPortConnection();

		public XBeeSerialConnectionWrapper(String port, int baudRate) {

			this.port = port;
			this.baudRate = baudRate;
		}

		XBeeConnection open() throws IOException {

			try {

				serial.openSerialPort(port, baudRate);

				return serial;

			} catch (Throwable t) {
				throw new IOException("Oops", t);
			}
		}

		XBeeConnection reopen() throws IOException {

			// VT: FIXME: Is there a way to assert that the connection is now closed?

			return open();
		}
	}
}
