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

package com.rapplogic.xbee.api;

import com.rapplogic.xbee.SerialPortConnection;
import com.rapplogic.xbee.XBeeConnection;
import com.rapplogic.xbee.api.HardwareVersion.RadioType;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is an API for communicating with Digi XBee 802.15.4 and ZigBee radios
 * via the serial port
 * <p/>
 * @author Andrew Rapp <andrew.rapp at gmail>
 *
 */
public class XBee implements IXBee {

    private final Logger logger = LogManager.getLogger(XBee.class);

	// object to synchronize on to protect access to sendPacket
	private final Object sendPacketBlock = new Object();
	private XBeeConnection xbeeConnection;
	private InputStreamThread parser;
	private final XBeeConfiguration conf;
	private RadioType type;

	public XBee() {
		this(new XBeeConfiguration().withMaxQueueSize(100).withStartupChecks(true));
	}

	public XBee(XBeeConfiguration conf) {
		this.conf = conf;

		if (this.conf.isShutdownHook()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (isConnected()) {
                    logger.info("ShutdownHook is closing connection");
                    close();
                }
            }));
		}
	}

	private void doStartupChecks() throws XBeeException {
		// Perform startup checks
		try {
			var ap = sendSynchronousAT(new AtCommand("AP"));

			if (!ap.isOk()) {
				throw new XBeeException("Attempt to query AP parameter failed: " + ap);
			}

			if (ap.getValue()[0] != 2) {

				logger.warn("XBee radio is in API mode without escape characters (AP=1).  The radio must be configured in API mode with escape bytes (AP=2) for use with this library.");
				logger.info("Attempting to set AP to 2");

				ap = sendSynchronousAT(new AtCommand("AP", 2));

				if (ap.isOk()) {
					logger.info("Successfully set AP mode to 2.  This setting will not persist a power cycle without the WR (write) command");
				} else {
					throw new XBeeException("Attempt to set AP=2 failed: " + ap);
				}
			} else {
				logger.info("Radio is in correct AP mode (AP=2)");
			}

			ap = sendSynchronousAT(new AtCommand("HV"));

            var radioType = HardwareVersion.parse(ap);

			logger.info("XBee radio is {}", radioType);

			if (radioType == RadioType.UNKNOWN) {
				logger.warn("Unknown radio type (HV): {}", ap.getValue()[0]);
			}

            var vr = sendSynchronousAT(new AtCommand("VR"));

			if (vr.isOk()) {
				logger.info("Firmware version is {}", ByteUtils.toBase16(vr.getValue()));
			}

			clearResponseQueue();
		} catch (XBeeTimeoutException ex) {
			throw new XBeeException("AT command timed-out while attempt to set/read in API mode.  Check that the XBee radio is in API mode (AP=2); it will not function propertly in AP=1", ex);
		}
	}

	/**
	 * If XBeeConnection.startUpChecks is set to true (default), this method will check if the AP parameter
	 * is set correctly and attempt to update if AP=1.  If AP=0 (Transparent mode), an
	 * exception will be thrown.
	 */
	@Override
    public void open(String port, int baudRate) throws XBeeException {
		try {
			if (isConnected()) {
				throw new IllegalStateException("Cannot open new connection -- existing connection is still open.  Please close first");
			}

			type = null;

			SerialPortConnection serial = new SerialPortConnection(); // NOSONAR False positive, this connection is closed in close()
			serial.openSerialPort(port, baudRate);

			initConnection(serial);

		} catch (XBeeException e) {
			throw e;
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	public static void registerResponseHandler(int apiId, Class<? extends XBeeResponse> clazz) {
		PacketParser.registerResponseHandler(apiId, clazz);
	}

	public static void unRegisterResponseHandler(int apiId) {
		PacketParser.unRegisterResponseHandler(apiId);
	}

	/**
	 * Allows a protocol specific implementation of XBeeConnection to be used instead of the default RXTX connection.
	 * The connection must already be established as the interface has no means to do so.
	 */
	public void initProviderConnection(XBeeConnection connection) throws XBeeException {
		if (isConnected()) {
			throw new IllegalStateException("Cannot open new connection -- existing connection is still open.  Please close first");
		}

		initConnection(connection);
	}

	private void initConnection(XBeeConnection conn) throws XBeeException {
		try {
			xbeeConnection = conn;

			parser = new InputStreamThread(xbeeConnection, conf);

			// startup heuristics
			if (conf.isStartupChecks()) {
				doStartupChecks();
			}
		} catch (XBeeException e) {
			throw e;
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	@Override
    public void addPacketListener(PacketListener packetListener) {
		if (parser == null) {
			throw new IllegalStateException("No connection");
		}

		synchronized (parser.getPacketListenerList()) {
			parser.getPacketListenerList().add(packetListener);
		}
	}

	@Override
    public void removePacketListener(PacketListener packetListener) {
		if (parser == null) {
			throw new IllegalStateException("No connection");
		}

		synchronized (parser.getPacketListenerList()) {
			parser.getPacketListenerList().remove(packetListener);
		}
	}

	public void sendRequest(XBeeRequest request) throws IOException {
		if (type != null) {
			// TODO use interface to mark series type
			if (type == RadioType.SERIES1 && request.getClass().getPackage().getName().indexOf("api.zigbee") > -1) {
				throw new IllegalArgumentException("You are connected to a Series 1 radio but attempting to send Series 2 requests");
			} else if (type == RadioType.SERIES2 && request.getClass().getPackage().getName().indexOf("api.wpan") > -1) {
				throw new IllegalArgumentException("You are connected to a Series 2 radio but attempting to send Series 1 requests");
			}
		}

		logger.info("Sending request to XBee: {}", request);
		sendPacket(request.getXBeePacket());
	}

	/**
	 * It's possible for packets to get interspersed if multiple threads send simultaneously.
	 * This method is not thread-safe because doing so would introduce a synchronized performance penalty
	 * for the vast majority of users that will not never need thread safety.
	 * That said, it is responsibility of the user to provide synchronization if multiple threads are sending.
	 *
	 * Not thread safe.
	 */
	@Override
    public void sendPacket(XBeePacket packet) throws IOException {
		sendPacket(packet.getByteArray());
	}

	/**
	 * This exists solely for the XMPP project.  Use sendRequest instead
	 *
	 * Not Thread Safe
     *
	 * @throws RuntimeException when serial device is disconnected
	 */
	@Override
    public void sendPacket(int[] packet)  throws IOException {
		// TODO should we synchronize on read lock so we are sending/recv. simultaneously?
		// TODO call request listener with byte array

		if (!isConnected()) {
			throw new XBeeNotConnectedException();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Sending packet to XBee {}", ByteUtils.toBase16(packet));
		}

        for (int packetByte : packet) {
        	// if connection lost
        	//Caused by: com.rapplogic.xbee.api.XBeeException
        	//Caused by: java.io.IOException: Input/output error in writeArray
        	xbeeConnection.getOutputStream().write(packetByte);
        }

        xbeeConnection.getOutputStream().flush();
	}

	/**
	 * Sends an XBeeRequest though the XBee interface in an asynchronous manner, such that
	 * it will return immediately, without waiting for a response.
	 * Refer to the getResponse method for obtaining a response
	 *
	 * Not thread safe
	 */
	@Override
    public void sendAsynchronous(XBeeRequest request) throws XBeeException {

		try {
			sendRequest(request);
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

    /**
     * Syntax sugar for {@link #sendSynchronous} returning {@link AtCommandResponse}.
     */
	public AtCommandResponse sendSynchronousAT(AtCommand command) throws XBeeException {
		return (AtCommandResponse) sendSynchronous(command);
	}

    /**
	 * Synchronous method for sending an XBeeRequest and obtaining the
	 * corresponding response (response that has same frame id).
	 * <p/>
	 * This method returns the first response object with a matching frame id, within the timeout
	 * period, so it is important to use a unique frame id (relative to previous subsequent requests).
	 * <p/>
	 * This method must only be called with requests that receive a response of
	 * type XBeeFrameIdResponse.  All other request types will timeout.
	 * <p/>
	 * Keep in mind responses received here will also be available through the getResponse method
	 * and the packet listener.  If you would prefer to not have these responses added to the response queue,
	 * you can add a ResponseQueueFilter via XBeeConfiguration to ignore packets that are sent in response to
	 * a request.  Another alternative is to call clearResponseQueue prior to calling this method.
	 * <p/>
	 * It is recommended to use a timeout of at least 5 seconds, since some responses can take a few seconds or more
	 * (e.g. if remote radio is not powered on).
	 * <p/>
	 * This method is thread-safe
	 *
	 * @throws XBeeTimeoutException thrown if no matching response is identified
	 */
	@Override
    public XBeeResponse sendSynchronous(XBeeRequest xbeeRequest, Duration timeout) throws XBeeException {
		if (xbeeRequest.getFrameId() == XBeeRequest.NO_RESPONSE_FRAME_ID) {
			throw new XBeeException("Frame Id cannot be 0 for a synchronous call -- it will always timeout as there is no response!");
		}

		PacketListener pl = null;

		try {
			var container = new ArrayList<XBeeResponse>();

			// this makes it thread safe -- prevents multiple threads from writing to output stream simultaneously
			synchronized (sendPacketBlock) {
				sendRequest(xbeeRequest);
			}

            // TODO handle error response as well
            pl = response -> {
                if (response instanceof XBeeFrameIdResponse && ((XBeeFrameIdResponse)response).getFrameId() == xbeeRequest.getFrameId()) {
                    // frame id matches -- yay we found it
                    container.add(response);

                    synchronized(container) {
                        container.notify();
                    }
                }
            };

			addPacketListener(pl);

			synchronized (container) {
				try {
					container.wait(timeout.toMillis());
				} catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.warn("Interrupted?", ex);
                }
			}

			if (container.isEmpty()) {
				// we didn't find a matching packet
				throw new XBeeTimeoutException(timeout.toString());
			}

			return container.get(0);
		} catch (IOException io) {
			throw new XBeeException(io);
		} finally {
			if (pl != null) {
				removePacketListener(pl);
			}
		}
	}

	/**
	 * Uses sendSynchronous timeout defined in XBeeConfiguration (default is 5000ms)
	 */
	public XBeeResponse sendSynchronous(XBeeRequest request) throws XBeeException {
		return sendSynchronous(request, conf.getSendSynchronousTimeout());
	}

	/**
	 * Same as getResponse(int) but does not timeout.
	 * It's highly recommend that you always use a timeout because
	 * if the serial connection dies under certain conditions, you will end up waiting forever!
	 * <p/>
	 * Consider using the PacketListener for asynchronous (non-blocking) behavior
	 */
	@Override
    public XBeeResponse getResponse() throws XBeeException {
		return getResponseTimeout(null);
	}

	/**
	 * This method returns an XBeeResponse from the queue, if available, or
	 * waits up to "timeout" milliseconds for a response.
	 * <p/>
	 * There are three possible outcomes:
	 * <p/>
	 * 1.  A packet is returned within "timeout" milliseconds <br/>
	 * 2.  An XBeeTimeoutException is thrown (i.e. queue was empty for duration of timeout) <br/>
	 * 3.  Null is returned if timeout is 0 and queue is empty. <br/>
	 * <p/>
	 * @param timeout Duration to wait for a response.  A value of zero disables the timeout
     *
	 * @throws XBeeTimeoutException if timeout occurs before a response is received
	 */
	@Override
    public XBeeResponse getResponse(Duration timeout) throws XBeeException {
		return getResponseTimeout(timeout);
	}

	private XBeeResponse getResponseTimeout(Duration timeout) throws XBeeException {

		// seeing this with xmpp
		if (!isConnected()) {
			throw new XBeeNotConnectedException();
		}

		XBeeResponse response;
		try {
			if (timeout != null && !timeout.isZero()) {
				response = parser.getResponseQueue().poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
			} else {
				response = parser.getResponseQueue().take();
			}
		} catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
			throw new XBeeException("Error while attempting to remove packet from queue", ex);
		}

		if (response == null && timeout != null && timeout.toMillis() > 0) {
			throw new XBeeTimeoutException(timeout.toString());
		}

		return response;
	}

	/**
	 * Collects responses until the timeout is reached or the CollectTerminator returns true
	 */
	@Override
    public List<? extends XBeeResponse> collectResponses(Duration wait, CollectTerminator terminator) throws XBeeException {

		// seeing this with xmpp
		if (!isConnected()) {
			throw new XBeeNotConnectedException();
		}

		var start = System.currentTimeMillis();
		var responseList = new ArrayList<XBeeResponse>();

		try {
			while (true) {
				// compute the remaining wait time
                // VT: FIXME: Write a test case, and rewrite this with Duration.between()
				var waitTime = Duration.ofMillis(wait.toMillis() - (int)(System.currentTimeMillis() - start));

				if (waitTime.isNegative()) {
					break;
				}

				logger.debug("calling getResponse with waitTime: {}", waitTime);

                var callStart = System.currentTimeMillis();
				var response = getResponse(waitTime);

                logger.debug("Got response in {}", (System.currentTimeMillis() - callStart));

				responseList.add(response);

				if (terminator != null && terminator.stop(response)) {
					logger.debug("Found terminating response.. exiting");
					break;
				}
			}
		} catch (XBeeTimeoutException ex) {
			logger.debug("Timeout reached, returning {} packets", responseList.size(), ex);
            return responseList;
		}

        // VT: FIXME: How is this different from above? Where did the XBeeTimeoutException come from? Action item: rework the workflow for early returns
		logger.debug("Time is up... returning {} packets", responseList.size());

		return responseList;
	}

	/**
	 * Collects responses for wait milliseconds and returns responses as List
	 */
	public List<? extends XBeeResponse> collectResponses(Duration wait) throws XBeeException {
		return collectResponses(wait, null);
	}

	/**
	 * Returns the number of packets available in the response queue for immediate consumption
	 */
	public int getResponseQueueSize() {
		// seeing this with xmpp
		if (!isConnected()) {
			throw new XBeeNotConnectedException();
		}

		return parser.getResponseQueue().size();
	}

	/**
	 * Shuts down RXTX and packet parser thread
	 */
	@Override
    public void close() {

		if (!isConnected()) {
			throw new IllegalStateException("XBee is not connected");
		}

		// shutdown parser thread
		if (parser != null) {
			parser.setDone(true);
			// interrupts thread, if waiting.  does not interrupt thread if blocking on read
			// serial port close will be closed prior to thread exit
			parser.interrupt();
		}

		try {
//			xbeeConnection.getOutputStream().close();
			xbeeConnection.close();
		} catch (Exception e) {
			logger.warn("Failed to close connection", e);
		}

		type = null;
		parser = null;
		xbeeConnection = null;
	}

	/**
	 * Indicates if serial port connection has been established.
	 * The open method may be called if this returns true
	 */
	@Override
    public boolean isConnected() {
		try {
			if (parser.getXBeeConnection().getInputStream() != null && parser.getXBeeConnection().getOutputStream() != null) {
				return true;
			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private byte sequentialFrameId = (byte) 0xFF;

	@Override
    public int getCurrentFrameId() {
		// TODO move to separate class (e.g. FrameIdCounter)
		return sequentialFrameId;
	}

	/**
	 * Obtain the next valid frame ID.
     *
	 * @return  Frame IDs in a sequential manner until the maximum is reached {@code 0xFF}, then
	 * it flips to {@code 1} (not {@code 0}, that is {@link XBeeRequest#NO_RESPONSE_FRAME_ID}) and starts over.
	 */
	@Override
    public synchronized byte getNextFrameId() {

		if (sequentialFrameId == (byte) 0xFF) {
			// flip
			sequentialFrameId = 0;
		}

		return ++sequentialFrameId;
	}

	/**
	 * Removes all packets off of the response queue
	 */
	@Override
    public void clearResponseQueue() {
		// seeing this with xmpp
		if (!isConnected()) {
			throw new XBeeNotConnectedException();
		}

		parser.getResponseQueue().clear();
	}
}
