package io.openems.edge.heatpump.buderus.logaplus;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface BuderusLogaplus extends OpenemsComponent {
	public static final int SYS_HEALTH_VAL_ERROR = 0;
	public static final int SYS_HEALTH_VAL_OK = 1;
	public static final int DHW1_STATUS_INACTIVE = 0;
	public static final int DHW1_STATUS_ACTIVE = 1;
	public static final int OPERATION_MODE_OFF = 0;
	public static final int OPERATION_MODE_LOW = 1;
	public static final int OPERATION_MODE_HIGH = 2;
	public static final int OPERATION_MODE_OWNPROGRAM = 3;
	public static final int OPERATION_MODE_ECO = 4;
	public static final String SYS_HEALTH = "/system/healthStatus";
	public static final String SYS_TEMP_OUTDOOR = "/system/sensors/temperatures/outdoor_t1";
	public static final String SYS_TEMP_SUPPLY = "/system/sensors/temperatures/supply_t1";
	public static final String SYS_TEMP_RETURN = "/system/sensors/temperatures/return";
	public static final String SYS_TEMP_SWITCH = "/system/sensors/temperatures/switch";
	public static final String SYS_TEMP_SUPPLY_SET = "/system/sensors/temperatures/supply_t1_setpoint";
	public static final String SYS_TEMP_MIN_OUTDOOR = "/system/minOutdoorTemp";
	public static final String DHW1_ACTUAL_TEMP = "/dhwCircuits/dhw1/actualTemp";
	public static final String DHW1_TEMP_LEVEL_OFF = "/dhwCircuits/dhw1/temperatureLevels/off";
	public static final String DHW1_TEMP_LEVEL_LOW = "/dhwCircuits/dhw1/temperatureLevels/low";
	public static final String DHW1_TEMP_LEVEL_HIGH = "/dhwCircuits/dhw1/temperatureLevels/high";
	public static final String DHW1_SINGLE_CHARGE_SETPT = "/dhwCircuits/dhw1/singleChargeSetpoint";
	public static final String DHW1_CURRENT_SETPT = "/dhwCircuits/dhw1/currentSetpoint";
	public static final String HC1_TEMP_LEVEL_COMFORT2 = "/heatingCircuits/hc1/temperatureLevels/comfort2";
	public static final String HC1_TEMP_LEVEL_ECO = "/heatingCircuits/hc1/temperatureLevels/eco";
	public static final String HC1_CURRENT_ROOM_SETPT = "/heatingCircuits/hc1/currentRoomSetpoint";
	public static final String HC1_TEMP_ROOM_SETPT = "/heatingCircuits/hc1/temporaryRoomSetpoint";
	public static final String DHW1_STATUS = "/dhwCircuits/dhw1/status";
	public static final String DHW1_WORKING_TIME = "/dhwCircuits/dhw1/workingTime";
	public static final String DHW1_WATERFLOW = "/dhwCircuits/dhw1/waterFlow";
	public static final String DHW1_OP_MODE = "/dhwCircuits/dhw1/operationMode";
	public static final String HS_NUMBER_OF_STARTS = "/heatSources/numberOfStarts";
	public static final String HS_MODULATION = "/heatSources/actualModulation";
	public static final String HS_WORKING_TIME_TOTAL_SYSTEM = "/heatSources/workingTime/totalSystem";
	public static final String HS_HS1_MODULATION = "/heatSources/hs1/actualModulation";
	
	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		SLAVE_COMMUNICATION_FAILED(Doc.of(Level.FAULT)),

		SYSTEM_HEALTH(Doc.of(OpenemsType.INTEGER)), //

		/** TEMP_OUTDOOR - Aussentemperatur. */
		TEMP_OUTDOOR(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** TEMP_SUPPLY - Vorlauftemperatur. */
		TEMP_SUPPLY(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** TEMP_RETURN - Ruecklauftemperatur. */
		TEMP_RETURN(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/**
		 * TEMP_SWITCH - temperatur ? TODO was ist das? liegt etwa so bei 25-26 Grad.
		 */
		TEMP_SWITCH(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** TEMP_SUPPLY_SET - VorlaufTemperatur SetPoint. */
		TEMP_SUPPLY_SETPOINT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** SYS_TEMP_MIN_OUTDOOR - Minimale Outdoor Temperatur. */
		TEMP_MIN_OUTDOOR(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_ACTUAL_TEMP - Warmwasser IST Temperatur. */
		TEMP_DHW1_ACTUAL(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_TEMP_LEVEL_OFF - Warmwasser Temperatur Off Level. */
		TEMP_DHW1_LEVEL_OFF(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_TEMP_LEVEL_LOW - Warmwasser Temperatur LOW. */
		TEMP_DHW1_LEVEL_LOW(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_TEMP_LEVEL_HIGH - Warmwasser Temperatur HIGH. */
		TEMP_DHW1_LEVEL_HIGH(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_SINGLE_CHARGE_SETPT - Warmwasser Single Charge SetPt. */
		TEMP_DHW1_SINGLE_CHARGE_SETPOINT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_CURRENT_SETPT - Warmwasser Current SetPt. */
		TEMP_DHW1_CURRENT_SETPOINT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** HC1_TEMP_LEVEL_COMFORT2 - HC1 Comfort 2. */
		TEMP_HC1_LEVEL_COMFORT2(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** HC1_TEMP_LEVEL_ECO - HC1 ECO. */
		TEMP_HC1_LEVEL_ECO(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** HC1_CURRENT_ROOM_SETPT - HC1 Current Room SetPt. */
		TEMP_HC1_CURRENT_ROOM_SETSETPOINT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/* TODO Heating Circuit 1 HC1_TEMP_SUPPLY Actual Vorlauf Temp : 34.3 C */

		/** HC1_TEMP_ROOM_SETPT - HC1 Current Room SetPt. */
		TEMP_HC1_TEMP_ROOM_SETPOINT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.DEZIDEGREE_CELSIUS)), //

		/** DHW1_STATUS. */
		DHW1_STATUS(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.ON_OFF)), //

		/** DHW1_WORKING_TIME - Arbeistzeit in Minuten. */
		DHW1_WORKING_TIME(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.MINUTE)), //

		/** DHW1_WATERFLOW - Wasserfluss l/min. */
		DHW1_WATERFLOW(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)), //

		/** DHW1_OP_MODE - Operation Mode. */
		DHW1_OP_MODE(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.NONE)), //

		/** HS_NUMBER_OF_STARTS. */
		HS_NUMBER_OF_STARTS(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.NONE)), //

		/** HS_MODULATION - %. */
		HS_MODULATION(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.PERCENT)), //

		/** HS_WORKING_TIME_TOTAL_SYSTEM - %. */
		HS_WORKING_TIME_TOTAL_SYSTEM(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.SECONDS)), //

		/** HS_HS1_MODULATION - %. */
		HS_HS1_MODULATION(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY)//
				.unit(Unit.PERCENT)), //

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

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#SLAVE_COMMUNICATION_FAILED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setSlaveCommunicationFailed(boolean value) {
		this.getSlaveCommunicationFailedChannel().setNextValue(value);
	}

	public default Channel<Integer> getSystemHealthChannel() {
		return this.channel(ChannelId.SYSTEM_HEALTH);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#SYSTEM_HEALTH}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setSystemHealth(Integer value) {
		this.getSystemHealthChannel().setNextValue(value);
	}

	public default Channel<Integer> getTempOutdoorChannel() {
		return this.channel(ChannelId.TEMP_OUTDOOR);
	}

	public default Channel<Integer> getTempSupplyChannel() {
		return this.channel(ChannelId.TEMP_SUPPLY);
	}

	public default Channel<Integer> getTempReturnChannel() {
		return this.channel(ChannelId.TEMP_RETURN);
	}

	public default Channel<Integer> getTempSwitchChannel() {
		return this.channel(ChannelId.TEMP_SWITCH);
	}

	public default Channel<Integer> getTempSupplySetChannel() {
		return this.channel(ChannelId.TEMP_SUPPLY_SETPOINT);
	}

	public default Channel<Integer> getTempMinOutdoorChannel() {
		return this.channel(ChannelId.TEMP_MIN_OUTDOOR);
	}

	public default Channel<Integer> getTempDhw1_ActualChannel() {
		return this.channel(ChannelId.TEMP_DHW1_ACTUAL);
	}

	public default Channel<Integer> getTempDhw1LevelOffChannel() {
		return this.channel(ChannelId.TEMP_DHW1_LEVEL_OFF);
	}

	public default Channel<Integer> getTempDhw1LevelLowChannel() {
		return this.channel(ChannelId.TEMP_DHW1_LEVEL_LOW);
	}

	public default Channel<Integer> getTempDhw1LevelHighChannel() {
		return this.channel(ChannelId.TEMP_DHW1_LEVEL_HIGH);
	}

	public default Channel<Integer> getTempDhw1SingleChargeSetpointChannel() {
		return this.channel(ChannelId.TEMP_DHW1_SINGLE_CHARGE_SETPOINT);
	}

	public default Channel<Integer> getTempDhw1CurrentSetpointChannel() {
		return this.channel(ChannelId.TEMP_DHW1_CURRENT_SETPOINT);
	}

	public default Channel<Integer> getTempHc1Comfort2Channel() {
		return this.channel(ChannelId.TEMP_HC1_LEVEL_COMFORT2);
	}

	public default Channel<Integer> getTempHc1EcoChannel() {
		return this.channel(ChannelId.TEMP_HC1_LEVEL_ECO);
	}

	public default Channel<Integer> getTempHc1CurRoomSetpointChannel() {
		return this.channel(ChannelId.TEMP_HC1_CURRENT_ROOM_SETSETPOINT);
	}

	public default Channel<Integer> getTempHc1TempRoomSetpointChannel() {
		return this.channel(ChannelId.TEMP_HC1_TEMP_ROOM_SETPOINT);
	}

	public default Channel<Integer> getDhw1StatusChannel() {
		return this.channel(ChannelId.DHW1_STATUS);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DHW1_STATUS}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDhw1Status(Integer value) {
		this.getDhw1StatusChannel().setNextValue(value);
	}

	public default Channel<Integer> getDhw1WorkTimeChannel() {
		return this.channel(ChannelId.DHW1_WORKING_TIME);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DHW1_WORKING_TIME}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDhw1WorkTime(Integer value) {
		this.getDhw1WorkTimeChannel().setNextValue(value);
	}

	
	public default Channel<Integer> getDhw1WaterflowChannel() {
		return this.channel(ChannelId.DHW1_WATERFLOW);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DHW1_WATERFLOW}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDhw1Waterflow(Integer value) {
		this.getDhw1WaterflowChannel().setNextValue(value);
	}

	public default Channel<Integer> getDhw1OperationModeChannel() {
		return this.channel(ChannelId.DHW1_OP_MODE);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DHW1_OP_MODE}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDhw1OperationMode(Integer value) {
		this.getDhw1OperationModeChannel().setNextValue(value);
	}

	public default Channel<Integer> getHsNumberOfStartsChannel() {
		return this.channel(ChannelId.HS_NUMBER_OF_STARTS);
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#HS_NUMBER_OF_STARTS} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setHsNumberOfStarts(Integer value) {
		this.getHsNumberOfStartsChannel().setNextValue(value);
	}

	public default Channel<Integer> getHsModulationChannel() {
		return this.channel(ChannelId.HS_MODULATION);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#HS_MODULATION}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setHsModulation(Integer value) {
		this.getHsModulationChannel().setNextValue(value);
	}

	public default Channel<Integer> getHsHs1ModulationChannel() {
		return this.channel(ChannelId.HS_HS1_MODULATION);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#HS_HS1_MODULATION}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setHsHs1Modulation(Integer value) {
		this.getHsHs1ModulationChannel().setNextValue(value);
	}

	public default Channel<Integer> getHsWorkingTimeTotalSystemChannel() {
		return this.channel(ChannelId.HS_WORKING_TIME_TOTAL_SYSTEM);
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#HS_WORKING_TIME_TOTAL_SYSTEM} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setHsWorkingTimeTotalSystem(Integer value) {
		this.getHsWorkingTimeTotalSystemChannel().setNextValue(value);
	}

}
