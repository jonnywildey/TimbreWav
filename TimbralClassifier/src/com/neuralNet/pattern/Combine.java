package com.neuralNet.pattern;

import java.io.File;
import java.util.Random;

import com.DSP.waveAnalysis.FFTBox;
import com.DSP.waveAnalysis.SampleRateException;
import com.DSP.waveProcess.FFTChain;
import com.DSP.waveProcess.Gain;
import com.DSP.waveProcess.SignalChain;
import com.riff.Signal;
import com.riff.Wave;
import com.util.Log;
import com.util.Serialize;

/**
 * The Class Combine.
 * 
 * @author Jonny Wildey
 * @version 1.0
 */
public class Combine {

	/**
	 * processes, mixes pairs of audio files in a factorial manner from
	 * directories. Creates combined files and writes to a folder. This can use
	 * up a lot of drive space!*
	 * 
	 * @param dirToWriteTo
	 *            the dir to write to
	 * @param dirs
	 *            the dirs
	 */
	public static void batchCombine(File dirToWriteTo, File... dirs) {
		// start combining
		Wave wWave = null;
		Wave nWave = null;
		for (int i = 0; i < dirs.length; ++i) {
			// get all audio files in directory
			File[] wFiles = Serialize.getActualFiles(dirs[i]);
			for (int j = 0; j < wFiles.length; ++j) {
				wWave = new Wave(wFiles[j]);
				for (int k = i; k < dirs.length; ++k) {
					File[] nFiles = Serialize.getActualFiles(dirs[k]);
					for (int l = 0; l < nFiles.length; ++l) {
						nWave = new Wave(nFiles[l]);
						// Modify and combine
						combineWaves(dirToWriteTo, wWave, nWave);
					}
				}
			}
		}
	}

	/**
	 * processes, mixes pairs of audio files in a factorial manner from
	 * directories. Creates new generations and writes these to a folder. This
	 * can use up a lot of drive space!*
	 * 
	 * @param dirToWriteTo
	 *            the dir to write to
	 * @param genCount
	 *            the gen count
	 * @param dirs
	 *            the dirs
	 */
	public static void batchCombineAndGenerate(File dirToWriteTo, int genCount,
			File... dirs) {
		Random pitchRand = new Random();
		Random noiseRand = new Random();
		Random hpRand = new Random();
		Random lpRand = new Random();
		// start combining
		Wave wWave = null;
		Wave nWave = null;
		for (int i = 0; i < dirs.length; ++i) {
			// get all audio files in directory
			File[] wFiles = Serialize.getActualFiles(dirs[i]);
			for (int j = 0; j < wFiles.length; ++j) {
				wWave = new Wave(wFiles[j]);
				for (int k = i; k < dirs.length; ++k) {
					File[] nFiles = Serialize.getActualFiles(dirs[k]);
					for (int l = 0; l < nFiles.length; ++l) {
						nWave = new Wave(nFiles[l]);
						// Modify and combine
						for (int m = 0; m < genCount; ++m) {
							combineAndGenWaves(pitchRand, noiseRand, hpRand,
									lpRand, dirToWriteTo, m, wWave, nWave);
						}

					}
				}
			}
		}
	}

	/**
	 * Combines and processes waves from files in a factorial manner *.
	 * 
	 * @param pitchRand
	 *            the pitch rand
	 * @param noiseRand
	 *            the noise rand
	 * @param hpRand
	 *            the hp rand
	 * @param lpRand
	 *            the lp rand
	 * @param dir
	 *            the dir
	 * @param id
	 *            the id
	 * @param waves
	 *            the waves
	 */
	protected static void combineAndGenWaves(Random pitchRand,
			Random noiseRand, Random hpRand, Random lpRand, File dir, int id,
			Wave... waves) {
		Signal[] signals = new Signal[waves.length];
		String newName = "";
		String instrument = "";
		String wName = "";
		Wave wave = null;
		for (int i = 0; i < waves.length; ++i) {
			// get signals
			wave = waves[i];
			signals[i] = wave.getSignals();
			signals[i] = SignalChain.processSignalChain(pitchRand, noiseRand,
					hpRand, lpRand, signals[i]);
			// get name of file
			wName = waves[i].getFilePath().getParentFile().getName();
			newName = renameCombine(newName, waves[i]);
			// get instrument name
			if (!wName.equals(instrument)) { // don't want cellocello
				instrument = instrument.concat(wName);
			}
			instrument = instrument.concat(String.valueOf(id));
		}
		Combine.sumAndWrite(dir, signals, newName, instrument);
	}

	/**
	 * Combine multiple wave patterns to one and serialize. Be careful with heap
	 * memory as large serialisations cane it *
	 * 
	 * @param toSerial
	 *            the to serial
	 * @param serials
	 *            the serials
	 */
	public static void combineAndJSON(File toSerial, File... serials) {
		WavePatterns finished = combineFromSerials(serials);
		Serialize.writeJSON(finished, toSerial);
		Log.d("Serialized WavePatterns to " + toSerial.getAbsolutePath());
	}

	/**
	 * Combine multiple wave patterns to one and serialize. Be careful with heap
	 * memory as large serialisations cane it *
	 * 
	 * @param toSerial
	 *            the to serial
	 * @param serialDir
	 *            the serial dir
	 */
	public static void combineAndJSON(File toSerial, File serialDir) {
		WavePatterns finished = combineFromSerials(Serialize
				.getActualFiles(serialDir));
		Serialize.writeJSON(finished, toSerial);
		Log.d("Serialized WavePatterns to " + toSerial.getAbsolutePath());
	}

	/**
	 * Combine multiple wave patterns to one and serialize. Be careful with heap
	 * memory as large serialisations cane it *
	 * 
	 * @param toSerial
	 *            the to serial
	 * @param serials
	 *            the serials
	 */
	public static void combineAndSerialize(File toSerial, File... serials) {
		WavePatterns finished = combineFromSerials(serials);
		Serialize.serialize(finished, toSerial);
		Log.d("Serialized WavePatterns to " + toSerial.getAbsolutePath());
	}

	/**
	 * Combine multiple wave patterns to one and serialize. Be careful with heap
	 * memory as large serialisations cane it *
	 * 
	 * @param toSerial
	 *            the to serial
	 * @param serialDir
	 *            the serial dir
	 */
	public static void combineAndSerialize(File toSerial, File serialDir) {
		WavePatterns finished = combineFromSerials(Serialize
				.getActualFiles(serialDir));
		Serialize.serialize(finished, toSerial);
		Log.d("Serialized WavePatterns to " + toSerial.getAbsolutePath());
	}

	/**
	 * Combine multiple wave patterns to one. Be careful with heap memory as
	 * large jsons cane it *
	 * 
	 * @param jsons
	 *            the jsons
	 * @return the wave patterns
	 */
	public static WavePatterns combineFromJSONs(File... jsons) {
		WavePatterns[] wp = new WavePatterns[jsons.length];
		int c = 0;
		for (File f : jsons) {
			wp[c] = Serialize.getFromJSON(f, WavePatterns.class);
			Log.d("imported " + f.getAbsolutePath());
			c++;
		}
		return WavePatterns.combinePatterns(wp);
	}

	/**
	 * Combine multiple wave patterns to one. Be careful with heap memory as
	 * large jsons cane it *
	 * 
	 * @param serialDir
	 *            the serial dir
	 * @return the wave patterns
	 */
	public static WavePatterns combineFromJSONs(File serialDir) {
		File[] serials = Serialize.getActualFiles(serialDir);
		return combineFromJSONs(serials);
	}

	/**
	 * Combine multiple wave patterns to one. Be careful with heap memory as
	 * large serialisations cane it *
	 * 
	 * @param serials
	 *            the serials
	 * @return the wave patterns
	 */
	public static WavePatterns combineFromSerials(File... serials) {
		WavePatterns[] wp = new WavePatterns[serials.length];
		int c = 0;
		for (File f : serials) {
			wp[c] = (WavePatterns) Serialize.getFromSerial(f.getAbsolutePath());
			c++;
		}
		return WavePatterns.combinePatterns(wp);
	}

	/**
	 * Combine multiple wave patterns to one. Be careful with heap memory as
	 * large serialisations cane it *
	 * 
	 * @param serialDir
	 *            the serial dir
	 * @return the wave patterns
	 */
	public static WavePatterns combineFromSerials(File serialDir) {
		File[] serials = Serialize.getActualFiles(serialDir);
		return combineFromSerials(serials);
	}

	/**
	 * Combines and processes waves from files in a factorial manner *.
	 * 
	 * @param dir
	 *            the dir
	 * @param waves
	 *            the waves
	 * @return the wave pattern
	 */
	protected static WavePattern combineWaves(File dir, Wave... waves) {
		Signal[] signals = new Signal[waves.length];
		String newName = "";
		String instrument = "";
		String wName = "";
		Wave wave = null;
		for (int i = 0; i < waves.length; ++i) {
			// get signals
			wave = waves[i];
			signals[i] = wave.getSignals();
			signals[i] = SignalChain.midAndNormalise(signals[i]);
			// get name of file
			wName = waves[i].getFilePath().getParentFile().getName();
			newName = renameCombine(newName, waves[i]);
			// get instrument name
			if (!wName.equals(instrument)) { // don't want cellocello
				instrument = instrument.concat(wName);
			}
		}
		FFTBox fftBox = Combine.sumAndWrite(dir, signals, newName, instrument);
		WavePattern wp = new WavePattern();
		wp.addInputData(fftBox);
		wp.instrument = wName;
		return wp;
	}

	/**
	 * Renames a combined file in a logical way *.
	 * 
	 * @param str
	 *            the str
	 * @param wave
	 *            the wave
	 * @return the string
	 */
	public static String renameCombine(String str, Wave wave) {
		if (str.length() == 0 | str == null) {
			str = wave.getFilePath().getName();
		} else {
			str = str.substring(0, str.length() - 4).concat(
					wave.getFilePath().getName());
		}
		return str;
	}

	/**
	 * sum a wave, write metadata and write signals *.
	 * 
	 * @param dir
	 *            the dir
	 * @param signals
	 *            the signals
	 * @param newName
	 *            the new name
	 * @param instrument
	 *            the instrument
	 * @return the fFT box
	 */
	protected static FFTBox sumAndWrite(File dir, Signal[] signals,
			String newName, String instrument) {
		Signal comb = null;
		// write the file
		FFTBox fftBox = null;
		try {
			comb = Gain.sumMono(signals);
			comb = Gain.normalise(comb);
			fftBox = FFTChain.polyFFTChain(comb);
			Wave newWave = WavePattern.addMetaData(comb, fftBox, "IAS8",
					instrument);
			newWave.writeFile(new File(dir.getAbsolutePath() + "/" + newName));
		} catch (SampleRateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fftBox;
	}

	/**
	 * Instantiates a new combine.
	 */
	public Combine() {
	}

}
