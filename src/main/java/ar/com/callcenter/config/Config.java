package ar.com.callcenter.config;

public abstract class Config {	
	public enum CATEGORIES {CALLS_DAILY_LIMIT, CALL_QUEUE_CAPACITY, MAX_CALL_TIME, MIN_CALL_TIME, OPERATORS_SIZE, SUPERVISORS_SIZE, DIRECTORS_SIZE}	
	public abstract int getConfigValueAsInt(CATEGORIES cat);
	public abstract String getConfigValue(CATEGORIES cat);
}
