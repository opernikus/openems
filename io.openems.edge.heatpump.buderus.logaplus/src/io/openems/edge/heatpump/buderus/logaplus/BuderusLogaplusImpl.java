package io.openems.edge.heatpump.buderus.logaplus;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Heatpump.Buderus.Logaplus", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
		)
@EventTopics(//
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE
		)
public class BuderusLogaplusImpl extends AbstractOpenemsComponent
		implements BuderusLogaplus, OpenemsComponent, EventHandler {


	@Reference
	private ConfigurationAdmin cm;

	private Config config;
	
	private ReadWorker worker = null;

	public BuderusLogaplusImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				BuderusLogaplus.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) 
			throws UnknownHostException, OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;

		this.worker = new ReadWorker(this, (Inet4Address) Inet4Address.getByName(this.config.ipAddress()), this.config.port(), this.config.debug(), this.config.dropCycles(), this.config.key());
		this.worker.activate(this.config.id());
	}

	@Deactivate
	protected void deactivate() {
		if (this.worker != null) {
			this.worker.deactivate();
		}
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled() || this.worker == null) {
			return;
		}

		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.worker.triggerNextRun();
			break;
		}
	}


	@Override
	public String debugLog() {
		return this.worker.debugLog();
	}

	

	
	
	
	
}
