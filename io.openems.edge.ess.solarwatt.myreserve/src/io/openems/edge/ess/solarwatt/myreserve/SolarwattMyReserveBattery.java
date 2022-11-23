package io.openems.edge.ess.solarwatt.myreserve;

import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;

public interface SolarwattMyReserveBattery extends SymmetricEss, AsymmetricEss, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		COMMUNICATION_FAILED(Doc.of(Level.FAULT)),

		/**
		 * 0 = ok 1 = other 2 = not available.
		 */
		BATTERY_SYSTEM_STATE(Doc.of(OpenemsType.INTEGER)), //

		/**
		 * KACO 0 = ok 1 = other 2 = not available.
		 */
		INVERTER_SYSTEM_STATE(Doc.of(OpenemsType.INTEGER)), //

		BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //

		BATTERY_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		SOH(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.PERCENT)), //

		BATTERY_CHARGING(Doc.of(OpenemsType.BOOLEAN)), //

		ACTIVE_POWER_GRID(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		ACTIVE_PRODUCTION_ENERGY_GRID(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),
		ACTIVE_CONSUMPTION_ENERGY_GRID(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),

		ACTIVE_POWER_HOUSE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		ACTIVE_PRODUCTION_ENERGY_HOUSE(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),
		ACTIVE_CONSUMPTION_ENERGY_HOUSE(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),

		ACTIVE_POWER_INVERTER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		ACTIVE_PRODUCTION_ENERGY_INVERTER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),
		ACTIVE_CONSUMPTION_ENERGY_INVERTER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT_HOURS)),

		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public default StateChannel getCommunicationFailedChannel() {
		return this.channel(ChannelId.COMMUNICATION_FAILED);
	}

	public default Value<Boolean> getCommunicationFailed() {
		return this.getCommunicationFailedChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#COMMUNICATION_FAILED}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setCommunicationFailed(boolean value) {
		this.getCommunicationFailedChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#BATTERY_SYSTEM_STATE}.
	 *
	 * @return the Channel
	 */
	public default Channel<Integer> getBatterySystemStateChannel() {
		return this.channel(ChannelId.BATTERY_SYSTEM_STATE);
	}

	public default Value<Integer> getBatterySystemState() {
		return this.getBatterySystemStateChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#BATTERY_SYSTEM_STATE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setBatterySystemState(Integer value) {
		this.getBatterySystemStateChannel().setNextValue(value);
	}

	public default Channel<Integer> getInverterSystemStateChannel() {
		return this.channel(ChannelId.INVERTER_SYSTEM_STATE);
	}

	public default Value<Integer> getInverterSystemState() {
		return this.getInverterSystemStateChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#INVERTER_SYSTEM_STATE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setInverterSystemState(Integer value) {
		this.getInverterSystemStateChannel().setNextValue(value);
	}

	public default Channel<Integer> getBatteryVoltageChannel() {
		return this.channel(ChannelId.BATTERY_VOLTAGE);
	}

	public default Value<Integer> getBatteryVoltage() {
		return this.getBatteryVoltageChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#BATTERY_VOLTAGE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setBatteryVoltage(Integer value) {
		this.getBatteryVoltageChannel().setNextValue(value);
	}

	public default Channel<Integer> getBatteryCurrentChannel() {
		return this.channel(ChannelId.BATTERY_CURRENT);
	}

	public default Value<Integer> getBatteryCurrent() {
		return this.getBatteryCurrentChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#BATTERY_CURRENT}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setBatteryCurrent(Integer value) {
		this.getBatteryCurrentChannel().setNextValue(value);
	}

	public default Channel<Integer> getSohChannel() {
		return this.channel(ChannelId.SOH);
	}

	public default Value<Integer> getSoh() {
		return this.getSohChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#SOH}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setSoh(Integer value) {
		this.getSohChannel().setNextValue(value);
	}

	public default Channel<Boolean> getBatteryChargingChannel() {
		return this.channel(ChannelId.BATTERY_CHARGING);
	}

	public default Value<Boolean> getBatteryCharging() {
		return this.getBatteryChargingChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_POWER_GRID}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setBatteryCharging(Boolean value) {
		this.getBatteryChargingChannel().setNextValue(value);
	}

	public default Channel<Integer> getActivePowerGridChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_GRID);
	}

	public default Value<Integer> getActivePowerGrid() {
		return this.getActivePowerGridChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_POWER_GRID}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActivePowerGrid(Integer value) {
		this.getActivePowerGridChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveProductionEnergyGridChannel() {
		return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_GRID);
	}

	public default Value<Long> getActiveProductionEnergyGrid() {
		return this.getActiveProductionEnergyGridChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_PRODUCTION_ENERGY_GRID}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveProductionEnergyGrid(Long value) {
		this.getActiveProductionEnergyGridChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveConsumptionEnergyGridChannel() {
		return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_GRID);
	}

	public default Value<Long> getActiveConsumptionEnergyGrid() {
		return this.getActiveConsumptionEnergyGridChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_CONSUMPTION_ENERGY_GRID}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveConsumptionEnergyGrid(Long value) {
		this.getActiveConsumptionEnergyGridChannel().setNextValue(value);
	}

	public default Channel<Integer> getActivePowerHouseChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_HOUSE);
	}

	public default Value<Integer> getActivePowerHouse() {
		return this.getActivePowerHouseChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_POWER_HOUSE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActivePowerHouse(Integer value) {
		this.getActivePowerHouseChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveProductionEnergyHouseChannel() {
		return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_HOUSE);
	}

	public default Value<Long> getActiveProductionEnergyHouse() {
		return this.getActiveProductionEnergyHouseChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_PRODUCTION_ENERGY_HOUSE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveProductionEnergyHouse(Long value) {
		this.getActiveProductionEnergyHouseChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveConsumptionEnergyHouseChannel() {
		return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_HOUSE);
	}

	public default Value<Long> getActiveConsumptionEnergyHouse() {
		return this.getActiveConsumptionEnergyHouseChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_CONSUMPTION_ENERGY_HOUSE}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveConsumptionEnergyHouse(Long value) {
		this.getActiveConsumptionEnergyHouseChannel().setNextValue(value);
	}

	public default Channel<Integer> getActivePowerInverterChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_INVERTER);
	}

	public default Value<Integer> getActivePowerInverter() {
		return this.getActivePowerInverterChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_POWER_INVERTER}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActivePowerInverter(Integer value) {
		this.getActivePowerInverterChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveProductionEnergyInverterChannel() {
		return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_INVERTER);
	}

	public default Value<Long> getActiveProductionEnergyInverter() {
		return this.getActiveProductionEnergyInverterChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_PRODUCTION_ENERGY_INVERTER}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveProductionEnergyInverter(Long value) {
		this.getActiveProductionEnergyInverterChannel().setNextValue(value);
	}

	public default Channel<Long> getActiveConsumptionEnergyInverterChannel() {
		return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_INVERTER);
	}

	public default Value<Long> getActiveConsumptionEnergyInverter() {
		return this.getActiveConsumptionEnergyInverterChannel().value();
	}

	/**
	 * Internal method to set the the 'nextValue' on {@link ChannelId#ACTIVE_CONSUMPTION_ENERGY_INVERTER}
	 * Channel.
	 * 
	 * @param value the next value
	 */
	public default void _setActiveConsumptionEnergyInverter(Long value) {
		this.getActiveConsumptionEnergyInverterChannel().setNextValue(value);
	}

}
