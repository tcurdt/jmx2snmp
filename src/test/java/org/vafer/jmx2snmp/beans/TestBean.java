package org.vafer.jmx2snmp.beans;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface TestBean {

	boolean getBoolean();

	void setBoolean(boolean b);

	long getLong();

	void setLong(long l);

	int getSomeInt();

	int getAnotherInt();

	void setSomeInt(int i);

	String getString();

	void setString(String s);

	String[] getStringArray();

	String[][] getStringTable();

	TestRow[] getTestRowArray();
	
	Collection<String> getSomeObjects();
	
	int[] getIntArray();
	
	Map<String,String> getSomethingMapped();
	
	Set<String> getSomethingUnique();
	
	boolean isBlue();

	boolean[] getBooleanArray();
	
	Integer[] getIntegerArray();

}