package io.openems.common.websocket;

import java.util.List;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.types.Device;
import io.openems.common.types.FieldValue;
import io.openems.common.types.NumberFieldValue;
import io.openems.common.types.StringFieldValue;

public class DefaultMessages {

	/**
	 * <pre>
	 *	{
	 *		authenticate: {
	 *			mode: "allow",
	 *			token: String
	 *		}, metadata: {
	 *			devices: [{
	 *				name: String,
	 *				comment: String,
	 *				producttype: String,
	 *				role: "admin" | "installer" | "owner" | "guest",
	 *				online: boolean
	 *			}]
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject browserConnectionSuccessfulReply(String token, List<Device> devices) {
		JsonObject jAuthenticate = new JsonObject();
		jAuthenticate.addProperty("mode", "allow");
		jAuthenticate.addProperty("token", token);
		JsonObject j = new JsonObject();
		j.add("authenticate", jAuthenticate);
		JsonObject jMetadata = new JsonObject();
		if(!devices.isEmpty()) {
			JsonArray jDevices = new JsonArray();
			for(Device device : devices) {
				JsonObject jDevice = new JsonObject();
				jDevice.addProperty("name", device.getName());
				jDevice.addProperty("comment", device.getComment());
				jDevice.addProperty("producttype", device.getProducttype());
				jDevice.addProperty("role", device.getRole().toString());
				jDevice.addProperty("online", device.isOnline());
				jDevices.add(jDevice);
			}
			jMetadata.add("devices", jDevices);
		}
		j.add("metadata", jMetadata);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		authenticate: {
	 *			mode: "deny"
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject browserConnectionFailedReply() {
		JsonObject jAuthenticate = new JsonObject();
		jAuthenticate.addProperty("mode", "deny");
		JsonObject j = new JsonObject();
		j.add("authenticate", jAuthenticate);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		authenticate: {
	 *			mode: "allow"
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject openemsConnectionSuccessfulReply() {
		JsonObject jAuthenticate = new JsonObject();
		jAuthenticate.addProperty("mode", "allow");
		JsonObject j = new JsonObject();
		j.add("authenticate", jAuthenticate);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		authenticate: {
	 *			mode: "deny",
	 *			message: String
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject openemsConnectionFailedReply(String message) {
		JsonObject jAuthenticate = new JsonObject();
		jAuthenticate.addProperty("mode", "deny");
		jAuthenticate.addProperty("message", message);
		JsonObject j = new JsonObject();
		j.add("authenticate", jAuthenticate);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		timedata: {
	 *			timestamp (Long): {
	 *				channel: String,
	 *				value: String | Number
	 *			}
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject timestampedData(Multimap<Long, FieldValue<?>> data) {
		JsonObject jTimedata = new JsonObject();
		data.asMap().forEach((timestamp, fieldValues) -> {
			JsonObject jTimestamp = new JsonObject();
			fieldValues.forEach(fieldValue -> {
				if (fieldValue instanceof NumberFieldValue) {
					jTimestamp.addProperty(fieldValue.field, ((NumberFieldValue) fieldValue).value);
				} else if (fieldValue instanceof StringFieldValue) {
					jTimestamp.addProperty(fieldValue.field, ((StringFieldValue) fieldValue).value);
				}
			});
			jTimedata.add(String.valueOf(timestamp), jTimestamp);
		});
		JsonObject j = new JsonObject();
		j.add("timedata", jTimedata);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		config: {
	 *			...
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @param token
	 * @return
	 */
	public static JsonObject configQueryReply(JsonObject config) {
		JsonObject j = new JsonObject();
		j.add("config", config);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		id: [string],
	 *		currentData: {[{ 
	 *			channel: string,
	 *			value: any
     *		}]}
	 *	}
	 * </pre>
	 * 
	 * @return
	 */
	public static JsonObject currentData(JsonArray jId, JsonObject jCurrentData) {
		JsonObject j = new JsonObject();
		j.add("id", jId);
		j.add("currentData", jCurrentData);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		id: [string]
	 *		historicData: {
	 *			data: [{
	 *				time: ...,
	 *				channels: {
	 *					thing: {
	 *						channel: any
	 *					} 
	 *				}
	 *			}]
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @return
	 */
	public static JsonObject historicDataQueryReply(JsonArray jId, JsonArray jData) {
		JsonObject j = new JsonObject();
		j.add("id", jId);
		JsonObject jHistoricData = new JsonObject();
		jHistoricData.add("data", jData);
		j.add("historicData", jHistoricData);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		notification: {
	 *			status: string,
	 *			message: string,
	 *			code: number,
	 *			params: string[]
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @return
	 */
	public static JsonObject notification(Notification code, Object... params) {
		JsonObject j = new JsonObject();
		JsonObject jNotification = new JsonObject();
		jNotification.addProperty("status", code.getStatus());
		jNotification.addProperty("message", String.format(code.getMessage(), params));
		jNotification.addProperty("code", code.getValue());
		JsonArray jParams = new JsonArray();
		for(Object param : params) {
			jParams.add(param.toString());
		}
		jNotification.add("params", jParams);
		j.add("notification", jNotification);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		currentData: {
	 *			mode: 'subscribe',
	 *			channels: {}
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @return
	 */
	public static JsonObject currentDataSubscribe(JsonArray jId, JsonObject jChannels) {
		JsonObject j = new JsonObject();
		j.add("id", jId);
		JsonObject jCurrentData = new JsonObject();
		jCurrentData.addProperty("mode", "subscribe");
		jCurrentData.add("channels", jChannels);
		j.add("currentData", jCurrentData);
		return j;
	}
	
	/**
	 * <pre>
	 *	{
	 *		id: [string],
	 *		log: {
	 *			times: number,
	 *			level: string,
	 *			source: string,
	 *			message: string
	 *		}
	 *	}
	 * </pre>
	 * 
	 * @return
	 */
	public static JsonObject log(JsonArray jId, long timestamp, String level, String source, String message) {
		JsonObject j = new JsonObject();
		j.add("id", jId);
		JsonObject jLog = new JsonObject();
		jLog.addProperty("time", timestamp);
		jLog.addProperty("level", level);
		jLog.addProperty("source", source);
		jLog.addProperty("message", message);
		j.add("log", jLog);
		return j;
	}
}
