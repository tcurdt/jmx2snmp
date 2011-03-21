package org.vafer.jmx2snmp.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.SocketFactory;

public final class RMIClientSocketFactoryImpl implements RMIClientSocketFactory, Serializable {

  private static final long serialVersionUID = 1L;

  public Socket createSocket(final String pHost, final int pPort) throws IOException {
    return SocketFactory.getDefault().createSocket(pHost, pPort);
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
      return RMIClientSocketFactoryImpl.class.hashCode();
    }
}
