package com.homeclimatecontrol.xbee;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.homeclimatecontrol.xbee.TestPortProvider.getTestPort;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class XBeeReactiveTest {

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void invalidPort() {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            var xbee = new XBeeReactive("/this/cant/be");
        }).withMessageStartingWith("/this/cant/be: not found, available ports are:");
    }

    @Test
    @Disabled("Enable only if safe to use hardware is connected")
    void breathe() {
        assertThatCode(() -> {
            var xbee = new XBeeReactive(getTestPort());
        }).doesNotThrowAnyException();
    }
}
