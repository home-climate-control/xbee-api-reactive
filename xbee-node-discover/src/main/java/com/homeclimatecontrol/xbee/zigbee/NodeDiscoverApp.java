package com.homeclimatecontrol.xbee.zigbee;

import com.homeclimatecontrol.xbee.XBeeReactive;
import com.homeclimatecontrol.xbee.response.command.NTResponse;
import com.homeclimatecontrol.xbee.response.frame.LocalATCommandResponse;
import com.rapplogic.xbee.api.AtCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

import static com.rapplogic.xbee.api.AtCommand.Command.NT;

public class NodeDiscoverApp {

    private final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        new NodeDiscoverApp().run(args);
    }

    private void run(String[] args) {

        if (args.length == 0) {
            logger.error("Usage: xbee-node-discover <adapter-port>");
            return;
        }

        try (var xbee = new XBeeReactive(args[0])) {

            var response = (LocalATCommandResponse) xbee.send(new AtCommand(NT), null).block();
            var ntResponse = (NTResponse) response.commandResponse; // NOSONAR Unlikely, this is a local command

            var timeout = Duration.ofMillis((ntResponse.timeout * 100L));

            logger.info("XBee is configured with node discovery timeout of {} seconds", timeout.getSeconds());

            var result = new NetworkBrowser()
                    .browse(xbee, timeout)
                    .collectList()
                    .block();

            logger.info("{} node{} discovered within {}{}", result.size(), // NOSONAR False positive for this specific case
                    result.size() == 1 ? "" : "s", timeout, result.isEmpty() ? "" : ":");

            result.forEach(n -> logger.info("  {}, NI={}, {}", n.address64, n.nodeIdentifier, n.deviceType));

            if (result.isEmpty()) {
                logger.warn("Increase NT value if not all of your nodes are discovered within current timeout ({})", timeout);
            }

        } catch (Exception ex) {
            logger.error("Unexpected error", ex);
        }
    }
}
