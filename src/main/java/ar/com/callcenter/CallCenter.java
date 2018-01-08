package ar.com.callcenter;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import ar.com.callcenter.config.Config;
import ar.com.callcenter.config.Config.CATEGORIES;
import ar.com.callcenter.config.PropertyConfig;
import ar.com.callcenter.model.Call;

public class CallCenter {
    
    private Config config;

    //Cola de llamadas
    private BlockingQueue<Call> callQueue;
    
    //Llamadas procesadas ok o no atendidas por demoras, se puede volcar a un archivo para tratarlas de manera manual
    private Set<Call> attendedCalls = ConcurrentHashMap.newKeySet();
    
    private ExecutorService executorService;

    private Dispatcher dispatcher;

    private CallCenter() {
    }
    public CallCenter(Config config, BlockingQueue<Call> callQueue, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.config = config;
        this.callQueue = callQueue;
    }    

	public void start() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        new Thread(this.callProducer()).start();  
    }

    public void stop() {
    	executorService.shutdown();
    	try {
            if (!executorService.awaitTermination(config.getConfigValueAsInt(CATEGORIES.CALLS_DAILY_LIMIT) * config.getConfigValueAsInt(CATEGORIES.MAX_CALL_TIME), TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    
    public Boolean isProcessing() {
    	return !executorService.isTerminated();
    }

    private Runnable callProducer() {
        Runnable callProducer = () -> { 
            int llamadas = 0;
           
            try {
                while (llamadas < config.getConfigValueAsInt(CATEGORIES.CALLS_DAILY_LIMIT) || config.getConfigValueAsInt(CATEGORIES.CALLS_DAILY_LIMIT) == -1) {
                    
                    //Tiempo aprox que demora en entrar una nueva llamada
                    int randDelayBetweenCalls = ThreadLocalRandom.current().nextInt(1, 5);
                    Thread.sleep(randDelayBetweenCalls * 100);

                    Call call = new Call(llamadas++ +"");
                    boolean isAdded = callQueue.offer(call, config.getConfigValueAsInt(CATEGORIES.MAX_CALL_TIME), TimeUnit.MILLISECONDS);
                    
                    if(!isAdded) {
                        attendedCalls.add(call);
                        System.out.println("<<<COLA DE MENSAJES AGOTADA>>> -> TIMEOUT VENCIDO PARA CALLID# : " + call.getId());
                    }
                    else {
                    	executorService.submit(dispatcher.callConsumer(callQueue, attendedCalls));
                    }	
                }
                stop();
                System.out.println("----------------------------------------------------------------------");
                System.out.println("RESUMEN LLAMADAS PERDIDAS");
                attendedCalls.stream().filter(call -> !call.isAttendedOk()).forEach(c -> System.out.println("CALLID# " + c.getId()));
                System.out.println("----------------------------------------------------------------------");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        };
        return callProducer;
    }
    
    public Set<Call> getAttendedCalls() {
		return attendedCalls;
	}

	public static void main(String[] args) {
    	BlockingQueue<Call> callQueue = new ArrayBlockingQueue<>(PropertyConfig.getConfig().getConfigValueAsInt(CATEGORIES.CALL_QUEUE_CAPACITY));
        Dispatcher dispatcher = new Dispatcher(PropertyConfig.getConfig());
        CallCenter callCenter = new CallCenter(PropertyConfig.getConfig(), callQueue, dispatcher);
        callCenter.start();
    }
}