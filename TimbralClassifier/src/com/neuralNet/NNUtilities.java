package com.neuralNet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.neuralNet.pattern.Pattern;
import com.util.ArrayMethods;

/**
 * Various utilities for helping to use data in Neural Networks *.
 * 
 * @author Jonny Wildey
 * @version 1.0
 */
public class NNUtilities {

	/**
	 * Converts an array to a unique bitArray (e.g. 001, 010, 100) the length of
	 * the array *
	 * 
	 * @param array
	 *            the array
	 * @return the double[][]
	 */
	public static double[][] convertToUniqueBits(Object[] array) {
		return convertToUniqueBits(array, false);
	}

	/**
	 * Converts an array to a unique bitArray (e.g. 001, 010, 100) the length of
	 * the array *
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the double[][]
	 */
	public static double[][] convertToUniqueBits(Object[] array, boolean verbose) {
		int length = array.length;
		double[][] bArray = new double[length][length];
		int i = 0;
		for (@SuppressWarnings("unused")
				Object val : array) { // don't actually need value
			for (int j = 0; j < length; ++j) { // Think it is a bit confusing if
				// I for each this...
				bArray[i][j] = 0; // every value 0 except...
			}
			bArray[i][length - i - 1] = 1; // ...this guy
			i++;
		}
		if (verbose) {
			System.out.println("Unique Bit table:\n" + Arrays.toString(array)
					+ "\n" + Arrays.deepToString(bArray));
		}
		return bArray;
	}

	/**
	 * creates a table showing target value (arbitrary) and bitArray it should
	 * be assigned to *.
	 * 
	 * @param array
	 *            the array
	 * @return the double[][][]
	 */
	public static double[][][] createConversionTable(double[] array) {
		return createConversionTable(array, false);
	}

	/**
	 * creates a table showing target value (arbitrary) and bitArray it should
	 * be assigned to *.
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the double[][][]
	 */
	public static double[][][] createConversionTable(double[] array,
			boolean verbose) {
		int length = array.length;
		double[][][] bArray = new double[length][2][length];
		int i = 0;
		for (double val : array) { // don't actually need value
			bArray[i][0] = new double[] { val }; // original value
			for (int j = 0; j < length; ++j) { // Think it is a bit confusing if
				// I for each this...
				bArray[i][1][j] = 0; // every value 0 except...
			}
			bArray[i][1][length - i - 1] = 1; // this guy
			i++;
		}
		if (verbose) {
			System.out.println("Conversion table:\n"
					+ Arrays.deepToString(bArray));
		}
		return bArray;
	}

	/**
	 * creates a table showing target value (arbitrary) and bitArray it should
	 * be assigned to *.
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the string[][][]
	 */
	public static String[][][] createConversionTable(String[] array,
			boolean verbose) {
		int length = array.length;
		String[][][] bArray = new String[length][2][length];
		int i = 0;
		for (String val : array) { // don't actually need value
			bArray[i][0] = new String[] { array[i] }; // original value
			for (int j = 0; j < length; ++j) { // Think it is a bit confusing if
				// I for each this...
				bArray[i][1][j] = "0"; // every value 0 except...
			}
			bArray[i][1][length - i - 1] = "1"; // this guy
			i++;
		}
		if (verbose) {
			System.out.println("Conversion table:\n"
					+ Arrays.deepToString(bArray));
		}
		return bArray;
	}

	/**
	 * assumes last value is target and turns rest into float array *.
	 * 
	 * @param array
	 *            the array
	 * @return the float[][]
	 */
	public static float[][] createInputArray(double[][] array) {
		return createInputArray(array, false);
	}

	/**
	 * assumes last value is target and turns rest into float array *.
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the float[][]
	 */
	public static float[][] createInputArray(double[][] array, boolean verbose) {
		float[][] newArray = new float[array.length][];
		int i = 0;
		for (double[] row : array) {
			float[] newRow = new float[row.length - 1];
			int j = 0;
			for (@SuppressWarnings("unused")
					float val : newRow) {
				newRow[j] = (float) row[j];
				++j;
			}
			newArray[i] = newRow;
			++i;
		}
		if (verbose) {
			System.out
			.println("Input Array:\n" + Arrays.deepToString(newArray));
		}
		return newArray;
	}

	/**
	 * converts a simple data table into the training patterns necessary for NNs
	 * *.
	 * 
	 * @param array
	 *            the array
	 * @return the array list
	 */
	public static ArrayList<Pattern> createPatterns(double[][] array) {
		return createPatterns(array, false);
	}

	/**
	 * converts a simple data table into the training patterns necessary for NNs
	 * *.
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the array list
	 */
	public static ArrayList<Pattern> createPatterns(double[][] array,
			boolean verbose) {
		double[][][] convTable = createTargetConversionTable(array, verbose);
		ArrayList<Pattern> patterns = new ArrayList<Pattern>(array.length);
		int fLength = array[0].length - 1; // length of input array
		int cLength = convTable.length; // length of target array
		for (int i = 0; i < array.length; ++i) {
			// get mini float array
			ArrayList<Double> inputArray = new ArrayList<Double>(fLength);
			for (int j = 0; j < fLength; ++j) {
				inputArray.add(array[i][j]);
			}
			// convert target value to bit
			ArrayList<Double> outArray = new ArrayList<Double>(fLength);
			for (int j = 0; j < cLength; ++j) {
				if (array[i][fLength] == convTable[j][0][0]) { // EWWW.
					for (double c : convTable[j][1]) {
						outArray.add(c);
					}
				}
			}
			patterns.add(new Pattern(inputArray, outArray, i));
		}
		if (verbose) {
			System.out.println("Patterns:\n" + patterns.toString());
		}
		return patterns;
	}

	/**
	 * gets target Conversion from .csv style array *
	 * 
	 * @param array
	 *            the array
	 * @return the double[][][]
	 */
	public static double[][][] createTargetConversionTable(double[][] array) {
		return createTargetConversionTable(array, false);
	}

	/**
	 * gets target Conversion from .csv style array *
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the double[][][]
	 */
	public static double[][][] createTargetConversionTable(double[][] array,
			boolean verbose) {
		array = ArrayMethods.flip(array); // flip table to make sense
		Double[][] nc = getCount(array[array.length - 1], verbose); // assumes
		// the
		// target is
		// the last
		// column in
		// array
		return createConversionTable(ArrayMethods.flip(Doubletodouble(nc))[0],
				verbose);
	}

	/**
	 * Creates a unique bitArray (e.g. 001, 010, 100) input long *
	 * 
	 * @param length
	 *            the length
	 * @return the double[][]
	 */
	public static double[][] createUniqueBits(int length) {
		return createUniqueBits(length, false);
	}

	/**
	 * Creates a unique bitArray (e.g. 001, 010, 100) input long *
	 * 
	 * @param length
	 *            the length
	 * @param verbose
	 *            the verbose
	 * @return the double[][]
	 */
	public static double[][] createUniqueBits(int length, boolean verbose) {
		double[][] bArray = new double[length][length];
		for (int i = 0; i < length; ++i) { // don't actually need value
			for (int j = 0; j < length; ++j) { // Think it is a bit confusing if
				// I for each this...
				bArray[i][j] = 0; // every value 0 except...
			}
			bArray[i][length - i - 1] = 1; // this guy
		}
		if (verbose) {
			System.out.println("Unique Bit table:\n"
					+ Arrays.deepToString(bArray));
		}
		return bArray;
	}

	/**
	 * converts Double to double.*
	 * 
	 * @param array
	 *            the array
	 * @return the double[][]
	 */
	public static double[][] Doubletodouble(Double[][] array) {
		double[][] nArray = new double[array.length][];
		int i = 0;
		for (Double[] row : array) {
			double[] newRow = new double[row.length];
			int j = 0;
			for (Double d : row) {
				newRow[j] = d.doubleValue();
				j++;
			}
			nArray[i] = newRow;
			i++;
		}
		return nArray;
	}

	/**
	 * return a 2d array with the value and its count. Can deal with multiples.
	 * Values should be in the same order as array (with subsequent duplicate
	 * values removed). *
	 * 
	 * @param array
	 *            the array
	 * @return the count
	 */
	public static Double[][] getCount(double[] array) {
		return getCount(array, false);
	}

	/**
	 * return a 2d array with the value and its count. Can deal with multiples.
	 * Values will be in the same order as array (with subsequent duplicate
	 * values removed). *
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the count
	 */
	public static Double[][] getCount(double[] array, boolean verbose) {
		ArrayList<Double[]> countArray = new ArrayList<Double[]>();
		double count = 0;
		for (int i = 0; i < array.length; ++i) {
			// check if list contains current value
			boolean unique = true;
			for (Double[] ia : countArray) {
				if (ia[0].doubleValue() == array[i]) {
					unique = false;
				}
			}
			// Count values
			if (unique) {
				count = 1;
				for (int j = i + 1; j < array.length; ++j) {
					if (array[i] == array[j]) {
						count++;
					}
				}
				countArray.add(new Double[] { array[i], count });
			}
		}
		Double[][] doubleArray = new Double[countArray.size()][];

		doubleArray = countArray.toArray(doubleArray);
		if (verbose) {
			System.out.println("Value and count of value:\n"
					+ Arrays.deepToString(doubleArray));
		}
		return doubleArray;
	}

	/**
	 * return a 2d array with the value and its count. Can deal with multiples.
	 * Values will be in the same order as array (with subsequent duplicate
	 * values removed). *
	 * 
	 * @param array
	 *            the array
	 * @param verbose
	 *            the verbose
	 * @return the count
	 */
	public static String[][] getCount(String[] array, boolean verbose) {
		ArrayList<String[]> countArray = new ArrayList<String[]>();
		double count = 0;
		for (int i = 0; i < array.length; ++i) {
			// check if list contains current value
			boolean unique = true;
			for (String[] ia : countArray) {
				if (ia[0].equals(array[i])) {
					unique = false;
				}
			}
			// Count values
			if (unique) {
				count = 1;
				for (int j = i + 1; j < array.length; ++j) {
					if (array[i] == array[j]) {
						count++;
					}
				}
				countArray
				.add(new String[] { array[i], String.valueOf(count) });
			}
		}
		// Log.d(countArray);
		String[][] stringArray = new String[countArray.size()][];
		stringArray = countArray.toArray(stringArray);
		if (verbose) {
			System.out.println("Value and count of value:\n"
					+ Arrays.deepToString(stringArray));
		}
		return stringArray;
	}

	/**
	 * must be arraylist because generics suck *.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param set
	 *            the set
	 * @return the array list
	 */
	public static <T> ArrayList<T> knuthShuffle(ArrayList<T> set) {
		return NNUtilities.knuthShuffle(set, System.currentTimeMillis());
	}

	/**
	 * must be arraylist because generics suck *.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param set
	 *            the set
	 * @param seed
	 *            the seed
	 * @return the array list
	 */
	public static <T> ArrayList<T> knuthShuffle(ArrayList<T> set, long seed) {
		Random r = new Random(seed);
		return knuthShuffle(set, r);
	}

	/**
	 * must be arraylist because generics suck *.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param set
	 *            the set
	 * @param r
	 *            the r
	 * @return the array list
	 */
	public static <T> ArrayList<T> knuthShuffle(ArrayList<T> set, Random r) {
		ArrayList<T> holder = new ArrayList<T>(1); // ugh
		int j = 0;
		for (int i = set.size() - 1; i > 0; --i) {
			j = (int) Math.rint(r.nextDouble() * i); // random number
			holder.add(set.get(j));
			set.set(j, set.get(i));
			set.set(i, holder.get(0));
			holder.remove(0);
		}
		return set;
	}

	/**
	 * remove null rows from a table *.
	 * 
	 * @param table
	 *            the table
	 * @return the double[][]
	 */
	public static double[][] removeNulls(double[][] table) {
		return removeNulls(table, false);
	}

	/**
	 * remove null rows from a table *.
	 * 
	 * @param table
	 *            the table
	 * @param verbose
	 *            the verbose
	 * @return the double[][]
	 */
	public static double[][] removeNulls(double[][] table, boolean verbose) {
		if (verbose) {
			System.out.println("Removing null rows...");
		}
		int counter = 0;
		// get size of non-null table
		for (double[] row : table) {
			if (row.length > 0) {
				counter++;
			}
		}
		double[][] newTable = new double[counter][];
		counter = 0;
		for (double[] row : table) {
			if (row.length > 0) {
				newTable[counter] = row;
				counter++;
			}
		}
		return newTable;
	}

	/**
	 * remove null rows from a table *.
	 * 
	 * @param table
	 *            the table
	 * @param verbose
	 *            the verbose
	 * @return the string[][]
	 */
	public static String[][] removeNulls(String[][] table, boolean verbose) {
		if (verbose) {
			System.out.println("Removing null rows...");
		}
		int counter = 0;
		// get size of non-null table
		for (String[] row : table) {
			if (row.length > 0) {
				counter++;
			}
		}
		String[][] newTable = new String[counter][];
		counter = 0;
		for (String[] row : table) {
			if (row.length > 0) {
				newTable[counter] = row;
				counter++;
			}
		}
		return newTable;
	}

}
