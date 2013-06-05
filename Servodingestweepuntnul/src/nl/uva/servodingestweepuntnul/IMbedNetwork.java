package nl.uva.servodingestweepuntnul;

public interface IMbedNetwork extends Runnable {
	public void newValue(int value);
	public void stop();
}
