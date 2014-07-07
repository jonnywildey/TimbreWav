package com.matrix;

import java.util.Arrays;

import neuralNet.NNFunctions;

public class ConfusionMatrix extends Matrix {

	public ConfusionMatrix(int size) {
		super(size);
		this.initValue(new Integer(0));
	}
	
	/** get maximum value of matrix **/
	public int getMax() {
		Integer max = 0;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				if ((Integer)array[i][j] > max) {
					max = (Integer) array[i][j];
				}
			}
		}
		return max.intValue();
	}
	
	public int getCount() {
		Integer count = 0;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
					count += (Integer) array[i][j];
			}
		}
		return count.intValue();
	}
	
	/** get total of matrix **/
	public int getTotal() {
		Integer tally = 0;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				tally += (Integer)array[i][j];
			}
		}
		return tally;
	}
	
	public void addToCell(int column, int row) {
		this.array[column][row] = (Integer)this.array[column][row] + 1;
	}
	
	public double matthewsCoefficient(int col) {
		double truePos = (Integer)array[col][col];
		double trueNeg = getTrueNeg();
		double falsePos = getFalsePositives(col);
		double falseNeg = getFalseNegatives(col);
		Double mat = 0d;
		//System.out.println("tp " + truePos + " tn " + trueNeg + 
		//					" fp " + falsePos + " fn " + falseNeg);
		mat = (truePos * trueNeg) / ((truePos + falseNeg) * (truePos + falsePos) *
									(trueNeg + falseNeg) * (trueNeg + falsePos));
		if (mat.isNaN()) {
			return 0; // 0 error seems to cause NaN...
		} else {
			return mat;
		}
		
	}
	
	public double matthewsCoefficient() {
		double[] ms = new double[array.length];
		for (int i = 0; i < array.length; ++i) {
			ms[i] = matthewsCoefficient(i);
		}
		return NNFunctions.average(ms);
	}
	
	private int getFalsePositives(int col) {
		int total = 0;
		for (int i = 0; i < array.length; ++i) {
			if (i != col) {
				total += (Integer)array[i][col];
			}
		}
		return total;
	}
	
	private int getFalseNegatives(int col) {
		int total = 0;
		for (int i = 0; i < array.length; ++i) {
			if (i != col) {
				total += (Integer)array[col][i];
			}
		}
		return total;
	}
	
	private int getTrueNeg() {
		int total = 0;
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array.length; ++j) {
				if (j != i) {
					total += (Integer)array[i][j];
				}
			}
		}
		return total;
	}
	
	/** returns rate of correct classifications to incorrect (i.e. 1 is perfect)**/
	public double getErrorRate(double total) {
		double rightCount = 0;
		double wrongCount = 0;
		for (int i = 0; i < this.size; ++i) {
					rightCount += (Integer)this.array[i][i]; //this will work right?
					for (int j = 0; j < this.size; ++j) {
						if (j != i) {
							wrongCount += (Integer)this.array[i][j];
						}
					}
			}
		
		if (rightCount + wrongCount < total) {
			return (total / rightCount) - 1;
		} else {
			
			return wrongCount / rightCount;
		}
		
		
	}
	
	public String toString() {
		//get max
		int max = this.getMax();
		//get length of max value.
		int length = (int)Math.floor(Math.log10((double)max)) + 1;
		String mainString = "";
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				
				String str = "|" + array[i][j].toString();
				while (str.length() <= length) {
					str = str.concat(" ");
				}
				mainString = mainString.concat(str);
			}
			mainString = mainString.concat("|\n");
		}
		//System.out.println(Arrays.deepToString(array));
		return mainString;
	}
	


}
