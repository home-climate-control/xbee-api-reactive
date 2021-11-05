package com.rapplogic.xbee.examples.wpan;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.rapplogic.xbee.TestPortProvider.getTestPort;

public class Processing {
	XBee xbee;
	Queue<XBeeResponse> queue = new ConcurrentLinkedQueue<>();
	boolean message;
	XBeeResponse response;

	void setup() {
	  try {

	    xbee = new XBee();
	    xbee.open(getTestPort(), 9600);

	    xbee.addPacketListener(new PacketListener() {
	      @Override
          public void processResponse(XBeeResponse response) {
	        queue.offer(response);
	      }
	    }
	    );
	  }
	  catch (Exception e) {
	    System.out.println("XBee failed to initialize");
	    e.printStackTrace();
	    System.exit(1);
	  }
	}

	void draw() {
	  try {
	    readPackets();
	  }
	  catch (Exception e) {
	    e.printStackTrace();
	  }
	}

	void readPackets() {

	  while ((response = queue.poll()) != null) {
	    // we got something!
	    try {
	      RxResponseIoSample ioSample = (RxResponseIoSample) response;

	      println("We received a sample from " + ioSample.getSourceAddress());

	      if (ioSample.containsAnalog()) {
	        println("10-bit temp reading (pin 19) is " +
	          ioSample.getSamples()[0].getAnalog(1).orElse(null));
	      }
	    }
	    catch (ClassCastException e) {
	      // not an IO Sample
	    }
	  }
	}

	void println(String s) {}

    String dataPath(String s) {return null;}
}
