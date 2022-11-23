package io.openems.edge.ess.sennec.home.meter.single;

import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.ess.sennec.home.SennecHomeV3Battery;
import io.openems.edge.meter.api.AsymmetricMeter;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.api.SinglePhase;
import io.openems.edge.meter.api.SinglePhaseMeter;
import io.openems.edge.meter.api.SymmetricMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Sennec.HomeV3.Battery.Virtual.Meter.Singlephase", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class SennecVirtualMeterSinglephase extends AbstractOpenemsComponent
		implements SinglePhaseMeter, SymmetricMeter, AsymmetricMeter, OpenemsComponent {

	protected Config config = null;
	protected MeterType meterType = MeterType.PRODUCTION;

	private final Logger log = LoggerFactory.getLogger(SennecVirtualMeterSinglephase.class);

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private SennecHomeV3Battery battery;

	public SennecVirtualMeterSinglephase() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SymmetricMeter.ChannelId.values(), //
				AsymmetricMeter.ChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException, OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.meterType = this.config.type();

		// update filter for 'battery'
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "battery", config.battery_id())) {
			return;
		}

		this.logInfo(this.log, "Activate Sennec virtual Meter of Type " + this.meterType);

		this.initializeSymmetricChannelHandling();

		this.mapBatteryPower();

		// TODO add energy values
		// TODO add OpenEMS state handling
	}

	@Deactivate
	protected void deactivate() {
		this.logInfo(this.log, "deactivation");
		super.deactivate();
	}

	private void mapBatteryPower() throws OpenemsException {

		switch (this.getMeterType()) {
		case GRID:
			this.mapBatteryChannelForMeterTypeGrid();
			break;
		case PRODUCTION:
			this.mapBatteryChannelForMeterTypeProduction();
			break;
		case PRODUCTION_AND_CONSUMPTION:
			this.logError(this.log, "No such sennec virtual meter for PRODUCTION_AND_CONSUMPTION");
			throw new OpenemsException("Virtual Sennec Meter does not provide meter type PRODUCTION_AND_CONSUMPTION");

		case CONSUMPTION_METERED:
			this.logError(this.log, "No such sennec virtual meter for CONSUMPTION_NOT_METERED");
			throw new OpenemsException("Virtual Sennec Meter does not provide meter type CONSUMPTION_METERED");

		case CONSUMPTION_NOT_METERED:
			this.logError(this.log, "No such sennec virtual meter for CONSUMPTION_NOT_METERED");
			throw new OpenemsException("Virtual Sennec Meter does not provide meter type CONSUMPTION_NOT_METERED");
		}
	}

	private void mapBatteryChannelForMeterTypeProduction() {

		// NOTE: we do not map voltage and current, we only map active and reactive
		// power

		this.battery.getActivePowerInverterChannel().onChange((oldValue, newValue) -> {
			switch (this.config.phase()) {
			case L1:
				this.updatePowerChannel(this.getActivePowerL1Channel(), this.getReactivePowerL1Channel(), newValue);
				break;
			case L2:
				this.updatePowerChannel(this.getActivePowerL2Channel(), this.getReactivePowerL2Channel(), newValue);
				break;
			case L3:
				this.updatePowerChannel(this.getActivePowerL3Channel(), this.getReactivePowerL3Channel(), newValue);
				break;
			}
		});
	}

	private void mapBatteryChannelForMeterTypeGrid() {

		switch (this.config.phase()) {
		case L1:
			this.battery.getGridActivePowerL1Channel().onChange((oldValue, newValue) -> {
				this.updatePowerChannel(this.getActivePowerL1Channel(), this.getReactivePowerL1Channel(), newValue);
			});
			this.battery.getGridVoltageL1Channel().onChange((oldValue, newValue) -> {
				this.getVoltageL1Channel().setNextValue(newValue);
			});
			this.battery.getGridCurrentL1Channel().onChange((oldValue, newValue) -> {
				this.getCurrentL1Channel().setNextValue(newValue);
			});
			break;

		case L2:
			this.battery.getGridActivePowerL2Channel().onChange((oldValue, newValue) -> {
				this.updatePowerChannel(this.getActivePowerL2Channel(), this.getReactivePowerL2Channel(), newValue);
			});
			this.battery.getGridVoltageL2Channel().onChange((oldValue, newValue) -> {
				this.getVoltageL2Channel().setNextValue(newValue);
			});
			this.battery.getGridCurrentL2Channel().onChange((oldValue, newValue) -> {
				this.getCurrentL2Channel().setNextValue(newValue);
			});
			break;

		case L3:
			this.battery.getGridActivePowerL3Channel().onChange((oldValue, newValue) -> {
				this.updatePowerChannel(this.getActivePowerL3Channel(), this.getReactivePowerL3Channel(), newValue);
			});
			this.battery.getGridVoltageL3Channel().onChange((oldValue, newValue) -> {
				this.getVoltageL3Channel().setNextValue(newValue);
			});
			this.battery.getGridCurrentL3Channel().onChange((oldValue, newValue) -> {
				this.getCurrentL3Channel().setNextValue(newValue);
			});
			break;
		}

	}

	protected void updatePowerChannel(Channel<Integer> activeChannel, Channel<Integer> reactiveChannel,
			Value<Integer> newValue) {
		Optional<Integer> valueOpt = (Optional<Integer>) newValue.asOptional();
		if (!valueOpt.isPresent()) {
			activeChannel.setNextValue(null);
			reactiveChannel.setNextValue(null);
			return;
		}
		int activePower = TypeUtils.getAsType(OpenemsType.INTEGER, newValue);
		activeChannel.setNextValue(activePower);
		reactiveChannel.setNextValue(activePower);
	}

	// support symmetricMeter interface

	private void initializeSymmetricChannelHandling() {

		// map activePowerL1|L2|L3 to activePower
		SinglePhaseMeter.initializeCopyPhaseChannel(this, this.getPhase());
		// map reactivePowerL1|L2|L3 to reactivePower
		this.initializeCopyPhaseChannelReactivePower();
	}

	/**
	 * Initializes Channel listeners. Copies the Active-Power Phase-Channel value to
	 * Active-Power Channel.
	 * 
	 */
	private void initializeCopyPhaseChannelReactivePower() {
		switch (this.getPhase()) {
		case L1:
			this.getReactivePowerL1Channel().onSetNextValue(value -> {
				this._setReactivePower(value.get());
			});
			break;
		case L2:
			this.getReactivePowerL2Channel().onSetNextValue(value -> {
				this._setReactivePower(value.get());
			});
			break;
		case L3:
			this.getReactivePowerL3Channel().onSetNextValue(value -> {
				this._setReactivePower(value.get());
			});
			break;
		}
	}

	@Override
	public SinglePhase getPhase() {
		return this.config.phase();
	}

	@Override
	public MeterType getMeterType() {
		return this.meterType;
	}

	@Override
	public String debugLog() {
		int power = this.getActivePowerChannel().value().asOptional().orElse(0);
		return (power) + " W," + this.getMeterType().toString();
	}

}
