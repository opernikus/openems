package io.openems.edge.ess.sennec.home;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.utils.JsonUtils;
import io.openems.common.worker.AbstractWorker;
import io.openems.edge.common.channel.value.Value;

public class ReadWorker extends AbstractWorker {
	private final Logger log = LoggerFactory.getLogger(ReadWorker.class);

	private static final String URL_REQ_PATH = "/lala.cgi";

	// https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.senechome
	// V2.0, V2.1, V3.0

	//	https://github.com/openhab/openhab-addons/tree/beb76810d86b3773ee0fb4d0a1d01d79a7e3ad72/bundles/org.openhab.binding.senechome/src/main/java/org/openhab/binding/senechome/internal/json
	//	https://github.com/openhab/openhab-addons/blob/beb76810d86b3773ee0fb4d0a1d01d79a7e3ad72/bundles/org.openhab.binding.senechome/src/main/java/org/openhab/binding/senechome/internal/json/SenecHomeEnergy.java
	//	https://github.com/openhab/openhab-addons/tree/887c1acea80c3c336e3a7385fad0c3b2011381ec/bundles/org.openhab.binding.senechome

	// read energy and pv
	//	private final String POST_REQ_PARAMETER 	=  "{\"PV1\":{\"POWER_RATIO\":\"\"},{\"ENERGY\":{\"STAT_STATE\":\"\",\"STAT_STATE_DECODE\":\"\",\"GUI_BAT_DATA_VOLTAGE\":\"\",\"GUI_BAT_DATA_CURRENT\":\"\",\"STAT_SULFAT_CHRG_COUNTER\":\"\",\"GUI_TEST_CHARGE_STAT\":\"\",\"TEST_CHARGE_CHRG_START\":\"\",\"TEST_CHARGE_CHRG_STOP\":\"\",\"GUI_TEST_DISCHARGE_STAT\":\"\",\"TEST_CHARGE_DIS_START\":\"\",\"TEST_CHARGE_DIS_STOP\":\"\",\"GUI_CAP_TEST_START\":\"\",\"GUI_CAP_TEST_STOP\":\"\",\"GUI_CAP_TEST_STATE\":\"\",\"GRID_POWER_OFFSET\":\"\",\"GUI_FACTORY_TEST_PUMP\":\"\",\"GUI_FACTORY_TEST_FAN\":\"\",\"TEST_CYCLE\":\"\"},\"SOCKETS\":{\"FORCE_ON\":\"\"},\"SYS_UPDATE\":{\"BOOT_REPORT_SUCCESS\":\"\",\"USER_REBOOT_DEVICE\":\"\",\"FEATURE_OVERWRITE\":\"\"}}}";
	// der hier geht mit Sennec Home V2
	//	private final String POST_REQ_PARAMETER 	=  "{\"ENERGY\":{\"STAT_STATE\":\"\",\"STAT_STATE_DECODE\":\"\",\"GUI_BAT_DATA_VOLTAGE\":\"\",\"GUI_BAT_DATA_CURRENT\":\"\",\"STAT_SULFAT_CHRG_COUNTER\":\"\",\"GUI_TEST_CHARGE_STAT\":\"\",\"TEST_CHARGE_CHRG_START\":\"\",\"TEST_CHARGE_CHRG_STOP\":\"\",\"GUI_TEST_DISCHARGE_STAT\":\"\",\"TEST_CHARGE_DIS_START\":\"\",\"TEST_CHARGE_DIS_STOP\":\"\",\"GUI_CAP_TEST_START\":\"\",\"GUI_CAP_TEST_STOP\":\"\",\"GUI_CAP_TEST_STATE\":\"\",\"GRID_POWER_OFFSET\":\"\",\"GUI_FACTORY_TEST_PUMP\":\"\",\"GUI_FACTORY_TEST_FAN\":\"\",\"TEST_CYCLE\":\"\"},\"SOCKETS\":{\"FORCE_ON\":\"\"},\"SYS_UPDATE\":{\"BOOT_REPORT_SUCCESS\":\"\",\"USER_REBOOT_DEVICE\":\"\",\"FEATURE_OVERWRITE\":\"\"}}";

	// channel ENERGY relevant options
	// documentation taken from https://github.com/nobl/ioBroker.senec

	@SuppressWarnings("unused")
	private static final String POLL_ALL = "{\"STATISTIC\":{},\"ENERGY\":{},\"FEATURES\":{},"
			+ "\"LOG\":{},\"SYS_UPDATE\":{},\"WIZARD\":{}," + "\"BMS\":{},\"BAT1\":{},\"BAT1OBJ1\":{},"
			+ "\"BAT1OBJ2\":{},\"BAT1OBJ2\":{},\"BAT1OBJ3\":{},\"BAT1OBJ4\":{},"
			+ "\"PWR_UNIT\":{},\"PV1\":{},\"FACTORY\":{},\"GRIDCONFIG\":{}}";

	// ---------------------------------------------------------------------------------------
	// ENERGY
	// -------------------------------------------------------------------------------

	/** read only, float, [W], house power - power used by house. */
	private static final String ENERGY_HOUSE_POWER = "GUI_HOUSE_POW";

	/** read only, float, [W], inverter power. */
	private static final String ENERGY_INVERTER_POWER = "GUI_INVERTER_POWER";

	/**
	 * read only, float, [W], battery power, pos Values = charging, neg. Values =
	 * discharging.
	 */
	private static final String ENERGY_BAT_POWER = "GUI_BAT_DATA_POWER";

	/** read only, float, [%] battery SOC. */
	private static final String ENERGY_BAT_SOC = "GUI_BAT_DATA_FUEL_CHARGE";

	/** read only, int, represents the system state. */
	private static final String ENERGY_SYSTEM_STATE = "STAT_STATE";

	/** read only, float, [V], battery voltage. */
	private static final String ENERGY_BAT_VOLTAGE = "GUI_BAT_DATA_VOLTAGE";

	/** read only, float, [A], battery current. */
	private static final String ENERGY_BAT_CURRENT = "GUI_BAT_DATA_CURRENT";

	/**
	 * read only, float, [W], grid power, pos Values coming from grid, neg Values
	 * going to grid.
	 */
	private static final String ENERGY_GRID_POWER = "GUI_GRID_POW";

	/** read only, bool, is the battery currently charging. */
	private static final String ENERGY_BAT_CHARGING_INFO = "GUI_CHARGING_INFO";

	/** read only, int, [h], read only, operation hours - system uptime. */
	private static final String ENERGY_HOURS_OF_OPERATION = "STAT_HOURS_OF_OPERATION";

	// ---------------------------------------------------------------------------------------
	// BMS
	// -------------------------------------------------------------------------------

	// channel BMS relevant options
	// documentation taken from
	// https://github.com/nobl/ioBroker.senec/blob/master/docs/de/README.md
	/** read only, int[], [%], battery SOC for each battery pack. */
	private static final String BMS_SOC = "SOC";
	/** read only, int[], [%], battery SOH for each battery pack. */
	private static final String BMS_SOH = "SOH";
	/** read only, int[], [%], max temperature for each battery pack. */
	private static final String BMS_TEMP_MAX = "TEMP_MAX";
	/** read only, int[], [%], min temperature for each battery pack. */
	private static final String BMS_TEMP_MIN = "TEMP_MIN";
	/** read only, int[], [V], max voltage for each battery pack. */
	private static final String BMS_VOLT_MAX = "MAX_CELL_VOLTAGE";
	/** read only, int[], [V], min voltage for each battery pack. */
	private static final String BMS_VOLT_MIN = "MIN_CELL_VOLTAGE";
	/** read only, int[], status for each battery pack. */
	private static final String BMS_STATUS = "STATUS";

	// ---------------------------------------------------------------------------------------
	// GRID
	// ---------------------------------------------------------------------------------

	/** read only, int, [kW], > 0 power from grid, < 0 power to grid. */
	private static final String GRID_TOTAL_POWER = "P_TOTAL";

	/** read only, int[], [], voltages for each phase. */
	private static final String GRID_VOLTAGES = "U_AC";

	/** read only, int[], [], current for each phase. */
	private static final String GRID_CURRENTS = "I_AC";

	/**
	 * read only, int[], [], power for each phase, > 0 power from grid, < 0 power to
	 * grid.
	 */
	private static final String GRID_POWER = "P_AC";

	/** read only, int, [Hz], frequency. */
	private static final String GRID_FREQUENCY = "FREQ";

	// ---------------------------------------------------------------------------------------
	// STATISTICS
	// ---------------------------------------------------------------------------

	/** read only, int, [Wh], amount of power charged into the battery. */
	private static final String STAT_BAT_CHARGE_ENERGY = "LIVE_BAT_CHARGE";
	/** read only, int, [Wh], amount of power discharged from the battery. */
	private static final String STAT_BAT_DISCHARGE_ENERGY = "LIVE_BAT_DISCHARGE";
	/** read only, int, [Wh], amount of power delivered to grid today. */

	private static final String STAT_GRID_EXPORT = "LIVE_GRID_EXPORT";
	/** read only, int, [Wh], amount of power got from grid today. */
	private static final String STAT_GRID_IMPORT = "LIVE_GRID_IMPORT";

	// ---------------------------------------------------------------------------------------
	// LIMITATION-
	// ---------------------------------------------------------------------------

	private static String ENERGY_REQ_PARAMETER = "{\"ENERGY\":{" + getReqParam(ENERGY_SYSTEM_STATE, true)
			+ getReqParam(ENERGY_BAT_CURRENT, true) + getReqParam(ENERGY_BAT_VOLTAGE, true)
			+ getReqParam(ENERGY_BAT_POWER, true) + getReqParam(ENERGY_BAT_SOC, true)
			+ getReqParam(ENERGY_BAT_CHARGING_INFO, true) + getReqParam(ENERGY_HOUSE_POWER, true)
			+ getReqParam(ENERGY_GRID_POWER, true) + getReqParam(ENERGY_INVERTER_POWER, true)
			+ getReqParam(ENERGY_HOURS_OF_OPERATION, false) + "}" + "}";
	protected static String BMS_REQ_PARAMETER = "{\"BMS\":{" + getReqParam(BMS_SOC, true) + getReqParam(BMS_SOH, true)
			+ getReqParam(BMS_TEMP_MAX, true) + getReqParam(BMS_TEMP_MIN, true) + getReqParam(BMS_VOLT_MAX, true)
			+ getReqParam(BMS_VOLT_MIN, true) + getReqParam(BMS_STATUS, false) + "}" + "}";

	protected static String STAT_REQ_PARAMETER = "{\"STATISTIC\":{" + getReqParam(STAT_BAT_CHARGE_ENERGY, true)
			+ getReqParam(STAT_BAT_DISCHARGE_ENERGY, true) + getReqParam(STAT_GRID_EXPORT, true)
			+ getReqParam(STAT_GRID_IMPORT, false) + "}" + "}";

	protected static String GRID_REQ_PARAMETER = "{\"PM1OBJ1\":{" + getReqParam(GRID_TOTAL_POWER, true)
			+ getReqParam(GRID_VOLTAGES, true) + getReqParam(GRID_CURRENTS, true) + getReqParam(GRID_POWER, true)
			+ getReqParam(GRID_FREQUENCY, false) + "}" + "}";

	private final SennecHomeV3BatteryImpl parent;
	private Config config;
	private final String baseUrl;
	private int debugMode;
	private int maxCycleTime = 0;
	private int dropCyclesCntr = 0;

	protected ReadWorker(SennecHomeV3BatteryImpl parent, Config config) throws UnknownHostException {
		this.config = config;
		Inet4Address ipAddress = (Inet4Address) Inet4Address.getByName(this.config.ipAddress());

		this.parent = parent;
		this.baseUrl = "http://" + ipAddress.getHostAddress() + ":" + config.port();
		this.debugMode = this.config.debugMode();
		this.maxCycleTime = this.config.cycleTime();
		this.dropCyclesCntr = this.maxCycleTime - 1;
	}

	@Override
	protected int getCycleTime() {
		return ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN;
	}

	protected void forever() throws Throwable {

		if ((this.dropCyclesCntr++ % this.maxCycleTime) != 0) {
			return;
		}
		final AtomicBoolean communicationError = new AtomicBoolean(false);
		try {
			// energy

			JsonObject respEnergy = this.getResponse(URL_REQ_PATH, ENERGY_REQ_PARAMETER);
			JsonObject energy = JsonUtils.getAsJsonObject(respEnergy, "ENERGY");

			this.handleBatSystemState(energy, communicationError);
			this.handleBatCurrent(energy, communicationError);
			this.handleBatVoltage(energy, communicationError);
			this.handleBatPower(energy, communicationError);
			this.handleBatCharging(energy, communicationError);
			this.handleHousePower(energy, communicationError);
			this.handleGridPower(energy, communicationError);
			this.handleInverterPower(energy, communicationError);
			this.handleHoursOfOperation(energy, communicationError);

			// BMS

			JsonObject respBms = this.getResponse(URL_REQ_PATH, BMS_REQ_PARAMETER);
			JsonObject bms = JsonUtils.getAsJsonObject(respBms, "BMS");

			int numberOfBatteryModules = this.handleBatStatus(bms, communicationError);
			this.handleBatCellSoc(bms, bms, communicationError, numberOfBatteryModules);
			this.handleBatCellSoh(bms, communicationError, numberOfBatteryModules);

			this.handleBatCellTemp(bms, communicationError, numberOfBatteryModules);
			this.handleBatCellVoltage(bms, communicationError, numberOfBatteryModules);

			// statistics
			JsonObject respStat = this.getResponse(URL_REQ_PATH, STAT_REQ_PARAMETER);
			JsonObject stats = JsonUtils.getAsJsonObject(respStat, "STATISTIC");
			this.handleStatEnergyBat(stats, communicationError);
			this.handleStatEnergyGrid(stats, communicationError);

			// grid
			JsonObject respGrid = this.getResponse(URL_REQ_PATH, GRID_REQ_PARAMETER);
			JsonObject grid = JsonUtils.getAsJsonObject(respGrid, "PM1OBJ1");
			this.handlePm1ObjGridPower(grid, communicationError);
			this.handlePm1ObjGridVoltages(grid, communicationError);
			this.handlePm1ObjGridCurrents(grid, communicationError);
			this.handlePm1ObjGridFrequency(grid, communicationError);

			// TODO all energy values are missing

			if (this.debugMode >= 2) {
				this.log.info("all evaluated comErrorState: " + communicationError.get());
			}

		} catch (OpenemsNamedException e) {
			if (this.debugMode >= 2) {
				this.log.info("Got Exception: " + e.getMessage());
			}
			communicationError.set(true);
		}
		this.parent._setSlaveCommunicationFailed(communicationError.get());
		if (communicationError.get()) {
			this.parent.getStateChannel().setNextValue(true);
		} else {
			this.parent.getStateChannel().setNextValue(false);
		}
	}

	private void handleBatSystemState(JsonObject energy, AtomicBoolean communicationError) {
		try {
			int systemState = this.getSennecFormatAsInteger(energy, ENERGY_SYSTEM_STATE);
			this.parent.setSystemState(systemState);
			if (this.debugMode >= 2) {
				this.log.info("Energy System State: " + systemState);
			}
		} catch (Exception e) {
			this.parent.setSystemState(null);
			communicationError.set(true);
		}
	}

	private void handleBatCurrent(JsonObject energy, AtomicBoolean communicationError) {
		try {
			float batCurrent = this.getSennecFormatAsFloat(energy, ENERGY_BAT_CURRENT);
			int mA = (int) (batCurrent * 1000.0);
			this.parent.setCurrent(mA);
			if (this.debugMode >= 2) {
				this.log.info("Energy Bat Current  " + mA + " mA ");
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setCurrent(null);
		}
	}

	private void handleBatVoltage(JsonObject energy, AtomicBoolean communicationError) {
		try {
			float batVoltage = this.getSennecFormatAsFloat(energy, ENERGY_BAT_VOLTAGE);
			int mV = (int) (batVoltage * 1000.0);
			this.parent.setVoltage(mV);
			if (this.debugMode >= 2) {
				this.log.info("Energy Bat Voltage Raw: " + mV + " mV");
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setVoltage(null);
		}
	}

	private void handleBatPower(JsonObject energy, AtomicBoolean communicationError) {
		try {
			// pos Values = charging, neg. Values = discharging
			float batPower = this.getSennecFormatAsFloat(energy, ENERGY_BAT_POWER);

			// convert to OpenEMS <-> negative values for Charge; positive for Discharge
			batPower *= -1.0;

			if (this.debugMode >= 2) {
				this.log.info("Energy Bat Power: " + batPower);
			}

			// TODO fix reactive Power
			float reactivePower = batPower;

			switch (this.parent.getPhase()) {
			case L1:
				this.parent._setActivePowerL1(Math.round(batPower));
				this.parent._setActivePowerL2(0);
				this.parent._setActivePowerL3(0);
				this.parent._setReactivePowerL1(Math.round(reactivePower));
				this.parent._setReactivePowerL2(0);
				this.parent._setReactivePowerL3(0);
				break;
			case L2:
				this.parent._setActivePowerL1(0);
				this.parent._setActivePowerL2(Math.round(batPower));
				this.parent._setActivePowerL3(0);
				this.parent._setReactivePowerL1(0);
				this.parent._setReactivePowerL2(Math.round(reactivePower));
				this.parent._setReactivePowerL3(0);
				break;
			case L3:
				this.parent._setActivePowerL1(0);
				this.parent._setActivePowerL2(0);
				this.parent._setActivePowerL3(Math.round(batPower));
				this.parent._setReactivePowerL1(0);
				this.parent._setReactivePowerL2(0);
				this.parent._setReactivePowerL3(Math.round(reactivePower));
				break;
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
	}

	private void handleBatCharging(JsonObject energy, AtomicBoolean communicationError) {
		try {
			boolean batCharging = this.getSennecFormatAsBoolean(energy, ENERGY_BAT_CHARGING_INFO);
			this.parent.setCharging(batCharging);
			if (this.debugMode >= 2) {
				this.log.info("Energy Bat Charging: " + batCharging);
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setCharging(null);
		}
	}

	private void handleHousePower(JsonObject energy, AtomicBoolean communicationError) {
		try {
			float housePower = this.getSennecFormatAsFloat(energy, ENERGY_HOUSE_POWER);
			this.parent.setActivePowerHouse(Math.round(housePower));
			if (this.debugMode >= 2) {
				this.log.info("Energy House Power: " + housePower);
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setActivePowerHouse(null);
		}
	}

	private void handleGridPower(JsonObject energy, AtomicBoolean communicationError) {
		try {
			float gridPower = this.getSennecFormatAsFloat(energy, ENERGY_GRID_POWER);
			this.parent.setActivePowerGrid(Math.round(gridPower));
			if (this.debugMode >= 2) {
				this.log.info("Energy Grid Power: " + gridPower);
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setActivePowerGrid(null);
		}
	}

	private void handleInverterPower(JsonObject energy, AtomicBoolean communicationError) {
		try {
			float inverterPower = this.getSennecFormatAsFloat(energy, ENERGY_INVERTER_POWER);
			this.parent.setActivePowerInverter(Math.round(inverterPower));
			if (this.debugMode >= 2) {
				this.log.info("Energy Inverter Power: " + inverterPower);
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setActivePowerInverter(null);
		}
	}

	private void handleHoursOfOperation(JsonObject energy, AtomicBoolean communicationError) {
		try {
			int hoursOfOperation = this.getSennecFormatAsInteger(energy, ENERGY_HOURS_OF_OPERATION);
			this.parent.setOperationHours(hoursOfOperation);
			if (this.debugMode >= 2) {
				this.log.info("Energy Hours of operation: " + hoursOfOperation);
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setOperationHours(null);
		}
	}

	private int handleBatStatus(JsonObject bms, AtomicBoolean communicationError) throws OpenemsException {
		int numberOfModules = 0;
		try {
			int[] bmsCellSoh = this.getSennecFormatAsIntegerArray(bms, BMS_STATUS);
			for (int i = 0; i < bmsCellSoh.length; i++) {
				if (bmsCellSoh[i] != 0) {
					numberOfModules++;
				}
				this.parent.setBatteryPackageStatus(i + 1, bmsCellSoh[i]);
				if (this.debugMode >= 2) {
					this.log.info("Battery Status of Battery " + (i + 1) + ": " + bmsCellSoh[i]);
				}
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			for (int i = 0; i < 4; i++) {
				this.parent.setBatteryPackageStatus(i + 1, null);
			}
		}
		return numberOfModules;
	}

	private void handleBatCellSoc(JsonObject energy, JsonObject bms, AtomicBoolean communicationError,
			int numberOfBatteryModules) {
		try {
			int[] bmsCellSoc = this.getSennecFormatAsIntegerArray(bms, BMS_SOC);

			int averageSoc = 0;
			for (int i = 0; i < numberOfBatteryModules; i++) {
				averageSoc += bmsCellSoc[i];
				if (this.debugMode >= 2) {
					this.log.info("Battery Cell SOC " + (i + 1) + ": " + bmsCellSoc[i]);
				}
				// TODO store cell SOCs in a channel if needed
			}
			if (numberOfBatteryModules > 0) {
				this.parent._setSoc(Math.round(averageSoc / numberOfBatteryModules));
				if (this.debugMode >= 2) {
					this.log.info("Battery SOC : " + (averageSoc / numberOfBatteryModules));
				}
			} else {
				this.parent._setSoc(null);
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent._setSoc(null);
		}
	}

	private void handleBatCellSoh(JsonObject bms, AtomicBoolean communicationError, int numberOfBatteryModules) {
		try {
			int[] bmsCellSoh = this.getSennecFormatAsIntegerArray(bms, BMS_SOH);
			int averageSoh = 0;
			for (int i = 0; i < numberOfBatteryModules; i++) {
				averageSoh += bmsCellSoh[i];
				if (this.debugMode >= 2) {
					this.log.info("Battery Cell SOH " + (i + 1) + ": " + bmsCellSoh[i]);
				}
				// TODO store cell SOHs in a channel if needed
			}
			if (numberOfBatteryModules > 0) {
				this.parent.setSoh(averageSoh / numberOfBatteryModules);
				if (this.debugMode >= 2) {
					this.log.info("Battery SOH : " + (averageSoh / numberOfBatteryModules));
				}
			} else {
				this.parent.setSoh(null);
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setSoh(null);
		}
	}

	private void handleBatCellTemp(JsonObject bms, AtomicBoolean communicationError, int numberOfBatteryModules) {
		try {
			int[] bmsCellTempMax = this.getSennecFormatAsIntegerArray(bms, BMS_TEMP_MAX);
			int[] bmsCellTempMin = this.getSennecFormatAsIntegerArray(bms, BMS_TEMP_MIN);

			Value<Integer> minTemp = this.parent.getMinCellTemperature();
			int minTemperature = Integer.MAX_VALUE;
			if (minTemp.isDefined()) {
				minTemperature = minTemp.get();
			}

			Value<Integer> maxTemp = this.parent.getMaxCellTemperature();
			int maxTemperature = Integer.MIN_VALUE;
			if (maxTemp.isDefined()) {
				maxTemperature = maxTemp.get();
			}

			for (int i = 0; i < numberOfBatteryModules; i++) {
				if (bmsCellTempMin[i] < minTemperature) {
					minTemperature = bmsCellTempMin[i];
					this.parent._setMinCellTemperature(minTemperature);
				}
				if (bmsCellTempMax[i] > maxTemperature) {
					maxTemperature = bmsCellTempMax[i];
					this.parent._setMaxCellTemperature(maxTemperature);
				}
			}
			if (this.debugMode >= 2) {
				this.log.info("Battery Temp Min: " + minTemperature + " Max: " + maxTemperature);
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			for (int i = 0; i < 4; i++) {
				this.parent._setMinCellTemperature(null);
				this.parent._setMaxCellTemperature(null);
			}
		}
	}

	private void handleBatCellVoltage(JsonObject bms, AtomicBoolean communicationError, int numberOfBatteryModules) {
		try {
			int[] bmsCellVoltMax = this.getSennecFormatAsIntegerArray(bms, BMS_VOLT_MAX);
			int[] bmsCellVoltMin = this.getSennecFormatAsIntegerArray(bms, BMS_VOLT_MIN);

			Value<Integer> minVolt = this.parent.getMinCellVoltage();
			int minVoltage = Integer.MAX_VALUE;
			if (minVolt.isDefined()) {
				minVoltage = minVolt.get();
			}

			Value<Integer> maxVolt = this.parent.getMaxCellVoltage();
			int maxVoltage = Integer.MIN_VALUE;
			if (maxVolt.isDefined()) {
				maxVoltage = maxVolt.get();
			}
			for (int i = 0; i < numberOfBatteryModules; i++) {
				if (bmsCellVoltMin[i] < minVoltage) {
					minVoltage = bmsCellVoltMin[i];
				}
				if (bmsCellVoltMax[i] > maxVoltage) {
					maxVoltage = bmsCellVoltMax[i];
				}
			}
			this.parent._setMinCellVoltage(minVoltage);
			this.parent._setMaxCellVoltage(maxVoltage);
			if (this.debugMode >= 2) {
				this.log.info("Battery Voltage Min: " + minVoltage + "mV  Max: " + maxVoltage + " mV");
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			for (int i = 0; i < 4; i++) {
				this.parent._setMinCellVoltage(null);
				this.parent._setMaxCellVoltage(null);
			}
		}
	}

	private void handleStatEnergyBat(JsonObject stats, AtomicBoolean communicationError) {

		try {
			float chargeEnergy = this.getSennecFormatAsFloat(stats, STAT_BAT_CHARGE_ENERGY);
			float dischargeEnergy = this.getSennecFormatAsFloat(stats, STAT_BAT_DISCHARGE_ENERGY);

			if (this.debugMode >= 2) {
				this.log.info("Stat Battery Charge Energy (daily): " + chargeEnergy);
				this.log.info("Stat Battery Discharge Energy (daily): " + dischargeEnergy);
			}
			// TODO charge energy on a daily base or on an overall base, what if we start
			// again

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
	}

	private void handleStatEnergyGrid(JsonObject stats, AtomicBoolean communicationError) {

		try {
			float gridExport = this.getSennecFormatAsFloat(stats, STAT_GRID_EXPORT);
			float gridImport = this.getSennecFormatAsFloat(stats, STAT_GRID_IMPORT);

			if (this.debugMode >= 2) {
				this.log.info("Stat Grid Export Energy (daily): " + gridExport);
				this.log.info("Stat Grid Import Energy (daily): " + gridImport);
			}
			// TODO this values seems to be daily values

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
	}

	private void handlePm1ObjGridPower(JsonObject stats, AtomicBoolean communicationError) {
		try {
			float gridTotalPower = this.getSennecFormatAsFloat(stats, GRID_TOTAL_POWER);
			float[] gridPhasePowers = this.getSennecFormatAsFloatArray(stats, GRID_POWER, 3);

			this.parent.setGridActivePower((int) gridTotalPower);

			if (gridPhasePowers.length == 3) {
				this.parent.setGridActivePowerL1((int) gridPhasePowers[0]);
				this.parent.setGridActivePowerL2((int) gridPhasePowers[1]);
				this.parent.setGridActivePowerL3((int) gridPhasePowers[2]);
			} else {
				this.parent.setGridActivePowerL1(null);
				this.parent.setGridActivePowerL2(null);
				this.parent.setGridActivePowerL3(null);
			}
			if (this.debugMode >= 2) {
				this.log.info("PM1 Grid Total Power: " + gridTotalPower);
				for (int i = 0; i < gridPhasePowers.length; i++) {
					this.log.info("PM1 Grid Phase " + i + " Power: " + gridPhasePowers[i]);
				}
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setGridActivePowerL1(null);
			this.parent.setGridActivePowerL2(null);
			this.parent.setGridActivePowerL3(null);
		}
	}

	private void handlePm1ObjGridVoltages(JsonObject stats, AtomicBoolean communicationError) {
		try {
			float[] gridVoltages = this.getSennecFormatAsFloatArray(stats, GRID_VOLTAGES, 3);
			if (gridVoltages.length == 3) {
				this.parent.setGridVoltageL1((int) (gridVoltages[0] * 1000f));
				this.parent.setGridVoltageL2((int) (gridVoltages[1] * 1000f));
				this.parent.setGridVoltageL3((int) (gridVoltages[2] * 1000f));
			} else {
				this.parent.setGridVoltageL1(null);
				this.parent.setGridVoltageL2(null);
				this.parent.setGridVoltageL3(null);
			}
			if (this.debugMode >= 2) {
				for (int i = 0; i < gridVoltages.length; i++) {
					this.log.info("PM1 Grid Voltages " + i + ": " + gridVoltages[i]);
				}
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setGridVoltageL1(null);
			this.parent.setGridVoltageL2(null);
			this.parent.setGridVoltageL3(null);
		}
	}

	private void handlePm1ObjGridCurrents(JsonObject stats, AtomicBoolean communicationError) {
		try {
			float[] gridCurrents = this.getSennecFormatAsFloatArray(stats, GRID_CURRENTS, 3);
			if (gridCurrents.length == 3) {
				this.parent.setGridCurrentL1((int) (gridCurrents[0] * 1000f));
				this.parent.setGridCurrentL2((int) (gridCurrents[1] * 1000f));
				this.parent.setGridCurrentL3((int) (gridCurrents[2] * 1000f));
			} else {
				this.parent.setGridCurrentL1(null);
				this.parent.setGridCurrentL2(null);
				this.parent.setGridCurrentL3(null);
			}

			if (this.debugMode >= 2) {
				for (int i = 0; i < gridCurrents.length; i++) {
					this.log.info("PM1 Grid Currents " + i + ": " + gridCurrents[i]);
				}
			}
		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setGridCurrentL1(null);
			this.parent.setGridCurrentL2(null);
			this.parent.setGridCurrentL3(null);
		}
	}

	private void handlePm1ObjGridFrequency(JsonObject stats, AtomicBoolean communicationError) {

		try {
			float gridFreq = this.getSennecFormatAsFloat(stats, GRID_FREQUENCY);
			this.parent.setGridFrequency((int) (gridFreq * 1000f));

			if (this.debugMode >= 2) {
				this.log.info("PM1 Grid Freq: " + this.parent.getGridFrequency().asString());
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
			this.parent.setGridFrequency(null);
		}
	}

	private float[] getSennecFormatAsFloatArray(JsonObject obj, String name, int expextedArraySize)
			throws OpenemsNamedException {
		JsonArray arr = JsonUtils.getAsJsonArray(obj, name);

		if (arr == null || arr.size() != expextedArraySize) {
			throw new OpenemsException("Not a sennec storage with four battery modules for " + name);
		}
		float[] data = new float[expextedArraySize];
		if (expextedArraySize > 0) {
			data[0] = this._parseFloat(arr.get(0).getAsString());
			if (expextedArraySize > 1) {
				data[1] = this._parseFloat(arr.get(1).getAsString());
				if (expextedArraySize > 2) {
					data[2] = this._parseFloat(arr.get(2).getAsString());
					if (expextedArraySize > 3) {
						data[3] = this._parseFloat(arr.get(3).getAsString());
					}
				}
			}
		}
		return data;
	}

	private int[] getSennecFormatAsIntegerArray(JsonObject obj, String name) throws OpenemsNamedException {
		JsonArray arr = JsonUtils.getAsJsonArray(obj, name);

		if (arr == null || arr.size() != 4) {
			throw new OpenemsException("Not a sennec storage with four battery modules for " + name);
		}
		int[] data = new int[4];
		data[0] = this._parseInt(arr.get(0).getAsString());
		data[1] = this._parseInt(arr.get(1).getAsString());
		data[2] = this._parseInt(arr.get(2).getAsString());
		data[3] = this._parseInt(arr.get(3).getAsString());
		return data;
	}

	private int _parseInt(String sennecInt) throws OpenemsNamedException {
		if (sennecInt.startsWith("u1_")) {
			return (int) (Long.parseLong(sennecInt.substring(3), 16));
		} else if (sennecInt.startsWith("u3_")) {
			return (int) (Long.parseLong(sennecInt.substring(3), 16));
		} else if (sennecInt.startsWith("u8_")) {
			return (int) (Long.parseLong(sennecInt.substring(3), 16));
		} else if (sennecInt.startsWith("i8_")) {
			int res = (int) (Long.parseLong(sennecInt.substring(3), 16));
			if ((res & 0x80) == 0x80) {
				return -(res | 0x80);
			}
			return res;
		} else {
			throw new OpenemsException("Unable to parse value for " + sennecInt + " as an integerarray");
		}
	}

	private float _parseFloat(String sennecFloat) throws OpenemsNamedException {

		if (sennecFloat.startsWith("fl_")) {
			int intval = (int) (Long.parseLong(sennecFloat.substring(3), 16));
			return Float.intBitsToFloat(intval);

		} else {
			throw new OpenemsException("Unable to parse value for " + sennecFloat + " as a floatarray");
		}
	}

	private int getSennecFormatAsInteger(JsonObject obj, String name) throws OpenemsNamedException {
		String str = JsonUtils.getAsString(obj, name);
		if (str.startsWith("u8_")) {
			return (int) (Long.parseLong(str.substring(3), 16));
		} else if (str.startsWith("u3_")) {
			return (int) (Long.parseLong(str.substring(3), 16));

		} else {
			throw new OpenemsException("Unable to parse value for " + name + " as an integer: " + str);
		}
	}

	private boolean getSennecFormatAsBoolean(JsonObject obj, String name) throws OpenemsNamedException {
		int ch = this.getSennecFormatAsInteger(obj, name);
		if (ch == 1) {
			return true;
		} else if (ch == 0) {
			return false;
		}
		throw new OpenemsException("Unable to parse boolean value for " + name + " as an integer: " + ch);
	}

	private float getSennecFormatAsFloat(JsonObject obj, String name) throws OpenemsNamedException {
		String str = JsonUtils.getAsString(obj, name);
		if (str.startsWith("fl_")) {
			int intval = (int) (Long.parseLong(str.substring(3), 16));
			return Float.intBitsToFloat(intval);

		} else {
			throw new OpenemsException("Unable to parse value for " + name + " as a float: " + str);
		}
	}

	/**
	 * Gets the JSON response of a HTTPS GET Request.
	 * 
	 * @param path the api path
	 * @param postData the post data
	 * @return the JsonObject
	 * @throws OpenemsNamedException on error
	 */
	private JsonObject getResponse(String path, String postData) throws OpenemsNamedException {
		try {
			URL url = new URL(this.baseUrl + path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// write post data to stream
			byte[] input = postData.getBytes("utf-8");
			try (OutputStream os = conn.getOutputStream()) {
				os.write(input, 0, input.length);
				os.flush();
			}

			// read result from webserver
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String content = reader.lines().collect(Collectors.joining());
				if (this.debugMode == 1 || this.debugMode == 3) {
					this.log.info(this.baseUrl + " Response: " + content);
				}
				return JsonUtils.parseToJsonObject(content);
			}
		} catch (IOException e) {
			throw new OpenemsException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private static String getReqParam(String param, boolean followUpParameter) {
		String resp = "\"" + param + "\":\"\"";
		if (followUpParameter) {
			resp += ",";
		}
		return resp;
	}

}
