package org.vafer.jmx2snmp.beans;

import java.io.Serializable;

public class TestRow implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String getString() {
		return "String";
	}

	public int getInt() {
		return 1;
	}
}
