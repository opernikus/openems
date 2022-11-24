package io.openems.edge.heatpump.buderus.logaplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.utils.JsonUtils;
import io.openems.common.worker.AbstractWorker;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.heatpump.buderus.logaplus.data.Debug;

public class ReadWorker extends AbstractWorker {

	private final String privateKey;

	private final Logger log = LoggerFactory.getLogger(ReadWorker.class);

	public static final int DROP_CYCLES = 300;

	private final BuderusLogaplusImpl parent;
	private final String baseUrl;

	/** 0 = none, 1 = values, 2 = values and REST. */
	private Debug debug;
	private int dropCyclcesCnt = 0;
	private int dropCycles;

	private Optional<Float> outdoorTmp;
	private Optional<Float> supplyTmp;
	private Optional<Float> returnTmp;
	@SuppressWarnings("unused")
	private Optional<Float> tmpSwitch;
	@SuppressWarnings("unused")
	private Optional<Float> tmpSupplySet;
	@SuppressWarnings("unused")
	private Optional<Float> tmpMinOutdoor;
	private Optional<Float> tmpDhw1Act;
	@SuppressWarnings("unused")
	private Optional<Float> tmpDhwLevelOff;
	@SuppressWarnings("unused")
	private Optional<Float> tmpDhwLevelLow;
	@SuppressWarnings("unused")
	private Optional<Float> tmpDhwLevelHigh;
	@SuppressWarnings("unused")
	private Optional<Float> tmpDhwSingleChargeSetpoint;
	@SuppressWarnings("unused")
	private Optional<Float> tmpDhwCurrentSetpoint;
	@SuppressWarnings("unused")
	private Optional<Float> tmpHc1Comfort2;
	@SuppressWarnings("unused")
	private Optional<Float> tmpHc1Eco;
	@SuppressWarnings("unused")
	private Optional<Float> tmpHc1CurRoomSetpoint;
	@SuppressWarnings("unused")
	private Optional<Float> tmpHc1TempRoomSetpoint;

	protected ReadWorker(BuderusLogaplusImpl parent, Inet4Address ipAddress, int port, Debug debug, int dropCycles, String key) {
		this.parent = parent;
		this.baseUrl = "http://" + ipAddress.getHostAddress() + ":" + port;
		this.debug = debug;
		this.dropCycles = dropCycles;
		this.privateKey = key;
	}

	@Override
	protected int getCycleTime() {
		return ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN;
	}

	protected void forever() throws Throwable {

		if ((this.dropCyclcesCnt++ % this.dropCycles) != 0) {
			return;
		}
		final AtomicBoolean communicationError = new AtomicBoolean(false);
		try {

			// see here
			// https://forum.fhem.de/index.php/topic,119337.0.html

			this.outdoorTmp = this.doTemperature(BuderusLogaplus.SYS_TEMP_OUTDOOR, this.parent.getTempOutdoorChannel(),
					"Aussentemperatur", communicationError);
			this.supplyTmp = this.doTemperature(BuderusLogaplus.SYS_TEMP_SUPPLY, this.parent.getTempSupplyChannel(),
					"Vorlauftemperatur", communicationError);

			this.returnTmp = this.doTemperature(BuderusLogaplus.SYS_TEMP_RETURN, this.parent.getTempReturnChannel(),
					"Ruecklauftemperatur", communicationError);

			this.tmpSwitch = this.doTemperature(BuderusLogaplus.SYS_TEMP_SWITCH, this.parent.getTempSwitchChannel(),
					"Switch Temperatur", communicationError);
			this.tmpSupplySet = this.doTemperature(BuderusLogaplus.SYS_TEMP_SUPPLY_SET,
					this.parent.getTempSupplySetChannel(), "Vorlauftemperatur SET", communicationError);

			this.tmpMinOutdoor = this.doTemperature(BuderusLogaplus.SYS_TEMP_MIN_OUTDOOR,
					this.parent.getTempMinOutdoorChannel(), "Min. Outdoor Temperatur", communicationError);
			this.tmpDhw1Act = this.doTemperature(BuderusLogaplus.DHW1_ACTUAL_TEMP,
					this.parent.getTempDhw1_ActualChannel(), "DHW1 Warmwasser Temperatur", communicationError);

			this.tmpDhwLevelOff = this.doTemperature(BuderusLogaplus.DHW1_TEMP_LEVEL_OFF,
					this.parent.getTempDhw1LevelOffChannel(), "DHW1 Warmwasser TempLevel off", communicationError);
			this.tmpDhwLevelLow = this.doTemperature(BuderusLogaplus.DHW1_TEMP_LEVEL_LOW,
					this.parent.getTempDhw1LevelLowChannel(), "DHW1 Warmwasser TempLevel low", communicationError);
			this.tmpDhwLevelHigh = this.doTemperature(BuderusLogaplus.DHW1_TEMP_LEVEL_HIGH,
					this.parent.getTempDhw1LevelHighChannel(), "DHW1 Warmwasser TempLevel high", communicationError);

			this.tmpDhwSingleChargeSetpoint = this.doTemperature(BuderusLogaplus.DHW1_SINGLE_CHARGE_SETPT,
					this.parent.getTempDhw1SingleChargeSetpointChannel(), "DHW1 Single Charge Setpoint",
					communicationError);
			this.tmpDhwCurrentSetpoint = this.doTemperature(BuderusLogaplus.DHW1_CURRENT_SETPT,
					this.parent.getTempDhw1CurrentSetpointChannel(), "DHW1 WW Temp Current Setpoint",
					communicationError);

			this.tmpHc1Comfort2 = this.doTemperature(BuderusLogaplus.HC1_TEMP_LEVEL_COMFORT2,
					this.parent.getTempHc1Comfort2Channel(), "HeatingCircuit1 Comfort2", communicationError);
			this.tmpHc1Eco = this.doTemperature(BuderusLogaplus.HC1_TEMP_LEVEL_ECO, this.parent.getTempHc1EcoChannel(),
					"HeatingCircuit1 ECO", communicationError);

			this.tmpHc1CurRoomSetpoint = this.doTemperature(BuderusLogaplus.HC1_CURRENT_ROOM_SETPT,
					this.parent.getTempHc1CurRoomSetpointChannel(), "HC1 Cur Room Setpoint", communicationError);
			this.tmpHc1TempRoomSetpoint = this.doTemperature(BuderusLogaplus.HC1_TEMP_ROOM_SETPT,
					this.parent.getTempHc1TempRoomSetpointChannel(), "HC1 Temp Room Setpoint", communicationError);

			JsonObject sysHealth = this.getResponse(BuderusLogaplus.SYS_HEALTH);
			this.handleStatusVar(sysHealth, communicationError, "Systemgesundheit", BuderusLogaplus.SYS_HEALTH);

			/* DHW 1 WW Status : ACTIVE */
			JsonObject dhw1Status = this.getResponse(BuderusLogaplus.DHW1_STATUS);
			this.handleStatusVar(dhw1Status, communicationError, "DHW1 WW Status ", BuderusLogaplus.DHW1_STATUS);

			/* DHW 1 WW Arbeitszeit : 0 mins */
			JsonObject dhw1WorkTime = this.getResponse(BuderusLogaplus.DHW1_WORKING_TIME);
			this.handleStatusVar(dhw1WorkTime, communicationError, "DHW1 WW Arbeitszeit",
					BuderusLogaplus.DHW1_WORKING_TIME);

			/* DHW 1 WW Wasserfluss : 0 l/min */
			JsonObject dhw1Waterflow = this.getResponse(BuderusLogaplus.DHW1_WATERFLOW);
			this.handleStatusVar(dhw1Waterflow, communicationError, "DHW1 WW Wasserfluss",
					BuderusLogaplus.DHW1_WATERFLOW);

			/* DHW 1 WW OperationMode : low */
			JsonObject dhw1OperationMode = this.getResponse(BuderusLogaplus.DHW1_OP_MODE);
			this.handleStatusVar(dhw1OperationMode, communicationError, "DHW1 WW OperationMode",
					BuderusLogaplus.DHW1_OP_MODE);

			/* Heat Sources Anzahl Starts : 76 */
			JsonObject hs1NumberOfStarts = this.getResponse(BuderusLogaplus.HS_NUMBER_OF_STARTS);
			this.handleStatusVar(hs1NumberOfStarts, communicationError, "HS1 Anzahl Starts",
					BuderusLogaplus.HS_NUMBER_OF_STARTS);

			/* Heat Sources Modulation : 0 % */
			JsonObject hs1Modulation = this.getResponse(BuderusLogaplus.HS_MODULATION);
			this.handleStatusVar(hs1Modulation, communicationError, "HS1 Modulation", BuderusLogaplus.HS_MODULATION);

			/* Heat Sources Total Working Time : 95600 s */
			JsonObject hs1WorkTimeTotalSystem = this.getResponse(BuderusLogaplus.HS_WORKING_TIME_TOTAL_SYSTEM);
			this.handleStatusVar(hs1WorkTimeTotalSystem, communicationError, "HS Total Working Time",
					BuderusLogaplus.HS_WORKING_TIME_TOTAL_SYSTEM);

			/* Heat Source HS1 Modulation Aktuelle Leistung : 0 % */
			JsonObject hsHs1Modulation = this.getResponse(BuderusLogaplus.HS_HS1_MODULATION);
			this.handleStatusVar(hsHs1Modulation, communicationError, "HS HS1 Modulation",
					BuderusLogaplus.HS_HS1_MODULATION);

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
		this.parent._setSlaveCommunicationFailed(communicationError.get());
	}

	private boolean compareValueIgnoreCase(String value, String ref) {
		if (value != null && value.toLowerCase().compareTo(ref.toLowerCase()) == 0) {
			return true;
		}
		return false;
	}

	private void logDebug(String txt) {
		if (this.debug == Debug.NONE) {
			return;
		}
		this.log.info(txt);
	}

	private void handleStatusVar(JsonObject obj, AtomicBoolean communicationError, String varName, String url) {
		try {
			String value;
			Float floatVal;
			switch (url) {
			case BuderusLogaplus.SYS_HEALTH:
				value = JsonUtils.getAsString(obj, "value");
				if (this.compareValueIgnoreCase(value, "ok")) {
					this.parent._setSystemHealth(BuderusLogaplus.SYS_HEALTH_VAL_OK);
					this.logDebug(String.format("%30s: OK", varName));
					return;
				}
				break;
			case BuderusLogaplus.DHW1_STATUS:
				value = JsonUtils.getAsString(obj, "value");
				if (this.compareValueIgnoreCase(value, "active")) {
					this.parent._setDhw1Status(BuderusLogaplus.DHW1_STATUS_ACTIVE);
					this.logDebug(String.format("%30s: ACTIVE", varName));
					return;
				} else if (this.compareValueIgnoreCase(value, "inactive")) {
					this.parent._setDhw1Status(BuderusLogaplus.DHW1_STATUS_INACTIVE);
					this.logDebug(String.format("%30s: INACTIVE", varName));
					return;
				}
				break;
			case BuderusLogaplus.DHW1_WORKING_TIME:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setDhw1WorkTime(floatVal.intValue());
				this.logDebug(String.format("%30s: %5.2f min", varName, floatVal));
				return;
			case BuderusLogaplus.DHW1_WATERFLOW:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setDhw1Waterflow(floatVal.intValue());
				this.logDebug(String.format("%30s: %5.2f l/min", varName, floatVal));
				return;
			case BuderusLogaplus.DHW1_OP_MODE:
				value = JsonUtils.getAsString(obj, "value");
				Integer op = this.getOpMode(value);
				this.parent._setDhw1OperationMode(op);
				this.logDebug(String.format("%30s: %s", varName, value));
				return;
			case BuderusLogaplus.HS_NUMBER_OF_STARTS:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setHsNumberOfStarts(floatVal.intValue());
				this.logDebug(String.format("%30s: %d ", varName, floatVal.intValue()));
				return;

			case BuderusLogaplus.HS_MODULATION:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setHsModulation(floatVal.intValue());
				this.logDebug(String.format("%30s: %d %%", varName, floatVal.intValue()));
				return;

			case BuderusLogaplus.HS_WORKING_TIME_TOTAL_SYSTEM:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setHsWorkingTimeTotalSystem(floatVal.intValue());
				this.logDebug(String.format("%30s: %d s", varName, floatVal.intValue()));
				return;

			case BuderusLogaplus.HS_HS1_MODULATION:
				floatVal = JsonUtils.getAsFloat(obj, "value");
				this.parent._setHsHs1Modulation(floatVal.intValue());
				this.logDebug(String.format("%30s: %d %%", varName, floatVal.intValue()));
				return;
			}

		} catch (Exception e) {
			this.logDebug(String.format("ERROR handling %30s", varName) + " ex: " + e.getMessage());
			communicationError.set(true);
		}
		switch (url) {
		case BuderusLogaplus.SYS_HEALTH:
			this.parent._setSystemHealth(BuderusLogaplus.SYS_HEALTH_VAL_ERROR);
			break;
		case BuderusLogaplus.DHW1_STATUS:
			this.parent._setDhw1Status(null);
			break;
		case BuderusLogaplus.DHW1_WORKING_TIME:
			this.parent._setDhw1WorkTime(null);
			break;
		case BuderusLogaplus.DHW1_WATERFLOW:
			this.parent._setDhw1Waterflow(null);
			break;
		case BuderusLogaplus.DHW1_OP_MODE:
			this.parent._setDhw1OperationMode(null);
			break;
		case BuderusLogaplus.HS_NUMBER_OF_STARTS:
			this.parent._setHsNumberOfStarts(null);
			break;
		case BuderusLogaplus.HS_MODULATION:
			this.parent._setHsModulation(null);
			break;
		case BuderusLogaplus.HS_WORKING_TIME_TOTAL_SYSTEM:
			this.parent._setHsWorkingTimeTotalSystem(null);
			break;
		case BuderusLogaplus.HS_HS1_MODULATION:
			this.parent._setHsHs1Modulation(null);
			break;
		}
		this.logDebug(String.format("%30s: ERROR", varName));
	}

	private Integer getOpMode(String txt) {
		if (txt == null) {
			return null;
		}
		// Operation Modes "Off","low","high","ownprogram","eco"
		switch (txt) {
		case "Off":
			return BuderusLogaplus.OPERATION_MODE_OFF;
		case "low":
			return BuderusLogaplus.OPERATION_MODE_LOW;
		case "high":
			return BuderusLogaplus.OPERATION_MODE_HIGH;
		case "ownprogram":
			return BuderusLogaplus.OPERATION_MODE_OWNPROGRAM;
		case "eco":
			return BuderusLogaplus.OPERATION_MODE_ECO;
		}
		return null;
	}

	private Optional<Float> doTemperature(String reqPath, Channel<Integer> channel, String varName,
			AtomicBoolean commError) throws OpenemsNamedException {
		Optional<Float> optVal;
		JsonObject jsonObj = this.getResponse(reqPath);
		optVal = this.handleTemperature(jsonObj, commError, varName);
		this.setTemp(channel, optVal);
		return optVal;
	}

	private void setTemp(Channel<Integer> channel, Optional<Float> value) {
		if (value.isPresent()) {
			int deziDegree = (int) (value.get() * 10f);
			channel.setNextValue(deziDegree);
		} else {
			channel.setNextValue(null);
		}
	}

	private Optional<Float> handleTemperature(JsonObject obj, AtomicBoolean communicationError, String varName) {

		Optional<Float> result = Optional.empty();
		Integer minValue = null;
		Integer maxValue = null;
		Float valForOpen = null;
		Float valForShort = null;

		String unit = "";
		try {
			String type = JsonUtils.getAsString(obj, "type");
			unit = JsonUtils.getAsString(obj, "unitOfMeasure");
			if (type != null && type.compareTo("floatValue") == 0) {
				float value = JsonUtils.getAsFloat(obj, "value");

				try {
					JsonArray state = JsonUtils.getAsJsonArray(obj, "state");
					valForOpen = JsonUtils.getAsFloat(state.get(0), "open");
					valForShort = JsonUtils.getAsFloat(state.get(1), "short");

				} catch (Exception e) {
					// object does not support state
				}

				result = Optional.of(value);

			} else {
				this.log.error("Unidentified Type " + type);
			}
			try {
				minValue = JsonUtils.getAsInt(obj, "minValue");
				maxValue = JsonUtils.getAsInt(obj, "maxValue");
			} catch (OpenemsNamedException e) {
				this.log.error("Error in parsing minValue and maxValue" + e.getMessage());
			}

		} catch (Exception e) {
			communicationError.set(true);
		}
		if (this.debug == Debug.VALUES_ONLY || this.debug == Debug.ALL) {
			String minmax = "";
			if (minValue != null && maxValue != null) {
				minmax = String.format(" (min: %3d, max: %3d)", minValue, maxValue);
			}
			if (result.isPresent()) {
				this.log.info(String.format("%30s: %5.2f %s%s", varName, result.get(), unit, minmax));
			} else {
				this.log.info(String.format("%30s: - %s%s", varName, unit, minmax));
			}
			if (result.get() == valForOpen) {
				this.log.info("WARNUNG: " + varName + " Hardwarestate is open");
			}
			if (result.get() == valForShort) {
				this.log.info("WARNUNG: " + varName + " Hardwarestate is short");
			}
		}

		return result;
	}

	/**
	 * Gets the JSON response of a HTTPS GET Request.
	 *
	 * @param reqPath the api path
	 * @return the JsonObject
	 * @throws OpenemsNamedException on error
	 */
	private JsonObject getResponse(String reqPath) throws OpenemsNamedException {
		try {
			URL url = new URL(this.baseUrl + reqPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "TeleHeater/2.2.3");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoInput(true);

			// read result from webserver
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String content = reader.lines().collect(Collectors.joining());
				if (this.debug == Debug.ALL) {
					this.log.info(this.baseUrl + " Response: " + content);
				}

				// decrypt buderus RIJANDEL-128 result
				byte[] keyBytes = this.hexToBin(this.privateKey);
				byte[] data = Base64.getDecoder().decode(content);
				byte[] decrypted = null;
				SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
				try {
					Cipher cipher;
					cipher = Cipher.getInstance("AES/ECB/NoPadding");

					// NOTE the US Law Restrictions on encryption
					// https://stackoverflow.com/questions/3862800/invalidkeyexception-illegal-key-size/3864276

					cipher.init(Cipher.DECRYPT_MODE, keySpec);
					decrypted = cipher.doFinal(data);
					String json = new String(decrypted);
					json = json.trim();
					if (this.debug == Debug.ALL) {
						this.log.info("Json Result: \n>" + json + "<\n");
					}
					JsonObject result = JsonUtils.parseToJsonObject(json);
					return result;

				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
					throw new OpenemsException(
							"got ex: no such algorighm, no such padding, invalid key " + e.getMessage());
				} catch (IllegalBlockSizeException e) {
					throw new OpenemsException("got IllegalBlockSizeException ex: " + e.getMessage());
				} catch (BadPaddingException e) {
					throw new OpenemsException("got BasPaddingException ex: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			throw new OpenemsException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private byte[] hexToBin(String s) {
		byte[] res = new byte[s.length() / 2];

		for (int i = 0; i < res.length; i++) {
			String p = s.substring(2 * i, 2 * i + 2);
			res[i] = (byte) Integer.parseInt(p, 16);
		}
		return res;
	}

	/**
	 * Called by parent for its debug log.
	 * 
	 * @return the debug message
	 */
	public String debugLog() {
		StringBuffer buf = new StringBuffer();
		if (this.outdoorTmp != null && this.outdoorTmp.isPresent()) {
			buf.append("Out:" + this.outdoorTmp.get() + " C,");
		}
		if (this.supplyTmp != null && this.supplyTmp.isPresent()) {
			buf.append("Sup:" + this.supplyTmp.get() + " C,");
		}
		if (this.returnTmp != null && this.returnTmp.isPresent()) {
			buf.append("Ret: " + this.returnTmp.get() + " C,");
		}
		if (this.tmpDhw1Act != null && this.tmpDhw1Act.isPresent()) {
			buf.append("WW:" + this.tmpDhw1Act.get() + " C");
		}
		return buf.toString();
	}

}
