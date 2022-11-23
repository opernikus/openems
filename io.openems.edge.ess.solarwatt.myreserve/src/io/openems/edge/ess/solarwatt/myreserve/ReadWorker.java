package io.openems.edge.ess.solarwatt.myreserve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.utils.JsonUtils;
import io.openems.common.worker.AbstractWorker;

public class ReadWorker extends AbstractWorker {
	private final Logger log = LoggerFactory.getLogger(ReadWorker.class);

	private static final String URL_REQ_PATH = "/rest/kiwigrid/wizard/devices";

	// battery
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_BAT = "com.kiwigrid.devices.batteryconverter.BatteryConverter";
	private static final String BAT_CURRENT_IN = "CurrentBatteryIn";
	private static final String BAT_CURRENT_OUT = "CurrentBatteryOut";
	private static final String BAT_POWER_IN = "PowerACIn";
	private static final String BAT_POWER_OUT = "PowerACOut";
	private static final String BAT_WORK_IN = "WorkACIn";
	private static final String BAT_WORK_OUT = "WorkACOut";
	private static final String BAT_SOH = "StateOfHealth";
	private static final String BAT_SOC = "StateOfCharge";
	private static final String BAT_STATE = "StateDevice";
	private static final String BAT_VOLT_CELL_MIN = "VoltageBatteryCellMin";
	private static final String BAT_VOLT_CELL_MAX = "VoltageBatteryCellMax";
	private static final String BAT_VOLT_CELL_STRING = "VoltageBatteryString";
	private static final String BAT_VOLT_CELL_MEAN = "VoltageBatteryCellMean";
	private static final String BAT_TEMP_CELL_MIN = "TemperatureBatteryCellMin";
	private static final String BAT_TEMP_CELL_MAX = "TemperatureBatteryCellMax";

	// Kostal inverter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_KOSTAL_PIKO_INVERTER = "com.kiwigrid.devices.kostal.PIKO";
	private static final String INVERTER_AC_CURRENT_L1 = "ACCurrentL1";
	private static final String INVERTER_AC_CURRENT_L2 = "ACCurrentL2";
	private static final String INVERTER_AC_CURRENT_L3 = "ACCurrentL3";
	private static final String INVERTER_ACTIVE_POWER_L1 = "ActivePowerL1";
	private static final String INVERTER_ACTIVE_POWER_L2 = "ActivePowerL2";
	private static final String INVERTER_ACTIVE_POWER_L3 = "ActivePowerL3";
	private static final String INVERTER_AC_VOLTAGE_L1 = "ACVoltageL1";
	private static final String INVERTER_AC_VOLTAGE_L2 = "ACVoltageL2";
	private static final String INVERTER_AC_VOLTAGE_L3 = "ACVoltageL3";
	private static final String INVERTER_STATE = "StateDevice";

	private static final String INVERTER_POWER_YIELD_SUM = "PowerYieldSum";
	private static final String INVERTER_POWER_AC_OUT_MAX = "PowerACOutMax";
	private static final String INVERTER_POWER_AC_OUT = "PowerACOut";
	private static final String INVERTER_POWER_AC_OUT_LIMIT = "PowerACOutLimit";
	private static final String INVERTER_WORK_AC_OUT = "WorkACOut";

	// My Reserve inverter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_MY_RESERVE_INVERTER = "com.kiwigrid.devices.solarwatt.MyReserveInverter";

	// PV Plant
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_PVPLANT = "com.kiwigrid.devices.pvplant.PVPlant";
	private static final String PLANT_PRICE_PROFIT = "PriceProfitFeedin";
	// INVERTER_POWER_AC_OUT
	private static final String PLANT_WORK_ANNUAL_YIELD = "WorkAnnualYield";
	private static final String PLANT_POWER_INSTALLED_PEAK = "PowerInstalledPeak";
	// INVERTER_WORK_AC_OUT
	private static final String PLANT_POWER_LIMIT = "PowerLimit";

	// Location
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_LOCATION = "com.kiwigrid.devices.location.Location";
	private static final String LOCATION_POWER_OUT_PRODUCERS = "PowerOutFromProducers";
	private static final String LOCATION_POWER_CONSUMED_FROM_PRODUCERS = "PowerConsumedFromProducers";
	private static final String LOCATION_POWER_IN = "PowerIn";
	private static final String LOCATION_POWER_OUT = "PowerOut";
	private static final String LOCATION_WORK_IN = "WorkIn";
	private static final String LOCATION_WORK_OUT = "WorkOut";
	private static final String LOCATION_WORK_OUT_FROM_STORAGE = "WorkOutFromStorage";
	private static final String LOCATION_POWER_SELF_SUPPLIED = "PowerSelfSupplied";
	private static final String LOCATION_WORK_BUFFERED_FROM_PRODUCER = "WorkBufferedFromProducers";
	private static final String LOCATION_POWER_BUFFERED_FROM_PRODUCER = "PowerBufferedFromProducers";
	private static final String LOCATION_WORK_SELF_CONSUMED = "WorkSelfConsumed";
	private static final String LOCATION_WORK_CONSUMED = "WorkConsumed";
	private static final String LOCATION_POWER_CONSUMED_FROM_GRID = "PowerConsumedFromGrid";
	private static final String LOCATION_POWER_PRODUCED = "PowerProduced";
	private static final String LOCATION_WORK_RELEASED = "WorkReleased";
	private static final String LOCATION_WORK_OUT_FROM_PRODUCER = "WorkOutFromProducers";
	private static final String LOCATION_POWER_CONSUMED = "PowerConsumed";
	private static final String LOCATION_POWER_RELEASED = "PowerReleased";
	private static final String LOCATION_PRICE_WORK_IN = "PriceWorkIn";
	private static final String LOCATION_POWER_BUFFERED = "PowerBuffered";
	private static final String LOCATION_WORK_CONSUMED_FROM_PRODUCERS = "WorkConsumedFromProducers";
	private static final String LOCATION_WORK_PRODUCED = "WorkProduced";
	private static final String LOCATION_WORK_CONSUMED_FROM_GRID = "WorkConsumedFromGrid";
	private static final String LOCATION_POWER_SELF_CONSUMED = "PowerSelfConsumed";
	private static final String LOCATION_WORK_BUFFERED_FROM_GRID = "WorkBufferedFromGrid";
	private static final String LOCATION_WORK_CONSUMED_FROM_STORAGE = "WorkConsumedFromStorage";
	private static final String LOCATION_WORK_BUFFERED = "WorkBuffered";

	// Powermeter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_POWERMETER = "com.kiwigrid.devices.solarwatt.MyReservePowermeter";
	// TODO hier gibts viel mehr
	private final SolarwattMyReserveBatteryImpl parent;
	private final String baseUrl;
	private boolean debugRest;
	private boolean debugSolarwatt;
	private int dropCyclcesCnt = 0;

	private int fetchInterval;

	protected ReadWorker(SolarwattMyReserveBatteryImpl parent, Inet4Address ipAddress, int port, boolean debugRest,
			boolean debugSolarwatt, int fetchInterval) {
		this.parent = parent;
		this.baseUrl = "http://" + ipAddress.getHostAddress() + ":" + port;
		this.debugRest = debugRest;
		this.debugSolarwatt = debugSolarwatt;
		this.fetchInterval = fetchInterval;
	}

	@Override
	protected int getCycleTime() {
		return ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN;
	}

	private JsonObject getDeviceClass(JsonArray items, String deviceClazz) {
		for (int i = 0; i < items.size(); i++) {

			// TODO hier ist wohl noch ein bug drin....

			JsonArray devModels = items.get(i).getAsJsonObject().getAsJsonArray("deviceModel");
			for (int j = 0; j < devModels.size(); j++) {
				JsonElement e = devModels.get(j);
				JsonObject o = e.getAsJsonObject();
				String dc = o.get("deviceClass").getAsString();
				if (dc.compareTo(deviceClazz) == 0) {
					return items.get(i).getAsJsonObject().getAsJsonObject("tagValues");
				}
			}
		}
		return null;
	}

	protected void forever() throws Throwable {

		if ((this.dropCyclcesCnt++ % this.fetchInterval) != 0) {
			return;
		}
		final AtomicBoolean communicationError = new AtomicBoolean(true);

		try {
			JsonObject solarwattResp = this.getResponse(URL_REQ_PATH);

			JsonObject result = JsonUtils.getAsJsonObject(solarwattResp, "result");
			JsonArray items = result.getAsJsonArray("items");

			JsonObject tagValues = this.getDeviceClass(items, DEVICE_CLASS_BAT);
			this.handleBattery(tagValues);

			tagValues = this.getDeviceClass(items, DEVICE_CLASS_KOSTAL_PIKO_INVERTER);
			this.handleKostalPicoInverter(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_MY_RESERVE_INVERTER);
			this.handleMyReserveInverter(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_PVPLANT);
			this.handlePvPlant(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_LOCATION);
			this.handleLocation(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_POWERMETER);
			this.handlePowermeter(tagValues);

			communicationError.set(false);
			if (this.debugSolarwatt) {
				this.log.info("all evaluated");
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
		this.parent._setCommunicationFailed(communicationError.get());
	}

	private void handlePowermeter(JsonObject tagValues) {

		JsonObject workOutObj = tagValues.get(LOCATION_WORK_OUT).getAsJsonObject();
		JsonObject workInObj = tagValues.get(LOCATION_WORK_IN).getAsJsonObject();
		JsonObject powerInObj = tagValues.get(LOCATION_POWER_IN).getAsJsonObject();
		JsonObject powerOutObj = tagValues.get(LOCATION_POWER_OUT).getAsJsonObject();

		// TODO here are a lot more values

		Float powerIn = this.getFloat(powerInObj);
		Float powerOut = this.getFloat(powerOutObj);
		Float workIn = this.getFloat(workInObj);
		Float workOut = this.getFloat(workOutObj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nPowermeter:" + "\npowerIn       :" + powerIn + "\npowerOut      :" + powerOut
					+ "\nworkIn        :" + workIn + "\nworkOut       :" + workOut);
		}

	}

	private void handleLocation(JsonObject tagValues) {

		JsonObject powerOutProducerObj = tagValues.get(LOCATION_POWER_OUT_PRODUCERS).getAsJsonObject();
		JsonObject powerConsumedFromProducersObj = tagValues.get(LOCATION_POWER_CONSUMED_FROM_PRODUCERS)
				.getAsJsonObject();
		JsonObject powerInObj = tagValues.get(LOCATION_POWER_IN).getAsJsonObject();
		JsonObject powerOutObj = tagValues.get(LOCATION_POWER_OUT).getAsJsonObject();
		JsonObject workInObj = tagValues.get(LOCATION_WORK_IN).getAsJsonObject();
		JsonObject workOutObj = tagValues.get(LOCATION_WORK_OUT).getAsJsonObject();
		JsonObject workOutFromStorageObj = tagValues.get(LOCATION_WORK_OUT_FROM_STORAGE).getAsJsonObject();
		JsonObject workBufferedFromProducerObj = tagValues.get(LOCATION_WORK_BUFFERED_FROM_PRODUCER).getAsJsonObject();
		JsonObject powerSelfSuppliedObj = tagValues.get(LOCATION_POWER_SELF_SUPPLIED).getAsJsonObject();
		JsonObject powerBufferedFromProducerObj = tagValues.get(LOCATION_POWER_BUFFERED_FROM_PRODUCER)
				.getAsJsonObject();
		JsonObject workSelfConsumedObj = tagValues.get(LOCATION_WORK_SELF_CONSUMED).getAsJsonObject();
		JsonObject workConsumedObj = tagValues.get(LOCATION_WORK_CONSUMED).getAsJsonObject();
		JsonObject powerConsumedFromGridObj = tagValues.get(LOCATION_POWER_CONSUMED_FROM_GRID).getAsJsonObject();
		JsonObject powerProducedObj = tagValues.get(LOCATION_POWER_PRODUCED).getAsJsonObject();
		JsonObject workReleasedObj = tagValues.get(LOCATION_WORK_RELEASED).getAsJsonObject();
		JsonObject workOutFromProducerObj = tagValues.get(LOCATION_WORK_OUT_FROM_PRODUCER).getAsJsonObject();
		JsonObject powerConsumedObj = tagValues.get(LOCATION_POWER_CONSUMED).getAsJsonObject();
		JsonObject powerReleasedObj = tagValues.get(LOCATION_POWER_RELEASED).getAsJsonObject();
		JsonObject privceWorkInObj = tagValues.get(LOCATION_PRICE_WORK_IN).getAsJsonObject();
		JsonObject powerBufferedObj = tagValues.get(LOCATION_POWER_BUFFERED).getAsJsonObject();
		JsonObject workConsumedFromProducerObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_PRODUCERS).getAsJsonObject();
		JsonObject workProducedObj = tagValues.get(LOCATION_WORK_PRODUCED).getAsJsonObject();
		JsonObject workConsumedFromGridObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_GRID).getAsJsonObject();
		JsonObject powerSelfConsumedObj = tagValues.get(LOCATION_POWER_SELF_CONSUMED).getAsJsonObject();
		JsonObject workBufferedFromGridObj = tagValues.get(LOCATION_WORK_BUFFERED_FROM_GRID).getAsJsonObject();
		JsonObject workConsumedFromStorageObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_STORAGE).getAsJsonObject();
		JsonObject workBufferedObj = tagValues.get(LOCATION_WORK_BUFFERED).getAsJsonObject();
	
		Float powerIn = this.getFloat(powerInObj);
		Float powerOut = this.getFloat(powerOutObj);
		
		if (powerIn > powerOut) {
			// get-from-grid
			this.parent._setActivePowerGrid(Math.round(powerIn));
		} else {
			// feed-to-grid
			this.parent._setActivePowerGrid(-1 * Math.round(powerOut));
		}
		
		Float powerOutProducer = this.getFloat(powerOutProducerObj);
		Float powerConsumedFromProducers = this.getFloat(powerConsumedFromProducersObj);
		Float workIn = this.getFloat(workInObj);
		Float workOut = this.getFloat(workOutObj);
		Float workOutFromStorage = this.getFloat(workOutFromStorageObj);
		Float workBufferedFromProducer = this.getFloat(workBufferedFromProducerObj);
		Float powerSelfSupplied = this.getFloat(powerSelfSuppliedObj);
		Float powerBufferedFromProducer = this.getFloat(powerBufferedFromProducerObj);
		Float workSelfConsumed = this.getFloat(workSelfConsumedObj);
		Float workConsumed = this.getFloat(workConsumedObj);
		Float powerConsumedFromGrid = this.getFloat(powerConsumedFromGridObj);
		Float powerProduced = this.getFloat(powerProducedObj);
		Float workReleased = this.getFloat(workReleasedObj);
		Float workOutFromProducer = this.getFloat(workOutFromProducerObj);
		Float powerConsumed = this.getFloat(powerConsumedObj);
		Float powerReleased = this.getFloat(powerReleasedObj);
		String privceWorkIn = this.getString(privceWorkInObj);
		Float powerBuffered = this.getFloat(powerBufferedObj);
		Float workConsumedFromProducer = this.getFloat(workConsumedFromProducerObj);
		Float workProduced = this.getFloat(workProducedObj);
		Float workConsumedFromGrid = this.getFloat(workConsumedFromGridObj);
		Float powerSelfConsumed = this.getFloat(powerSelfConsumedObj);
		Float workBufferedFromGrid = this.getFloat(workBufferedFromGridObj);
		Float workConsumedFromStorage = this.getFloat(workConsumedFromStorageObj);
		Float workBuffered = this.getFloat(workBufferedObj);

		
		// TODO it is probably not workout
		this.parent._setActivePowerHouse(powerConsumed.intValue());
		this.parent._setActiveConsumptionEnergyHouse(workConsumed.longValue());
		this.parent._setActiveProductionEnergyGrid(workOut.longValue());
		this.parent._setActiveConsumptionEnergyGrid(workConsumedFromGrid.longValue());
		this.parent._setActiveProductionEnergyHouse(0L);
		

		if (this.debugSolarwatt) {
			this.log.info("\n---\nLocation:" + "\npowerOutProducer          :" + powerOutProducer
					+ "\npowerConsumedFromProducers:" + powerConsumedFromProducers + "\npowerIn                   :"
					+ powerIn + "\npowerOut                  :" + powerOut + "\nworkIn                    :" + workIn
					+ "\nworkOut                   :" + workOut + "\nworkOutFromStorage        :" + workOutFromStorage
					+ "\nworkBufferedFromProducer  :" + workBufferedFromProducer + "\npowerSelfSupplied         :"
					+ powerSelfSupplied + "\npowerBufferedFromProducer :" + powerBufferedFromProducer
					+ "\nworkSelfConsumed          :" + workSelfConsumed + "\nworkConsumed              :"
					+ workConsumed + "\npowerConsumedFromGrid     :" + powerConsumedFromGrid
					+ "\npowerProduced             :" + powerProduced + "\nworkReleased              :" + workReleased
					+ "\nworkOutFromProducer       :" + workOutFromProducer + "\npowerConsumed             :"
					+ powerConsumed + "\npowerReleased             :" + powerReleased + "\nprivceWorkIn              :"
					+ privceWorkIn + "\npowerBuffered             :" + powerBuffered + "\nworkConsumedFromProducer  :"
					+ workConsumedFromProducer + "\nworkProduced              :" + workProduced
					+ "\nworkConsumedFromGrid      :" + workConsumedFromGrid + "\npowerSelfConsumed         :"
					+ powerSelfConsumed + "\nworkBufferedFromGrid      :" + workBufferedFromGrid
					+ "\nworkConsumedFromStorage   :" + workConsumedFromStorage + "\nworkBuffered              :"
					+ workBuffered);
		}

	}

	private void handlePvPlant(JsonObject tagValues) {

		JsonObject stateObj = tagValues.get(PLANT_PRICE_PROFIT).getAsJsonObject();
		JsonObject powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		JsonObject workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();
		JsonObject workAnnualYieldObj = tagValues.get(PLANT_WORK_ANNUAL_YIELD).getAsJsonObject();
		JsonObject powerInstalledPeakObj = tagValues.get(PLANT_POWER_INSTALLED_PEAK).getAsJsonObject();
		JsonObject powerLimitObj = tagValues.get(PLANT_POWER_LIMIT).getAsJsonObject();

		String state = this.getString(stateObj);
		Float powerAcOut = this.getFloat(powerAcOutObj);
		Float workAcOut = this.getFloat(workAcOutObj);

		Float workAnnualYield = this.getFloat(workAnnualYieldObj);
		Float powerInstalledPeak = this.getFloat(powerInstalledPeakObj);
		Float powerLimit = this.getFloat(powerLimitObj);

		this.parent._setActivePowerInverter(powerAcOut.intValue());
		this.parent._setActiveProductionEnergyInverter(workAcOut.longValue());
		this.parent._setActiveConsumptionEnergyInverter(0L);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nPV Plant:" + "\nState              : " + state + "\npowerAcOut         : "
					+ powerAcOut + "\nworkAcOut          : " + workAcOut + "\nworkAnnualYield    : " + workAnnualYield
					+ "\npowerInstalledPeak : " + powerInstalledPeak + "\npowerLimit         : " + powerLimit);

		}
	}

	private void handleMyReserveInverter(JsonObject tagValues) {

		JsonObject stateObj = tagValues.get(INVERTER_STATE).getAsJsonObject();

		JsonObject powerAcOutMaxObj = tagValues.get(INVERTER_POWER_AC_OUT_MAX).getAsJsonObject();
		JsonObject powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		JsonObject powerAcOutLimitObj = tagValues.get(INVERTER_POWER_AC_OUT_LIMIT).getAsJsonObject();
		JsonObject workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();

		String state = this.getString(stateObj);

		Float powerAcOutMax = this.getFloat(powerAcOutMaxObj);
		Float powerAcOut = this.getFloat(powerAcOutObj);
		Float powerAcOutLimit = this.getFloat(powerAcOutLimitObj);
		Float workAcOut = this.getFloat(workAcOutObj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nMy Reserve Inverter:" + "\nState          : " + state + "\npowerAcOut     : "
					+ powerAcOut + "\npowerAcOutLimit: " + powerAcOutLimit + "\npowerAcOutMax  : " + powerAcOutMax
					+ "\nworkAcOut      : " + workAcOut);
		}

	}

	private void handleKostalPicoInverter(JsonObject tagValues) {

		JsonObject acCur1Obj = tagValues.get(INVERTER_AC_CURRENT_L1).getAsJsonObject();
		JsonObject acCur2Obj = tagValues.get(INVERTER_AC_CURRENT_L2).getAsJsonObject();
		JsonObject acCur3Obj = tagValues.get(INVERTER_AC_CURRENT_L3).getAsJsonObject();

		JsonObject activPower1Obj = tagValues.get(INVERTER_ACTIVE_POWER_L1).getAsJsonObject();
		JsonObject activPower2Obj = tagValues.get(INVERTER_ACTIVE_POWER_L2).getAsJsonObject();
		JsonObject activPower3Obj = tagValues.get(INVERTER_ACTIVE_POWER_L3).getAsJsonObject();

		JsonObject acVolt1Obj = tagValues.get(INVERTER_AC_VOLTAGE_L1).getAsJsonObject();
		JsonObject acVolt2Obj = tagValues.get(INVERTER_AC_VOLTAGE_L2).getAsJsonObject();
		JsonObject acVolt3Obj = tagValues.get(INVERTER_AC_VOLTAGE_L3).getAsJsonObject();

		JsonObject stateObj = tagValues.get(INVERTER_STATE).getAsJsonObject();

		JsonObject powerYieldSumObj = tagValues.get(INVERTER_POWER_YIELD_SUM).getAsJsonObject();
		JsonObject powerAcOutMaxObj = tagValues.get(INVERTER_POWER_AC_OUT_MAX).getAsJsonObject();
		JsonObject powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		JsonObject powerAcOutLimitObj = tagValues.get(INVERTER_POWER_AC_OUT_LIMIT).getAsJsonObject();
		JsonObject workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();

		Float acCur1 = this.getFloat(acCur1Obj);
		Float acCur2 = this.getFloat(acCur2Obj);
		Float acCur3 = this.getFloat(acCur3Obj);

		Float activePower1 = this.getFloat(activPower1Obj);
		Float activePower2 = this.getFloat(activPower2Obj);
		Float activePower3 = this.getFloat(activPower3Obj);

		Float acVolt1 = this.getFloat(acVolt1Obj);
		Float acVolt2 = this.getFloat(acVolt2Obj);
		Float acVolt3 = this.getFloat(acVolt3Obj);

		String state = this.getString(stateObj);

		Float powerYieldSum = this.getFloat(powerYieldSumObj);
		Float powerAcOutMax = this.getFloat(powerAcOutMaxObj);
		Float powerAcOut = this.getFloat(powerAcOutObj);
		Float powerAcOutLimit = this.getFloat(powerAcOutLimitObj);
		Float workAcOut = this.getFloat(workAcOutObj);

		/*
		 * system state 0 = ok 1 = other 2 = not available
		 */
		if (state == null) {
			this.parent._setInverterSystemState(2);
		} else if (state.compareTo("OK") == 0) {
			this.parent._setInverterSystemState(0);
		} else {
			this.parent._setInverterSystemState(1);
		}

		// TODO provide current and voltage channels for each phase for better metering

		// TODO this is inverter power, we need this for grid meter ?

		if (this.debugSolarwatt) {
			this.log.info("\n---\nKostal Piko Inverter:" + "\nacVolt1        : " + acVolt1 + "\nacVolt2        : "
					+ acVolt2 + "\nacVolt3        : " + acVolt3 + "\nacCur1         : " + acCur1 + "\nacCur2         : "
					+ acCur2 + "\nacCur3         : " + acCur3 + "\nState          : " + state + "\nactivePower1   : "
					+ activePower1 + "\nactivePower2   : " + activePower2 + "\nactivePower3   : " + activePower3
					+ "\npowerYieldSum1 : " + powerYieldSum + "\npowerAcOut     : " + powerAcOut + "\npowerAcOutLimit: "
					+ powerAcOutLimit + "\npowerAcOutMax  : " + powerAcOutMax + "\nworkAcOut      : " + workAcOut);
		}

	}

	private void handleBattery(JsonObject tagValues) {
		JsonObject sohObj = tagValues.get(BAT_SOH).getAsJsonObject();
		JsonObject socObj = tagValues.get(BAT_SOC).getAsJsonObject();

		JsonObject batCurInObj = tagValues.get(BAT_CURRENT_IN).getAsJsonObject();
		JsonObject batCurOutObj = tagValues.get(BAT_CURRENT_OUT).getAsJsonObject();
		JsonObject batStateObj = tagValues.get(BAT_STATE).getAsJsonObject();
		JsonObject batPowerInObj = tagValues.get(BAT_POWER_IN).getAsJsonObject();
		JsonObject batPowerOutObj = tagValues.get(BAT_POWER_OUT).getAsJsonObject();
		JsonObject batWorkInObj = tagValues.get(BAT_WORK_IN).getAsJsonObject();
		JsonObject batWorkOutObj = tagValues.get(BAT_WORK_OUT).getAsJsonObject();
		JsonObject batVoltCellMinObj = tagValues.get(BAT_VOLT_CELL_MIN).getAsJsonObject();
		JsonObject batVoltCellMaxObj = tagValues.get(BAT_VOLT_CELL_MAX).getAsJsonObject();
		JsonObject batVoltCellMeanObj = tagValues.get(BAT_VOLT_CELL_MEAN).getAsJsonObject();

		JsonObject batVoltStringObj = tagValues.get(BAT_VOLT_CELL_STRING).getAsJsonObject();

		JsonObject batTempCellMinObj = tagValues.get(BAT_TEMP_CELL_MIN).getAsJsonObject();
		JsonObject batTempCellMaxObj = tagValues.get(BAT_TEMP_CELL_MAX).getAsJsonObject();

		Float batSoh = this.getFloat(sohObj);
		Float batSoc = this.getFloat(socObj);

		Float batCIn = this.getFloat(batCurInObj);
		Float batCurOut = this.getFloat(batCurOutObj);

		String batState = this.getString(batStateObj);
		Float batPowerIn = this.getFloat(batPowerInObj);
		Float batPowerOut = this.getFloat(batPowerOutObj);
		Float batWorkIn = this.getFloat(batWorkInObj);
		Float batWorkOut = this.getFloat(batWorkOutObj);

		Float batVoltCellMin = this.getFloat(batVoltCellMinObj);
		Float batVoltCellMax = this.getFloat(batVoltCellMaxObj);
		Float batVoltCellMean = this.getFloat(batVoltCellMeanObj);

		Float batVoltString = this.getFloat(batVoltStringObj);

		Float batTempCellMin = this.getFloat(batTempCellMinObj);
		Float batTempCellMax = this.getFloat(batTempCellMaxObj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nBatteryConverter:" + "\nSOH         : " + batSoh + "\nSOC         : " + batSoc
					+ "\nCurrentIn   : " + batCIn + "\nCurrentOut  : " + batCurOut + "\nState       : " + batState
					+ "\nPowerIn     : " + batPowerIn + "\nPowerOut    : " + batPowerOut + "\nWorkIn      : "
					+ batWorkIn + "\nWorkOut     : " + batWorkOut + "\nVoltCellMin : " + batVoltCellMin
					+ "\nVoltCellMax : " + batVoltCellMax + "\nVoltCellMean: " + batVoltCellMean + "\nVoltString  : "
					+ batVoltString + "\nTempCellMin : " + batTempCellMin + "\nTempCellMax : " + batTempCellMax

			);
		}

		this.parent._setSoc(Math.round(batSoc));
		this.parent._setSoh(Math.round(batSoh));
		/**
		 * system state 0 = ok 1 = other 2 = not available
		 */
		if (batState == null) {
			this.parent._setBatterySystemState(2);
		} else if (batState.compareTo("OK") == 0) {
			this.parent._setBatterySystemState(0);
		} else {
			this.parent._setBatterySystemState(1);
		}

		this.parent._setMinCellVoltage(Math.round(batVoltCellMin * 1000f));
		this.parent._setMaxCellVoltage(Math.round(batVoltCellMax * 1000f));

		this.parent._setMinCellTemperature(Math.round(batTempCellMin));
		this.parent._setMaxCellTemperature(Math.round(batTempCellMax));

		// TODO battery charging, discharging

		if (batPowerIn > batPowerOut) {
			// charge
			this.parent._setBatteryCharging(true);
			// Range: negative values for Charge; positive for Discharge
			this.parent._setActivePower(batPowerIn.intValue() * -1);
		} else {
			// discharge
			this.parent._setBatteryCharging(false);
			// Range: negative values for Charge; positive for Discharge
			this.parent._setActivePower(batPowerOut.intValue());

		}
		this.parent._setReactivePower(0);

		this.parent._setActiveChargeEnergy(Math.round(batWorkIn));
		this.parent._setActiveDischargeEnergy(Math.round(batWorkOut));

		// current
		if (batCIn > batCurOut) {
			int mA = (int) (batCIn * 1000.0);
			this.parent._setBatteryCurrent(mA);
			this.parent._setBatteryCharging(true);
		} else {
			int mA = (int) (batCurOut * 1000.0);
			this.parent._setBatteryCurrent(mA);
			this.parent._setBatteryCharging(true);
		}

		// voltage
		int mV = (int) (batVoltString * 1000.0);
		this.parent._setBatteryVoltage(mV);
	}

	private Float getFloat(JsonObject obj) {
		try {
			return obj.get("value").getAsFloat();
		} catch (Exception e) {
			this.log.error("Invalid element " + obj.toString());
			return 0.0f;
		}
	}

	private String getString(JsonObject obj) {
		return obj.get("value").getAsString();
	}

	/**
	 * Gets the JSON response of a HTTPS GET Request.
	 * 
	 * @param path the api path
	 * @return the JsonObject
	 * @throws OpenemsNamedException on error
	 */
	private JsonObject getResponse(String path) throws OpenemsNamedException {
		try {
			URL url = new URL(this.baseUrl + path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(false);
			conn.setDoInput(true);

			// read result from webserver
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String content = reader.lines().collect(Collectors.joining());
				if (this.debugRest) {
					this.log.info(this.baseUrl + " Response: " + content);
				}
				return JsonUtils.parseToJsonObject(content);
			}
		} catch (IOException e) {
			throw new OpenemsException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

}
