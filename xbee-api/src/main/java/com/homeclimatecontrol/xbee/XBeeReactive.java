package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive implementation of XBee serial port driver.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 *
 * @see com.rapplogic.xbee.api.XBee
 */
public class XBeeReactive implements AutoCloseable {

    public static final String NOT_IMPLEMENTED = "Not Implemented";
    private final Logger logger = LogManager.getLogger();

    /**
     * Create an instance.
     *
     * The instance starts in the background and becomes operational immediately.
     *
     * @param port Port to connect to.
     */
    public XBeeReactive(String port) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Send a request that requires no response, or response will be collected by other means.
     *
     * @param rq Request to send.
     *
     * @return Mono that will be completed when the request is actually sent, or errors out if
     * there was a hardware problem.
     */
    public Mono<Void> sendAsync(XBeeRequest rq) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Send a request and expect a response.
     *
     * The response to this call will also be emitted in the flux returned by {@link #receive(Duration)} call.
     *
     * @param rq Request to send.
     * @param timeout Timeout to wait for response, {@code null} to wait indefinitely (be careful with it, eh?).
     *
     * @return Mono with a response, or empty Mono if the response didn't come within timeout, or error Mono if
     * there was a hardware problem.
     */
    public Mono<XBeeResponse> send(XBeeRequest rq, Duration timeout) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Get the flux of all incoming XBee packets.
     *
     * This flux will contain the packets returned as a result of a {@link #send(XBeeRequest, Duration)} call as well.
     *
     * @param timeout Timeout to wait for response, {@code null} to wait indefinitely (be careful with it, eh?).
     *
     * @return Mono with a response, or empty Mono if the response didn't come within timeout, or error Mono if
     * there was a hardware problem.
     */
    public Flux<XBeeResponse> receive(Duration timeout) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void close() throws Exception {
        logger.warn("close(): not implemented");
    }
}
