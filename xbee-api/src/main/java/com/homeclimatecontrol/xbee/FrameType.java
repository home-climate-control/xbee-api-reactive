package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.util.HexFormat;

/**
 * Frame types.
 *
 * See <a href="https://www.digi.com/resources/documentation/Digidocs/90002002/Default.htm#Containers/cont_frame_descriptions.htm">XBee Frame Descriptions</a>
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public enum FrameType {

    LOCAL_AT_COMMAND_REQUEST((byte) 0x08, "Local AT Command Request"),
    QUEUE_LOCAL_AT_COMMAND_REQUEST((byte) 0x09, "Queue Local AT Command Request"),
    TX_REQUEST((byte) 0x10, "Transmit Request"),
    EXPLICIT_ADDRESSING_COMMAND_REQUEST((byte) 0x10, "Explicit Addressing Command Request"),
    REMOTE_AT_COMMAND_REQUEST((byte) 0x17, "Local AT Command Request"),
    CREATE_SOURCE_ROUTE((byte) 0x21, "Create Source Route"),
    LOCAL_AT_COMMAND_RESPONSE((byte) 0x88, "Local AT Command Response"),
    MODEM_STATUS((byte) 0x8A, "Modem Status"),
    EXTENDED_TRANSMIT_STATUS((byte) 0x8B, "Extended Transmit Status"),
    RECEIVE_PACKET((byte) 0x90, "Receive Packet"),
    EXPLICIT_RECEIVE_INDICATOR((byte) 0x91, "Explicit Receive Indicator"),
    IO_SAMPLE_INDICATOR((byte) 0x92, "I/O Sample Indicator"),
    XBEE_SENSOR_READ_INDICATOR((byte) 0x94, "XBee Sensor Read Indicator"),
    NODE_IDENTIFICATION_INDICATOR((byte) 0x05, "NodeIdentificationIndicator"),
    REMOTE_AT_COMMAND_RESPONSE((byte) 0x97, "Remote AT Command Response"),
    EXTENDED_MODEM_STATUS((byte) 0x98, "Extended Modem Status"),
    OTA_UPDATE_STATUS((byte) 0xA0, "OTA Update Status"),
    ROUTE_RECORD_INDICATOR((byte) 0xA1, "Route Record Indicator"),
    MANY_TO_ONE_ROUTE_REQUEST_INDICATOR((byte) 0xA3, "Many-to-One Route Request Indicator");

    public final byte type;
    public final String description;

    FrameType(byte type, String description) {
        this.type = type;
        this.description = description;
    }

    public static FrameType getByType(byte type) {
        for (var frameType : values()) {
            if (frameType.type == type) {
                return frameType;
            }
        }

        throw new IllegalArgumentException("Unknown frame type " + HexFormat.format(type));
    }

    @Override
    public String toString() {
        return "{Frame type=" + HexFormat.format(type) + " " + description + "}";
    }
}
