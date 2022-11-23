package io.openems.edge.ess.sennec.home;

import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;

public interface SennecHomeV3Battery extends SymmetricEss, AsymmetricEss, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		SLAVE_COMMUNICATION_FAILED(Doc.of(Level.FAULT)),

		/**
		 * see ../enums/SennecHomeState.
		 */
		SYSTEM_STATE(Doc.of(OpenemsType.INTEGER)), //

		/**
		 * the Sennec battery voltage of the battery.
		 */
		VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //

		/**
		 * the Sennec current to/from the battery.
		 */
		CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		SOH(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.PERCENT)), //

		CHARGING(Doc.of(OpenemsType.BOOLEAN)), //
		OPERATION_HOURS(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)), //

		BATTERY_PACKAGE_STATE1(Doc.of(OpenemsType.INTEGER)), //
		BATTERY_PACKAGE_STATE2(Doc.of(OpenemsType.INTEGER)), //
		BATTERY_PACKAGE_STATE3(Doc.of(OpenemsType.INTEGER)), //
		BATTERY_PACKAGE_STATE4(Doc.of(OpenemsType.INTEGER)), //

		// values from the battery module

		ACTIVE_POWER_GRID(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		ACTIVE_POWER_HOUSE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		ACTIVE_POWER_INVERTER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //

		// values from the grid module

		GRID_ACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		GRID_ACTIVE_POWER_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		GRID_ACTIVE_POWER_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		GRID_ACTIVE_POWER_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT)), //
		GRID_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		GRID_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		GRID_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		GRID_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //
		GRID_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //
		GRID_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //
		GRID_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIHERTZ)) //

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

	public default StateChannel getSlaveCommunicationFailedChannel() {
		return this.channel(ChannelId.SLAVE_COMMUNICATION_FAILED);
	}

	public default Value<Boolean> getSlaveCommunicationFailed() {
		return this.getSlaveCommunicationFailedChannel().value();
	}
	
	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#SLAVE_COMMUNICATION_FAILED} Channel.
	 *
	 * @param value the next value
	 */

	public default void _setSlaveCommunicationFailed(boolean value) {
		this.getSlaveCommunicationFailedChannel().setNextValue(value);
	}

	public default Channel<Integer> getSystemStateChannel() {
		return this.channel(ChannelId.SYSTEM_STATE);
	}

	public default Value<Integer> getSystemState() {
		return this.getSystemStateChannel().value();
	}

	public default void setSystemState(Integer val) {
		this.getSystemStateChannel().setNextValue(val);
	}

	public default Channel<Integer> getVoltageChannel() {
		return this.channel(ChannelId.VOLTAGE);
	}

	public default Value<Integer> getVoltage() {
		return this.getVoltageChannel().value();
	}

	public default void setVoltage(Integer val) {
		this.getVoltageChannel().setNextValue(val);
	}

	public default Channel<Integer> getCurrentChannel() {
		return this.channel(ChannelId.CURRENT);
	}

	public default Value<Integer> getCurrent() {
		return this.getCurrentChannel().value();
	}

	public default void setCurrent(Integer val) {
		this.getCurrentChannel().setNextValue(val);
	}

	public default Channel<Integer> getSohChannel() {
		return this.channel(ChannelId.SOH);
	}

	public default Value<Integer> getSoh() {
		return this.getSohChannel().value();
	}

	public default void setSoh(Integer val) {
		this.getSohChannel().setNextValue(val);
	}

	public default Channel<Boolean> getChargingChannel() {
		return this.channel(ChannelId.CHARGING);
	}
	
	/**
	 * "True" if the battery is charging. See
	 * {@link ChannelId#CHARGING}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getCharging() {
		return this.getChargingChannel().value();
	}

	public default void setCharging(Boolean val) {
		this.getChargingChannel().setNextValue(val);
	}

	/**
	 * Gets the channel for the battery status of the corresponding package.
	 * 
	 * @param channelNumber index of the channel.
	 * @return the channel.
	 * @throws OpenemsException if channelNumber is out of range.
	 */
	public default Channel<Integer> getBatteryPackageStatusChannel(int channelNumber) throws OpenemsException {
		switch (channelNumber) {
		case 1:
			return this.channel(ChannelId.BATTERY_PACKAGE_STATE1);
		case 2:
			return this.channel(ChannelId.BATTERY_PACKAGE_STATE2);
		case 3:
			return this.channel(ChannelId.BATTERY_PACKAGE_STATE3);
		case 4:
			return this.channel(ChannelId.BATTERY_PACKAGE_STATE4);
		}
		throw new OpenemsException("Unknown BatteryPackage_STATE channel " + channelNumber);
	}

	/**
	 * Gets value of the channel for the battery status of the corresponding package.
	 * 
	 * @param channelNumber index of the channel.
	 * @return the value.
	 * @throws OpenemsException if channelNumber is out of range.
	 */
	public default Value<Integer> getBatteryPackageStatus(int channelNumber) throws OpenemsException {
		return this.getBatteryPackageStatusChannel(channelNumber).value();
	}

	public default void setBatteryPackageStatus(int channelNumber, Integer val) throws OpenemsException {
		this.getBatteryPackageStatusChannel(channelNumber).setNextValue(val);
	}

	public default Channel<Integer> getOperationHoursChannel() {
		return this.channel(ChannelId.OPERATION_HOURS);
	}

	public default Value<Integer> getOperationHours() {
		return this.getOperationHoursChannel().value();
	}

	public default void setOperationHours(Integer val) {
		this.getOperationHoursChannel().setNextValue(val);
	}

	public default Channel<Integer> getActivePowerGridChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_GRID);
	}

	public default Value<Integer> getActivePowerGrid() {
		return this.getActivePowerGridChannel().value();
	}

	public default void setActivePowerGrid(Integer val) {
		this.getActivePowerGridChannel().setNextValue(val);
	}

	public default Channel<Integer> getActivePowerHouseChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_HOUSE);
	}

	public default Value<Integer> getActivePowerHouse() {
		return this.getActivePowerHouseChannel().value();
	}

	public default void setActivePowerHouse(Integer val) {
		this.getActivePowerHouseChannel().setNextValue(val);
	}

	public default Channel<Integer> getActivePowerInverterChannel() {
		return this.channel(ChannelId.ACTIVE_POWER_INVERTER);
	}

	public default Value<Integer> getActivePowerInverter() {
		return this.getActivePowerInverterChannel().value();
	}

	public default void setActivePowerInverter(Integer val) {
		this.getActivePowerInverterChannel().setNextValue(val);
	}

	public default Channel<Integer> getGridActivePowerChannel() {
		return this.channel(ChannelId.GRID_ACTIVE_POWER);
	}

	public default Value<Integer> getGridActivePower() {
		return this.getGridActivePowerChannel().value();
	}

	public default void setGridActivePower(Integer val) {
		this.getGridActivePowerChannel().setNextValue(val);
	}

	public default Channel<Integer> getGridActivePowerL1Channel() {
		return this.channel(ChannelId.GRID_ACTIVE_POWER_L1);
	}

	public default Value<Integer> getGridActivePowerL1() {
		return this.getGridActivePowerL1Channel().value();
	}

	public default void setGridActivePowerL1(Integer val) {
		this.getGridActivePowerL1Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridActivePowerL2Channel() {
		return this.channel(ChannelId.GRID_ACTIVE_POWER_L2);
	}

	public default Value<Integer> getGridActivePowerL2() {
		return this.getGridActivePowerL2Channel().value();
	}

	public default void setGridActivePowerL2(Integer val) {
		this.getGridActivePowerL2Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridActivePowerL3Channel() {
		return this.channel(ChannelId.GRID_ACTIVE_POWER_L3);
	}

	public default Value<Integer> getGridActivePowerL3() {
		return this.getGridActivePowerL3Channel().value();
	}

	public default void setGridActivePowerL3(Integer val) {
		this.getGridActivePowerL3Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridVoltageL1Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L1);
	}

	public default Value<Integer> getGridVoltageL1() {
		return this.getGridVoltageL1Channel().value();
	}

	public default void setGridVoltageL1(Integer val) {
		this.getGridVoltageL1Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridVoltageL2Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L2);
	}

	public default Value<Integer> getGridVoltageL2() {
		return this.getGridVoltageL2Channel().value();
	}

	public default void setGridVoltageL2(Integer val) {
		this.getGridVoltageL2Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridVoltageL3Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L3);
	}

	public default Value<Integer> getGridVoltageL3() {
		return this.getGridVoltageL3Channel().value();
	}

	public default void setGridVoltageL3(Integer val) {
		this.getGridVoltageL3Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridCurrentL1Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L1);
	}

	public default Value<Integer> getGridCurrentL1() {
		return this.getGridCurrentL1Channel().value();
	}

	public default void setGridCurrentL1(Integer val) {
		this.getGridCurrentL1Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridCurrentL2Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L2);
	}

	public default Value<Integer> getGridCurrentL2() {
		return this.getGridCurrentL2Channel().value();
	}

	public default void setGridCurrentL2(Integer val) {
		this.getGridCurrentL2Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridCurrentL3Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L3);
	}

	public default Value<Integer> getGridCurrentL3() {
		return this.getGridCurrentL3Channel().value();
	}

	public default void setGridCurrentL3(Integer val) {
		this.getGridCurrentL3Channel().setNextValue(val);
	}

	public default Channel<Integer> getGridFrequencyChannel() {
		return this.channel(ChannelId.GRID_FREQUENCY);
	}

	public default Value<Integer> getGridFrequency() {
		return this.getGridFrequencyChannel().value();
	}

	public default void setGridFrequency(Integer val) {
		this.getGridFrequencyChannel().setNextValue(val);
	}

}
