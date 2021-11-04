package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.tools.agent.ReactorDebugAgent;

import java.util.concurrent.atomic.AtomicInteger;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static org.assertj.core.api.Assertions.assertThatCode;

class ZBNodeDiscoverTest {

    private final Logger logger = LogManager.getLogger();

    @BeforeAll
    static void init() {
        ReactorDebugAgent.init();
    }

    /**
     * Discover all the associated nodes on the network.
     *
     * In order for this test to produce meaningful results, you need to have your network already configured.
     */
    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void discover() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var result = new NetworkBrowser().browse(xbee);
                var count = new AtomicInteger();
                result.discovered
                        .doOnNext(n -> logger.info("Node discovered: {}", n))
                        .doOnNext(ignored -> count.incrementAndGet())
                        .blockLast();


                logger.info("{} node{} discovered within {}", count.get(), // NOSONAR False positive for this specific case
                        count.get() == 1 ? "" : "s", result.timeout);

                if (count.get() == 0) {
                    logger.warn("Increase NT value if not all of your nodes are discovered within current timeout ({})", result.timeout);
                }
            }
        }).doesNotThrowAnyException();
    }
}
