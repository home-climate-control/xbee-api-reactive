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
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Series 1 XBee.  16-bit address Transmit Packet.  This is received on the destination XBee
 * radio as a RxResponse16 response
 * <p/>
 * API ID: 0x1
 * <p/>
 * @author andrew
 *
 */
public class TxRequest16 extends TxRequestBase {

	private final XBeeAddress16 remoteAddr16;

	/**
	 * 16 bit Tx Request with default frame id and awk option
	 *
	 * Keep in mind that if you programmed the destination address with X-CTU, the unit is
	 * hex, so if you set MY=1234, use 0x1234.
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, int[] payload) {
            this(remoteAddr16, FrameIdGenerator.getInstance().getNext(), Option.UNICAST, payload);
	}

	/**
	 * 16 bit Tx Request with frame id argument
	 *
	 *
	 * Payload size is limited to 100 bytes, according to MaxStream documentation.
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, byte frameId, int[] payload) {
		this(remoteAddr16, frameId, Option.UNICAST, payload);
	}

	/**
	 * Note: if option is DISABLE_ACK_OPTION you will not get a ack response and you must use the asynchronous send method
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, byte frameId, Option option, int[] payload) {
        super(frameId);
		this.remoteAddr16 = remoteAddr16;
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
		// destination address (broadcast is 0xFFFF)
		out.write(remoteAddr16.getAddress());
		// options byte disable ack = 1, send pan id = 4
		out.write(getOption().getValue());
		out.write(getPayload());

		return out.getIntArray();
	}

    @Override
	public ApiId getApiId() {
		return ApiId.TX_REQUEST_16;
	}

    @Override
    public String toString() {
		return super.toString() +
			",remoteAddress16=" + remoteAddr16;
	}
}
