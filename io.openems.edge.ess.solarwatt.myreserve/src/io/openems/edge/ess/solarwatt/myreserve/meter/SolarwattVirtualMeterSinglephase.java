package io.openems.edge.ess.solarwatt.myreserve.meter;

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
import io.openems.edge.ess.solarwatt.myreserve.SolarwattMyReserveBattery;
import io.openems.edge.meter.api.AsymmetricMeter;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.api.SinglePhase;
import io.openems.edge.meter.api.SinglePhaseMeter;
import io.openems.edge.meter.api.SymmetricMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Solarwatt.MyReserve.Battery.VirtualMeter.Singlephase", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class SolarwattVirtualMeterSinglephase extends AbstractOpenemsComponent
		implements SinglePhaseMeter, SymmetricMeter, AsymmetricMeter, OpenemsComponent {

	protected Config config = null;
	protected MeterType meterType = MeterType.PRODUCTION;

	private final Logger log = LoggerFactory.getLogger(SolarwattVirtualMeterSinglephase.class);

	// TODO solarwatt supports threephase

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private SolarwattMyReserveBattery battery;

	public SolarwattVirtualMeterSinglephase() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SymmetricMeter.ChannelId.values(), //
				AsymmetricMeter.ChannelId.values() //
		);
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

		this.logInfo(this.log, "Activate virtual Solarwatt Meter of Type " + this.meterType);

		this.initializeSymmetricChannelHandling();

		this.mapBatteryPower();

		// TODO diese Channel muessen noch bedient werden :
		// FREQUENCY(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE(Doc.of(OpenemsType.INTEGER) //
		// CURRENT(Doc.of(OpenemsType.INTEGER) //
		// ACTIVE_POWER_L2(Doc.of(OpenemsType.INTEGER) //
		// ACTIVE_POWER_L3(Doc.of(OpenemsType.INTEGER) //
		// REACTIVE_POWER_L2(Doc.of(OpenemsType.INTEGER) //
		// REACTIVE_POWER_L3(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
		// CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
		// CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
	}

	@Deactivate
	protected void deactivate() {
		this.logInfo(this.log, "deactivation");
		super.deactivate();
	}

	private void mapBatteryPower() {

		switch (this.getMeterType()) {
		case GRID:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerGridChannel());
			this.mapBatteryEnergyByInChannel(this.battery.getActiveConsumptionEnergyGridChannel(),
					this.battery.getActiveProductionEnergyGridChannel());
			break;
		case PRODUCTION:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerInverterChannel());
			this.mapBatteryEnergyByInChannel(this.battery.getActiveProductionEnergyInverterChannel(),
					this.battery.getActiveConsumptionEnergyInverterChannel());
			break;
		case PRODUCTION_AND_CONSUMPTION:
			this.logError(this.log, "No such virtual Solarwatt meter for PRODUCTION_AND_CONSUMPTION");
			break;
		case CONSUMPTION_METERED:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerHouseChannel());
			this.mapBatteryEnergyByInChannel(this.battery.getActiveProductionEnergyHouseChannel(),
					this.battery.getActiveConsumptionEnergyHouseChannel());
			break;
		case CONSUMPTION_NOT_METERED:
			this.logError(this.log, "No such virtual Solarwatt meter for CONSUMPTION_NOT_METERED");
			break;
		}
	}

	private void mapBatteryPowerByInChannel(Channel<Integer> channel) {
		channel.onUpdate((newValue) -> {
			switch (this.config.phase()) {
			case L1:
				this.updateOutChannel(this.getActivePowerL1Channel(), this.getReactivePowerL1Channel(), newValue);
				break;
			case L2:
				this.updateOutChannel(this.getActivePowerL2Channel(), this.getReactivePowerL2Channel(), newValue);
				break;
			case L3:
				this.updateOutChannel(this.getActivePowerL3Channel(), this.getReactivePowerL2Channel(), newValue);
				break;
			}
		});
	}

	private void mapBatteryEnergyByInChannel(Channel<Long> prodEnergyChannel, Channel<Long> consEnergyChannel) {

		prodEnergyChannel.onUpdate((newValue) -> {
			this.getActiveProductionEnergyChannel().setNextValue(newValue);
		});

		consEnergyChannel.onUpdate((newValue) -> {
			this.getActiveConsumptionEnergyChannel().setNextValue(newValue);
		});

	}

	protected void updateOutChannel(Channel<Integer> activeChannel, Channel<Integer> reactiveChannel,
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

	private void initializeSymmetricChannelHandling() {
		SinglePhaseMeter.initializeCopyPhaseChannel(this, this.getPhase());
		// map reactivePowerL1|L2|L3 to reactivePower
		this.initializeCopyPhaseChannelReactivePower();
	}

	/**
	 * Initializes Channel listeners. Copies the Active-Power Phase-Channel value to
	 * Active-Power Channel.
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
