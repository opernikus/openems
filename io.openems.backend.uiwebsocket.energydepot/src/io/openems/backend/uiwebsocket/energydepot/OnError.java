package io.openems.backend.uiwebsocket.energydepot;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsException;

public class OnError implements io.openems.common.websocket.OnError {

	private final Logger log = LoggerFactory.getLogger(OnError.class);
	private final UiWebsocketKaco parent;

	public OnError(UiWebsocketKaco parent) {
		this.parent = parent;
	}

	@Override
	public void run(WebSocket ws, Exception ex) throws OpenemsException {
		WsData wsData = ws.getAttachment();
		this.parent.logWarn(this.log, "User [" + wsData.getUserId().orElse("UNKNOWN") + "] websocket error. "
				+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
	}

}
