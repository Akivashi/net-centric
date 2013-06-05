package nl.uva.sd2;

public interface IMbedNetwork extends Runnable {
	public void newValue(int value);
	public void stop();
}
