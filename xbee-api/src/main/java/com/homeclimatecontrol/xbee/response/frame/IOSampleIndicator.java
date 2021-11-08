package com.homeclimatecontrol.xbee.response.frame;

import com.homeclimatecontrol.xbee.AddressParser;
import com.homeclimatecontrol.xbee.util.HexFormat;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import java.util.BitSet;
import java.util.Map;
import java.util.Optional;

public class IOSampleIndicator extends XBeeResponseFrame{

    public final XBeeAddress64 sourceAddress64;
    public final XBeeAddress16 sourceAddress16;
    public final byte receiveOptions;
    public final byte sampleCount;
    public final BitSet digitalSampleMask;
    public final byte analogSampleMask;
    public final Optional<BitSet> digitalSamples; // NOSONAR This is intended

    /**
     * The key is the analog sample mask (frame byte at offset 18), the value is the sample.
     */
    public final Map<Integer, Integer> analogSamples;

    public IOSampleIndicator(
            XBeeAddress64 sourceAddress64,
            XBeeAddress16 sourceAddress16,
            byte receiveOptions,
            byte sampleCount,
            BitSet digitalSampleMask,
            byte analogSampleMask,
            BitSet digitalSamples,
            Map<Integer, Integer> analogSamples) {

        this.sourceAddress64 = sourceAddress64;
        this.sourceAddress16 = sourceAddress16;
        this.receiveOptions = receiveOptions;
        this.sampleCount = sampleCount;
        this.digitalSampleMask = digitalSampleMask;
        this.analogSampleMask = analogSampleMask;
        this.digitalSamples = digitalSamples == null ? Optional.empty() : Optional.of(digitalSamples);
        this.analogSamples = analogSamples;
    }

    @Override
    public String toString() {

        var sb = new StringBuilder("{IOSample");

        sb.append(" sourceAddress=").append(AddressParser.render4x4(sourceAddress64)).append("/").append(sourceAddress16);
        sb.append(", receiveOptions=").append(HexFormat.format(receiveOptions));
        sb.append(", sampleCount=").append(sampleCount);

        digitalSamples.ifPresent(sample -> {
            sb.append(", digital(");

            var printed = 0;
            for (var bit = 0; bit < 16; bit++) {
                if (digitalSampleMask.get(bit)) {
                    if (printed > 0) {
                        sb.append(",");
                    }
                    sb.append(bit).append(":").append(sample.get(bit));
                    printed++;
                }
            }
            sb.append(")");
        });

        if (!analogSamples.isEmpty()) {
            sb.append(", analog(");
            var printed = 0;
            for (var kv : analogSamples.entrySet()) {
                if (printed > 0) {
                    sb.append(",");
                }
                sb.append(kv.getKey()).append(":").append(kv.getValue());
                printed++;
            }
            sb.append(")");
        }

        return sb.append("}").toString();
    }
}
