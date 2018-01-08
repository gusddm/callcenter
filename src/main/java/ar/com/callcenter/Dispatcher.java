package ar.com.callcenter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import ar.com.callcenter.config.Config;
import ar.com.callcenter.config.Config.CATEGORIES;
import ar.com.callcenter.model.Call;
import ar.com.callcenter.model.Operator;
import ar.com.callcenter.model.Operator.TYPE;

public class Dispatcher {
    
    private Map<Operator.TYPE, ArrayBlockingQueue<Operator>> mapOpQueue = new HashMap<>();
    
    private Config config;

    public Dispatcher(Config config) {
        this.config = config;
        this.initOpQueues();
    }        

	private Operator process(Operator op, Call call) throws InterruptedException {
        int randDuration = ThreadLocalRandom.current().nextInt(config.getConfigValueAsInt(CATEGORIES.MIN_CALL_TIME), config.getConfigValueAsInt(CATEGORIES.MAX_CALL_TIME));
        call.setDuration(randDuration);
        Thread.sleep(randDuration);
        return op;
    }

    Runnable callConsumer(BlockingQueue<Call> callQueue, Set<Call> attendedCalls) {
        Runnable callConsumer = () -> {

            Call call = callQueue.poll();

            try {
                Operator op = this.getFreeOperator();
                
                if(op == null) {                        
                    throw new Exception("Operadores 100% ocupados - CALL#ID " + call.getId() + " PERDIDA");
                }
                
                this.process(op, call);
                
                //regreso el op a la cola
                mapOpQueue.get(op.getType()).offer(op);
                
                System.out.println("CallID# " + call.getId() + " ATENDIDA POR " + op.getType() + "ID#" + op.getId() + ". Duracion: " + call.getDuration() +  "milisegundos");
                call.setAttendedOk(true);
                attendedCalls.add(call);
            }
            catch (Exception e) {
                attendedCalls.add(call);
                System.out.println("Error :" + e.getMessage() );
            }
        };
        return callConsumer;
    }
    
    /**
     * 
     */
    private void initOpQueues() {
        
        //Populo el map de operadores
        ArrayBlockingQueue<Operator> opQueue = new ArrayBlockingQueue<>(config.getConfigValueAsInt(CATEGORIES.OPERATORS_SIZE));
        for(int x = 0; x < config.getConfigValueAsInt(CATEGORIES.OPERATORS_SIZE) ; x++) {
            opQueue.offer(Operator.createOperator(x+"",Operator.TYPE.OPERADOR));
        }
        mapOpQueue.put(Operator.TYPE.OPERADOR, opQueue);
        
        opQueue = new ArrayBlockingQueue<>(config.getConfigValueAsInt(CATEGORIES.SUPERVISORS_SIZE));
        for(int x = 0; x < config.getConfigValueAsInt(CATEGORIES.SUPERVISORS_SIZE) ; x++) {
            opQueue.offer(Operator.createOperator(x+"" , Operator.TYPE.SUPERVISOR));
        }
        mapOpQueue.put(Operator.TYPE.SUPERVISOR, opQueue);
        
        opQueue = new ArrayBlockingQueue<>(config.getConfigValueAsInt(CATEGORIES.DIRECTORS_SIZE));
        for(int x = 0; x < config.getConfigValueAsInt(CATEGORIES.DIRECTORS_SIZE) ; x++) {
            opQueue.offer(Operator.createOperator(x+"", Operator.TYPE.DIRECTOR));
        }        
        mapOpQueue.put(Operator.TYPE.DIRECTOR, opQueue);    
    }
    
    /**
     * Obtiene el operador libre de menor jerarquia disponible segun el siguiente esquema: Operador -> Supervisor -> Director 
     * @return Operador
     * @throws InterruptedException 
     */
    private Operator getFreeOperator() throws InterruptedException {
        TYPE[] ops = Operator.TYPE.values();
        Operator op = null;
        for (TYPE type : ops) {            
            op = mapOpQueue.get(type).poll(config.getConfigValueAsInt(CATEGORIES.MIN_CALL_TIME), TimeUnit.MILLISECONDS);
            if(op != null) {
                break;
            }
        }
        return op;
    }
}