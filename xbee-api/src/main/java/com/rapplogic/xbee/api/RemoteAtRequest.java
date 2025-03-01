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

import com.homeclimatecontrol.xbee.FrameIdGenerator;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Supported by both series 1 (10C8 firmware and later) and series 2.
 * Allows AT commands to be sent to a remote radio.
 * <p/>
 * Warning: this command may not return a response if the remote radio is unreachable.
 * You will need to set your own timeout when waiting for a response from this command,
 * or you may wait forever.
 * <p/>
 * API ID: 0x17
 * <p/>
 * @author andrew
 *
 */
public class RemoteAtRequest extends AtCommand {

    private XBeeAddress64 remoteAddr64;
    private XBeeAddress16 remoteAddr16;
    private boolean applyChanges;

    /**
     * Creates a Remote AT request for setting an AT command on a remote XBee
     * <p/>
     * Note: When setting a value, you must set applyChanges for the setting to
     * take effect.  When sending several requests, you can wait until the last
     * request before setting applyChanges=true.
     *
     * @param applyChanges set to true if setting a value or issuing a command that changes the state of the radio (e.g. FR); not applicable to query requests
     * @param command two character AT command to set or query
     * @param value if null then the current setting will be queried
     */
    public RemoteAtRequest(byte frameId, XBeeAddress64 remoteAddress64, XBeeAddress16 remoteAddress16, boolean applyChanges, Command command, int[] value) {
        super(command, value, frameId);
        remoteAddr64 = remoteAddress64;
        remoteAddr16 = remoteAddress16;
        this.applyChanges = applyChanges;
    }

    public RemoteAtRequest(byte frameId, XBeeAddress64 remoteAddress64, XBeeAddress16 remoteAddress16, boolean applyChanges, Command command, int value) {
        this(frameId, remoteAddress64, remoteAddress16, applyChanges, command, new int[]{value});
    }

    /**
     * Creates a Remote AT request for querying the current value of an AT command on a remote XBee
     */
    public RemoteAtRequest(byte frameId, XBeeAddress64 remoteAddress64, XBeeAddress16 remoteAddress16, boolean applyChanges, Command command) {
        this(frameId, remoteAddress64, remoteAddress16, applyChanges, command, null);
    }

    /**
     * Abbreviated Constructor for setting an AT command on a remote XBee.
     * This defaults to {@link FrameIdGenerator#getNext()} and {@code true} for "apply changes".
     */
    public RemoteAtRequest(XBeeAddress64 dest64, Command command, int[] value) {
        // Note: the ZNET broadcast also works for series 1.  We could also use ffff but then that wouldn't work for series 2
        this(FrameIdGenerator.getInstance().getNext(), dest64, XBeeAddress16.ZNET_BROADCAST, true, command, value);
    }

    public RemoteAtRequest(XBeeAddress64 dest64, Command command, int value) {
        this(FrameIdGenerator.getInstance().getNext(), dest64, XBeeAddress16.ZNET_BROADCAST, true, command, new int[]{value});
    }

    /**
     * Abbreviated Constructor for querying the value of an AT command on a remote XBee.
     * This defaults to {@link FrameIdGenerator#getNext()} and {@code false} for "apply changes".
     */
    public RemoteAtRequest(XBeeAddress64 dest64, Command command) {
        this(dest64, command, null);
        // apply changes doesn't make sense for a query
        setApplyChanges(false);
    }

    /**
     * Creates a Remote AT instance for querying the value of an AT command on a remote XBee,
     * by specifying the 16-bit address.  Uses the broadcast address for 64-bit address (00 00 00 00 00 00 ff ff)
     * <p/>
     * Defaults are: frame id: 1, applyChanges: false
     */
    public RemoteAtRequest(XBeeAddress16 dest16, Command command) {
        this(dest16, command, null);
        // apply changes doesn't make sense for a query
        setApplyChanges(false);
    }

    /**
     * Creates a Remote AT instance for setting the value of an AT command on a remote XBee,
     * by specifying the 16-bit address and value.  Uses the broadcast address for 64-bit address (00 00 00 00 00 00 ff ff)
     * <p/>
     * Defaults are: {@link FrameIdGenerator#getNext()}, applyChanges: true
     */
    public RemoteAtRequest(XBeeAddress16 remoteAddress16, Command command, int[] value) {
        this(FrameIdGenerator.getInstance().getNext(), XBeeAddress64.BROADCAST, remoteAddress16, true, command, value);
    }

    public RemoteAtRequest(XBeeAddress16 remoteAddress16, Command command, int value) {
        this(FrameIdGenerator.getInstance().getNext(), XBeeAddress64.BROADCAST, remoteAddress16, true, command, new int[]{value});
    }

    @Override
    public int[] getFrameData() {
        IntArrayOutputStream out = new IntArrayOutputStream();

        // api id
        out.write(getApiId().getId());
        // frame id (arbitrary byte that will be sent back with ack)
        out.write(getFrameId());

        out.write(remoteAddr64.getAddress());

        // 16-bit address
        out.write(remoteAddr16.getAddress());

        // TODO S2B remote command options
        // TODO 0x40 is a bit field, ugh
//		0x01 - Disable retries and route repair
//		0x02 - Apply changes.
//		0x20 - Enable APS encryption (if EE=1)
//		0x40 - Use the extended transmission timeout

        if (applyChanges) {
            out.write(2);
        } else {
            // queue changes -- don't forget to send AC command
            out.write(0);
        }

        // command name ascii [1]
        out.write(getCommand().code.substring(0, 1).toCharArray()[0]);
        // command name ascii [2]
        out.write(getCommand().code.substring(1, 2).toCharArray()[0]);

        if (getValue() != null) {
            out.write(getValue());
        }

        return out.getIntArray();
    }

    @Override
    public ApiId getApiId() {
        return ApiId.REMOTE_AT_REQUEST;
    }

    public void setApplyChanges(boolean applyChanges) {
        this.applyChanges = applyChanges;
    }

    @Override
    public String toString() {
        return super.toString() +
                ",remoteAddr64=" + remoteAddr64 +
                ",remoteAddr16=" + remoteAddr16 +
                ",applyChanges=" + applyChanges;
    }
}
