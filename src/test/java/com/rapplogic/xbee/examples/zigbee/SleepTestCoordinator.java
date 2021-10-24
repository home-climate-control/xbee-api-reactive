package com.rapplogic.xbee.examples.zigbee;

import com.rapplogic.xbee.api.RemoteAtRequest;
import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeConfiguration;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * The premise of this example is we have a end device configured in cyclic sleep. We'll send a command
 * to the coordinator to turn on/off a pin. Periodically when the end device wakes up, it will poll the
 * coordinator and receive packets sent while it was sleeping
 *
 * @author andrew
 *
 */
public class SleepTestCoordinator {

	private final static Logger log = LogManager.getLogger(ZNetSenderExample.class);

	public SleepTestCoordinator(String args[]) throws XBeeTimeoutException, XBeeException, InterruptedException {

		XBee xbee = new XBee(new XBeeConfiguration().withStartupChecks(false));

		//coord
		xbee.open("/dev/tty.usbserial-A6005uRz", 9600);

		// replace with end device's 64-bit address (SH + SL)
		// router (firmware 23A7)
		XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);

		if (args.length == 1 && (args[0].equals("on") || args[0].equals("off"))) {

			log.info("Turning D0 " + args[0]);

			RemoteAtRequest request = new RemoteAtRequest(addr64, "D0", new int[] {args[0].equals("on") ? 5 : 4});

			RemoteAtResponse response = (RemoteAtResponse) xbee.sendSynchronous(request, Duration.ofSeconds(15));

			if (response.isOk()) {
				log.info("Successfully turned " + args[0] + " pin 20 (D0)");
			} else {
				log.warn("Failed to turn " + args[0] + " pin 20.  status is " + response.getStatus());
			}

			Thread.sleep(2000);

			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		} else {
			System.err.println("arg should be on or off");
		}
	}

	public static void main(String args[]) throws XBeeTimeoutException, XBeeException, InterruptedException {
		new SleepTestCoordinator(args);
	}
}
