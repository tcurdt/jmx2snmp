package org.vafer.jmx2snmp.spring;

import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.net.InetAddress;
import java.net.URL;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxServer;
import org.vafer.jmx2snmp.snmp.SnmpBridge;

public class SpringTestCase {

	@Test
	public void testStartup() throws Exception {

		// BasicConfigurator.configure();		
		final BeanFactory factory = new XmlBeanFactory(new ClassPathResource("org/vafer/jmx2snmp/spring/beans.xml"));
		// this should not be required
		factory.getBean("exporter");	

		final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
		jmxServer.start();	

		final URL url = SpringTestCase.class.getResource("/org/vafer/jmx2snmp/mapping.properties");
		
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
