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


import static com.rapplogic.xbee.api.AtCommand.Command.HV;

/**
 * Represents a XBee Address.
 * <p/>
 * @author andrew
 *
 */
public class HardwareVersion {

    public enum RadioType {
        SERIES1(0x17, "Series 1"),
        SERIES1_PRO(0x18, "Series 1 Pro"),
        SERIES2(0x19, "Series 2"),
        SERIES2_PRO(0x1a, "Series 2 Pro"),
        SERIES2B_PRO(0x1e, "Series 2B Pro");

        public final int id;
        public final String description;

        RadioType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static RadioType getById(int id) {
            for (var type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public static RadioType parse(AtCommandResponse response) throws XBeeException {

        if (!response.getCommand().equals(HV)) {
            throw new IllegalArgumentException("This is only applicable to the HV command, given: " + response);
        }

        if (!response.isOk()) {
            throw new XBeeException("Attempt to query HV parameter failed");
        }

        return RadioType.getById(response.getValue()[0]);
    }
}
