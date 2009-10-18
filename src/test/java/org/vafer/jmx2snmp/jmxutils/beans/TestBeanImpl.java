package org.vafer.jmx2snmp.jmxutils.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mt.jmx.Managed;

import org.vafer.jmx2snmp.beans.TestBean;
import org.vafer.jmx2snmp.beans.TestRow;


public final class TestBeanImpl implements TestBean  {

	private final Collection<String> col = new ArrayList<String>();
	
	public TestBeanImpl() {
		col.add("row1");
		col.add("row2");
	}
	
	@Managed
	public Collection<String> getSomeObjects() {
		return col;
	}

	@Managed
	public boolean isBlue() {
		return false;
	}

	@Managed
	public boolean getBoolean() {
		return true;
	}
	
	public void setBoolean( boolean b ) {		
	}
	
	@Managed
	public long getLong() {
		return 1;
	}
	
	public void setLong(long l) {
	}
	
	@Managed
	public int getSomeInt() {
		return 1;
	}

	@Managed
	public int getAnotherInt() {
		return 1;
	}
	
	public void setSomeInt(int i) {
	}

	@Managed
	public String getString() {
		return "string";
	}

	public void setString(String s) {		
	}
	
	@Managed
	public String[][] getStringTable() {
		return new String[][] {
            { "column1row1", "column2row1" },
            { "column1row2", "column2row2" }
		};
	}
	
	@Managed
	public TestRow[] getTestRowArray() {
		return new TestRow[] {
			new TestRow(),
			new TestRow()
		};
	}

	@Managed
	public int[] getIntArray() {
		return new int[] { 1, 2 };
	}

	@Managed
	public String[] getStringArray() {
		return new String[] { "row1", "row2" };
	}
	
	@Managed
	public boolean[] getBooleanArray() {
		return new boolean[] { true, true };
	}
	
	@Managed
	public Integer[] getIntegerArray() {
		return new Integer[] { new Integer(1), new Integer(2)};
	}

	@Managed
	public Map<String,String> getSomethingMapped() {
		final Map<String,String> map = new HashMap<String,String>();
		map.put("a", "valueA");
		map.put("b", "valueB");
		return map;
	}

	@Managed
	public Set<String> getSomethingUnique() {
		final Set<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		return set;
	}
}
