package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static org.assertj.core.api.Assertions.assertThatCode;

class BroadcastReceiverTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void listen() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {
                xbee
                        .receive()
                        .take(5)
                        .doOnNext(p -> logger.info("received: {}", p))
                        .blockLast();
            }
        }).doesNotThrowAnyException();
    }
}
