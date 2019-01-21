package io.openems.edge.fenecon.pro.pvmeter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "FENECON Pro 9-12 PV Meter", //
		description = "Implements the FENECON Pro energy storage system.")
@interface Config {
	String id() default "meter1";

	boolean enabled() default true;

	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus brige.")
	String modbus_id();

	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "";

	String webconsole_configurationFactory_nameHint() default "FENECON Pro 9-12 PV Meter [{id}]";
}