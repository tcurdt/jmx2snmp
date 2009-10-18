package org.vafer.jmx2snmp.snmp;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.snmp4j.CommandResponderEvent;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxServer;

public final class SnmpBridgeTestCase {

//	@Test
	public void testSomething() throws Exception {
		
//		final JmxMib jmxMapping = mock(JmxMib.class);
//		when(jmxMapping.getPathFromOid("")).thenReturn("");
//
//		final JmxAttribute jmxAttribute = mock(JmxAttribute.class);
//		when(jmxAttribute.getValue()).thenReturn("");
//		
//		final JmxServer jmxServer = mock(JmxServer.class);
//		when(jmxServer.getAttributeAtPath("")).thenReturn(jmxAttribute);
	
		final JmxServer jmxServer = null;
		final JmxMib jmxMib = null;
		final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("localhost"), 1161, jmxServer, jmxMib);
		snmpBridge.start();

		final CommandResponderEvent event = null;
		
		snmpBridge.processPdu(event);
		
//		verify(jmxMapping).getPathFromOid("");
		
		assertNotNull(jmxMib);
		
		snmpBridge.stop();
	}

	
}
