/**
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

/**
 * AT Command Queue
 * <p/>
 * API ID: 0x9
 * <p/>
 * @author andrew
 *
 */
public class AtCommandQueue extends AtCommand {

    public AtCommandQueue(String command) {
        this(command, null, FrameIdGenerator.getInstance().getNext());
    }

    /**
     * Create an instance.
     *
     * @param frameId Must not be {@link XBeeRequest#NO_RESPONSE_FRAME_ID} for the response to be returned.
     *                Be careful with {@link XBeeRequest#DEFAULT_FRAME_ID} as well.
     */
    public AtCommandQueue(String command, int[] value, int frameId) {
        super(command, value, frameId);
    }

    @Override
    public ApiId getApiId() {
        return ApiId.AT_COMMAND_QUEUE;
    }
}
