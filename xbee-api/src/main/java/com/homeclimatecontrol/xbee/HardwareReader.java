package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.PacketParser;
import com.rapplogic.xbee.api.XBeePacket;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;

public class HardwareReader implements AutoCloseable {

    private final Logger logger = LogManager.getLogger();
    private final InputStream in;
    private final Flux<XBeeResponse> inFlux;
    private FluxSink<XBeeResponse> receiveSink;
    private Thread reader;

    public HardwareReader(InputStream in) {
        this.in = in;

        inFlux = Flux
                .create(this::connect)
                .doOnSubscribe(this::onSubscribe)
                .doOnComplete(this::onComplete)
                .publishOn(Schedulers.boundedElastic())
                .publish()
                .autoConnect();
    }

    private void onSubscribe(Subscription subscription) {
        logger.debug("subscribed");
        reader = new Thread(this::read);
        reader.setName("XBeeReader");
        reader.start();
    }

    private void read() {
        ThreadContext.push("read");
        try {
            logger.info("started");

            while (true) {

                // VT: FIXME: blocking read will not get interrupted by Thread.interrupt(), need to look for alternatives.

                var packet = readPacket(in);

                if (Thread.currentThread().isInterrupted()) {
                    logger.info("Interrupted, terminating");
                    return;
                }

                if (receiveSink == null) {
                    logger.debug("No subscriptions yet, packet dropped: {}", packet);
                    continue;
                }

                receiveSink.next(packet);
            }

        } catch (IOException ex) {
            logger.error("Unexpected I/O problem, stopping the reader", ex);
            receiveSink.error(ex);
        } finally {
            logger.debug("completed");
            ThreadContext.pop();
        }
    }

    private XBeeResponse readPacket(InputStream in) throws IOException {
        ThreadContext.push("readPacket");
        try {
            syncOnStartByte(in);
            return new PacketParser(in).parsePacket();
        } finally {
            ThreadContext.pop();
        }
    }

    private void syncOnStartByte(InputStream in) throws IOException {
        ThreadContext.push("syncOnStartByte");
        try {
            while (true) {
                if (in.available() == 0) {
                    logger.debug("Awaiting start byte...");
                }
                var read = in.read();

                if (read == -1) {
                    throw new IOException("Unexpected end of stream while synchronizing on start byte");
                }

                if (read == XBeePacket.SpecialByte.START_BYTE.getValue()) {
                    // We are at the start of the packet
                    logger.debug("Got the start byte");
                    return;
                }

                logger.debug("Not on the start byte, skipping 0x{}", () -> ByteUtils.toBase16(read));
            }

        } finally {
            ThreadContext.pop();
        }
    }

    private void onComplete() {
        logger.debug("completed");
        reader.interrupt();
    }

    private void connect(FluxSink<XBeeResponse> sink) {
        receiveSink = sink;
    }

    public Flux<XBeeResponse> receive() {
        return inFlux;
    }

    @Override
    public void close() throws Exception {
        ThreadContext.push("close");
        try {
            if (receiveSink == null) {
                logger.debug("null sink, no readers yet?");
                return;
            }
            receiveSink.complete();
        } finally {
            ThreadContext.pop();
        }
    }
}
