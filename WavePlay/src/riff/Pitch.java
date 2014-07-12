package riff;

import filemanager.Log;

public class Pitch {
	
	/** Pitch Shifts the audio 0 < factor < inf **/
	public static Signal pitchShift(Signal signal, double factor) {
		//set up arrays
		double[][] os = signal.getSignal();
		double[][] ns = new double[os.length][(int)((os[0].length / factor) + 1)]; //round up
		//Log.bad(os[0].length + " " + ns[0].length);
		for (int i = 0; i < ns.length;++i) {
			for (int j = 0; j < ns[i].length;++j) {
				ns[i][j] = linearApproximate(os[i], j * factor);  
				
			}
		}
		return new Signal(ns, signal.getBit(), signal.getSampleRate());
	}
	
	/**Linearly approximates the continous value between two discrete values **/
	private static double linearApproximate(double[] array, double distance) {
		/*Obviously we can't round the array lengths so I've decided to slightly 
		 * extend the new one by one sample. Probably better than reducing the information
		 */
		if ((int)distance >= array.length - 1) { 
			return array[array.length - 1];
		} else {
			int r = (int) distance;
			double rd = array[r];
			double ru = array[r + 1];
			double f = distance - r;
			return rd + (ru - rd) * f;
		}
	}
	
	//public static void main (String[] args) {
		//double[] arr = {0,10,20,30};
		//Log.d(linearApproximate(arr, 1.75));
		//Log.d((int)((20 / 1.234) + 1));
	//}

}