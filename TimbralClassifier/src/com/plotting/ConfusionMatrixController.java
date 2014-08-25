package com.plotting;
import java.awt.*;
import javax.swing.*;
import com.DSP.waveAnalysis.FFT;
import com.DSP.waveAnalysis.FFTBox;
import com.DSP.waveAnalysis.FrameFFT;
import com.neuralNet.matrix.ConfusionMatrix;
import com.riff.Signal;
import com.util.ArrayMethods;

/**
 * Controller for generating readable Confusion Matrixes graphs *.
 *
 * @author Jonny Wildey
 * @version 1.0
 */
public class ConfusionMatrixController extends Controller{
	
	private ConfusionMatrix confusionMatrix;
	
	/**
	 * Instantiates a new confusion matrix controller.
	 *
	 * @param confusionMatrix the confusion matrix
	 */
	public ConfusionMatrixController(ConfusionMatrix confusionMatrix) {
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.size = new Dimension(600, 560);
		this.confusionMatrix = confusionMatrix;
	}
	
	/**
	 * Instantiates a new confusion matrix controller.
	 *
	 * @param confusionMatrix the confusion matrix
	 * @param width the width
	 * @param height the height
	 */
	public ConfusionMatrixController(ConfusionMatrix confusionMatrix, int width, int height) {
		this.size = new Dimension(width, height);
		this.confusionMatrix = confusionMatrix;
	}
	
	/* (non-Javadoc)
	 * @see com.plotting.Controller#makeChart()
	 */
	public void makeChart() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createChart(confusionMatrix, size);
			}
		});
	}
	
	/**
	 * Creates the chart.
	 *
	 * @param confusionMatrix the confusion matrix
	 * @param winSize the win size
	 */
	private static void createChart(ConfusionMatrix confusionMatrix, Dimension winSize) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(winSize.width, 0, winSize.width, winSize.height);
		frame.setSize(winSize);
		//Variables
		frame.getContentPane().add(new ConfusionMatrixGraph(confusionMatrix, winSize));
		//frame.pack();
		frame.setVisible(true);
	}
	
	



	
}