package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.HardwareVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;
import java.time.Instant;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static com.homeclimatecontrol.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class XBeeReactiveTest {

    private final Logger logger = LogManager.getLogger();

    @BeforeAll
    static void init() {
        ReactorDebugAgent.init();
    }

    @Test
    @Disabled("Fails in GitHub CI/CD - but only if this project is a submodule")
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
    void sendAsync() {
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

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void sendInvalidFrameId() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            try (var xbee = new XBeeReactive(getTestPort())) {
                xbee.send(new AtCommand(HV, new int[] {}, (byte) 0), null);
            }
        }).withMessageStartingWith("Invalid FrameID of zero for synchronous request, see https://www.digi.com/resources/documentation/Digidocs/90001942-13/reference/r_zigbee_frame_examples.htm");
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void send() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getTestPort())) {
                logger.info("Sending the command");
                var start = Instant.now();
                var hvMono = xbee.send(new AtCommand(HV), null);
                logger.info("Waiting for the response...");
                var hvResponse = hvMono.block();
                logger.info("Response received {}ms later: {}", Duration.between(start, Instant.now()).toMillis(), hvResponse);
                logger.info("Hardware: {}", HardwareVersion.parse((AtCommandResponse) hvResponse));

                var sb = new StringBuilder();

                for (var b : hvResponse.getRawPacketBytes()) {

                    if (!(sb.toString().length() == 0)) {
                        sb.append(", ");
                    }
                    sb.append(HexFormat.format((byte) b));
                }

                logger.info("raw response: {}", sb);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void sendTimeoutPass() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getTestPort())) {

                var start = Instant.now();

                // This should give enough time for the response
                var hvMono = xbee.send(new AtCommand(HV), Duration.ofMillis(200));
                var hvResponse = hvMono.block();

                logger.info("Response received {}ms later: {}", Duration.between(start, Instant.now()).toMillis(), hvResponse);
                assertThat(hvResponse).isNotNull();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void sendTimeoutFail() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getTestPort())) {

                var start = Instant.now();

                // Unlikely the XBee will be able to respond within this time
                var hvMono = xbee.send(new AtCommand(HV), Duration.ofMillis(5));
                var hvResponse = hvMono.block();

                logger.info("Response received {}ms later: {}", Duration.between(start, Instant.now()).toMillis(), hvResponse);
                assertThat(hvResponse).isNull();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void receive() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {
                xbee
                        .receive()
                        .take(1)
                        .doOnNext(p -> logger.info("received: {}", p))
                        .blockLast();
            }
        }).doesNotThrowAnyException();
    }
}
