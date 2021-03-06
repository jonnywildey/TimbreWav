package com.DSP.waveAnalysis;

import java.io.File;

import com.DSP.waveProcess.Gain;
import com.DSP.waveProcess.Pitch;
import com.riff.Signal;
import com.util.ArrayMethods;
import com.util.Log;
import com.util.fileReading.CSVWriter;

/**
 * Object for representing the data from Fourier Transforms. Would have called
 * it FFTTable but i found the two Ts confusing. The box contains a row
 * representing frequency (or some variant of) and at least 1 row (potentially
 * more if using windows) of fourier data. Also contains lots of useful static
 * methods for manipulating this data*
 * 
 * @author Jonny Wildey
 * @version 1.0
 */
public class FFTBox {

	/**
	 * Combines multiple FFT boxes into one. Assumes freq row is the same for
	 * all. *
	 * 
	 * @param fftBoxes
	 *            the fft boxes
	 * @return the fFT box
	 */
	protected static FFTBox combine(FFTBox... fftBoxes) {
		// get overall values length
		int length = 0;
		for (int i = 0; i < fftBoxes.length; ++i) {
			length += fftBoxes[i].getValues().length;
		}
		// insert values
		double[][] values = new double[length + 1][];
		// freqRow. Assume first freqBox is good
		values[0] = fftBoxes[0].getFreqRow();
		double[][] row = null;
		int c = 1;
		for (int i = 0; i < fftBoxes.length; ++i) {
			row = fftBoxes[i].getValues();
			for (int j = 0; j < row.length; ++j) {
				values[c] = row[j];
				c++;
			}
		}
		FFTBox fb = new FFTBox(values);
		return fb;
	}
	/**
	 * Combines the frequency row and values into one table *.
	 * 
	 * @param freqRow
	 *            the freq row
	 * @param values
	 *            the values
	 * @return the double[][]
	 */
	protected static double[][] combineFreqRowAndValues(double[] freqRow,
			double[][] values) {
		double[][] nt = new double[values.length + 1][];
		nt[0] = freqRow;
		for (int i = 1; i < nt.length; ++i) {
			nt[i] = values[i - 1];
		}
		return nt;
	}
	/**
	 * converts table to decibels *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @return the fFT box
	 */
	public static FFTBox convertTableToDecibels(FFTBox fftBox) {
		Signal s = new Signal(null, 0, 0); // 0 max amplitude
		return convertTableToDecibels(s, fftBox);
	}
	/**
	 * Converts tables amplitude values to decibels away from maximum amplitude
	 * *.
	 * 
	 * @param s
	 *            the s
	 * @param fftBox
	 *            the fft box
	 * @return the fFT box
	 */
	public static FFTBox convertTableToDecibels(Signal s, FFTBox fftBox) {
		double[][] table = fftBox.getTable();
		double max = Gain.amplitudeToDecibel(s.getMaxAmplitude());
		double[][] nt = new double[table.length][table[0].length];
		double min = Gain.amplitudeToDecibel(s.findSmallestAmplitude());
		nt[0] = table[0]; // freqrrow
		for (int i = 1; i < table.length; ++i) { // skip first column
			for (int j = 0; j < table[i].length; ++j) {
				nt[i][j] = Gain.amplitudeToDecibel(table[i][j]) - max;
				if (nt[i][j] == Double.NEGATIVE_INFINITY) {
					nt[i][j] = min - max * 2;
				}
			}
		}
		return new FFTBox(nt);
	}
	/**
	 * Filters an FFTBox by frequency. Does not care about frame size (as long
	 * as to and from frequencies exist). Also works for windowed FFTs *
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @return the fFT box
	 */
	public static FFTBox filter(FFTBox fftBox, double from, double to) {
		double[][] table = fftBox.getTable();
		// get min and max
		int min = 0;
		int max = 0;
		double[] freqRow = table[0];
		// get array positions of to and from
		for (int i = 0; i < freqRow.length; ++i) {
			if (freqRow[i] > from) {
				min = i;
				break;
			}
		}
		for (int i = freqRow.length - 1; i >= 0; --i) {
			if (freqRow[i] < to) {
				max = i;
				break;
			}
		}
		// make new table
		double[][] newt = new double[table.length][];
		for (int i = 0; i < newt.length; ++i) {
			newt[i] = ArrayMethods.getSubset(table[i], min, max);

		}
		return new FFTBox(newt, fftBox);
	}
	/**
	 * returns the table as bark subset *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @return the barked subset
	 */
	public static FFTBox getBarkedSubset(FFTBox fftBox) {
		return getHiResBarkedSubset(fftBox, 1);
	}
	/**
	 * Sum table, adding increasingly less value to further rows in time *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param exponent
	 *            the exponent
	 * @return the exponent fft box
	 */
	public static FFTBox getExponentFFTBox(FFTBox fftBox, double exponent) {
		double[][] table = fftBox.getTable();
		double[][] sums = new double[2][table[0].length];
		sums[0] = table[0]; // freq row
		for (int i = 0; i < table[0].length; ++i) {
			for (int j = 1; j < table.length; ++j) {
				sums[1][i] += table[j][i] * (Math.pow(exponent, j));
			}
		}
		return new FFTBox(sums, fftBox);
	}

	/**
	 * returns the table as bark subset with extra half measurements in the
	 * first x *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param div
	 *            the div
	 * @return the hi res barked subset
	 */
	public static FFTBox getHiResBarkedSubset(FFTBox fftBox, double div) {
		double[][] table = fftBox.getTable();
		// get tables
		int to = 0; // holder for array index
		int from = 0; // holder for array index
		int b = (int) (20 * (1 / div)); // how many barks max
		double lim = 0; // for determining cutoff
		double[][] nt = new double[table.length][b]; // new array
		table[0] = Pitch.freqToBark(table[0], div);
		double bCount = 1;
		for (int i = 0; i < b; ++i) { // for each bark
			nt[0][i] = bCount;

			lim = i + 1;
			bCount += 1;

			for (int j = to; j < table[0].length; ++j) { // cycle through from
				// last
				if (table[0][j] > lim | j >= table[0].length - 1) { // found new
					// value in
					// freq
					// table
					// Log.d(from + " " + to + " : " + table[0][j] + " " + j);
					lim = table[0][j]; // set to new
					from = to; // set to new
					to = j; // set to new
					for (int k = 1; k < table.length; ++k) { // input into new
						// array
						nt[k][i] = ArrayMethods.getAverageOfSubset(table[k],
								from, to);
					}
					break; // and out to i loop
				}
			}
		}
		return new FFTBox(nt);
	}

	/**
	 * returns the sum of the bins *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @return the sum fft box
	 */
	public static FFTBox getSumFFTBox(FFTBox fftBox) {
		return getSumTable(fftBox, fftBox.getTable().length);
	}

	/**
	 * returns the sum of the bins *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param count
	 *            the count
	 * @return the sum table
	 */
	public static FFTBox getSumTable(FFTBox fftBox, int count) {
		return getSumTable(fftBox, 0, count);
	}

	/**
	 * returns the sum of the bins *.
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param from
	 *            the from
	 * @param count
	 *            the count
	 * @return the sum table
	 */
	public static FFTBox getSumTable(FFTBox fftBox, int from, int count) {
		double[][] table = fftBox.getTable();
		if (count + 1 > table.length) {
			Log.d("Count larger than table length. Using table length");
			count = table.length - 1;
		}
		if (from + 1 > table.length) {
			Log.d("from larger than table length. Using 0");
			from = 0;
		}
		double[][] sums = new double[2][table[0].length];
		sums[0] = table[0]; // freq row
		for (int i = 0; i < table[0].length; ++i) {
			for (int j = from + 1; j < count + 1; ++j) {
				sums[1][i] += table[j][i];
			}
		}
		return new FFTBox(sums, fftBox);
	}

	/**
	 * converts frequecies to a power of 2. Useful for graphs *
	 * 
	 * @param fftBox
	 *            the fft box
	 * @return the fFT box
	 */
	public static FFTBox logarithmicFreq(FFTBox fftBox) {
		return logarithmicFreq(fftBox, 2);
	}

	/**
	 * converts frequecies to a power of x. *
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param power
	 *            the power
	 * @return the fFT box
	 */
	public static FFTBox logarithmicFreq(FFTBox fftBox, double power) {
		double[][] table = fftBox.getTable();
		// double min = ArrayStuff.getMin(table[0]);
		double[][] nt = ArrayMethods.copy(table);
		double mlog = Math.log(power);
		for (int i = 0; i < table[0].length; ++i) {
			nt[0][i] = Math.log(table[0][i]) / mlog;
		}
		return new FFTBox(nt);
	}

	/**
	 * Finds the maximum value in an FFT table and normalises the table to the
	 * ceiling. *
	 * 
	 * @param fftBox
	 *            the fft box
	 * @param ceiling
	 *            the ceiling
	 * @return the fFT box
	 */
	public static FFTBox normaliseTable(FFTBox fftBox, double ceiling) {
		double[][] table = fftBox.getTable();
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 1; i < table.length; ++i) {
			for (int j = 0; j < table[i].length; ++j) {
				max = (table[i][j] > max) ? table[i][j] : max;
			}
		}
		// Log.d("max:" + max);
		double[][] nt = new double[table.length][table[0].length];
		double factor = ceiling / max;
		nt[0] = table[0]; // get barks
		// Log.d("factor: " + factor);
		for (int i = 1; i < table.length; ++i) {
			for (int j = 0; j < table[i].length; ++j) {
				nt[i][j] = table[i][j] * factor;

			}
		}
		return new FFTBox(nt);
	}

	/**
	 * basic noise cancelling. Also removes first one of array
	 * 
	 * @param fftBox
	 *            the fft box
	 * @return *
	 */
	public static FFTBox sumDifference(FFTBox fftBox) {
		double[][] table = fftBox.getTable();
		double[][] nt = new double[table.length - 1][table[0].length];
		nt[0] = table[0];
		for (int i = 2; i <= nt.length; ++i) {
			for (int j = 0; j < table[i].length; ++j) {
				nt[i - 1][j] = (table[i - 1][j] - table[i][j]);
			}
		}
		// Log.d(Arrays.deepToString(nt));
		return new FFTBox(nt, fftBox);
	}

	double[][] table;

	int sampleFreq;

	int bits;

	int frameSize; // frame size of fft. Must be 2^

	boolean freq;

	boolean bark;

	boolean log;

	/**
	 * Instantiates a new fFT box.
	 * 
	 * @param fftData
	 *            the fft data
	 */
	public FFTBox(double[][] fftData) {
		this.setTable(fftData);
	}

	/**
	 * Constructor that takes soft information (bits, sampleRate) from fftBox *.
	 * 
	 * @param fftData
	 *            the fft data
	 * @param fftBox
	 *            the fft box
	 */
	public FFTBox(double[][] fftData, FFTBox fftBox) {
		this(fftData);
		this.setSoft(fftBox);
	}

	/**
	 * Instantiates a new fFT box.
	 * 
	 * @param fftData
	 *            the fft data
	 * @param s1
	 *            the s1
	 */
	public FFTBox(double[][] fftData, Signal s1) {
		this(fftData);
		this.setFromSignal(s1);
	}

	public FFTBox(FFTBox fftBox) {
		this(ArrayMethods.copy(fftBox.getTable()));
		this.sampleFreq = fftBox.getSampleFreq();
		this.bits = fftBox.getBits();
	}

	/**
	 * Converts the table to bark scale, if it isn't already *.
	 */
	public void convertToBark() {
		if (!this.bark) {
			// do thing

			this.bark = true;
			this.freq = false;
			this.log = false;
		}
	}

	/**
	 * Converts the table to freq scale, if it isn't already *.
	 */
	public void convertToFreq() {
		if (!this.freq) {
			// do thing

			this.bark = false;
			this.freq = true;
			this.log = false;
		}
	}

	/**
	 * Converts the table to log scale, if it isn't already *.
	 */
	public void convertToLog() {
		if (!this.log) {
			// do thing

			this.bark = false;
			this.freq = false;
			this.log = true;
		}
	}

	/**
	 * Export to csv.
	 * 
	 * @param file
	 *            the file
	 */
	public void exportToCSV(File file) {
		CSVWriter csvWriter = new CSVWriter(file.getAbsolutePath());
		if (this.getValues() != null) {
			// flip so excel doesn't complain
			csvWriter.writeArraytoFile(ArrayMethods.flip(this.getTable()));
		}
	}

	/**
	 * Gets the bits.
	 * 
	 * @return the bits
	 */
	public int getBits() {
		return bits;
	}

	/**
	 * Gets the frame.
	 * 
	 * @return the frame
	 */
	public int getFrame() {
		return frameSize;
	}

	/**
	 * Returns the frequency row in its current form *.
	 * 
	 * @return the freq row
	 */
	public double[] getFreqRow() {
		if (tableExists()) {
			return table[0];
		} else {
			Log.d("ARGHH");
			return null;
		}
	}

	/**
	 * Gets the sample freq.
	 * 
	 * @return the sample freq
	 */
	public int getSampleFreq() {
		return sampleFreq;
	}

	/**
	 * get frequency row and values as a single 2d table *.
	 * 
	 * @return the table
	 */
	public double[][] getTable() {
		return table;
	}

	/**
	 * return the values (without the frequency row) *.
	 * 
	 * @return the values
	 */
	public double[][] getValues() {

		double[][] vals = new double[this.table.length - 1][];
		for (int i = 0; i < vals.length; ++i) {
			vals[i] = this.table[i + 1];
		}
		return vals;
	}

	/**
	 * returns how many windows in table *.
	 * 
	 * @return the windows
	 */
	public int getWindows() {
		if (tableExists()) {
			return table.length - 1;
		} else {
			return 0;
		}
	}

	/**
	 * Sets the frame size. Be careful as this can be different than the table
	 * lengths and could badly effect normalisation. *
	 * 
	 * @param frameSize
	 *            the new frame size
	 */
	public void setFrameSize(double frameSize) {
		this.frameSize = (int) frameSize;
	}

	/**
	 * Sets the frame size. Be careful as this can be different than the table
	 * lengths and could badly effect normalisation. *
	 * 
	 * @param frameSize
	 *            the new frame size
	 */
	public void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}

	/**
	 * Sets the frequency row. Assumes hz *
	 * 
	 * @param freqRow
	 *            the new freq row
	 */
	public void setFreqRow(double[] freqRow) {
		// check if freq row length and f lengths are the same?
		this.table[0] = freqRow;
		this.bark = false;
		this.freq = true;
		this.log = false;
	}

	/**
	 * Sets the from signal.
	 * 
	 * @param s1
	 *            the new from signal
	 */
	private void setFromSignal(Signal s1) {
		if (s1 != null) {
			this.sampleFreq = s1.getSampleRate();
			this.bits = s1.getBit();
		}
	}

	/**
	 * sets everything apart from table data *.
	 * 
	 * @param fftBox
	 *            the new soft
	 */
	private void setSoft(FFTBox fftBox) {
		this.bark = fftBox.bark;
		this.freq = fftBox.freq;
		this.log = fftBox.log;
		this.frameSize = fftBox.frameSize;
		this.bits = fftBox.bits;
		this.sampleFreq = fftBox.sampleFreq;

	}

	/**
	 * Sets the table.
	 * 
	 * @param fftData
	 *            the new table
	 */
	private void setTable(double[][] fftData) {
		this.table = fftData;
		this.frameSize = fftData[0].length;
		this.freq = true;
		this.bark = false;
		this.log = false;
	}

	/**
	 * Does the table exist? *.
	 * 
	 * @return true, if successful
	 */
	private boolean tableExists() {
		return (this.table != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.table.length * 15);
		double[] fr = this.getFreqRow();
		double[][] vals = this.getValues();
		for (int i = 0; i < vals.length; ++i) {
			for (int j = 0; j < vals[i].length; ++j) {
				sb.append(Statistics.round(fr[j], 4) + "hz ");
				sb.append(Statistics.round(vals[i][j], 4) + " ");
			}
			sb.append("/t");
		}
		return sb.toString();
	}
}
