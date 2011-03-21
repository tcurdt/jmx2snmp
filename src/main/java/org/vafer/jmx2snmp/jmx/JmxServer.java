package org.vafer.jmx2snmp.jmx;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import sun.management.jmxremote.LocalRMIServerSocketFactory;

/**
 * This JmxServer exposes the MBeans on a dedicated interface/port combination.
 *
 * Please note: when using a non-localhost address the connecting JMX client
 * needs to have the class RMIServerSocketFactoryImpl available in it's classpath.
 *
 * @threadsafe no
 */
public final class JmxServer {

  private final InetAddress address;
  private final int protocolPort;
  private final int namingPort;

  private RMIConnectorServer rmiServer;
  private String url;
  private Registry registry;

  public JmxServer(InetAddress pAddress, int pProtocolPort, int pNamingPort) {
    address = pAddress;
    protocolPort = pProtocolPort;
    namingPort = pNamingPort;
  }

  public JmxServer() throws UnknownHostException {
    this(InetAddress.getLocalHost());
  }

  public JmxServer(InetAddress pAddress) {
    this(pAddress, 0, 5100);
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


  public synchronized void stop() throws Exception {
    rmiServer.stop();
    UnicastRemoteObject.unexportObject(registry, true);
    registry = null;
    rmiServer = null;
    url = null;
  }

 // public static void main(String[] args) throws Exception {
 //    final JmxServer server = new JmxServer(InetAddress.getByName("localhost"));
 //    server.start();
 // 
 //    System.out.println("jconsole " + server.getUrl());
 // 
 //    System.out.println("enter 'quit' to stop...");
 //    final Scanner sc = new Scanner(System.in);
 //    while(!sc.nextLine().equals("quit"));
 //    server.stop();
 //  }
}
