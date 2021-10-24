package com.rapplogic.xbee.api;

import java.time.Duration;

public class XBeeConfiguration {

    public static final Duration DEFAULT_SYNCHRONOUS_TIMEOUT = Duration.ofSeconds(5);

	private boolean shutdownHook = false;
	private boolean startupChecks = true;
	private int maxQueueSize = 100;
	private Duration sendSynchronousTimeout = DEFAULT_SYNCHRONOUS_TIMEOUT;
	private ResponseFilter responseQueueFilter;

    private static final ResponseFilter noRequestResponseQueueFilter = NoRequestResponse.class::isInstance;

	/**
	 * Controls is a startup check is performed when connecting to the XBee.
	 * The startup check attempts to determine the firmware type and if it is
	 * configured correctly for use with this software.  Default is true.
	 */
	public XBeeConfiguration withShutdownHook(boolean shutdownHook) {
		this.shutdownHook = shutdownHook;
		return this;
	}

	/**
	 * Controls is a startup check is performed when connecting to the XBee.
	 * The startup check attempts to determine the firmware type and if it is
	 * configured correctly for use with this software.  Default is true.
	 */
	public XBeeConfiguration withStartupChecks(boolean startupChecks) {
		this.startupChecks = startupChecks;
		return this;
	}

	/**
	 * Sets the maximum size of the internal queue that supports the getResponse(..) method.
	 * Packets are removed from the head of the queue once this limit is reached.  The default is 100
	 */
	public XBeeConfiguration withMaxQueueSize(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be > 0");
		}

		this.maxQueueSize = size;
		return this;
	}

	public XBeeConfiguration withResponseQueueFilter(ResponseFilter filter) {
		this.responseQueueFilter = filter;
		return this;
	}

	public XBeeConfiguration withSendSynchronousTimeout(Duration sendSynchronousTimeout) {
		this.sendSynchronousTimeout = sendSynchronousTimeout;
		return this;
	}

	/**
	 * Only adds responses that implement NoRequestResponse
	 */
	public XBeeConfiguration withNoRequestResponseQueueFilter() {
		this.responseQueueFilter = XBeeConfiguration.noRequestResponseQueueFilter;
		return this;
	}

	public boolean isStartupChecks() {
		return startupChecks;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public ResponseFilter getResponseQueueFilter() {
		return responseQueueFilter;
	}

	public Duration getSendSynchronousTimeout() {
		return sendSynchronousTimeout;
	}

	public boolean isShutdownHook() {
		return shutdownHook;
	}
}
