package io.openems.backend.uiwebsocket.impl;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import io.openems.common.jsonrpc.base.JsonrpcNotification;
import io.openems.common.jsonrpc.notification.CurrentDataNotification;
import io.openems.common.jsonrpc.notification.EdgeRpcNotification;
import io.openems.common.types.ChannelAddress;

public class SubscribedChannelsWorker extends io.openems.common.websocket.SubscribedChannelsWorker {

	private final UiWebsocketImpl parent;

	private String edgeId = null;

	public SubscribedChannelsWorker(UiWebsocketImpl parent, WsData wsData) {
		super(wsData);
		this.parent = parent;
	}

	/**
	 * Sets the Edge-ID.
	 * 
	 * @param edgeId the Edge-ID
	 */
	public void setEdgeId(String edgeId) {
		this.edgeId = edgeId;
	}

	@Override
	protected JsonElement getChannelValue(ChannelAddress channelAddress) {
		if (this.edgeId == null) {
			return JsonNull.INSTANCE;
		}

		Optional<JsonElement> channelCacheValue = this.parent.timeData.getChannelValue(this.edgeId, channelAddress);
		return channelCacheValue.orElse(JsonNull.INSTANCE);
	}

	@Override
	protected JsonrpcNotification getJsonRpcNotification(CurrentDataNotification currentData) {
		return new EdgeRpcNotification(this.edgeId, currentData);
	}
}
