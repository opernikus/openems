package io.openems.edge.ess.solarwatt.myreserve;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.common.sum.GridMode;

@ObjectClassDefinition(//
		name = "ESS Solarwatt MyReserve Battery", //
		description = "Implements the Solarwatt MyReserve Battery component.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "sw0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "IP-Address", description = "IP-Address of the Solarwatt MyReserve")
	String ipAddress() default "";

	@AttributeDefinition(name = "Port", description = "Port of the Solarwatt MyReserve API")
	int port() default 80;

	@AttributeDefinition(name = "Capacity", description = "Capacity in [Wh]")
	int capacity() default (4 * 2400); // size of customer0104

	@AttributeDefinition(name = "Power", description = "The maximum Power of the system in [VA]")
	int maxApparentPower() default 3600;

	@AttributeDefinition(name = "GridMode", description = "Is Solarwatt MyReserve is used in Grid-Mode?")
	GridMode gridMode() default GridMode.UNDEFINED;

	@AttributeDefinition(name = "Debug Rest", description = "Show REST Response?")
	boolean debugRest() default false;

	@AttributeDefinition(name = "Debug Solarwatt", description = "Show Solarwatt Values?")
	boolean debugSolarwatt() default false;

	@AttributeDefinition(name = "Intervall", description = "REST API Call fetch intervall in OpenEMS cycles")
	int interval() default 60;

	String webconsole_configurationFactory_nameHint() default "Solarwatt MyReserve [{id}]";

}