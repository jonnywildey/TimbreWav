package neuralNet;

import java.util.ArrayList;

import com.matrix.ConfusionMatrix;

import filemanager.ArrayMethods;
import filemanager.Log;

/** Class for combining multiple nets **/
public class Committee {
	
	private MultiLayerNet[] nets;
	
	
	public static int runPattern(MultiLayerNet[] nets, Pattern p) {
		
		
		return 0;
	}
	
	public static int[] runPatterns(MultiLayerNet[] nets, Pattern[] patterns) {
		double[][][] vals = new double[nets.length][][];
		Epoch e = null;
		//get values
		for (int i = 0; i < vals.length; ++i) {
			e = new Epoch(null, null, nets[i].getNeuronLayers(), 0d, nets[i].isVerbose(), nets[i].isDebug());
			vals[i] = e.runPatterns(patterns);
		}
		
		double[][][] nv = new double[patterns.length][nets.length][patterns[0].getOutputCount()];
		int[] outputs = new int[patterns.length];
		for (int i = 0; i < patterns.length; ++i) {
			//get array of every net for pattern
			for (int j = 0; j < nets.length; ++j) {
				for (int k = 0; k < patterns[0].getOutputCount(); ++k) {
					nv[i][j][k] = vals[j][i][k];
				}
			}
			outputs[i] = getIndexOfMax(
					ArrayMethods.getAverageOfColumn(nv[i]));
		}
		return outputs;
	}
	
	/**return the index of the max **/
	public static int getIndexOfMax(double[] array) {
		double max = Double.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < array.length; ++i) {
			if (array[i] > max) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}
	
	public static ConfusionMatrix testPatterns(MultiLayerNet[] nets, Pattern[] patterns) {
		int[] outputs = runPatterns(nets, patterns);
		ConfusionMatrix cm = new ConfusionMatrix(patterns[0].getOutputCount());
		for (int i = 0; i < patterns.length; ++i) {
			cm.addToCell(outputs[i], patterns[i].getTargetNumber());
		}
		Log.d(cm.toString());
		Log.d("MC for Committee: " + cm.matthewsCoefficient());
		return cm;
	}
	
	public static ConfusionMatrix testPatterns(MultiLayerNet[] nets, ArrayList<Pattern> patterns) {
		return testPatterns(nets, TestPatterns.convertPatterns(patterns));
	}

	public MultiLayerNet[] getNets() {
		return nets;
	}

	public void setNets(MultiLayerNet[] nets) {
		this.nets = nets;
	}
	
}