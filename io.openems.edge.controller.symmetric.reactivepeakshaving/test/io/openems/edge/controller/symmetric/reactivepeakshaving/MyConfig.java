package io.openems.edge.controller.symmetric.reactivepeakshaving;

import io.openems.edge.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
//		private String setting0;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

//		public Builder setSetting0(String setting0) {
//			this.setting0 = setting0;
//			return this;
//		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 * 
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String ess_id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String meter_id() {
		// TODO Auto-generated method stub
		return null;
	}

	public int peakShavingReactivePower() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float parP() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float parI() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ReactivePowerLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float pidKp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean enableIdelay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float pidTi_s() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float pidMaxReactivePower_pct() {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public String setting0() {
//		return this.builder.setting0;
//	}

}