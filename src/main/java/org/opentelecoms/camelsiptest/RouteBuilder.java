package org.opentelecoms.camelsiptest;

import org.apache.camel.Predicate;
import org.apache.camel.PropertyInject;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class RouteBuilder extends SpringRouteBuilder {
	
	private Logger logger = LoggerFactory.getLogger(RouteBuilder.class);
	
	// FIXME - bug CAMEL-8125
	//@PropertyInject("local.country")
	private String localCountry = "CH";
	
	//@PropertyInject("smsc.country")
	private String smscCountry = "UK";
	
	//@PropertyInject("throttle.timePeriodMillis")
	long throttleTimePeriodMillis = 1000;
	
	//@PropertyInject("throttle.maximumRequestsPerPeriod")
	int throttleRequestsPerPeriod = 1;

    /**
     * Configures two camel routes:
     * one receives messages at localhost port 5154 and stores them in an activeMQ queue
     * one sends the messages from that activeMQ queue to a specified sip address
     * @throws Exception
     */
	@Override
	public void configure() throws Exception {

		/**
		 * Create some strings that will be used in the Camel routes
		 */
        String localhost = InetAddress.getLocalHost().getHostAddress();

        //the activeMQ queue
        String activeMQqueue = "activemq:sip-messages";

        //the SIP uri sending out to
		String sendingUsername = "niktocamel";
        String sendingHost     = localhost;
        String sendingPort     = "5156";

        String sendingSipURI =
				"sip://" + sendingUsername + "@" + sendingHost + ":" + sendingPort +
                "?stackName=Retriever" +
                "&fromUser=sending" +
                "&fromHost=" + localhost +
                "&fromPort=5155" +
                "&eventHeaderName=sendingFromQueue" +
                "&eventId=sourceJSMqueue";


        //receiving from the SIP world
		String receivingSipURI =
                "sip://listener@" + localhost + ":5154" +
				"?stackName=Listener" +
                "&transport=udp" +
                "&eventHeaderName=retrievedFromSIP" +
                "&eventId=SIP";

        //to log the messages in the console
        String logReceivedMessage =
                "log:ReceivedMessage" +
                "?level=DEBUG";

        //to store the messages in a file
        String testFile = "file://test";

        /**
         * This Camel routes handles messages from JMS going out to the SIP world
         */
		from(activeMQqueue)
		    .to(sendingSipURI);     //send message to given SIP uri
		
		/**
		 * This Camel route handles messages coming to us from the SIP world
		 */
		from(receivingSipURI)
            .to(logReceivedMessage) // Log a copy of the message
         	.to(activeMQqueue)  	// put it in a JMS queue
            .to(testFile);          // put message in a file

	}

}
