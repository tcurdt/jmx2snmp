package org.vafer.jmx2snmp.jmxutils;

import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;

import org.junit.Test;
import org.vafer.jmx2snmp.jmx.JmxIndex;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxServer;
import org.vafer.jmx2snmp.jmxutils.beans.TestBeanImpl;
import org.vafer.jmx2snmp.snmp.SnmpBridge;
import org.weakref.jmx.MBeanExporter;

public class JmxutilsTestCase {

	@Test
	public void testStartup() throws Exception {
		final MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
		exporter.export("bean:name=test1", new TestBeanImpl());	

		final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
		jmxServer.start();	

		final URL url = JmxutilsTestCase.class.getResource("/org/vafer/jmx2snmp/mapping.properties");
		
		assertNotNull(url);
		
		final JmxMib jmxMapping = new JmxMib();
		jmxMapping.load(new FileReader(url.getFile()));
		
		final JmxIndex jmxIndex = new JmxIndex();
		
		final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("localhost"), 1161, jmxIndex, jmxMapping);
		snmpBridge.start();

		assertNotNull(jmxServer);
		assertNotNull(jmxIndex);
		assertNotNull(snmpBridge);

		// snmpwalk -On -c public -v 1 localhost:1161 1.3.6.1.4.1.27305.12
		// snmpget -On -c public -v 1 localhost:1161 1.3.6.1.4.1.27305.12.8

//		System.out.println("enter 'quit' to stop...");		
//		final Scanner sc = new Scanner(System.in);
//	    while(!sc.nextLine().equals("quit"));
		
		snmpBridge.report();
		
		snmpBridge.stop();
		jmxServer.stop();
	}
	
}
