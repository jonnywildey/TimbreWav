package com.plotting;
import java.awt.*;

import javax.swing.*;

import com.riff.Signal;
import com.waveAnalysis.FFT;
import com.waveAnalysis.FFTBox;
import com.waveAnalysis.FrameFFT;

/** Controller for the FFT graph **/
public class FFTController {
	
	private Dimension size;
	private double[][] table;
	
	public FFTController(double[][] table) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.size = new Dimension(screenSize.width, screenSize.height / 3);
		this.table = table;
	}
	
	public FFTController(FFTBox fftBox, int width, int height) {
		this.size = new Dimension(width, height);
		this.table = fftBox.getTable();
	}
	
	public void makeChart() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createChart(table, size);
			}
		});
	}
	
	
	private static void createChart(double[][] table, Dimension winSize) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(winSize.width, 0, winSize.width, winSize.height);
		frame.setSize(winSize);
		//Variables
		//String[] names = new String{""}; 
		String[] axisLabels = {"DB", "Frequency (log2)"};
		frame.getContentPane().add(new FFTGraph(table, winSize, axisLabels));
		//frame.pack();
		frame.setVisible(true);
	}



	
}
