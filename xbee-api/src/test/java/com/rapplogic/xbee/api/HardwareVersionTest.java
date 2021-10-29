package com.rapplogic.xbee.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HardwareVersionTest {

    @Test
    void version() throws XBeeException {
        var versions = Map.of(
                0x17, HardwareVersion.RadioType.SERIES1,
                0x18, HardwareVersion.RadioType.SERIES1_PRO,
                0x19, HardwareVersion.RadioType.SERIES2,
                0x1A, HardwareVersion.RadioType.SERIES2_PRO,
                0x1E, HardwareVersion.RadioType.SERIES2B_PRO
        );

        for (var kv : versions.entrySet()) {
            var code = kv.getKey();
            var hv = mock(AtCommandResponse.class);
            when(hv.getCommand()).thenReturn("HV");
            when(hv.isOk()).thenReturn(true);
            when(hv.getValue()).thenReturn(new int[] {code});
            assertThat(HardwareVersion.parse(hv)).isEqualTo(kv.getValue());
        }
    }
}
