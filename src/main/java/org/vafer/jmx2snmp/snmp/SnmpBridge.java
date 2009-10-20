package org.vafer.jmx2snmp.snmp;

import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;
import java.util.Scanner;

import javax.management.JMException;

import mt.jmx.MBeanExporter;

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
import org.vafer.jmx2snmp.jmx.JmxMib;
import org.vafer.jmx2snmp.jmx.JmxServer;
// import org.vafer.jmx2snmp.jmxutils.JmxutilsTestCase;
// import org.vafer.jmx2snmp.jmxutils.beans.TestBeanImpl;

public final class SnmpBridge implements CommandResponder {

	private final InetAddress address;
	private final int port;
	private final JmxServer jmxServer;
	private final JmxMib jmxMib;

	private Snmp snmp;
	
	public SnmpBridge(InetAddress pAddress, int pPort, JmxServer pJmxServer, JmxMib pJmxMib) {
		address = pAddress;
		port = pPort;
		jmxServer = pJmxServer;
		jmxMib = pJmxMib;
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
            		
            		final JmxAttribute attribute = jmxServer.getAttributeAtPath(path);

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

            		final JmxAttribute attribute = jmxServer.getAttributeAtPath(path);
            		
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
    //  final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("192.168.214.1"), 1161, jmxServer, jmxMib);
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
