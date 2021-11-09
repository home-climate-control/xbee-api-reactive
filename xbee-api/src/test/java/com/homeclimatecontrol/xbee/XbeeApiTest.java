package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.RemoteAtRequest;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeePacket;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.homeclimatecontrol.xbee.TestPortProvider.getCoordinatorTestPort;
import static com.homeclimatecontrol.xbee.TestPortProvider.getTestPort;
import static com.rapplogic.xbee.api.AtCommand.Command.AI;
import static com.rapplogic.xbee.api.AtCommand.Command.AP;
import static com.rapplogic.xbee.api.AtCommand.Command.BD;
import static com.rapplogic.xbee.api.AtCommand.Command.CH;
import static com.rapplogic.xbee.api.AtCommand.Command.D0;
import static com.rapplogic.xbee.api.AtCommand.Command.DD;
import static com.rapplogic.xbee.api.AtCommand.Command.EE;
import static com.rapplogic.xbee.api.AtCommand.Command.HV;
import static com.rapplogic.xbee.api.AtCommand.Command.ID;
import static com.rapplogic.xbee.api.AtCommand.Command.MY;
import static com.rapplogic.xbee.api.AtCommand.Command.NC;
import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NI;
import static com.rapplogic.xbee.api.AtCommand.Command.NJ;
import static com.rapplogic.xbee.api.AtCommand.Command.NO;
import static com.rapplogic.xbee.api.AtCommand.Command.NP;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;
import static com.rapplogic.xbee.api.AtCommand.Command.OI;
import static com.rapplogic.xbee.api.AtCommand.Command.OP;
import static com.rapplogic.xbee.api.AtCommand.Command.P0;
import static com.rapplogic.xbee.api.AtCommand.Command.SD;
import static com.rapplogic.xbee.api.AtCommand.Command.VR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.fail;

class XbeeApiTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    void packetEscape() {

        final int[] knownGoodPacket = new int[]{
                0x7E, // Start delimiter
                0x00, // Length MSB
                0x0F, // Length LSB
                0x17, // 'Remote AT' command (frame data start)
                0x01, // Frame ID
                0x00,
                0x7D, // 0x7D33 is 0x13 escaped
                0x33,
                0xA2,
                0x00,
                0x40,
                0x62,
                0xAC,
                0x98,
                0xFF,
                0xFE,
                0x02, // 0x02 means 'apply changes'
                0x44,
                0x30,
                0xD9  // Checksum
        };

        ThreadContext.push("testPacketEscape");

        try {

            XBeeAddress64 xbeeAddress = AddressParser.parse("0013A200.4062AC98");
            RemoteAtRequest request = new RemoteAtRequest(xbeeAddress, D0);

            request.setApplyChanges(true);

            XBeePacket packet = request.getXBeePacket();
            int[] byteBuffer = packet.getByteArray();

            logger.info("Source: " + ByteUtils.toBase16(knownGoodPacket));
            logger.info("Packet: " + ByteUtils.toBase16(byteBuffer));

            assertThat(byteBuffer).hasSameSizeAs(knownGoodPacket);

            for (int offset = 0; offset < knownGoodPacket.length; offset++) {
                assertThat(byteBuffer[offset]).as("Packet content mismatch @" + offset).isEqualTo(knownGoodPacket[offset]);
            }

        } catch (Throwable t) {

            logger.error("Oops", t);
            fail(t.getMessage());

        } finally {

            ThreadContext.pop();
        }
    }

    @Test
    @Disabled("Enable this if you have the actual hardware (you will need to adjust the addresses, too")
    void lookingAround() {

        ThreadContext.push("lookingAround");

        assertThatCode(() -> {

            try (var xbee = new XBeeReactive(getTestPort())) {

                Flux.just(
                                MY,
                                NC,
                                NI,
                                NP,
                                DD,
                                CH,
                                ID,
                                OP,
                                OI,
                                NT,
                                NO,
                                SD,
                                NJ,
                                EE,
                                AP,
                                BD,
                                P0,
                                VR,
                                HV,
                                AI,
                                ND
                        )
                        .map(command -> xbee.sendAT(new AtCommand(command), Duration.ofSeconds(5)))
                        .map(Mono::block) // NOSONAR Acceptable in this context
                        .doOnNext(response -> logger.info("AT response: {}", response))
                        .blockLast();

            } finally {
                ThreadContext.pop();
            }

        }).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Enable this if you have the actual hardware (you will need to adjust the addresses, too")
    void dxCommandSetGet() {

        ThreadContext.push("dxCommandSetGet");

        assertThatCode(() -> {

            try (var xbee = new XBeeReactive(getCoordinatorTestPort())) {


                for (int offset = 0; offset < 4; offset++) {

                    var target = "D" + offset;
                    var addr64 = AddressParser.parse("0013A200.402D52DD");

                    ThreadContext.push(addr64 + ":D" + offset);

                    try {

                        Flux.just(
                                        new AtCommand(AP, new int[] { 2}),
                                        new AtCommand(AP),
                                        new RemoteAtRequest(addr64, AtCommand.Command.valueOf(target), new int[] {5}),
                                        new RemoteAtRequest(addr64, AtCommand.Command.valueOf(target)),
                                        new RemoteAtRequest(addr64, AtCommand.Command.valueOf(target), new int[] {4}),
                                        new RemoteAtRequest(addr64, AtCommand.Command.valueOf(target))
                                )
                                .map(command -> xbee.sendAT(command, Duration.ofSeconds(5)))
                                .map(Mono::block) // NOSONAR Acceptable in this context
                                .doOnNext(response -> logger.info("{} response: {}", target, response))
                                .blockLast();

                    } finally {
                        ThreadContext.pop();
                    }
                }

            } finally {
                ThreadContext.pop();
            }

        }).doesNotThrowAnyException();
    }
}
