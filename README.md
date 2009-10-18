# Bridge

    // export JMX beans either through Spring, Guice or jmxutils
		final MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
		exporter.export("bean:name=test1", new TestBeanImpl());	

    // fire up JMX
		final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
		jmxServer.start();	

    // load mib mapping
		final URL url = JmxutilsTestCase.class.getResource("/org/vafer/jmx2snmp/mapping.properties");
		final JmxMib jmxMib = new JmxMib();
		jmxMib.load(new FileReader(url.getFile()));

		// fire up SNMP bridge
		final SnmpBridge snmpBridge = new SnmpBridge(InetAddress.getByName("localhost"), 1161, jmxServer, jmxMib);
		snmpBridge.start();


# Mapping

    1.3.6.1.4.1.27305 = <oid>
    1.3.6.1.4.1.27305.12 = test1
    1.3.6.1.4.1.27305.12.1 = AnotherInt
    1.3.6.1.4.1.27305.12.2 = Blue
    1.3.6.1.4.1.27305.12.3 = Boolean
    1.3.6.1.4.1.27305.12.4 = BooleanArray
    1.3.6.1.4.1.27305.12.5 = IntArray
    1.3.6.1.4.1.27305.12.6 = IntegerArray
    1.3.6.1.4.1.27305.12.7 = Long
    1.3.6.1.4.1.27305.12.8 = SomeInt
    1.3.6.1.4.1.27305.12.9 = SomeObjects
    1.3.6.1.4.1.27305.12.10 = SomethingMapped
    1.3.6.1.4.1.27305.12.11 = SomethingUnique
    1.3.6.1.4.1.27305.12.12 = String
    1.3.6.1.4.1.27305.12.13 = StringArray
    1.3.6.1.4.1.27305.12.14 = StringTable
    1.3.6.1.4.1.27305.12.15 = TestRowArray

# Test

    snmpwalk -On -c public -v 1 localhost:1161 1.3.6.1.4.1.27305.12
    snmpget -On -c public -v 1 localhost:1161 1.3.6.1.4.1.27305.12.8

# Integration with snmpd

    proxy -v 1 -c public localhost:1161 .1.3.6.1.4.1.27305

# License

Licensed under the Apache License, Version 2.0 (the "License")
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
