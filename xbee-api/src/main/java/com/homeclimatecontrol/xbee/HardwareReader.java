package com.homeclimatecontrol.xbee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

public class HardwareReader {

    private final Logger logger = LogManager.getLogger();
    private final InputStream in;

    public HardwareReader(InputStream in) {
        this.in = in;
    }
}
