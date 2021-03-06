package com.DSP.waveProcess;

import java.util.Random;

import com.DSP.waveAnalysis.SampleRateException;
import com.riff.Signal;
import com.util.Log;

/**
 * Generation of signals *.
 * 
 * @author Jonny Wildey
 * @version 1.0
 */
public class Gen {

	/**
	 * gets the max amplitude *.
	 * 
	 * @param bit
	 *            the bit
	 * @return the max
	 */
	private static double getMax(double bit) {
		return Math.pow(2, bit - 1) - 1;
	}

	/**
	 * Generates pink noise length: in samples db: 0 would be normalised. so
	 * you'll want -dbs in general sampleRate: the sample rate e.g. 44100
	 * bitRate: normally 16 or 24*
	 * 
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal pinkNoise(int length, double dbBelowCeiling,
			int sampleRate, int bitRate) {
		int octaves = 7; // seems reasonable..
		Signal[] signals = new Signal[octaves];
		for (int i = 0; i < octaves; ++i) {
			signals[i] = Pitch.pitchShift(Gen.whiteNoise(
					(int) ((double) (length) / (Math.pow(2, i))), 0,
					sampleRate, bitRate), -12 * i);
		}
		try {
			Signal s = Gain.sumMono(signals);
			return Gain.changeGain(s, dbBelowCeiling);
		} catch (SampleRateException e) {
		}
		return null;
	}

	/**
	 * saw function. Actually PI / 2 ahead in phase than normal function*
	 * 
	 * @param position
	 *            the position
	 * @return the double
	 */
	private static double saw(double position) {
		position += Math.PI / 2; // phase
		position %= (Math.PI);
		return ((2 * position) / Math.PI) - 1;
	}

	/**
	 * Generates a saw wave frequency: in hz length: in samples db: 0 would be
	 * normalised. so you'll want -dbs in general sampleRate: the sample rate
	 * e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequency
	 *            the frequency
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal saw(double frequency, int length,
			double dbBelowCeiling, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double x = sampleRate / frequency / (Math.PI * 2);
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = saw((double) i / x) * maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Generates silence length: in samples sampleRate: the sample rate e.g.
	 * 44100 bitRate: normally 16 or 24*
	 * 
	 * @param length
	 *            the length
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal silence(int length, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = 0;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Generates a sine wave frequency: in hz length: in samples db: 0 would be
	 * normalised. so you'll want -dbs in general sampleRate: the sample rate
	 * e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequency
	 *            the frequency
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal sine(double frequency, int length,
			double dbBelowCeiling, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double x = sampleRate / frequency / (Math.PI * 2);
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = Math.sin(((double) i / x)) * maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Generates a sine wave sweeping linearly frequency: in hz length: in
	 * samples db: 0 would be normalised. so you'll want -dbs in general
	 * sampleRate: the sample rate e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequencyStart
	 *            the frequency start
	 * @param frequencyEnd
	 *            the frequency end
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal sineSweep(double frequencyStart, double frequencyEnd,
			int length, double dbBelowCeiling, int sampleRate, int bitRate) {
		// not too sure why reverse isn't working but...
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double freq = frequencyStart;
		double x = sampleRate / (Math.PI * 2);
		boolean reverse = (frequencyEnd < frequencyStart);

		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);

		for (int i = 0; i < ns.length; ++i) {
			// Process
			if (reverse) {
				freq = frequencyEnd
				+ ((frequencyStart - frequencyEnd) * ((double) (i) / (double) length));
				ns[length - 1 - i] = Math.sin((double) (i) / (x / freq))
				* maxThresh;
			} else {
				freq = frequencyStart
				+ ((frequencyEnd - frequencyStart) * ((double) (i) / (double) length));
				ns[i] = Math.sin((double) (i) / (x / freq)) * maxThresh;
			}

		}
		Log.d("DONE");

		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * computes the square function in radians *.
	 * 
	 * @param position
	 *            the position
	 * @return the double
	 */
	private static double square(double position) {
		if ((position % (Math.PI * 2)) < Math.PI) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * Generates a Square wave frequency: in hz length: in samples db: 0 would
	 * be normalised. so you'll want -dbs in general sampleRate: the sample rate
	 * e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequency
	 *            the frequency
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal square(double frequency, int length,
			double dbBelowCeiling, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double x = sampleRate / frequency / (Math.PI * 2);
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = square((double) i / x) * maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * adds tape hiss to the signal. db should be -ve *
	 * 
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal tapeHiss(int length, double dbBelowCeiling,
			int sampleRate, int bitRate) {
		// set up arrays
		double[][] tape = TapeHiss.tape.getSignal();
		double[][] ns = new double[1][length];
		double factor = Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < 1; ++i) {
			for (int j = 0; j < length; ++j) {
				// Process
				ns[i][j] = (tape[i % tape[0].length][j % tape[0].length])
				* factor;
			}
		}
		return new Signal(ns, bitRate, sampleRate);
	}

	/**
	 * computes the triangle function in radians *.
	 * 
	 * @param position
	 *            the position
	 * @return the double
	 */
	private static double triangle(double position) {
		position %= (Math.PI * 2);
		double p = position % Math.PI;
		double val = 0;
		if (p < Math.PI / 2) { // up
			val = p / (Math.PI * 0.5);
		} else { // down
			val = ((-2 * p) / Math.PI) + 2;
		}
		if (position > Math.PI) {
			val *= -1;
		}
		return val;
	}

	/**
	 * Generates a triangle wave frequency: in hz length: in samples db: 0 would
	 * be normalised. so you'll want -dbs in general sampleRate: the sample rate
	 * e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequency
	 *            the frequency
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal triangle(double frequency, int length,
			double dbBelowCeiling, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double x = sampleRate / frequency / (Math.PI * 2);
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = triangle((double) i / x) * maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Messed up the saw function but this sounds good *.
	 * 
	 * @param position
	 *            the position
	 * @return the double
	 */
	private static double weirdSaw(double position) {
		position %= (Math.PI * 2);
		double p = position % Math.PI;
		double val = 0;
		if (p < Math.PI / 2) { // up
			val = p / (Math.PI * 0.5);
		} else { // down
			val = ((2 * p) / Math.PI) - 2;
		}
		if (position > Math.PI) {
			val *= -1;
		}
		return val;
	}

	/**
	 * Generates a weird saw wave frequency: in hz length: in samples db: 0
	 * would be normalised. so you'll want -dbs in general sampleRate: the
	 * sample rate e.g. 44100 bitRate: normally 16 or 24*
	 * 
	 * @param frequency
	 *            the frequency
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal weirdSaw(double frequency, int length,
			double dbBelowCeiling, int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double x = sampleRate / frequency / (Math.PI * 2);
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = weirdSaw((double) i / x) * maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Generates white noise length: in samples db: 0 would be normalised. so
	 * you'll want -dbs in general sampleRate: the sample rate e.g. 44100
	 * bitRate: normally 16 or 24*
	 * 
	 * @param length
	 *            the length
	 * @param dbBelowCeiling
	 *            the db below ceiling
	 * @param sampleRate
	 *            the sample rate
	 * @param bitRate
	 *            the bit rate
	 * @return the signal
	 */
	public static Signal whiteNoise(int length, double dbBelowCeiling,
			int sampleRate, int bitRate) {
		// set up arrays
		double[] ns = new double[length];
		// sine normally has a 2PI frequency e.g. 7018hz
		// 44100 / (2PI * x) = freq
		double maxThresh = getMax(bitRate)
		* Gain.decibelToAmplitude(dbBelowCeiling);
		Random r = new Random();
		for (int i = 0; i < ns.length; ++i) {
			// Process
			ns[i] = (r.nextDouble() * (maxThresh * 2)) - maxThresh;
		}
		return new Signal(new double[][] { ns }, bitRate, sampleRate);
	}

	/**
	 * Instantiates a new gen.
	 */
	public Gen() {
	}

}
