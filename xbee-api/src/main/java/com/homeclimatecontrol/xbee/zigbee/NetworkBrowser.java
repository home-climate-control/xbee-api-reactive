package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.zigbee.ZBNodeDiscover;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;

public class NetworkBrowser {

    private final Logger logger = LogManager.getLogger();

    /**
     * Find out the timeout and browse the network.
     */
    public Result browse(XBeeReactive xbee) {

        var ntResponse = (AtCommandResponse) xbee.send(new AtCommand(NT), null).block();
        var timeout = Duration.ofMillis(ByteUtils.convertMultiByteToInt(ntResponse.getValue()) * 100L); // NOSONAR Unlikely, this is a local command

        return browse(xbee, timeout);
    }

    /**
     * Browse the network with the given timeout.
     */
    public Result browse(XBeeReactive xbee, Duration timeout) {

        xbee.sendAsync(new AtCommand(ND));

        logger.info("Collecting responses for NT={}", timeout);

        var discovered = xbee
                .receive()
                .take(timeout)
                .doOnNext(incoming -> logger.debug("Incoming packet: {}", incoming))
                .filter(AtCommandResponse.class::isInstance)
                .map(AtCommandResponse.class::cast)
                .filter(rsp -> rsp.getCommand().equals("ND"))
                .map(ZBNodeDiscover::parse)
                .doOnNext(nd -> logger.debug("ND response: {}", nd));

        return new Result(timeout, discovered);
    }

    public static class Result {

        /**
         * Timeout returned by NT command.
         */
        public final Duration timeout;

        /**
         * Flux of discovered nodes.
         */
        public final Flux<ZBNodeDiscover> discovered;

        Result(Duration timeout, Flux<ZBNodeDiscover> discovered) {
            this.timeout = timeout;
            this.discovered = discovered;
        }
    }
}
