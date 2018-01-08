package callcenter.test;

import java.util.Map;

import ar.com.callcenter.config.Config;

public class TestConfig extends Config {
	
	Map<CATEGORIES, Integer> mapConfig;
	
	public TestConfig() {}
	
	public TestConfig(Map<CATEGORIES, Integer> mapConfig) {
		this.mapConfig = mapConfig;
	}

	@Override
	public String getConfigValue(CATEGORIES cat) {
		return null;
	}

	@Override
	public int getConfigValueAsInt(CATEGORIES cat) {
		return this.mapConfig.get(cat);
	}
	
	public void setMapConfigValue(CATEGORIES cat, Integer value) {
		this.mapConfig.put(cat, value);
	}

}
