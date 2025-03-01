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

package com.rapplogic.xbee.api.wpan;

import com.homeclimatecontrol.xbee.FrameIdGenerator;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.util.IntArrayOutputStream;

// TODO test setting DH/DL to 0 and SH/SL

/**
 * Series 1 XBee.  64-bit address Transmit Packet.  This is received on the destination XBee
 * radio as a RxResponse64 response
 * <p/>
 * Constructs frame data portion of a 64-bit transmit request
 * <p/>
 * Note: The MY address of the receiving XBee must be set to 0xffff to receive this as a RxResponse64;
 * otherwise the packet will be transmitted but will be received as a RxResponse16
 * <p/>
 * API ID: 0x0
 * <p/>
 * @author andrew
 *
 */
public class TxRequest64 extends TxRequestBase {

	private final XBeeAddress64 remoteAddr64;

	/**
	 * 16 bit Tx Request with default frame id and awk option
	 */
	public TxRequest64(XBeeAddress64 destination, int[] payload) {
            this(destination, FrameIdGenerator.getInstance().getNext(), Option.UNICAST, payload);
	}

	/**
	 * 16 bit Tx Request.
	 *
	 * Keep in mind that if you programmed the destination address with AT commands, it is in Hex,
	 * so prepend int with 0x (e.g. 0x1234).
	 *
	 * Payload size is limited to 100 bytes, according to MaxStream documentation.
	 */
	public TxRequest64(XBeeAddress64 destination, byte frameId, int[] payload) {
		this(destination, frameId, Option.UNICAST, payload);
	}

	/**
	 * Note: if option is DISABLE_ACK_OPTION you will not get a ack response and you must use the asynchronous send method
	 */
	public TxRequest64(XBeeAddress64 remoteAddr64, byte frameId, Option option, int[] payload) {
        super(frameId);
		this.remoteAddr64 = remoteAddr64;
		setOption(option);
		setPayload(payload);
	}

	@Override
    public int[] getFrameData() {

		// 3/6/10 fixed bug -- broadcast address is used with broadcast option, not no ACK

		IntArrayOutputStream out = new IntArrayOutputStream();

		// api id
		out.write(getApiId().getId());
		// frame id (arbitrary byte that will be sent back with ack)
		out.write(getFrameId());
		// destination high (broadcast is 0xFFFF)

		// add 64-bit dest address
		out.write(remoteAddr64.getAddress());

		// options byte disable ack = 1, send pan id = 4
		out.write(getOption().getValue());
		out.write(getPayload());

		return out.getIntArray();
	}

    @Override
	public ApiId getApiId() {
		return ApiId.TX_REQUEST_64;
	}

    @Override
	public String toString() {
		return super.toString() +
			",remoteAddress64=" + remoteAddr64.toString();
	}

}
