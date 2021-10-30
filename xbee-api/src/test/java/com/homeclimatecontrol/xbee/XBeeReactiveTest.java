package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.AtCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;
import java.time.Instant;

import static com.homeclimatecontrol.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class XBeeReactiveTest {

    private final Logger logger = LogManager.getLogger();

    @BeforeAll
    static void init() {
        ReactorDebugAgent.init();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void invalidPort() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            try (var ignored = new XBeeReactive("/this/cant/be")) {
                logger.fatal("WE WILL NEVER GET HERE");
            }
        }).withMessageStartingWith("/this/cant/be: not found, available ports are:");
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void breathe() {
        assertThatCode(() -> {
            try (var ignored = new XBeeReactive(getTestPort())) {
                logger.info("Instantiated");
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void write() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getTestPort())) {
                logger.info("Sending the command");
                var start = Instant.now();
                var hv = xbee.sendAsync(new AtCommand(HV));
                logger.info("Waiting for the command to be sent...");
                hv.block();
                logger.info("Command sent {}ms later", Duration.between(start, Instant.now()).toMillis());
            }
        }).doesNotThrowAnyException();
    }
}
