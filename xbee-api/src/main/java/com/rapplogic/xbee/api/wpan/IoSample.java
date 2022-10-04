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

import com.rapplogic.xbee.util.ByteUtils;

import java.util.Optional;

/**
 * Series 1 XBee.  Represents an I/O Sample, sent from a remote radio.
 * Each I/O packet (RxResponseIoSample) may contain one for more IoSample instances.
 * <p/>
 * This class is accessed from the getSamples() method of RxResponseIoSample, which
 * returns an array of IoSample objects.
 * <p/>
 * Provides access to XBee's 8 Digital (0-7) and 6 Analog (0-5) IO pins
 * <p/>
 * @author andrew
 *
 */
public class IoSample {

	private final RxResponseIoSample parent;

	private Integer dioMsb;
	private Integer dioLsb;
	private final Integer[] analog = new Integer[6];

	public IoSample(RxResponseIoSample parent) {
		this.parent = parent;
	}

	public void setDioMsb(Integer dioMsb) {
		this.dioMsb = dioMsb;
	}

	public void setDioLsb(Integer dioLsb) {
		this.dioLsb = dioLsb;
	}

	public Integer getDioMsb() {
		return dioMsb;
	}

	public Integer getDioLsb() {
		return dioLsb;
	}

	/**
     * Get the pin analog value.
     *
	 * @return 10-bit analog value of the specified pin, or empty if the pin is not configured for analog input.
	 */
	public Optional<Integer> getAnalog(int pin) {

		if (!parent.isAnalogEnabled(pin)) {
            return Optional.empty();
        }

        return Optional.of(analog[pin]);
	}

	public void setAnalog0(Integer analog0) {
		analog[0] = analog0;
	}
	public void setAnalog1(Integer analog1) {
		analog[1] = analog1;
	}
	public void setAnalog2(Integer analog2) {
		analog[2] = analog2;
	}
	public void setAnalog3(Integer analog3) {
		analog[3] = analog3;
	}
	public void setAnalog4(Integer analog4) {
		analog[4] = analog4;
	}
	public void setAnalog5(Integer analog5) {
		analog[5] = analog5;
	}

	/**
     * Get te pin digital value.
     *
	 * @return Digital value of the specified pin, or empty if the pin is not configured for digital input.
	 */
	public Optional<Boolean> isDigitalOn(int pin) {

		if (!parent.isDigitalEnabled(pin)) {
			return Optional.empty();
		}

		if (pin >= 0 && pin <= 7) {
			return Optional.of(ByteUtils.getBit(dioLsb, pin + 1));
		} else {
			// pin 8
			return Optional.of(ByteUtils.getBit(dioMsb, 1));
		}
	}
    @Override
	public String toString() {
		var builder = new StringBuilder();
		// TODO only prefix with comma if not first entry written.  Use reflection

		if (parent.containsDigital()) {
			for (int i = 0; i <= 8; i++) {
                builder.append(i > 0 ? "," : "").append("digital[").append(i).append("]=").append((digital(isDigitalOn(i))));
			}
		}

		if (parent.containsAnalog()) {
			for (int i = 0; i <= 5; i++) {
                builder.append(i > 0 ? "," : "").append("analog[").append(i).append("]=").append(getAnalog(i).orElse(null));
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
