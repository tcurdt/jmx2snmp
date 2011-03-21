package org.vafer.jmx2snmp.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Represents a property exposed through the MBeanServer
 *
 * @threadsafe yes
 */
public final class JmxAttribute {

  private final String attributeName;
  private final String type;
  private final ObjectName objectName;
  private final MBeanServer mbeanServer;
  private final String path;

  public JmxAttribute(String pAttributeName, String pType, ObjectName pObjectName, MBeanServer pMbeanServer) {
    attributeName = pAttributeName;
    type = pType;
    objectName = pObjectName;
    mbeanServer = pMbeanServer;

    String beanName = pObjectName.getKeyProperty("name");
    if (beanName == null) {
      beanName = pObjectName.getKeyProperty("type");
    }
    path = "<oid>." + beanName + "." + attributeName;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return attributeName;
  }

  public String getType() {
    return type;
  }

  public Object getValue() throws JMException {
    final Object attributeValue = mbeanServer.getAttribute(objectName, attributeName);
    return attributeValue;
  }
}