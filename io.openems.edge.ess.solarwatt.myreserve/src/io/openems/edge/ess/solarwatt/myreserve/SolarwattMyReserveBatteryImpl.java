package io.openems.edge.ess.solarwatt.myreserve;

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
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.SinglePhaseEss;
import io.openems.edge.ess.api.SymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Solarwatt.MyReserve.Battery", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
})
public class SolarwattMyReserveBatteryImpl extends AbstractOpenemsComponent
		implements SolarwattMyReserveBattery, SymmetricEss, AsymmetricEss, OpenemsComponent, EventHandler {

	@Reference
	private ConfigurationAdmin cm;

	private Config config;

	private ReadWorker worker = null;

	public SolarwattMyReserveBatteryImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SymmetricEss.ChannelId.values(), //
				AsymmetricEss.ChannelId.values(), //
				SinglePhaseEss.ChannelId.values(), //
				SolarwattMyReserveBattery.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws UnknownHostException, OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;

		this.worker = new ReadWorker(this, (Inet4Address) Inet4Address.getByName(config.ipAddress()), config.port(),
				config.debugRest(), config.debugSolarwatt(), config.interval());
		this.worker.activate(config.id());
		this._setGridMode(this.config.gridMode());

		this._setCapacity(this.config.capacity());
		this._setMaxApparentPower(this.config.maxApparentPower());

		AsymmetricEss.initializePowerSumChannels(this);
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
		return "SoC:" + this.getSoc().asString() //
				+ "|L:" + this.getActivePower().asString();
	}

}
