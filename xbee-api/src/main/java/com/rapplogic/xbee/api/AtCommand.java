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
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * API technique to set/query commands
 * <p/>
 * WARNING: Any changes made will not survive a power cycle unless written to memory with WR command
 * According to the manual, the WR command can only be written so many times.. however many that is.
 * <p/>
 * API ID: 0x8
 * <p/>
 * Determining radio type with HV:<br/>
 * Byte 1, Part Number<br/>
 * x17, XB24 (series 1)<br/>
 * x18, XBP24 (series 1)<br/>
 * x19, XB24-B (series 2)<br/>
 * x1A, XBP24-B (series 2)<br/>
 * <p/>
 * XB24-ZB<br/>
 * XBP24-ZB<br/>
 * @author andrew
 */
public class AtCommand extends XBeeRequest {

    /**
     * See <a href="https://www.digi.com/resources/documentation/digidocs/pdfs/90000976.pdf">Zigbee RF Modules</a>
     * for more details.
     *
     */
    public enum Command {
        AI("AI", "Association Status"),
        AO("AO", "API Options"),
        AP("AP", "API Enable"),
        CH("CH", "Operating Channel"),
        D0("D0", "AD0/DIO0 Configuration"),
        D1("D1", "AD1/DIO1 Configuration"),
        D2("D2", "AD2/DIO2 Configuration"),
        D3("D3", "AD3/DIO3 Configuration"),
        D4("D4", "DIO4 Configuration"),
        D5("D5", "DIO5 Configuration"),
        D6("D6", "DIO6 Configuration"),
        D7("D7", "DIO7 Configuration"),
        D8("D8", "DIO8 Configuration"),
        DB("DB", "Received Signal Strength"),
        FR("FR", "Software Reset"),
        HV("HV", "Hardware Version"),
        ID("ID", "Extended PAN ID"),
        IR("IR", "IO Sample Rate"),
        IS("IS", "Force Sample"),
        MY("MY", "16-bit Network Address"),
        ND("ND", "Node Discover"),
        NI("NI", "Node Identifier"),
        NT("NT", "Node Discovery Timeout"),
        P0("P0", "PWM0 Configuration"),
        P1("P1", "DIO11 Configuration"),
        P2("P2", "DIO12 Configuration"),
        P3("P3", "DIO13 Configuration"),
        RE("RE", "Restore Defaults"),
        SH("SH", "Serial Number High"),
        SL("SL", "Serial Number Low"),
        VR("VR", "Firmware Version"),
        WR("WR", "Write");

        public final String code;
        public final String description;

        Command(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }

	public final Command command;
	private final int[] value;

//	// common i/o pin settings.  it is up to the developer to ensure the setting is applicable to the pin (e.g. not all pins support analog input)
//	public enum IoSetting {
//		DISABLED (new int[] {0x0}),
//		ANALOG_INPUT (new int[] {0x2}),
//		DIGITAL_INPUT (new int[] {0x3}),
//		DIGITAL_OUTPUT_LOW (new int[] {0x4}),
//		DIGITAL_OUTPUT_HIGH (new int[] {0x5});
//
//	    private final int[] value;
//
//	    IoSetting(int[] value) {
//	        this.value = value;
//	    }
//	}

	public AtCommand(Command command) {
		this(command, null, DEFAULT_FRAME_ID);
	}

	public AtCommand(Command command, int value) {
		this(command, new int[] {value}, DEFAULT_FRAME_ID);
	}

	public AtCommand(Command command, int[] value) {
		this(command, value, DEFAULT_FRAME_ID);
	}

	/**
	 * Warning: frameId must be > 0 for a response
	 */
	public AtCommand(Command command, int[] value, byte frameId) {
        super(frameId);
        this.command = command;
		this.value = value;
	}

	@Override
    public int[] getFrameData() {

		IntArrayOutputStream out = new IntArrayOutputStream();

		// api id
		out.write(getApiId().getId());
		// frame id
		out.write(getFrameId());
		// at command byte 1
		out.write(command.code.substring(0, 1).toCharArray()[0]);
		// at command byte 2
		out.write(command.code.substring(1, 2).toCharArray()[0]);

		// int value is up to four bytes to represent command value
		if (value != null) {
			out.write(value);
		}

		return out.getIntArray();
	}

    @Override
	public ApiId getApiId() {
		return ApiId.AT_COMMAND;
	}

	public Command getCommand() {
		return command;
	}

	public int[] getValue() {
		return value;
	}

    @Override
	public String toString() {
		return super.toString() +
			",command=" + command +
			",value=" + (value == null ? "null" : ByteUtils.toBase16(value));
	}
}
