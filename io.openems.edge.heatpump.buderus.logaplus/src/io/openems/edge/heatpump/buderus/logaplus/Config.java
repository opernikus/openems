package io.openems.edge.heatpump.buderus.logaplus;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.heatpump.buderus.logaplus.data.Debug;

@ObjectClassDefinition(//
		name = "Heatpump Buderus Logaplus", //
		description = "Implements the Buderus Logaplus Heatpump component.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "heatpump0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "IP-Address", description = "IP-Address of the Sennec Home V3")
	String ipAddress() default "";

	@AttributeDefinition(name = "Port", description = "Port of the Sennec Home V3 API")
	int port() default 80;
	
	@AttributeDefinition(name = "Key", description = "Private key for the heatpump", type = AttributeType.PASSWORD)
	String key();

	@AttributeDefinition(name = "DropCycles", description = "The number of cycles to pass, before fetching new values.")
	int dropCycles() default ReadWorker.DROP_CYCLES;
	
	@AttributeDefinition(name = "Debug", description = "Verbose Information ")
	Debug debug() default Debug.NONE;

	
	String webconsole_configurationFactory_nameHint() default "Heatpump Buderus Logaplus [{id}]";

}