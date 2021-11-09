package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import com.homeclimatecontrol.xbee.response.command.NDResponse;
import com.homeclimatecontrol.xbee.response.command.NTResponse;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponse;
import com.rapplogic.xbee.api.AtCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.rapplogic.xbee.api.AtCommand.Command.ND;
import static com.rapplogic.xbee.api.AtCommand.Command.NT;

public class NetworkBrowser {

    private final Logger logger = LogManager.getLogger();

    /**
     * Find out the timeout and browse the network.
     *
     * @param xbee Adapter to use.
     *
     * @return Mono with the composite of the timeout discovered, and the flux of discovered nodes.
     */
    public Mono<Result> browse(XBeeReactive xbee) {

        return xbee
                .sendAT(new AtCommand(NT), null)
                .doOnNext(nt -> logger.debug("NT: {}", nt))
                .map(nt -> nt.commandResponse)
                .map(NTResponse.class::cast)
                .map(nt -> {
                    var timeout = Duration.ofMillis(nt.timeout * 100L); // NOSONAR Unlikely, this is a local command
                    return new Result(timeout, browse(xbee, timeout));
                });
    }

    /**
     * Browse the network with the given timeout.
     *
     * @param xbee Adapter to use.
     * @param timeout Timeout to wait for responses for.
     *
     * @return Flux of discovered nodes (we know the timeout already, we've set it up ourselves).
     */
    public Flux<NDResponse> browse(XBeeReactive xbee, Duration timeout) {

        xbee.sendAsync(new AtCommand(ND));

        logger.debug("Collecting responses for NT={}", timeout);

        return xbee
                .receive()
                .take(timeout)
                .doOnNext(incoming -> logger.debug("Incoming packet: {}", incoming))
                .filter(LocalATCommandResponse.class::isInstance)
                .map(LocalATCommandResponse.class::cast)
                .filter(rsp -> rsp.command.equals(ND))
                .map(rsp -> rsp.commandResponse)
                .map(NDResponse.class::cast)
                .doOnNext(nd -> logger.debug("ND response: {}", nd));
    }

    public static class Result {

        /**
         * Timeout returned by NT command.
         */
        public final Duration timeout;

        /**
         * Flux of discovered nodes.
         */
        public final Flux<NDResponse> discovered;

        Result(Duration timeout, Flux<NDResponse> discovered) {
            this.timeout = timeout;
            this.discovered = discovered;
        }
    }
}
