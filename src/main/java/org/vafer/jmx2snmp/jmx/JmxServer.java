package org.vafer.jmx2snmp.jmx;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import sun.management.jmxremote.LocalRMIServerSocketFactory;

public class JmxServer {

	private final InetAddress address;
	private final int protocolPort;
	private final int namingPort;
	private final String expression;
	private final AtomicReference<Map<String, JmxAttribute>> attributesRef = new AtomicReference<Map<String, JmxAttribute>>(Collections.unmodifiableMap(new HashMap<String, JmxAttribute>()));

	private RMIConnectorServer rmiServer;
	private String url;
	private Registry registry;

	public JmxServer(InetAddress pAddress, int pProtocolPort, int pNamingPort, String pExpression) {
		address = pAddress;
		protocolPort = pProtocolPort;
		namingPort = pNamingPort;
		expression = pExpression;
	}

	public JmxServer(InetAddress pAddress) {
		this(pAddress, 0, 5100, "bean:*");
	}

	public String getUrl() {
		return url;
	}

	public int getProtocolPort() {
		return protocolPort;
	}

	public int getNamingPort() {
		return namingPort;
	}

	public synchronized void start() throws Exception {
		if (rmiServer != null) {
			return;
		}

		final RMIServerSocketFactory serverFactory;
		
		if (address.isLoopbackAddress()) {
			serverFactory = new LocalRMIServerSocketFactory();
		} else {
			serverFactory = new RMIServerSocketFactoryImpl(address);
			// Make sure to have the class RMIServerSocketFactoryImpl.class in your JMX client's classpath to connect.
		}
		
		registry = LocateRegistry.createRegistry(namingPort, null, serverFactory);

		final Map<String,Object> env = new HashMap<String,Object>();
		env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverFactory);
		
		final StringBuffer serviceUrl = new StringBuffer();
		serviceUrl.append("service:jmx:");
		serviceUrl.append("rmi://").append(address.getHostAddress()).append(':').append(protocolPort).append("/jndi/");
		serviceUrl.append("rmi://").append(address.getHostAddress()).append(':').append(namingPort).append("/connector");
		url = serviceUrl.toString();

		rmiServer = new RMIConnectorServer(
				new JMXServiceURL(url),
				env,
				ManagementFactory.getPlatformMBeanServer()
				);

		rmiServer.start();		
	}
	
	public JmxAttribute getAttributeAtPath(String pPath) {
		final Map<String, JmxAttribute> attributes = attributesRef.get();
		return attributes.get(pPath);
	}
	
	public synchronized void load() throws Exception {
		final HashMap<String, JmxAttribute> newAttributes = new HashMap<String, JmxAttribute>();
		
		final MBeanServer mbeanServer = rmiServer.getMBeanServer();
		final Collection<ObjectInstance> mbeans = mbeanServer.queryMBeans(new ObjectName(expression), null);
		
		for (Iterator<ObjectInstance> it = mbeans.iterator(); it.hasNext();) {
			final ObjectInstance mbean = it.next();
			
			final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(mbean.getObjectName());

			final MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
			for (final MBeanAttributeInfo attribute : attributes) {
				
				if (attribute.isReadable()) {
					final String attributeName = attribute.getName();
					
					final JmxAttribute jmxAttribute = new JmxAttribute(attributeName, attribute.getType(), mbean.getObjectName(), mbeanServer);
					
					newAttributes.put(jmxAttribute.getPath(), jmxAttribute);
				}
			}
		}
		
		attributesRef.set(Collections.unmodifiableMap(newAttributes));
	}

	public synchronized void stop() throws Exception {
		rmiServer.stop();
		UnicastRemoteObject.unexportObject(registry, true);
		registry = null;
		rmiServer = null;
		url = null;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append('{').append(attributesRef.get()).append('}');
		return sb.toString();
	}
	
//	public static void main(String[] args) throws Exception {
//		final JmxServer server = new JmxServer(InetAddress.getByName("localhost"));
//		server.start();
//		
//		System.out.println("jconsole " + server.getUrl());
//
//		System.out.println("enter 'quit' to stop...");		
//		final Scanner sc = new Scanner(System.in);
//	    while(!sc.nextLine().equals("quit"));
//	    server.stop();
//	}
}
