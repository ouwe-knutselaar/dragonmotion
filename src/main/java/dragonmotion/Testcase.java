package dragonmotion;

import java.io.File;

import dragonmotion.services.WaveService;

public class Testcase {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		 WaveService waveService=WaveService.getInstance();
		 File file=new File("d:\\Erwin\\drugs_x.wav");
		 
		 //File file=new File("d:\\Erwin\\fftesten.wav");
		 
		 waveService.loadFile(file);
		 //waveService.playWave();
		 
		 
		
	}

}
