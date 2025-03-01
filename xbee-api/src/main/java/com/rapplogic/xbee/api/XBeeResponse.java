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

import com.rapplogic.xbee.util.ByteUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * The super class of all XBee Receive packets
 *
 * @author andrew
 *
 */
public abstract class XBeeResponse {

    // TODO consider adding UUID to each response

    // the raw (escaped) bytes of this packet (minus start byte)
    // this is the most compact representation of the packet;
    // useful for sending the packet over a wire (e.g. xml),
    // for later reconstitution
    private int[] rawPacketBytes;
    private int[] processedPacketBytes;

    private ApiId apiId;
    private int checksum;

    private XBeePacketLength length;

    private boolean error = false;

    protected XBeeResponse() {

    }

    public XBeePacketLength getLength() {
        return length;
    }

    public void setLength(XBeePacketLength length) {
        this.length = length;
    }

    public ApiId getApiId() {
        return apiId;
    }

    public void setApiId(ApiId apiId) {
        this.apiId = apiId;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    /**
     * Indicates an error occurred during the parsing of the packet.
     * This may indicate a bug in this software or in the XBee firmware.
     * Absence of an error does not indicate the request was successful;
     * you will need to inspect the status byte of the response object (if available)
     * to determine success.
     */
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Returns an array all bytes (as received off radio, including escape bytes) in packet except the start byte.
     */
    public int[] getRawPacketBytes() {
        return rawPacketBytes;
    }

    /**
     * Returns an array of all bytes (after being un-escaped) in the packet except the start byte.
     */
    public int[] getProcessedPacketBytes() {
        return processedPacketBytes;
    }

    public void setRawPacketBytes(int[] packetBytes) {
        rawPacketBytes = packetBytes;
        processedPacketBytes = XBeePacket.unEscapePacket(packetBytes);
    }

    /**
     * For internal use only.  Called after successful parsing to allow subclass to do any final processing before delivery
     */
    public void finish() {

    }

    /**
     * All subclasses must implement to parse the packet from the input stream.
     * The subclass must parse all bytes in the packet starting after the API_ID, and
     * up to but not including the checksum.  Reading either more or less bytes that expected will
     * result in an error.
     */
    protected abstract void parse(IPacketParser parser) throws IOException;

    @Override
    public int hashCode() {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((apiId == null) ? 0 : apiId.hashCode());
        result = prime * result + checksum;
        result = prime * result + (error ? 1231 : 1237);
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + Arrays.hashCode(rawPacketBytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof XBeeResponse)) {
            return false;
        }

        XBeeResponse other = (XBeeResponse) obj;
        if (apiId == null) {
            if (other.apiId != null) {
                return false;
            }
        } else if (!apiId.equals(other.apiId)) {
            return false;
        }

        if (checksum != other.checksum) {
            return false;
        }

        if (error != other.error) {
            return false;
        }

        if (length == null) {
            if (other.length != null) {
                return false;
            }
        } else if (!length.equals(other.length)) {
            return false;
        }

        return Arrays.equals(rawPacketBytes, other.rawPacketBytes);
    }

    @Override
    public String toString() {
        return "apiId=" + apiId +
                ",length=" + (length == null ? "null" : length.get16BitValue()) +
                ",checksum=" + ByteUtils.toBase16(checksum) +
                ",error=" + error;
    }
}
