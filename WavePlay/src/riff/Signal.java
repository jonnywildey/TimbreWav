package riff;

import plotting.SignalController;
import plotting.WavController;

/**Basic data holder for signals **/
public class Signal {
	public double[][] signal;
	public int bit;
	private int sampleRate;
	
	public Signal(double[][] signal, int bit, int sampleRate) {
		this.signal = signal;
		this.bit = bit;
		this.sampleRate = sampleRate;
	}
	
	/**returns the maximum potential amplitude
	 * for fixed point calculations
	 */
	public double getMaxAmplitude() {
		return  Math.pow(2, this.bit - 1) - 1;
	}
	
	/**returns the minimum potential amplitude
	 * for fixed point calculations
	 */
	public double getMinAmplitude() {
		return  Math.pow(2, this.bit - 1) * -1;
	}
	
	public void makeGraph() {
		SignalController sc = new SignalController(this, 600, 400);
		sc.makeChart();
	}
	
	public WaveChunk toWave() {
		FMTChunk fmt = new FMTChunk(this.bit, this.sampleRate, getChannels());
		//Make data chunk, make wav chunk, put them together
		//compareByte[] method
		return null;
	}
	
	/** get the amount of channels **/
	public int getChannels() {
		return signal.length;
	}

	public double[][] getSignal() {
		return signal;
	}

	public int getBit() {
		return bit;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	
}