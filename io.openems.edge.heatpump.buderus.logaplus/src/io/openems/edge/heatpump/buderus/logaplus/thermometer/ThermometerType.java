package io.openems.edge.heatpump.buderus.logaplus.thermometer;

import io.openems.edge.heatpump.buderus.logaplus.BuderusLogaplus;
import io.openems.edge.heatpump.buderus.logaplus.BuderusLogaplus.ChannelId;

public enum ThermometerType {
	HOT_WATER(BuderusLogaplus.ChannelId.TEMP_DHW1_ACTUAL),
	SUPPLY_TEMPERATURE(BuderusLogaplus.ChannelId.TEMP_SUPPLY),
	RETURN_TEMPERATURE(BuderusLogaplus.ChannelId.TEMP_RETURN),
	OUTDOOR_TEMPERATURE(BuderusLogaplus.ChannelId.TEMP_OUTDOOR);
	
	ThermometerType(ChannelId copyChannel) {
		this.copyChannel = copyChannel;
	}

	public BuderusLogaplus.ChannelId copyChannel;
	
	
}
