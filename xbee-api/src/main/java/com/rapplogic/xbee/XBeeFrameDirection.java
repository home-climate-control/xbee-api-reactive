package com.rapplogic.xbee;

/**
 * XBee frame directions.
 *
 * See <a href="https://www.digi.com/resources/documentation/Digidocs/90001942-13/reference/r_supported_frames_zigbee.htm">Supported Frames</a>.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public enum XBeeFrameDirection {

    /**
     * Transmit data frames are sent through the serial input, with data to be transmitted wirelessly to remote XBees.
     */
    TRANSMIT,

    /**
     * Receive data frames are received through the serial output, with data received wirelessly from remote XBees.
     */
    RECEIVE;
}
