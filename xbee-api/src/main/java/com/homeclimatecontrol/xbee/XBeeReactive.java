package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.response.frame.FrameIdAwareResponse;
import com.homeclimatecontrol.xbee.response.frame.XBeeResponseFrame;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.util.ByteUtils;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Reactive implementation of XBee serial port driver.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 *
 * @see com.rapplogic.xbee.api.XBee
 */
public class XBeeReactive implements AutoCloseable {

    private final Logger logger = LogManager.getLogger();

    private final CommPort serialPort;
    private final HardwareReader reader;
    private final HardwareWriter writer;

    private FluxSink<Map.Entry<XBeeRequest, CompletableFuture<Void>>> sendSink;

    /**
     * Create an instance.
     *
     * The instance starts in the background and becomes operational immediately.
     *
     * @param port Port to connect to.
     *
     * @exception IOException if there was a problem talking to hardware.
     */
    public XBeeReactive(String port) throws IOException {

        try {
            serialPort = open(port);

            logger.debug("{}: open", port);

            reader = new HardwareReader(serialPort.getInputStream());
            writer = new HardwareWriter(serialPort.getOutputStream(), getSendFlux());

        } catch (IllegalArgumentException ex) {
            // Pass it on
            throw ex;
        } catch (Exception ex) {
            throw new IOException(port + ": failed to initialize", ex);
        }
    }

    private CommPort open(String port) throws PortInUseException {

        var portsAvailable = (Enumeration<CommPortIdentifier>) CommPortIdentifier.getPortIdentifiers(); // NOSONAR Unavoidable
        var portsFound = new TreeMap<String, CommPortIdentifier>();

        for (var i = portsAvailable.asIterator(); i.hasNext(); ) {
            var found = i.next();
            logger.debug("port found: {}", found.getName());
            portsFound.put(found.getName(), found);
        }

        var ourPort = portsFound.get(port);

        if (ourPort == null) {
            throw new IllegalArgumentException(port + ": not found, available ports are: " + portsFound.keySet());
        }

        return ourPort.open("xbee-api", 9600);
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

        var written = new CompletableFuture<Void>();
        getSendSink().next(new AbstractMap.SimpleEntry<>(rq, written));

        return Mono.fromFuture(written);
    }

    /**
     * Send a request and expect a response.
     *
     * The response to this call will also be emitted in the flux returned by {@link #receive} call.
     *
     * @param rq Request to send.
     * @param timeout Timeout to wait for response, {@code null} to wait indefinitely (be careful with it, eh?).
     *
     * @return Mono with a response, or empty Mono if the response didn't come within timeout, or error Mono if
     * there was a hardware problem.
     */
    public Mono<XBeeResponseFrame> send(XBeeRequest rq, Duration timeout) {

        if (rq.getFrameId() == XBeeRequest.NO_RESPONSE_FRAME_ID) {
            throw new IllegalArgumentException("Invalid FrameID of zero for synchronous request, see https://www.digi.com/resources/documentation/Digidocs/90001942-13/reference/r_zigbee_frame_examples.htm");
        }

        ThreadContext.push("send");
        try {

            var frameId = rq.getFrameId();
            logger.debug("Expecting frameId={}", () -> ByteUtils.toBase16(frameId));
            return Mono.create(sink -> {
                try {

                    // Make sure that the request was indeed sent before waiting
                    sendAsync(rq).block();

                    var rawResponse = receive();
                    var timedResponse = timeout == null
                            ? rawResponse
                            : rawResponse.take(timeout).doOnComplete(() -> logger.debug("timed: done"));

                    var response = timedResponse
                            .filter(FrameIdAwareResponse.class::isInstance)
                            .map(FrameIdAwareResponse.class::cast)
                            .filter(rsp -> rsp.frameId == frameId)
                            .blockFirst();

                    sink.success(response);
                } catch (Exception ex) {
                    sink.error(ex);
                }
            });
        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Get the flux of all incoming XBee packets.
     *
     * This flux will contain the packets returned as a result of a {@link #send(XBeeRequest, Duration)} call as well.
     *
     * @return Flux of all incoming XBee packets. It will error out if there was a hardware problem.
     */
    public Flux<XBeeResponseFrame> receive() {
        return reader.receive();
    }

    private Flux<Map.Entry<XBeeRequest, CompletableFuture<Void>>> getSendFlux() {
        return Flux
                .create(this::connect)
                .doOnSubscribe(ignored -> logger.debug("Subscribed:flux"))
                .subscribeOn(Schedulers.newSingle("XbeeWriter", true));
    }

    private synchronized void connect(FluxSink<Map.Entry<XBeeRequest, CompletableFuture<Void>>> sink) {
        logger.debug("Sink connected");
        sendSink = sink;
        notifyAll();
    }

    private synchronized FluxSink<Map.Entry<XBeeRequest, CompletableFuture<Void>>> getSendSink() {

        var start = Instant.now();

        while (sendSink == null) {
            logger.debug("Waiting for sendSink...");
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for sendSink", ex);
            }
        }

        logger.debug("Got sendSink {}ms later", Duration.between(start, Instant.now()).toMillis());
        return sendSink;
    }

    @Override
    public void close() throws Exception {

        sendSink.complete();

        reader.close();
        writer.close();

        serialPort.close();
    }
}
