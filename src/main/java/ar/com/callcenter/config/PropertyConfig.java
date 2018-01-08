package ar.com.callcenter.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ar.com.callcenter.CallCenter;

public class PropertyConfig extends Config {
	
	private PropertyConfig() {
		this.initResources();
	}
	
	private static Properties defaultProps = new Properties();
	private static PropertyConfig instance;
	
	public static PropertyConfig getConfig() {
		if(instance == null) {
			instance = new PropertyConfig();
		}
		return instance;
	}
	
	private void initResources() {
		InputStream is = CallCenter.class.getResourceAsStream("/callcenter.properties");
		try {
			defaultProps.load(is);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private int getPropertyAsInt(String key) {
		return Integer.parseInt(defaultProps.getProperty(key));
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return instance;
	}

	@Override
	public int getConfigValueAsInt(CATEGORIES cat) {
		return this.getPropertyAsInt(cat.toString().replaceAll("_", "."));
	}

	@Override
	public String getConfigValue(CATEGORIES cat) {
		return this.defaultProps.getProperty(cat.toString().replaceAll("_", "."));
	}	
}