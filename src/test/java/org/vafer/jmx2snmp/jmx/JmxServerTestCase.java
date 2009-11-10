package org.vafer.jmx2snmp.jmx;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

import org.weakref.jmx.MBeanExporter;

import org.junit.Test;
import org.vafer.jmx2snmp.jmxutils.beans.TestBeanImpl;


public final class JmxServerTestCase {

	@Test
	public void testSomething() throws Exception {
		final MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
		exporter.export("bean:name=test1", new TestBeanImpl());	

		final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
		jmxServer.start();	

		assertEquals("service:jmx:rmi://127.0.0.1:0/jndi/rmi://127.0.0.1:5100/connector", jmxServer.getUrl());
		assertEquals(5100, jmxServer.getNamingPort());
		assertEquals(0, jmxServer.getProtocolPort());
		
		jmxServer.stop();
	}

}
