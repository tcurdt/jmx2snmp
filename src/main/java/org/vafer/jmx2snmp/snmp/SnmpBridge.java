package org.vafer.jmx2snmp.snmp;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.vafer.jmx2snmp.jmx.JmxAttribute;
import org.vafer.jmx2snmp.jmx.JmxIndex;
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxMib.Bean;

/**
 * The SnmpBridge starts a SNMP agent and provides access to the MBean objects. 
 * It looks up the JMX attribute path from the JmxMib mapping and looks up the
 * JmxAttribute from the JmxIndex.
 * 
 * Calling report() on startup will log mapping incosistencies to System.err
 */
public final class SnmpBridge implements CommandResponder {

	private final InetAddress address;
	private final int port;
	private final JmxIndex jmxIndex;
	private final JmxMib jmxMib;

	private Snmp snmp;
	
	public SnmpBridge(InetAddress pAddress, int pPort, JmxIndex pJmxIndex, JmxMib pJmxMib) {
		address = pAddress;
		port = pPort;
		jmxIndex = pJmxIndex;
		jmxMib = pJmxMib;
	}
	
	public void report() {

		final Map<String,Bean> mibMapping = jmxMib.getMapping();

		final Set<String> attributesInMib = new HashSet<String>();
		final Set<String> attributesInIndex = new HashSet<String>(jmxIndex.getAttributePaths());		
		for(Map.Entry<String, Bean> entry : mibMapping.entrySet()) {
			String oid = entry.getKey();
			Bean bean = entry.getValue();
			if (attributesInIndex.contains(bean.absolutePath)) {
				if (attributesInMib.contains(bean.absolutePath)) {
					System.err.println("jmx2snmp: attribute mapping for [" + bean.absolutePath + "] found more than once");
				}
				attributesInMib.add(bean.absolutePath);
			} else {
				if (bean.leaf) {
					System.err.println("jmx2snmp: attribute [" + bean.absolutePath + "] no longer exists at OID [" + oid + "]");
				}
			}
		}
		
		attributesInIndex.removeAll(attributesInMib);
		
		for(String attribute : attributesInIndex) {
			System.err.println("jmx2snmp: attribute not mapped yet: " + attribute);
		}

	}
	
	public void processPdu(CommandResponderEvent pRequest) {

		final PDU requestPdu = pRequest.getPDU();
		
		if (requestPdu == null) {
			return;
		}
		
        try {

        	final PDU responsePdu = new PDU(requestPdu);
        	responsePdu.setType(PDU.RESPONSE);
        	
        	if (requestPdu.getType() == PDU.GET) {

        		for(VariableBinding binding : responsePdu.toArray()) {
        			final OID oid = binding.getOid();            		
            		final String path = jmxMib.getPathFromOid(oid.toString());
            		
            		if (path == null) {
            			binding.setVariable(Null.noSuchObject);
            			continue;
            		}
            		
            		final JmxAttribute attribute = jmxIndex.getAttributeAtPath(path);

            		if (attribute == null) {
            			binding.setVariable(Null.noSuchObject);
            			continue;            			
            		}
            		
            		final Variable variable = getVariableFromJmxAttribute(attribute);
            		
            		if (variable != null) {
            			binding.setVariable(variable);
            		}
            	}
        		
        	} else if (requestPdu.getType() == PDU.GETNEXT) {

        		
        		for(VariableBinding binding : responsePdu.toArray()) {
        			final OID oid = binding.getOid();
        			final String next = jmxMib.getNextOidFromOid(oid.toString());
        			
        			if (next == null) {
            			binding.setVariable(Null.noSuchObject);
            			continue;
        			}
        			
        			final OID nextOid = new OID(next);

            		binding.setOid(nextOid);

            		final String path = jmxMib.getPathFromOid(nextOid.toString());
            		
            		if (path == null) {
            			binding.setVariable(Null.noSuchObject);
            			continue;
            		}

            		final JmxAttribute attribute = jmxIndex.getAttributeAtPath(path);
            		
            		if (attribute == null) {
            			binding.setVariable(Null.noSuchObject);
            			continue;            			
            		}
            		
            		final Variable variable = getVariableFromJmxAttribute(attribute);
            		
            		if (variable != null) {	            		
	            		binding.setVariable(variable);
            		}
            	}
        		
        	} else {
        		
        	}
        	
        	pRequest.getStateReference().setTransportMapping(pRequest.getTransportMapping());
        	pRequest.getMessageDispatcher().returnResponsePdu(
        			pRequest.getMessageProcessingModel(),
        			pRequest.getSecurityModel(),
        			pRequest.getSecurityName(),
        			pRequest.getSecurityLevel(),
        			responsePdu,
        			pRequest.getMaxSizeResponsePDU(),
        			pRequest.getStateReference(),
        			new StatusInformation()
        			);
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void start() throws Exception {
		snmp = new Snmp(new DefaultUdpTransportMapping(new UdpAddress(address, port)));
		snmp.addCommandResponder(this);
		snmp.listen();
	}
	
	public void stop() throws Exception {
		snmp.close();
		snmp = null;
	}

	private Variable getVariableFromJmxAttribute(JmxAttribute pAttribute) throws JMException {
		
		final Object value = pAttribute.getValue();
		
		if (value == null) {
			return new Null();
		}

		final String type = pAttribute.getType(); 
		
		if ("int".equals(type)) {
			final Number n = (Number) value;
			return new Integer32(n.intValue());
		} else if ("long".equals(type)) {
			final Number n = (Number) value;
			return new Counter64(n.longValue());			
		} else if ("boolean".equals(type)) {
			final Boolean b = (Boolean) value;
			return new Integer32(b?1:0);			
		} else if ("java.lang.String".equals(type)) {
			return new OctetString(String.valueOf(value));			
		} else {
			return new OctetString("Unsupported Type: " + pAttribute.getType());
		}
		
	}
	
    // public static void main(String[] args) throws Exception {
    //  
    //  System.out.println("starting...");
    //  
    //  final MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    //  exporter.export("bean:name=test1", new TestBeanImpl());     
    // 
    //  final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
    //  jmxServer.start();
    // 
    //  final URL url = JmxutilsTestCase.class.getResource("/org/vafer/jmx2snmp/mapping.properties");
    //  
    //  final JmxMib jmxMib = new JmxMib();
    //  jmxMib.load(new FileReader(url.getFile()));
    // 
	//  final JmxIndex jmxIndex = new JmxIndex();
	//
    //  final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("192.168.214.1"), 1161, jmxIndex, jmxMib);
    //  snmpBridge.start();
    //  
    //  System.out.println("enter 'quit' to stop...");
    //  final Scanner sc = new Scanner(System.in);
    //     while(!sc.nextLine().equals("quit"));
    //     
    //     snmpBridge.stop();
    //     jmxServer.stop();
    // }    
}
