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

import com.rapplogic.xbee.XBeeConnection;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reads data from the input stream and hands off to PacketParser for packet parsing.
 * Notifies XBee class when a new packet is parsed
 * <p/>
 * @author andrew
 *
 */
public class InputStreamThread implements Runnable {

    private final Logger logger = LogManager.getLogger();

	private final Thread thread;
	private final ExecutorService listenerPool;
	private volatile boolean done = false;
	private final XBeeConnection connection;
	private final XBeeConfiguration conf;

	public XBeeConnection getXBeeConnection() {
		return connection;
	}

	private final BlockingQueue<XBeeResponse> responseQueue = new LinkedBlockingQueue<>();

	// TODO use weak references
	private final List<PacketListener> packetListenerList = new ArrayList<>();

	public List<PacketListener> getPacketListenerList() {
		return packetListenerList;
	}

	public BlockingQueue<XBeeResponse> getResponseQueue() {
		return responseQueue;
	}

	public InputStreamThread(XBeeConnection connection, XBeeConfiguration conf) {
		this.connection = connection;
		this.conf = conf;

        // Create an executor to deliver incoming packets to listeners. We'll use a single
        // thread with an unbounded queue.
		listenerPool = Executors.newSingleThreadExecutor();

		thread = new Thread(this);
		thread.setName("InputStreamThread");
		thread.start();

		logger.debug("starting packet parser thread");
	}

	private void addResponse(XBeeResponse response) throws InterruptedException {

		if (conf.getResponseQueueFilter() != null) {
			if (conf.getResponseQueueFilter().accept(response)) {
				addToResponseQueue(response);
			}
		} else {
			addToResponseQueue(response);
		}

		listenerPool.submit(() -> {
            // must synchronize to avoid  java.util.ConcurrentModificationException at java.util.AbstractList$Itr.checkForComodification(Unknown Source)
            // this occurs if packet listener add/remove is called while we are iterating

            synchronized (packetListenerList) {
                for (var pl : packetListenerList) {
                    try {
                        if (pl != null) {
                            pl.processResponse(response);
                        } else {
                            logger.warn("PacketListener is null, size is {}", packetListenerList.size());
                        }
                    } catch (Throwable th) {
                        logger.warn("Exception in packet listener", th);
                    }
                }
            }
        });
	}

	private void addToResponseQueue(XBeeResponse response) throws InterruptedException{

		if (conf.getMaxQueueSize() == 0) {
			// warn
			return;
		}
		// trim the queue
		while (responseQueue.size() >= conf.getMaxQueueSize()) {
            logger.warn("Response queue has reached the maximum size of {} packets.  Trimming a packet from head of queue to make room", conf.getMaxQueueSize());
			responseQueue.poll();
		}

		responseQueue.put(response);
	}

	@Override
    public void run() {

		try {
			while (!done) {
				try {
                    if (connection.getInputStream().available() > 0) {
                        logger.debug("About to read from input stream");
                        var val = connection.getInputStream().read();
                        logger.debug("Read {} from input stream", ByteUtils.formatByte(val));

                        if (val == XBeePacket.SpecialByte.START_BYTE.getValue()) {
                            var packetStream = new PacketParser(connection.getInputStream());
                            var response = packetStream.parsePacket();

                            logger.debug("Received packet from XBee: {}", response);
//								log.debug("Received packet: int[] packet = {" + ByteUtils.toBase16(response.getRawPacketBytes(), ", ") + "};");

                            // success
                            addResponse(response);
                        } else {
                            logger.warn("expected start byte but got this " + ByteUtils.toBase16(val) + ", discarding");
                        }
                    } else {
                        logger.debug("No data available.. waiting for new data event");

                        // we will wait here for RXTX to notify us of new data
                        synchronized (connection) {
                            // There's a chance that we got notified after the first in.available check
                            if (connection.getInputStream().available() > 0) {
                                continue;
                            }

                            // wait until new data arrives
                            connection.wait();
                        }
                    }
                } catch (InterruptedException ex) {
                    // Nothing we can do with it here, just rethrow
                    throw ex;
				} catch (Exception ex) {

					logger.error("Error while parsing packet:", ex);

					if (ex instanceof IOException) {
						// this is thrown by RXTX if the serial device unplugged while we are reading data; if we are waiting then it will waiting forever
						logger.error("Serial device IOException.. exiting");
						break;
					}
				}
			}
		} catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
			// We've been told to stop -- the user called the close() method
			logger.info("Packet parser thread was interrupted.  This occurs when close() is called");
		} catch (Throwable t) {
			logger.error("Error in input stream thread.. exiting", t);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}

				if (listenerPool != null) {
					try {
						listenerPool.shutdownNow();
					} catch (Throwable t) {
						logger.warn("Failed to shutdown listner thread pool", t);
					}
				}
			} catch (Throwable t) {
				logger.error("Error in input stream thread finally", t);
			}
		}

		logger.info("InputStreamThread is exiting");
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void interrupt() {
		if (thread != null) {
			try {
				thread.interrupt();
			} catch (Exception e) {
				logger.warn("Error interrupting parser thread", e);
			}
		}
	}
}
