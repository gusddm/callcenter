package callcenter.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ar.com.callcenter.CallCenter;
import ar.com.callcenter.Dispatcher;
import ar.com.callcenter.config.Config.CATEGORIES;
import ar.com.callcenter.model.Call;

public class CallCenterTest {
	
	TestConfig config;
	BlockingQueue<Call> callQueue;
	
	@Before
	public void setup() {

		Map<CATEGORIES, Integer> mapConfig = new HashMap<>();
		mapConfig.put(CATEGORIES.CALLS_DAILY_LIMIT, 5);
		mapConfig.put(CATEGORIES.CALL_QUEUE_CAPACITY, 10);
		mapConfig.put(CATEGORIES.DIRECTORS_SIZE, 1);
		mapConfig.put(CATEGORIES.MAX_CALL_TIME, 10000);
		mapConfig.put(CATEGORIES.MIN_CALL_TIME, 5000);
		mapConfig.put(CATEGORIES.OPERATORS_SIZE, 15);
		mapConfig.put(CATEGORIES.SUPERVISORS_SIZE, 2);
		
		config = new TestConfig(mapConfig);
		callQueue = new ArrayBlockingQueue<>(config.getConfigValueAsInt(CATEGORIES.CALL_QUEUE_CAPACITY));		
	}
	
	@Test() 
	public void takeAllCall() {
		Dispatcher dispatcher = new Dispatcher(config);
        CallCenter callCenter = new CallCenter(config, callQueue, dispatcher);
        callCenter.start();                       
        while(callCenter.isProcessing()) {
        	
        }
        callCenter.stop();
        int llamadasProcesadasOk = (int)callCenter.getAttendedCalls().stream().filter(call -> call.isAttendedOk()).count();
               
        //Al haber suficientes operadores se espera que ninguna llamada se pierdan
        Assert.assertEquals(config.getConfigValueAsInt(CATEGORIES.CALLS_DAILY_LIMIT), llamadasProcesadasOk);        
	}

	
	@Test() 
	public void loseSomeCall() {
		config.setMapConfigValue(CATEGORIES.CALL_QUEUE_CAPACITY, 1);
        config.setMapConfigValue(CATEGORIES.OPERATORS_SIZE, 1);
        config.setMapConfigValue(CATEGORIES.SUPERVISORS_SIZE, 1);
        config.setMapConfigValue(CATEGORIES.DIRECTORS_SIZE, 1);
        config.setMapConfigValue(CATEGORIES.MIN_CALL_TIME, 1000);
        config.setMapConfigValue(CATEGORIES.MAX_CALL_TIME, 10000);
        
		Dispatcher dispatcher = new Dispatcher(config);
        CallCenter callCenter = new CallCenter(config, callQueue, dispatcher);
        callCenter.start();                       
        while(callCenter.isProcessing()) {
        	
        }
        callCenter.stop();
        int llamadasProcesadasOk = (int)callCenter.getAttendedCalls().stream().filter(call -> call.isAttendedOk()).count();
               
        //Al haber pocos operadores se espera que algunas llamadas se pierdan
        Assert.assertNotEquals(config.getConfigValueAsInt(CATEGORIES.CALLS_DAILY_LIMIT), llamadasProcesadasOk);        
	}

}
