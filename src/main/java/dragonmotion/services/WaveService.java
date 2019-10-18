package dragonmotion.services;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import dragonmotion.DragonMotion;

public class WaveService {
	
	private AudioFileFormat audioFileFormat;
	private AudioInputStream audioInputStream;
	
	private float sampleRate;
	private float frameLength;
	private int sampleFormat;
	private float duration;
	private int steps;
	private int channels;
	
	private int[][] samples;
	
	private static WaveService INSTANCE;
	
	private double maxvol = 1;
	
	private WaveService()
	{
		
	}
	
	public static WaveService getInstance()
	{
		if(INSTANCE==null)
		{
			INSTANCE=new WaveService();
		}
		return INSTANCE;
	}
	
	
	public boolean loadFile(File audioFile)
	{
		try {
			audioInputStream=AudioSystem.getAudioInputStream(audioFile);
			AudioFormat format=audioInputStream.getFormat();
			
			sampleRate=format.getSampleRate();
			frameLength=audioInputStream.getFrameLength();
			sampleFormat=audioInputStream.getFormat().getSampleSizeInBits();
			duration=frameLength/sampleRate;
			steps=(int) (duration/DragonMotion.interval);
			channels=audioInputStream.getFormat().getChannels();
			samples = new int[audioInputStream.getFormat().getChannels()][(int) frameLength];
			
			System.out.println("SampleRate is "+sampleRate);
			System.out.println("Size is "+(int)frameLength);
			System.out.println("Samplesize is "+sampleFormat);
			System.out.println("Duration is "+duration+" Seconds");
			System.out.println("Number of steps is "+steps);
			System.out.println("Number of channels is "+channels);
			System.out.println("Number of samples is "+samples.length);
			
			byte[] eightBitByteArray = new byte[(int) (frameLength * audioInputStream.getFormat().getFrameSize())];
			int result = audioInputStream.read(eightBitByteArray);
			
			int sampleIndex = 0;
			for (int t = 0; t < eightBitByteArray.length;) {
				for (int channel = 0; channel < audioInputStream.getFormat().getChannels(); channel++) {
					int low = (int) eightBitByteArray[t];
					t++;
					int high = (int) eightBitByteArray[t];
					t++;
					int sample = getSixteenBitSample(high, low) + 16000;
					if (sample > maxvol)
					maxvol = sample;
					samples[channel][sampleIndex] = sample;
					// System.out.printf("S:%d M:%f\t",sample,maxvol);
				}

				sampleIndex++;
			}
			
		} catch (UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		return true;
		
	}
	
	protected int getSixteenBitSample(int high, int low) {
		return (high << 8) + (low & 0x00ff);
	}
	
	
	public int getSteps()
	{
		return steps;
	}
	
	
	public int[][] getSample()
	{
		return samples;
	}
	
	public int sampleSize()
	{
		return (int)frameLength;
	}
	
	public double getMaxvol()
	{
		return maxvol;
	}
}
