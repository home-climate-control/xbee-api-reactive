package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.response.command.DxResponse;
import com.homeclimatecontrol.xbee.response.frame.RemoteATCommandResponse;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.RemoteAtRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static com.homeclimatecontrol.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.AP;
import static com.rapplogic.xbee.api.AtCommand.Command.D0;
import static com.rapplogic.xbee.api.AtCommand.Command.D1;
import static com.rapplogic.xbee.api.AtCommand.Command.D2;
import static com.rapplogic.xbee.api.AtCommand.Command.D3;
import static com.rapplogic.xbee.api.AtCommand.Command.D4;
import static com.rapplogic.xbee.api.AtCommand.Command.D5;
import static com.rapplogic.xbee.api.AtCommand.Command.D6;
import static com.rapplogic.xbee.api.AtCommand.Command.D7;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static com.rapplogic.xbee.api.AtCommand.Command.IS;
import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;
import static com.rapplogic.xbee.api.AtCommand.Command.P0;
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
                logger.info("Hardware: {}", hvResponse);
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
                var response = xbee
                        .receive()
                        .take(1)
                        .doOnNext(p -> logger.info("received: {}", p))
                        .blockLast();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void ap2() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var response = xbee.send(new AtCommand(AP, 2), null).block();
                logger.info("AP2 response: {}", response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void ap3() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                // Value of 3 is invalid
                var response = xbee.send(new AtCommand(AP, 3), null).block();
                logger.info("AP2 response: {}", response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void is() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var response = xbee.send(new AtCommand(IS), null).block();
                logger.info("IS response: {}", response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void nd() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                xbee.send(new AtCommand(AP, 2), null).block();
                var response = xbee.send(new AtCommand(ND), null).block();
                logger.info("ND response: {}", response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void nt() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var response = xbee.send(new AtCommand(NT), null).block();
                logger.info("NT response: {}", response);

            }
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("dXCommandProvider")
    @Disabled("Enable only if safe to use hardware is connected")
    void dX(AtCommand.Command command) {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var response = xbee.send(new AtCommand(command), null).block();
                logger.info("{}} response: {}", command, response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void remoteP0() {
        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                var command = new RemoteAtRequest(AddressParser.parse("0013A200.402D52DD"), P0);
                var response = xbee.send(command, null).block();
                logger.info("{} response: {}", P0, response);

            }
        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void remoteD3AsAdcOverride() {

        var remoteAddress = AddressParser.parse("0013A200.402D52DD");

        assertThatCode(() -> {
            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {

                // Verify the state; must be configured as ADC so that "set state" command should fail.
                // If this part fails, configure the remote accordingly.

                var d3getState1 = xbee.sendAT(new RemoteAtRequest(remoteAddress, D3), null).block();
                assertThat(d3getState1.status).isEqualTo(RemoteATCommandResponse.Status.OK); // NOSONAR This is expected

                var state1 = (DxResponse<?>) d3getState1.commandResponse;
                assertThat(((DxResponse<?>) d3getState1.commandResponse).code).isEqualTo((byte) 0x02);

                // Set the state
                var d3setState1 = xbee.sendAT(new RemoteAtRequest(remoteAddress, D3, new int[] { 5 }), null).block();

                // Crap... The commands overrides the setting.
                var d3getState2 = xbee.sendAT(new RemoteAtRequest(remoteAddress, D3), null).block();
                var state2 = (DxResponse<?>) d3getState2.commandResponse; // NOSONAR This is expected
                assertThat(((DxResponse<?>) d3getState2.commandResponse).code).isEqualTo((byte) 0x05);

                // All right... Set it back to ADC.
                xbee.sendAT(new RemoteAtRequest(remoteAddress, D3, new int[] { 2 }), null).block();

            }
        }).doesNotThrowAnyException();
    }

    private static Stream<AtCommand.Command> dXCommandProvider() {
        return Stream.of(
                D0,
                D1,
                D2,
                D3,
                D4,
                D5,
                D6,
                D7
        );
    }
}
