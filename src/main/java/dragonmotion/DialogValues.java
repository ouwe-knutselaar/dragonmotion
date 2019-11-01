package dragonmotion;

public class DialogValues {

	private String ipadres;
	private int port;
	private int steps;
	
	
	
	public DialogValues(String ipadres, int port, int steps) {
		super();
		this.ipadres = ipadres;
		this.port = port;
		this.steps = steps;
	}

	public String getIpadres() {
		return ipadres;
	}
	
	public void setIpadres(String ipadres) {
		this.ipadres = ipadres;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getSteps() {
		return steps;
	}
	
	public void setSteps(int steps) {
		this.steps = steps;
	}
	
}
