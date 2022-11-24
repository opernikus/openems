package io.openems.edge.heatpump.buderus.logaplus.data;

public enum Debug {
	NONE("None"), //
	VALUES_ONLY("Values Only"), //
	ALL("All");

	private final String symbol;

	private Debug(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return this.symbol;
	}

}
