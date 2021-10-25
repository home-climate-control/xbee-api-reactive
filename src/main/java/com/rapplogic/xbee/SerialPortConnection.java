/*
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *
 * This file is part of XBee-API.
 *
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee;

import com.rapplogic.xbee.api.XBeeException;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Optional;
import java.util.TooManyListenersException;

/**
 * This class encapsulates a RXTX serial port, providing access to input/output streams,
 * and notifying the subclass of new data events via the handleSerialData method.
 *
 * @author andrew
 *
 */
public class SerialPortConnection implements XBeeConnection, SerialPortEventListener {

	private final Logger logger = LogManager.getLogger(SerialPortConnection.class);

	private InputStream inputStream;
	private OutputStream outputStream;

	private SerialPort serialPort;

	public void openSerialPort(String port, int baudRate) throws PortInUseException, UnsupportedCommOperationException, TooManyListenersException, IOException, XBeeException {
		this.openSerialPort(port, "XBee", 0, baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE);
	}

	public void openSerialPort(String port, String appName, int timeout, int baudRate) throws PortInUseException, UnsupportedCommOperationException, TooManyListenersException, IOException, XBeeException {
		this.openSerialPort(port, appName, timeout, baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE);
	}

	public void openSerialPort(String port, String appName, int timeout, int baudRate, int dataBits, int stopBits, int parity, int flowControl) throws PortInUseException, UnsupportedCommOperationException, TooManyListenersException, IOException, XBeeException {
		// Apparently you can't query for a specific port, but instead must iterate
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

		CommPortIdentifier portId = null;

        var found = false;

		while (portList.hasMoreElements()) {

			portId = portList.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

				//log.debug("Found port: " + portId.getName());

				if (portId.getName().equals(port)) {
					//log.debug("Using Port: " + portId.getName());
					found = true;
					break;
				}
			}
		}

		if (!found) {
			throw new XBeeException("Could not find port: " + port);
		}

		serialPort = (SerialPort) portId.open(appName, timeout);

		serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

		// activate the DATA_AVAILABLE notifier
		serialPort.notifyOnDataAvailable(true);

		// activate the OUTPUT_BUFFER_EMPTY notifier
		//serialPort.notifyOnOutputEmpty(true);

		serialPort.addEventListener(this);

		inputStream = serialPort.getInputStream();
		outputStream = new BufferedOutputStream(serialPort.getOutputStream());
	}

    /**
     * Shuts down RXTX
     */
    @Override
    public void close() throws IOException {

        ThreadContext.push("close");
        try {

            if (serialPort == null) {
                logger.warn("serialPort is null, bailing out");
                return;
            }

            Optional.ofNullable(serialPort.getInputStream()).ifPresent(this::close);
            Optional.ofNullable(serialPort.getOutputStream()).ifPresent(this::close);

            // this call blocks while thread is attempting to read from inputstream
            serialPort.close();

        } finally {
            ThreadContext.pop();
        }
    }

    private void close(Closeable c) {
        try {
            c.close();
        } catch (IOException ex) {
            logger.warn("Failed to close(), there's nothing we can do at this point", ex);
        }
    }


    @Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		ThreadContext.push("serialEvent");

		try {

			switch (event.getEventType()) {

			case SerialPortEvent.DATA_AVAILABLE:

				if (this.getInputStream().available() == 0) {

					logger.warn("We were notified of new data but available() is returning 0");
					return;
				}

				logger.debug("{} bytes available", serialPort.getInputStream().available());

				synchronized (this) {

					// VT: FIXME: Whom are we notifying? There's not a single wait() in this class,
					// and this is the only synchronized clause. No subclasses, either
					// (at least in this project and in examples).

					this.notify();
				}

				break;

			default:

				logger.warn("Ignoring serial port event type: {}", event.getEventType());
			}

		} catch (Throwable t) {

			// it's best not to throw the exception because the RXTX thread may not be prepared to handle
			logger.error("Unexpected RXTX error", t);

		} finally {
			ThreadContext.pop();
		}
	}
}
