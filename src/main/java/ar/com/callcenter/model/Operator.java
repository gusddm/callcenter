package ar.com.callcenter.model;

public class Operator {
	
	public String id;
	public enum TYPE {OPERADOR, SUPERVISOR, DIRECTOR};
	private TYPE type;
	private Operator(String id, TYPE type) {
		this.id = id;
		this.type = type;
	}
	public TYPE getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public static Operator createOperator(String id, TYPE tipo) {
		Operator op = new Operator(id, tipo);
		return op;
	}
}