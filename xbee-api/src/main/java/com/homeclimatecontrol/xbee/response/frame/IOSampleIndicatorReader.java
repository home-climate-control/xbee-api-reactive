package com.homeclimatecontrol.xbee.response.frame;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class IOSampleIndicatorReader extends FrameReader {
    @Override
    public XBeeResponseFrame read(ByteBuffer frameData) {

        var sourceAddress64 = new XBeeAddress64(frameData);
        var sourceAddress16 = new XBeeAddress16(frameData);
        var receiveOptions = frameData.get();
        var sampleCount = frameData.get();
        var digitalSampleMask = parseDigitalMask(frameData);
        var analogSampleMask = frameData.get();
        var digitalSamples = dataPresent(digitalSampleMask) ? parseDigitalSamples(digitalSampleMask, frameData) : null;
        var analogSamples = parseAnalogSamples(analogSampleMask, frameData);

        return new IOSampleIndicator(sourceAddress64, sourceAddress16, receiveOptions, sampleCount, digitalSampleMask, analogSampleMask, digitalSamples, analogSamples);
    }

    static BitSet parseDigitalMask(ByteBuffer source) {

        var result = new BitSet(15);

        set(result, source.get(), 8);
        set(result, source.get(), 0);

        return result;
    }

    static void set(BitSet target, byte mask, int offset) {

        var bitmask = 0x01;
        for (var bit = 0; bit < 8; bit++) {
            target.set(bit + offset, (mask & bitmask) > 0);
            bitmask <<= 1;
        }
    }

    private boolean dataPresent(BitSet mask) {

        for (var offset = 0; offset < mask.size(); offset++) {
            if (mask.get(offset)) {
                return true;
            }
        }

        return false;
    }

    static BitSet parseDigitalSamples(BitSet presenceMask, ByteBuffer source) {

        var result = new BitSet(15);
        var mask = (source.get() << 8) + (source.get() & 0xFF);

        for (var i = presenceMask.stream().iterator(); i.hasNext(); ) {
            var bit = i.next();
            result.set(bit, (mask & (0x01 << bit)) > 0);
        }

        return result;
    }

    static Map<Integer, Integer> parseAnalogSamples(byte mask, ByteBuffer source) {

        var result = new LinkedHashMap<Integer, Integer>();

        for (var bit = 0; bit < 8; bit++) {
            if ((mask & (0x01 << bit)) > 0) {
                result.put(bit, (source.get() << 8) + (source.get() & 0xFF));
            }
        }

        return result;
    }
}
