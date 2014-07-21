package waveProcess;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import javax.naming.InvalidNameException;

import neuralNet.Pattern;
import neuralNet.WavePattern;
import neuralNet.WavePatterns;
import filemanager.ArrayStuff;
import filemanager.CSVString;
import filemanager.HexByte;
import filemanager.Log;
import filemanager.Serialize;
import riff.*;
import waveAnalysis.FrameFFT;

public class Samples {
	
	public static void batchFolder(File sampleDir, File newDir) {
		String instrument = sampleDir.getName();
		File[] files = getActualFiles(sampleDir);
		Random pitchRand = new Random();
		Random noiseRand = new Random();
		Random hpRand = new Random();
		Random lpRand = new Random();
		
		for (File nf: files) {
			for (int i = 0; i < 20; ++i) {	
				Wave nw = new Wave(nf);
				Signal s1 = nw.getSignals();
				s1 = Gain.getMid(s1);
				s1 = randomPitch(s1, 24, pitchRand);
				s1 = Gain.volume(s1, -6);
				s1 = addNoise(s1, noiseRand);
				s1 = randomHP(s1, 0.1, hpRand);
				s1 = randomLP(s1, 0.1, lpRand);
				s1 = EQFilter.highPassFilter(s1, 40, 0.72);
				s1 = EQFilter.lowPassFilter(s1, 20000, 0.72);
				s1 = Gain.normalise(s1);
				Wave newWave = addMetaData(s1, "IAS8", instrument);			
				newWave.writeFile(new File(newDir.getAbsolutePath() + 
						"/" + instrument + i + nf.getName()));
			}
			
		}
	}
	
	public static File[] getActualFiles(File dir) {
		if (dir.isDirectory()) {
			File[] allFiles = dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir,
			               String name) {
					File f = new File(dir.getAbsolutePath() + "/" + name);
					return (!name.startsWith(".") & !f.isDirectory());
				}
			});
			return allFiles;
		} else {
			return null;
		}
		
	}
	
	/**Applies a random pitch change to the file **/
	public static Signal randomPitch(Signal s, double range, Random r) {
		double semi = (range * r.nextDouble()) - (range * 0.5);
		return Pitch.pitchShift(s, semi);
	}
	
	/**chance is a percentage. Normally 0.1 **/
	public static Signal randomHP(Signal s, double chance, Random r) {
		double freq = r.nextDouble() * 130 + 100;
		if (r.nextDouble() > chance) {
			s = EQFilter.highPassFilter(s, freq, 0.72);
		}
		return s;
	}
	
	/**chance is a percentage. Normally 0.1 **/
	public static Signal randomLP(Signal s, double chance, Random r) {
		double freq = 10000 - r.nextDouble() * 6000;
		if (r.nextDouble() > chance) {
			s = EQFilter.lowPassFilter(s, freq, 0.72);
		}
		return s;
	}
	
	
	
	/** Get Waves **/
	public static Wave[] getWavs(File dir) {
		File[] files = getActualFiles(dir);
		Wave[] waves = new Wave[files.length];
		for (int i = 0; i < files.length; ++i) {
			waves[i] = new Wave(files[i]);
		}
		return waves;
	}
	
	public static Signal addNoise(Signal s, Random nr) {
		double nc = nr.nextDouble();
		double maxLoud = -36;
		double loudness = maxLoud - (nr.nextDouble() * 60);
		//Log.d(loudness);
		try {
			//pick which
			if (nc < 0.3333) {
				Signal s2 = Gen.pinkNoise(s.getLength(), 0, s.getSampleRate(), s.getBit());
				s2 = Gain.volumeRMS(s2, loudness);
				s = Gain.sumMono(s, s2);
			} else if(nc < 0.6666) {
				Signal s2 = Gen.whiteNoise(s.getLength(), 0, s.getSampleRate(), s.getBit());
				s2 = Gain.volumeRMS(s2, loudness);
				s = Gain.sumMono(s, s2);
			} else {
				Signal s2 = Gen.tapeHiss(s.getLength(), 0, s.getSampleRate(), s.getBit());
				s2 = Gain.volumeRMS(s2, loudness);
				s = Gain.sumMono(s, s2);
			}
		} catch (Exception e) {
			
		}
		return s;
	}
	
	/** Adds meta data to a Wave file **/
	public static Wave addMetaData(Signal signal, String type, String input) {
		Chunk fftChunk = getFFTData(signal);
		Chunk metaChunk = new Chunk();
		Chunk artist = new Chunk();
		InfoChunk infoChunk = new InfoChunk();
		Wave wav = new Wave();
		wav.setSignal(signal);
		try {
			artist.setName("IART");
			artist.setData("Jonny Wildey");
			metaChunk.setName(type);
			metaChunk.setData(input);
			infoChunk.addChunk(artist);
			infoChunk.addChunk(metaChunk);
			infoChunk.addChunk(fftChunk);
			wav.addChunk(infoChunk);
			Log.d(infoChunk.toStringRecursive());
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wav;
	}
	
	public static Chunk getFFTData(Signal s) {
		//4096 works well
		try {
			FrameFFT fft = new FrameFFT(s, 4096);
			double[][] dd = fft.analyse(20, 20000);
			//dd = FrameFFT.getExponentTable(dd, 0.7); //rate
			dd = FrameFFT.getSumTable(dd);
			dd = FrameFFT.getBarkedSubset(dd);
			
			//dd = FrameFFT.convertTableToDecibels(fft.signal, dd, fft.frameSize);
			String str = ArrayStuff.arrayToString(dd);
			Chunk chunk = new Chunk();
			chunk.setName("IAS7");
			chunk.setData(str);
			return chunk;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Cello"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Marimba"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Harp"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Trombone"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch")); 
		
		Wave[] waves = getWavs(new File(
				"/Users/Jonny/Documents/Timbre/Samples/Batch"));
		WavePatterns wp = new WavePatterns();
		wp.waves = waves;
		wp.wavToPattern();
		for (int i = 0; i < wp.patterns.length; ++i) {
			Log.d(wp.patterns[i]);
		}
		
		Serialize.serialize(wp, "/Users/Jonny/Documents/Timbre/WavePatterns.ser");
		
	}
	
	/*public static void main(String[] args) {
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Cello"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Marimba"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Harp"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
		batchFolder(new File("/Users/Jonny/Documents/Timbre/Samples/Trombone"), 
				new File("/Users/Jonny/Documents/Timbre/Samples/Batch"));
	} */
	
	
}