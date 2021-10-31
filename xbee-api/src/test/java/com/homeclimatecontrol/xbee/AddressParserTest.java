package com.homeclimatecontrol.xbee;

import com.rapplogic.xbee.api.XBeeAddress64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AddressParserTest {

    private final Logger logger = LogManager.getLogger(getClass());

    @Test
    void parser() {

        var a64 = new XBeeAddress64(0x48, 0xFE, 0x00, 0x13, 0xA2, 0x00, 0x40, 0x5d);

        logger.info("a64: {}", a64);

        var a1 = AddressParser.parse("48 fe 00 13 a2 00 40 5d");
        var a2 = AddressParser.parse("48fe0013 a200405d");
        var a3 = AddressParser.parse("48fe0013a200405d");

        assertThat(a64).isEqualTo(a1);
        assertThat(a64).isEqualTo(a2);
        assertThat(a64).isEqualTo(a3);
    }

    @Test
    void highBit() {

        assertThat(AddressParser.render4x4("00 13 a2 00 40 5d 80 27")).isEqualTo("0013A200.405D8027");
        assertThat(AddressParser.render4x4("00 13 a2 00 f0 5d 80 27")).isEqualTo("0013A200.F05D8027");
    }
}
