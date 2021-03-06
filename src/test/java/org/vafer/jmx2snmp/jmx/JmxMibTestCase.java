package org.vafer.jmx2snmp.jmx;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Map;

import org.junit.Test;
import org.vafer.jmx2snmp.jmx.JmxMib.Bean;

public final class JmxMibTestCase {

	@Test
	public void testLoading() throws Exception {
		final JmxMib mib = new JmxMib();
		
		mib.load(new StringReader(
				"1.2.3 = <oid>\n" +
				"1.2.3.1 = bean1\n" +
				"1.2.3.1.1 = attribute1\n" +
				"1.2.3.1.2 = attribute2\n" +
				"1.2.3.2 = bean2\n" +
				"1.2.3.2.1 = attribute1\n" +
				"1.2.3.2.2 = attribute2\n"
				));
		
		assertEquals(9, mib.getNodeCount());

		assertEquals("<oid>", mib.getPathFromOid("1.2.3"));
		assertEquals("<oid>.bean1", mib.getPathFromOid("1.2.3.1"));
		assertEquals("<oid>.bean2", mib.getPathFromOid("1.2.3.2"));
		assertEquals("<oid>.bean1.attribute1", mib.getPathFromOid("1.2.3.1.1"));
		assertEquals("<oid>.bean1.attribute2", mib.getPathFromOid("1.2.3.1.2"));
		assertEquals("<oid>.bean2.attribute1", mib.getPathFromOid("1.2.3.2.1"));
		assertEquals("<oid>.bean2.attribute2", mib.getPathFromOid("1.2.3.2.2"));

		final Map<String,Bean> mapping = mib.getMapping();

		assertEquals(7, mapping.size());
				
		mib.load(new StringReader(
				"2.2.3 = <oid>\n" +
				"2.2.3.1 = bean1\n" +
				"2.2.3.1.1 = attribute1\n" +
				"2.2.3.1.2 = attribute2\n" +
				"2.2.3.2 = bean2\n" +
				"2.2.3.2.1 = attribute1\n" +
				"2.2.3.2.2 = attribute2\n"
				));

		assertEquals(9, mib.getNodeCount());

		assertEquals("<oid>", mib.getPathFromOid("2.2.3"));
		assertEquals("<oid>.bean1", mib.getPathFromOid("2.2.3.1"));
		assertEquals("<oid>.bean2", mib.getPathFromOid("2.2.3.2"));
		assertEquals("<oid>.bean1.attribute1", mib.getPathFromOid("2.2.3.1.1"));
		assertEquals("<oid>.bean1.attribute2", mib.getPathFromOid("2.2.3.1.2"));
		assertEquals("<oid>.bean2.attribute1", mib.getPathFromOid("2.2.3.2.1"));
		assertEquals("<oid>.bean2.attribute2", mib.getPathFromOid("2.2.3.2.2"));
	
	}
}
