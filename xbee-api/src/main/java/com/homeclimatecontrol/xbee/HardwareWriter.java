package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.XBeeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HardwareWriter implements AutoCloseable {

    private final Logger logger = LogManager.getLogger();
    private final OutputStream out;
    private final Disposable sourceSubscription;

    public HardwareWriter(OutputStream out, Flux<Map.Entry<XBeeRequest, CompletableFuture<Void>>> packetSource) {
        this.out = out;

        sourceSubscription = packetSource
                .doOnSubscribe(ignored -> logger.debug("Subscribed:writer"))
                .subscribe(this::write);
    }

    private void write(Map.Entry<XBeeRequest, CompletableFuture<Void>> pair) {
        ThreadContext.push("write");

        var rq = pair.getKey();
        var packet = rq.getXBeePacket().getByteArray();
        var future = pair.getValue();

        try {

            logger.debug("Writing {}", rq);

            // VT: FIXME: Replace with array write when getByteArray() is fixed
            for (var b : packet) {
                out.write(b);
            }
            out.flush();

            future.complete(null);

        } catch (IOException ex) {
            // The result is delivered via a Mono, it may be just discarded, so let's log it here
            logger.error("Packet write failed for {}", rq, ex);
            future.completeExceptionally(ex);
        } finally {
            ThreadContext.pop();
        }
    }

    @Override
    public void close() throws Exception {
        sourceSubscription.dispose();
    }
}
