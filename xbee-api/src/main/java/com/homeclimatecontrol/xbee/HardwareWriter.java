package com.homeclimatecontrol.xbee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;

public class HardwareWriter implements AutoCloseable {

    private final Logger logger = LogManager.getLogger();
    private final OutputStream out;

    public HardwareWriter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void close() throws Exception {
        logger.warn("close(): not implemented");
    }
}
