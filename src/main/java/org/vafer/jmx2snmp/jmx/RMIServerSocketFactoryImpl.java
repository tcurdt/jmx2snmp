package org.vafer.jmx2snmp.jmx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import javax.net.ServerSocketFactory;

public final class RMIServerSocketFactoryImpl implements RMIServerSocketFactory {

  private final InetAddress localAddress;

  public RMIServerSocketFactoryImpl( final InetAddress pAddress ) {
    localAddress = pAddress;
  }

  public ServerSocket createServerSocket(final int pPort) throws IOException  {
    // Socket created on localAddress on pPort
    return ServerSocketFactory.getDefault().createServerSocket(pPort, 0, localAddress);
  }

    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }

      return obj.getClass().equals(getClass());
    }


    public int hashCode() {
      return RMIServerSocketFactoryImpl.class.hashCode();
    }
}
