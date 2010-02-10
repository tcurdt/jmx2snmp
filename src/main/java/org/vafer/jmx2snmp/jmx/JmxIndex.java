package org.vafer.jmx2snmp.jmx;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 *  Represents an index of all bean paths of the MBeanServer to the individual
 *  JMX attributes. Use the expression to formulate a javax.management.QueryExp
 *  for MBean selection.
 *
 *  @threadsafe yes
 */
public final class JmxIndex {

	private final AtomicReference<Map<String, JmxAttribute>> attributesRef = new AtomicReference<Map<String, JmxAttribute>>(Collections.unmodifiableMap(new HashMap<String, JmxAttribute>()));
	private final MBeanServer mbeanServer;
	private final String expression;

	public JmxIndex() throws Exception {
		this(ManagementFactory.getPlatformMBeanServer(), "bean:*");
	}

	public JmxIndex(MBeanServer pMbeanServer, String pExpression) throws Exception {
		mbeanServer = pMbeanServer;
		expression = pExpression;
		
		update();
	}
	
	public void update() throws Exception {

		final HashMap<String, JmxAttribute> newAttributes = new HashMap<String, JmxAttribute>();
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
	
	public Set<String> getAttributePaths() {
		final Map<String, JmxAttribute> mapping = attributesRef.get();
		return Collections.unmodifiableSet(mapping.keySet());
	}
	
	public JmxAttribute getAttributeAtPath(String pPath) {
		final Map<String, JmxAttribute> attributes = attributesRef.get();
		return attributes.get(pPath);
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append('{').append(attributesRef.get()).append('}');
		return sb.toString();
	}	
}
