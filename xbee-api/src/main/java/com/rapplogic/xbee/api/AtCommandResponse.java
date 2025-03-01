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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Sent in response to an AtCommand
 * <p/>
 * API ID: 0x88
 * <p/>
 * @author andrew
 *
 */
public class AtCommandResponse extends XBeeFrameIdResponse {

    public enum Status {
//		0 = OK
//		1 = ERROR
//		2 = Invalid Command
//		3 = Invalid Parameter
//		4 = Remote Command Transmission Failed

        OK(0),
        ERROR(1),
        INVALID_COMMAND(2),
        INVALID_PARAMETER(3),
        NO_RESPONSE(4);  // series 1 remote AT only according to spec.  also series 2 in 2x64 zb pro firmware

        private static final Map<Integer, Status> lookup = new HashMap<>();

        static {
            for (Status s : EnumSet.allOf(Status.class)) {
                lookup.put(s.getValue(), s);
            }
        }

        public static Status get(int value) {
            return lookup.get(value);
        }

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int char1;
    private int char2;
    private Status status;
    // response value msb to lsb
    private int[] value;

    public int getChar1() {
        return char1;
    }


    public void setChar1(int char1) {
        this.char1 = char1;
    }


    public int getChar2() {
        return char2;
    }


    public void setChar2(int char2) {
        this.char2 = char2;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isOk() {
        return status == Status.OK;
    }

    // TODO should return null if not specified

    /**
     * Returns the command data byte array.
     * A zero length array will be returned if the command data is not specified.
     * This is the case if the at command set a value, or executed a command that does
     * not have a value (like FR)
     */
    public int[] getValue() {
        return value;
    }

    public void setValue(int[] data) {
        value = data;
    }

    public AtCommand.Command getCommand() {
        return AtCommand.Command.valueOf((char) char1 + String.valueOf((char) char2));
    }

    @Override
    public void parse(IPacketParser parser) throws IOException {
        setFrameId(parser.read("AT Response Frame Id"));
        setChar1(parser.read("AT Response Char 1"));
        setChar2(parser.read("AT Response Char 2"));
        setStatus(Status.get(parser.read("AT Response Status")));

        setValue(parser.readRemainingBytes());
    }

    @Override
    public String toString() {
        return "command=" + getCommand() +
                ",status=" + getStatus() + ",value=" +
                (value == null ? "null" : ByteUtils.toBase16(getValue())) +
                "," +
                super.toString();
    }
}
