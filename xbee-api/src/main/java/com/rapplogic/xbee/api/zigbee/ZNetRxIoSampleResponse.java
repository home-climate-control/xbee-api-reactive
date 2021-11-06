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

package com.rapplogic.xbee.api.zigbee;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.api.NoRequestResponse;
import com.rapplogic.xbee.api.XBeeParseException;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IIntInputStream;
import com.rapplogic.xbee.util.IntArrayInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

import static com.rapplogic.xbee.api.AtCommand.Command.IS;

/**
 * Series 2 XBee.  Represents an I/O Sample response sent from a remote radio.
 * Provides access to the XBee's 4 Analog (0-4), 11 Digital (0-7,10-12), and 1 Supply Voltage pins
 * <p/>
 * Note: Series 2 XBee does not support multiple samples (IT) per packet
 * <p/>
 * @author andrew
 *
 */
public class ZNetRxIoSampleResponse extends ZNetRxBaseResponse implements NoRequestResponse {

    private static final Logger log = LogManager.getLogger(ZNetRxIoSampleResponse.class);

	private int digitalChannelMaskMsb;
	private int digitalChannelMaskLsb;
	private int analogChannelMask;

	// all values that may not be in the packet use Integer to distinguish between null and non-null
	private Integer dioMsb;
	private Integer dioLsb;

    private static final int SUPPLY_VOLTAGE_INDEX = 4;
	private final Integer[] analog = new Integer[5];

	public static ZNetRxIoSampleResponse parseIsSample(AtCommandResponse response) throws IOException {

		if (!response.getCommand().equals(IS)) {
			throw new IllegalStateException("This is only applicable to the \"IS\" AT command, given: " + response);
		}

		IntArrayInputStream in = new IntArrayInputStream(response.getValue());
		ZNetRxIoSampleResponse sample = new ZNetRxIoSampleResponse();
		sample.parseIoSample(in);

		return sample;
	}

	@Override
    public void parse(IPacketParser parser) throws IOException {
		parseAddress(parser);
		parseOption(parser);
		parseIoSample((IIntInputStream)parser);
	}

	/**
	 * This method is a bit non standard since it needs to parse an IO sample
	 * from either a RX response or a Remote AT/Local AT response (IS).
	 */
	public void parseIoSample(IIntInputStream parser) throws IOException {
		// eat sample size.. always 1
		int size = parser.read("ZNet RX IO Sample Size");

		if (size != 1) {
			throw new XBeeParseException("Sample size is not supported if > 1 for ZNet I/O");
		}

		setDigitalChannelMaskMsb(parser.read("ZNet RX IO Sample Digital Mask 1"));
		setDigitalChannelMaskLsb(parser.read("ZNet RX IO Sample Digital Mask 2"));
		setAnalogChannelMask(parser.read("ZNet RX IO Sample Analog Channel Mask"));

		// zero out n/a bits
		analogChannelMask = analogChannelMask & 0x8f; //10001111
		// zero out all but bits 3-5
		// TODO apparent bug: channel mask on ZigBee Pro firmware has DIO10/P0 as enabled even though it's set to 01 (RSSI).  Digital value reports low.
		digitalChannelMaskMsb = digitalChannelMaskMsb & 0x1c; //11100

		if (containsDigital()) {
			log.info("response contains digital data");
			// next two bytes are digital
			setDioMsb(parser.read("ZNet RX IO DIO MSB"));
			setDioLsb(parser.read("ZNet RX IO DIO LSB"));
		} else {
			log.info("response does not contain digital data");
		}

		// parse 10-bit analog values

		int enabledCount = 0;

		for (int i = 0; i < 4; i++) {
			if (isAnalogEnabled(i)) {
				log.info("response contains analog[{}]", i);
				analog[i] = ByteUtils.parse10BitAnalog(parser, enabledCount);
				enabledCount++;
			}
		}

		if (isSupplyVoltageEnabled()) {
			analog[SUPPLY_VOLTAGE_INDEX] = ByteUtils.parse10BitAnalog(parser, enabledCount);
			enabledCount++;
		}

		log.debug("There are {} analog inputs in this packet", enabledCount);
	}

	public int getDigitalChannelMaskMsb() {
		return digitalChannelMaskMsb;
	}

	private void setDigitalChannelMaskMsb(int digitalChannelMask1) {
		digitalChannelMaskMsb = digitalChannelMask1;
	}

	public int getDigitalChannelMaskLsb() {
		return digitalChannelMaskLsb;
	}

	private void setDigitalChannelMaskLsb(int digitalChannelMask2) {
		digitalChannelMaskLsb = digitalChannelMask2;
	}

	public int getAnalogChannelMask() {
		return analogChannelMask;
	}

	private void setAnalogChannelMask(int analogChannelMask) {
		this.analogChannelMask = analogChannelMask;
	}

	public boolean isDigitalEnabled(int pin) {
		if (pin >=0 && pin <= 7) {
			return ByteUtils.getBit(digitalChannelMaskLsb, pin + 1);
		} else if (pin >=10 && pin <= 12) {
			return ByteUtils.getBit(digitalChannelMaskMsb, pin - 7);
		} else {
			throw new IllegalArgumentException("Unsupported pin: " + pin);
		}
	}

	public boolean isAnalogEnabled(int pin) {
		if (pin >=0 && pin <= 3) {
			return ByteUtils.getBit(analogChannelMask, pin + 1);
		} else {
			throw new IllegalArgumentException("Unsupported pin: " + pin);
		}
	}

	/**
	 * (from the spec) The voltage supply threshold is set with the V+ command.  If the measured supply voltage falls
	 * below or equal to this threshold, the supply voltage will be included in the IO sample set.  V+ is
	 * set to 0 by default (do not include the supply voltage).
	 */
	public boolean isSupplyVoltageEnabled() {
		return ByteUtils.getBit(analogChannelMask, 8);
	}

	/**
	 * If digital I/O line (DIO0) is enabled: returns true if digital 0 is HIGH (ON); false if it is LOW (OFF).
	 * If digital I/O line is not enabled this method returns null as it has no value.
	 * <p/>
	 * Important: the pin number corresponds to the logical pin (e.g. D4), not the physical pin number.
	 * <p/>
	 * Digital I/O pins seem to report high when open circuit (unconnected)
	 */
	public Optional<Boolean> isDigitalOn(int pin) {

		if (!isDigitalEnabled(pin)) {
            return Optional.empty();
        }

        if (pin >=0 && pin <= 7) {
            return Optional.of(ByteUtils.getBit(dioLsb, pin + 1));
        }

        // isDigitalEnabled() already exploded if the pin number is out of range
        return Optional.of(ByteUtils.getBit(dioMsb, pin - 7));
	}

	/**
	 * Returns true if this sample contains data from digital inputs
	 *
	 * See manual page 68 for byte bit mapping
	 */
	public boolean containsDigital() {
        return getDigitalChannelMaskMsb() > 0 || getDigitalChannelMaskLsb() > 0;
    }

	/**
	 * Returns true if this sample contains data from analog inputs or supply voltage
	 *
	 * How does supply voltage get enabled??
	 *
	 * See manual page 68 for byte bit mapping
	 */
	public boolean containsAnalog() {
        return getAnalogChannelMask() > 0;
    }

	/**
	 * Returns the DIO MSB, only if sample contains digital; null otherwise
	 */
	public Integer getDioMsb() {
		return dioMsb;
	}

	private void setDioMsb(Integer dioMsb) {
		this.dioMsb = dioMsb;
	}

	/**
	 * Returns the DIO LSB, only if sample contains digital; null otherwise
	 */
	public Integer getDioLsb() {
		return dioLsb;
	}

	private void setDioLsb(Integer dioLsb) {
		this.dioLsb = dioLsb;
	}

	/**
	 * Consider using getAnalog(pin) instead
	 */
	public Integer getAnalog0() {
		return analog[0];
	}

	public void setAnalog0(Integer analog0) {
		analog[0] = analog0;
	}

	public Integer getAnalog1() {
		return analog[1];
	}

	public void setAnalog1(Integer analog1) {
		analog[1] = analog1;
	}

	public Integer getAnalog2() {
		return analog[2];
	}

	public void setAnalog2(Integer analog2) {
		analog[2] = analog2;
	}

	public Integer getAnalog3() {
		return analog[3];
	}

	public void setAnalog3(Integer analog3) {
		analog[3] = analog3;
	}

	/**
	 * Returns a 10 bit value of ADC line 0, if enabled.
	 * Returns null if ADC line 0 is not enabled.
	 * <p/>
	 * The range of Digi XBee series 2 ADC is 0 - 1.2V and although I couldn't find this in the spec
	 * a few google searches seems to confirm.  When I connected 3.3V to just one of the ADC pins, it
	 * displayed its displeasure by reporting all ADC pins at 1023.
	 * <p/>
	 * Analog pins seem to float around 512 when open circuit
	 * <p/>
	 * The reason this returns null is to prevent bugs in the event that you thought you were reading the
	 * actual value when the pin is not enabled.
	 */
	public Integer getAnalog(int pin) {
		if (isAnalogEnabled(pin)) {
			return analog[pin];
		}

		return null;
	}

	public Integer getSupplyVoltage() {
		return analog[SUPPLY_VOLTAGE_INDEX];
	}

	public void setSupplyVoltage(Integer supplyVoltage) {
		analog[SUPPLY_VOLTAGE_INDEX] = supplyVoltage;
	}

	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(super.toString()).append(",");

		if (containsDigital()) {
			for (int i = 0; i <= 7; i++) {
                builder.append(i > 0 ? "," : "").append("digital[").append(i).append("]=").append(digital(isDigitalOn(i)));
			}

			for (int i = 10; i <= 12; i++) {
                builder.append(",digital[").append(i).append("]=").append(digital(isDigitalOn(i)));
			}
		}

		if (containsAnalog()) {
			for (int i = 0; i <= 3; i++) {
				if (isAnalogEnabled(i)) {
					builder.append(i > 0 ? "," : "").append("analog[").append(i).append("]=").append(getAnalog(i));
				}
			}

			if (isSupplyVoltageEnabled()) {
				builder.append(",supplyVoltage=").append(getSupplyVoltage());
			}
		}

		return builder.toString();
	}

    private String digital(Optional<Boolean> value) { // NOSONAR This is intended
        if (value.isEmpty()) {
            return "off";
        }
        return value.get() ? "high" : "low"; // NOSONAR This is intended
    }
}
