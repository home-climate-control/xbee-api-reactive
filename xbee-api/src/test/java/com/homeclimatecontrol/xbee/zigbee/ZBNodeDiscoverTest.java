package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;
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

                var ntResponse = (AtCommandResponse) xbee.send(new AtCommand(NT), null).block();
                var timeout = Duration.ofMillis(ByteUtils.convertMultiByteToInt(ntResponse.getValue()) * 100L); // NOSONAR Unlikely, this is a local command

                xbee.sendAsync(new AtCommand(ND));

                logger.info("Collecting responses for NT={}", timeout);

                var discovered = xbee
                        .receive()
                        .take(timeout)
                        .doOnNext(incoming -> logger.debug("Incoming packet: {}", incoming))
                        .filter(AtCommandResponse.class::isInstance)
                        .map(AtCommandResponse.class::cast)
                        .filter(rsp -> rsp.getCommand().equals("ND"))
                        .doOnNext(nd -> logger.info("ND response: {}", nd))
                        .collectList()
                        .block();

                logger.info("{} node{} discovered within {}{}", discovered.size(), // NOSONAR False positive for this specific case
                        discovered.size() == 1 ? "" : "s", timeout, discovered.isEmpty() ? "" : ":");

                discovered.forEach(n -> logger.info("  {}", n));

                if (discovered.isEmpty()) {
                    logger.warn("Increase NT value if not all of your nodes are discovered within current timeout ({})", timeout);
                }
            }
        }).doesNotThrowAnyException();
    }
}
