package io.openems.edge.ess.sennec.home;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.common.sum.GridMode;
import io.openems.edge.ess.api.SinglePhase;

@ObjectClassDefinition(//
		name = "ESS Sennec Home V3 Battery", //
		description = "Implements the Sennec Home V3 Battery component.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "senec0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "IP-Address", description = "IP-Address of the Sennec Home V3")
	String ipAddress() default "";

	@AttributeDefinition(name = "Port", description = "Port of the Sennec Home V3 API")
	int port() default 80;

	@AttributeDefinition(name = "Capacity", description = "Capacity in [WH]")
	int capacity() default 9900;

	@AttributeDefinition(name = "Power", description = "The maximum Power of the system in [VA]")
	int maxApparentPower() default 10000;

	@AttributeDefinition(name = "CycleTime", description = "The number of cyclces between fetching of new values")
	int cycleTime() default 60;

	@AttributeDefinition(name = "Phase", description = "On which Phase is the Powerwall connected?")
	SinglePhase phase() default SinglePhase.L1;

	@AttributeDefinition(name = "GridMode", description = "Is Sennec used in Grid-Mode?")
	GridMode gridMode() default GridMode.UNDEFINED;

	@AttributeDefinition(name = "Debug", description = "1 = Show REST Response, 2 = show Variables, 3 = show all, 0 = none")
	int debugMode() default 0;

	String webconsole_configurationFactory_nameHint() default "Sennec Home V3 [{id}]";

}