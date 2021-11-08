package com.homeclimatecontrol.xbee;

import com.homeclimatecontrol.xbee.response.ResponseReader;
import com.homeclimatecontrol.xbee.response.frame.XBeeResponseFrame;
import com.rapplogic.xbee.api.XBeePacket;
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
    private final ResponseReader responseReader = new ResponseReader();

    private final InputStream in;
    private final Flux<XBeeResponseFrame> inFlux;
    private FluxSink<XBeeResponseFrame> receiveSink;
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

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    var packet = readPacket(in);

                    if (receiveSink == null) {
                        logger.debug("No subscriptions yet, packet dropped: {}", packet);
                        continue;
                    }

                    receiveSink.next(packet);

                } catch (UnsupportedOperationException ex) {
                    // Most likely, the frame data was read in its entirety and we're going to land at the sync byte
                    logger.error("Unsupported frame, dropped", ex);
                }
            }


        } catch (IOException ex) {
            logger.error("Unexpected I/O problem, stopping the reader", ex);
            receiveSink.error(ex);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Interrupted, stopping the reader");
        } finally {
            logger.debug("completed");
            ThreadContext.pop();
        }
    }

    private XBeeResponseFrame readPacket(InputStream in) throws IOException, InterruptedException {
        ThreadContext.push("readPacket");
        try {
            syncOnStartByte(in);
            return responseReader.read(in);
        } finally {
            ThreadContext.pop();
        }
    }

    private void syncOnStartByte(InputStream in) throws IOException, InterruptedException {
        ThreadContext.push("syncOnStartByte");
        try {
            while (true) {
                while (in.available() == 0) {
                    logger.debug("Awaiting start byte...");
                    synchronized (this) {

                        // VT: NOTE: SerialEventPortNotification doesn't work reliably with RxTx.
                        // This is a half assed measure that will waste a O(1 second) on application exit,
                        // considering it good enough until https://github.com/home-climate-control/xbee-api/issues/15
                        // is closed.

                        wait(1000);
                    }
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

    private void connect(FluxSink<XBeeResponseFrame> sink) {
        receiveSink = sink;
    }

    public Flux<XBeeResponseFrame> receive() {
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
