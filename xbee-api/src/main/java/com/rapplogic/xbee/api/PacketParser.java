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

import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetNodeIdentificationResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IIntInputStream;
import com.rapplogic.xbee.util.InputStreamWrapper;
import com.rapplogic.xbee.util.IntArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a packet from the input stream, verifies checksum and creates an XBeeResponse object
 * <p/>
 * Notes:
 * <p/>
 * Escaped bytes increase packet length but packet stated length only indicates un-escaped bytes.
 * Stated length includes all bytes after Length bytes, not including the checksum
 * <p/>
 * @author Andrew Rapp
 *
 */
public class PacketParser implements IIntInputStream, IPacketParser {

    private static final Logger logger = LogManager.getLogger();

	private final IIntInputStream in;

	// size of packet after special bytes have been escaped
	private XBeePacketLength length;
	private final Checksum checksum = new Checksum();

	private boolean done = false;

	private int bytesRead;
	private int escapeBytes;

	private XBeeResponse response;
	private ApiId apiId;
	private int intApiId;

	private static final Map<Integer, Class<? extends XBeeResponse>> handlerMap = new HashMap<>();

	// TODO reuse this object for all packets

	// experiment to preserve original byte array for transfer over network (Starts with length)
	private final IntArrayOutputStream rawBytes = new IntArrayOutputStream();

	static {
		// TODO put all response handlers in specific packet and load all.  implement static handlesApi method
		handlerMap.put(ApiId.AT_RESPONSE.getId(), AtCommandResponse.class);
		handlerMap.put(ApiId.MODEM_STATUS_RESPONSE.getId(), ModemStatusResponse.class);
		handlerMap.put(ApiId.REMOTE_AT_RESPONSE.getId(), RemoteAtResponse.class);
		handlerMap.put(ApiId.RX_16_IO_RESPONSE.getId(), RxResponseIoSample.class);
		handlerMap.put(ApiId.RX_64_IO_RESPONSE.getId(), RxResponseIoSample.class);
		handlerMap.put(ApiId.RX_16_RESPONSE.getId(), RxResponse16.class);
		handlerMap.put(ApiId.RX_64_RESPONSE.getId(), RxResponse64.class);
		handlerMap.put(ApiId.TX_STATUS_RESPONSE.getId(), TxStatusResponse.class);
		handlerMap.put(ApiId.ZNET_EXPLICIT_RX_RESPONSE.getId(), ZNetExplicitRxResponse.class);
		handlerMap.put(ApiId.ZNET_IO_NODE_IDENTIFIER_RESPONSE.getId(), ZNetNodeIdentificationResponse.class);
		handlerMap.put(ApiId.ZNET_IO_SAMPLE_RESPONSE.getId(), ZNetRxIoSampleResponse.class);
		handlerMap.put(ApiId.ZNET_RX_RESPONSE.getId(), ZNetRxResponse.class);
		handlerMap.put(ApiId.ZNET_TX_STATUS_RESPONSE.getId(), ZNetTxStatusResponse.class);
	}

	static void registerResponseHandler(int apiId, Class<? extends XBeeResponse> clazz) {
		if (handlerMap.get(apiId) == null) {
            logger.info("Registering response handler {} for apiId: {}", clazz.getCanonicalName(), apiId);
		} else {
            logger.warn("Overriding existing implementation: {}, with {} for apiId: {}", handlerMap.get(apiId).getCanonicalName(), clazz.getCanonicalName(), apiId);
		}

		handlerMap.put(apiId, clazz);
	}

	static void unRegisterResponseHandler(int apiId) {
		if (handlerMap.get(apiId) != null) {
			logger.info("Unregistering response handler {} for apiId: ", handlerMap.get(apiId).getCanonicalName(), apiId);
			handlerMap.remove(apiId);
		} else {
			throw new IllegalArgumentException("No response handler for: " + apiId);
		}
	}

	public PacketParser(InputStream in) {
		this.in = new InputStreamWrapper(in);
	}

	// for parsing a packet from a byte array
	public PacketParser(IIntInputStream in) {
		this.in = in;
	}

	/**
	 * This method is guaranteed (unless I screwed up) to return an instance of XBeeResponse and should never throw an exception
	 * If an exception occurs, it will be packaged and returned as an ErrorResponse.
	 */
	public XBeeResponse parsePacket() {

		try {
			// BTW, length doesn't account for escaped bytes
            var msbLength = read("Length MSB");
            var lsbLength = read("Length LSB");

			// length of api structure, starting here (not including start byte or length bytes, or checksum)
			length = new XBeePacketLength(msbLength, lsbLength);

			logger.debug("packet length is {}", () -> String.format("[0x%03X]", length.getLength()));

			// total packet length = stated length + 1 start byte + 1 checksum byte + 2 length bytes

			intApiId = read("API ID");
			apiId = ApiId.get(intApiId);

			if (apiId == null) {
				apiId = ApiId.UNKNOWN;
			}

			logger.info("Handling ApiId: {}", apiId);

			// TODO parse I/O data page 12. 82 API Identifier Byte for 64 bit address A/D data (83 is for 16bit A/D data)

			for (Integer handlerApiId : handlerMap.keySet()) {
				if (intApiId == handlerApiId) {
					logger.debug("Found response handler for apiId={}: {}", ByteUtils.toBase16(intApiId), handlerMap.get(handlerApiId).getCanonicalName());
					response = handlerMap.get(handlerApiId).newInstance();
					response.parse(this);
					break;
				}
			}

			if (response == null) {
				logger.info("Did not find a response handler for ApiId [{}].  Returning GenericResponse", ByteUtils.toBase16(intApiId));
				response = new GenericResponse();
				response.parse(this);
			}

			response.setChecksum(read("Checksum"));

			if (!isDone()) {
				throw new XBeeParseException("There are remaining bytes according to stated packet length but we have read all the bytes we thought were required for this packet (if that makes sense)");
			}

			response.finish();
		} catch (Exception e) {

			// added bytes read for troubleshooting
			logger.error("Failed due to exception.  Returning ErrorResponse.  bytes read: {}", ByteUtils.toBase16(rawBytes.getIntArray()), e);

			response = new ErrorResponse();

			((ErrorResponse)response).setErrorMsg(e.getMessage());
			// but this isn't
			((ErrorResponse)response).setException(e);
		}

		if (response != null) {
			response.setLength(length);
			response.setApiId(apiId);
			// preserve original byte array for transfer over networks
			response.setRawPacketBytes(rawBytes.getIntArray());
		}

		return response;
	}

	/**
	 * Same as read() but logs the context of the byte being read.  useful for debugging
	 */
	@Override
    public int read(String context) throws IOException {
		int b = read();
		logger.debug("Read {} byte, val is {}", context, ByteUtils.formatByte(b));
		return b;
	}

	/**
	 * This method should only be called by read()
	 */
	private int readFromStream() throws IOException {
		int b = in.read();
		// save raw bytes to transfer via network
		rawBytes.write(b);

		return b;
	}

	/**
	 * This method reads bytes from the underlying input stream and performs the following tasks:
	 * 1. Keeps track of how many bytes we've read
	 * 2. Un-escapes bytes if necessary and verifies the checksum.
	 */
	@Override
    public int read() throws IOException {

		if (done) {
			throw new XBeeParseException("Packet has read all of its bytes");
		}

		int b = readFromStream();


		if (b == -1) {
			throw new XBeeParseException("Read -1 from input stream while reading packet!");
		}

		if (XBeePacket.isSpecialByte(b)) {
			logger.debug("Read special byte that needs to be unescaped");

			if (b == XBeePacket.SpecialByte.ESCAPE.getValue()) {
				logger.debug("found escape byte");
				// read next byte
				b = readFromStream();

				logger.debug("next byte is {}", ByteUtils.formatByte(b));
				b = 0x20 ^ b;
				logger.debug("unescaped (xor) byte is {}", ByteUtils.formatByte(b));

				escapeBytes++;
			} else {
				// TODO some responses such as AT Response for node discover do not escape the bytes?? shouldn't occur if AP mode is 2?
				// while reading remote at response Found unescaped special byte base10=19,base16=0x13,base2=00010011 at position 5
                logger.warn("Found unescaped special byte {}} at position {}", ByteUtils.formatByte(b), bytesRead);
			}
		}

		bytesRead++;

		// do this only after reading length bytes
		if (bytesRead > 2) {

			// when verifying checksum you must add the checksum that we are verifying
			// checksum should only include unescaped bytes!!!!
			// when computing checksum, do not include start byte, length, or checksum; when verifying, include checksum
			checksum.addByte(b);

            logger.debug("Read byte {} at position {}, packet length is {}, #escapeBytes is {}, remaining bytes is {}", ByteUtils.formatByte(b), bytesRead, length.get16BitValue(), escapeBytes, getRemainingBytes());

			// escape bytes are not included in the stated packet length
			if (getFrameDataBytesRead() >= (length.get16BitValue() + 1)) {
				// this is checksum and final byte of packet
				done = true;

				logger.debug("Checksum byte is {}", b);

				if (!checksum.verify()) {
					throw new XBeeParseException("Checksum is incorrect.  Expected 0xff, but got " + checksum.getChecksum());
				}
			}
		}

		return b;
	}

	/**
	 * Reads all remaining bytes except for checksum
	 */
	@Override
    public int[] readRemainingBytes() throws IOException {

		// minus one since we don't read the checksum
		int[] value = new int[getRemainingBytes() - 1];

		logger.debug("There are {} remaining bytes", value.length);

		for (int i = 0; i < value.length; i++) {
			value[i] = read("Remaining bytes " + i);
		}

		return value;
	}

	@Override
    public XBeeAddress64 parseAddress64() throws IOException {
		XBeeAddress64 addr = new XBeeAddress64();

		for (int i = 0; i < 8; i++) {
			addr.getAddress()[i] = read("64-bit Address byte " + i);
		}

		return addr;
	}

	@Override
    public XBeeAddress16 parseAddress16() throws IOException {
		XBeeAddress16 addr16 = new XBeeAddress16();

		addr16.setMsb(read("Address 16 MSB"));
		addr16.setLsb(read("Address 16 LSB"));

		return addr16;
	}

	/**
	 * Returns number of bytes remaining, relative to the stated packet length (not including checksum).
	 */
	@Override
    public int getFrameDataBytesRead() {
		// subtract out the 2 length bytes
		return getBytesRead() - 2;
	}

	/**
	 * Number of bytes remaining to be read, including the checksum
	 */
	@Override
    public int getRemainingBytes() {
		// add one for checksum byte (not included) in packet length
		return length.get16BitValue() - getFrameDataBytesRead() + 1;
	}

	// get unescaped packet length
	// get escaped packet length

	/**
	 * Does not include any escape bytes
	 */
	@Override
    public int getBytesRead() {
		return bytesRead;
	}

	public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public int getChecksum() {
		return checksum.getChecksum();
	}

	@Override
    public XBeePacketLength getLength() {
		return length;
	}

	@Override
    public ApiId getApiId() {
		return apiId;
	}

	@Override
    public int getIntApiId() {
		return intApiId;
	}
}
