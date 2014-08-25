package com.neuralNet.layers;

import java.io.Serializable;
import java.util.ArrayList;

import com.neuralNet.NNFunctions;
import com.neuralNet.pattern.Pattern;

public class NeuralLayer implements Serializable {
	
	private static final long serialVersionUID = -8167132484261105715L;
	public NeuralLayer inputLayer;
	public NeuralLayer outputLayer;
	public ArrayList<Neuron> neurons;
	public int neuronCount;
	public int layerNumber;

	public NeuralLayer() {
		neurons = new ArrayList<Neuron>();
		neuronCount = 0;
		layerNumber = 0;
	}
	
	/** Copy Constructor **/
	public NeuralLayer(NeuralLayer neuralLayer) {
		
		this.neurons = new ArrayList<Neuron>();
		for (Neuron n : neuralLayer.neurons) {
			this.neurons.add(new Neuron(n));
		}
		this.neuronCount = this.neurons.size();
		this.layerNumber = neuralLayer.layerNumber;
		
		if (neuralLayer.outputLayer != null) {
			this.outputLayer = new NeuralLayer(neuralLayer.outputLayer);
			this.outputLayer.inputLayer = this;
		} 

	}


	/** returns true if this layer is last of network **/
	public boolean isLast() {
		if (this.outputLayer == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isFirst() {
		if (this.inputLayer == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public void initialiseLayer(int inputCount) {
		int ic = (this.isFirst()) ? inputCount : this.inputLayer.neuronCount;
		for (int i = 0; i < neuronCount; ++i) {
			Neuron n = new Neuron(i, ic, layerNumber);
			neurons.add(n);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(); 
		sb.append("Layer number: " + this.layerNumber);
		sb.append(" Count " + this.neuronCount);
		return sb.toString();
	}
	
	public String toStringVerbose() {
		StringBuilder sb = new StringBuilder(); 
		sb.append("Layer number: " + this.layerNumber);
		sb.append(" Count " + this.neuronCount);
		for (Neuron n : this.neurons) {
			sb.append(n.toString());
		}
		return sb.toString();
	}
		
	
	/** calculates delta values for entire layer and sets weights. returns pattern error **/
	public void calculateDelta(Pattern p) {

		if (this.isLast()) {	
			for (Neuron n : this.neurons) {
				n.setTarget(p.getTargetArray().get(n.getId())); //set target
				if (n.learning) {
					n.delta = (n.target - n.output) * NNFunctions.sigmoidDerivative(n.activation);
				}	
			}
		} else {
			for (Neuron n: this.neurons) {
				if (n.learning) {
					double sum = 0;
					for (Neuron nn : this.outputLayer.neurons) {
						sum += nn.delta * nn.getWeightList().get(n.getId());
						//System.out.println(nn.weightList.get(n.id) + "\t" + nn.delta);
					}
					n.delta = sum * NNFunctions.sigmoidDerivative(n.activation);
				}
			}
		}
	}
	
	/** Processes entire layer. Pattern p necessary for first and 
	 * last layers
	 * @param p
	 */
	public void process(Pattern p) {
		if (this.isFirst()) {
			for (Neuron n : this.neurons) {
				double sum = 0;
				for (int i = 0; i < p.getInputArray().size(); ++i) {
					sum += (p.getInputArray().get(i).getValue() * n.getWeightList().get(i)); 
				} 
				//and bias
				sum += n.getWeightList().get(n.getWeightList().size() - 1);
				//System.out.println("bias: " + n.weightList.get(n.weightList.size() - 1));
				n.activation = sum;
				n.output = NNFunctions.sigmoid(sum);
				//System.out.println("output: " + n.output + "\tactivation: " + n.activation + "\t" + n.id);
			}
		} else {
			for (Neuron n : this.neurons) {
				double sum = 0;
				for (int i = 0; i < this.inputLayer.neurons.size(); ++i) {
					sum += (this.inputLayer.neurons.get(i).getValue() * n.getWeightList().get(i)); 
					//System.out.println("input: " + this.inputArray.get(i) +
					//" current weight: " + this.weightList.get(i) + " sum: " + sum);
				}
				//and bias
				sum += n.getWeightList().get(n.getWeightList().size() - 1);
				n.activation = sum;
				n.output = NNFunctions.sigmoid(sum);
			}
		}
	}
	
	public void setTrainingRate(double trainingRate) {
		for (Neuron n : this.neurons) {
			n.trainingRate = trainingRate;
		}
	}


	public Double getErrors() {
		double sum = 0;
		for (Neuron n : this.neurons) {
			sum += n.getError();
		}
		return sum;
	}
	
	public void setWeights(Pattern p) {
		for (Neuron n : this.neurons) {
			if (n.learning) {
				for (int i = 0; i < n.getWeightList().size(); ++i) {
					Double weightChange = 1 * n.trainingRate * n.delta; 
					if (i < n.getWeightList().size() - 1) { //bias will always have a signal
						if (this.isFirst()) {
							weightChange *= p.getInputArray().get(i).getValue(); //first layer pattern input
						} else {
							weightChange *= this.inputLayer.neurons.get(i).getValue(); //only change if it had an input signal
						}
					}
					n.getWeightList().set(i, n.getWeightList().get(i)  + weightChange);// current weight NOT RIGHT
					}
			}
			//System.out.println(n.toString());
		}	
	}

}
