package org.vafer.jmx2snmp.jmxutils;

import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;

import mt.jmx.MBeanExporter;

import org.junit.Test;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxServer;
import org.vafer.jmx2snmp.jmxutils.beans.TestBeanImpl;
import org.vafer.jmx2snmp.snmp.SnmpBridge;

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
		
		final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("localhost"), 1161, jmxServer, jmxMapping);
		snmpBridge.start();

		assertNotNull(jmxServer);
		assertNotNull(snmpBridge);

		
		snmpBridge.stop();
		jmxServer.stop();
	}
	
}
