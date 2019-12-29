package dragonmotion.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.log4j.Logger;
import dragonmotion.DragonMotion;


public class WaveService {
	
	Logger log=Logger.getLogger(WaveService.class);
	
	
	private AudioInputStream audioInputStream;
	private float sampleRate;
	private int frameLength;
	private int frameSize;
	private int sampleFormat;
	private float duration;
	private int steps;
	private int channels;
	private AudioFormat format;
	private byte[] eightBitByteArray;
	private int[][] samples;
	private int[][] lowPassSamples;
	private static WaveService INSTANCE;
	private double maxvol = 1;
	private Clip clip;
	private File waveFile;
	
	
	private WaveService()
	{
		
	}
	
	
	/**
	 * Singleton initializer
	 * @return
	 */
	public static WaveService getInstance()
	{
		if(INSTANCE==null)
		{
			INSTANCE=new WaveService();
		}
		return INSTANCE;
	}
	
	
	/**
	 * Write the sample file under the right name
	 * @param audioFile
	 * @return
	 */
	public boolean saveFile(File audioFile)
	{
		// Maak een audio stream
		AudioInputStream ais=new AudioInputStream(new ByteArrayInputStream(eightBitByteArray), format, eightBitByteArray.length);
		int rc=0;

		try {
			rc=AudioSystem.write(ais, Type.WAVE, audioFile);		//schrijf de audio stream
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Wave file saved as "+audioFile.getAbsolutePath()+" size is "+rc);
		return true;
	}
	
	
	/**
	 * Load a wave fiel from disk
	 * @param audioFile File handler
	 * @return
	 */
	public boolean loadFile(File audioFile)
	{
		log.info("Load file "+audioFile.getAbsolutePath());
		waveFile=audioFile;
		try {
			audioInputStream=AudioSystem.getAudioInputStream(audioFile);		// Open de audiofile naar een inputstream
			format=audioInputStream.getFormat();								// Haal het formaat op
			
			sampleRate		= format.getSampleRate();							// Zet de varaibelen				
			frameLength		= (int)audioInputStream.getFrameLength();
			frameSize		= format.getFrameSize();
			sampleFormat	= format.getSampleSizeInBits();
			duration		= frameLength/sampleRate;
			steps			= (int) (duration/DragonMotion.interval);
			channels		= format.getChannels();
			samples			= new int[channels][frameLength];					// Dit wordt de definitieve lijst met samples
			lowPassSamples	= new int[channels][frameLength];					// Dit wordt de lowpass lijst
			maxvol			= 0;												// Reset de max volume
			int sampleIndex = 0;												// teller om langs de sample te gaan
			eightBitByteArray = new byte[(int) (frameLength * frameSize)];		// Maak een 8bits buffer om de ruwe wave data in op te slaan
			int result 		  = audioInputStream.read(eightBitByteArray);		// Vul de 8bits buffer met ruwe data
			
			log.info("SampleRate is "+sampleRate);				// Some logging
			log.info("Framelength is "+frameLength);
			log.info("Framesize is "+frameSize);
			log.info("Samplesize is "+sampleFormat);
			log.info("Duration is "+duration+" Seconds");
			log.info("Number of steps is "+steps);
			log.info("Number of channels is "+channels);
			log.info("Number of samples is "+samples.length);
			log.info("eightBitByteArray size is "+eightBitByteArray.length);
			log.info("There are "+result+" bytes read");
			
			if (sampleFormat == 8) {												// verwerk een 8 bits sample
				for (int t = 0; t < eightBitByteArray.length; t++) {				// loop alle samples langs
					for (int channel = 0; channel < channels; channel++) {			// Binnen de channels
						int sample= (int) eightBitByteArray[t]+128;					// Laad een sample, een signed bit dus even een +128 correctie naar int
						if (sample > maxvol)
							maxvol = sample;										// Bepaal de hoogste sample
						samples[channel][sampleIndex] = sample;						// En laadt deze in de lijst	
					}
					sampleIndex++;
				}
			}
			
			if (sampleFormat == 16) {												// verwerk een 16 bits sample
				for (int t = 0; t < eightBitByteArray.length; t++) {				// loop door de ruwe data
					for (int channel = 0; channel < channels; channel++) {			// ga alle channels langs
						int low = (int) eightBitByteArray[t];						// lage byte
						t++;
						int high = (int) eightBitByteArray[t];						// hoge byte

						int sample = getSixteenBitSample(high, low) + 16000;		// Maak er 16 bits van
						if (sample > maxvol)
							maxvol = sample;										// en bepalen wat het maximum is
						samples[channel][sampleIndex] = sample;						// En opslaan in de array
					}
					sampleIndex++;
				}
			}
			log.info("Sample loaded size "+sampleIndex);
			log.info("Maximal volume is "+maxvol);
			log.info("Apply lowpassfilter");
			lowPassFilter(882);
			
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Stop playing the wave file
	 */
	public void stopWave()
	{
		if(clip==null)return;
		clip.stop();
	}
	
	
	/**
	 * Start the wave file
	 */
	public void playWave()
	{
		log.info("Play wave file");
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip=(Clip)AudioSystem.getLine(info);
			clip.addLineListener(new LineListener(){
				@Override
				public void update(LineEvent event) {
					log.debug("Event "+event.getFramePosition());
					
				}});
			
			audioInputStream=AudioSystem.getAudioInputStream(waveFile);
			clip.open(audioInputStream);
			
			log.debug("frame len "+clip.getFrameLength());
			log.debug("frame len n ms  "+clip.getMicrosecondLength());
			clip.setMicrosecondPosition(0);
			clip.start();
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	// Moving average filter
	private void lowPassFilter_temp2(int points)
	{
		int bias=(int)(maxvol/2);
		for(int count=points;count<frameLength-points;count++)
		{
			int sum=0;
			for(int tel=-points;tel<points;tel++)
			{
				sum=sum+Math.abs(samples[0][count+tel]-bias);		// Sum the value
			}
			int avg=sum/(2*points+1);
			lowPassSamples[0][count]=avg;
		}	
	}
	
	// Moving average filter
	private void lowPassFilter_temp(int points) 
	{
		int pos=0;
		boolean loopflag=true;
		int bias = (int) (maxvol / 2);
		
		while(loopflag)
		{	
			log.debug("LPF position "+pos);
			int sum=0;
			for(int tel=0;tel<points;tel++)
			{
				sum=sum+samples[0][pos+tel];
			}
			int avg=sum/points;
			for(int tel=0;tel<points;tel++)
			{
				lowPassSamples[0][pos+tel]=avg;
			}
			pos=pos+points;
			if(pos+points>frameLength)loopflag=false;
		}
		
	}
	
	// Moving average filter
	private void lowPassFilter(int points) 
		{
			int minpoint=(int) maxvol;
			int pos=0;
			boolean loopflag=true;
			int bias = (int) (maxvol / 2);
			
			while(loopflag)
			{	
				//log.debug("LPF position "+pos);
				int max=0;
				for(int tel=0;tel<points;tel++)
				{
					if(max<samples[0][pos+tel])max=samples[0][pos+tel];
					if(minpoint>samples[0][pos+tel])minpoint=samples[0][pos+tel];
				}
				;
				for(int tel=0;tel<points;tel++)
				{
					lowPassSamples[0][pos+tel]=max-minpoint;
				}
				pos=pos+points;
				if(pos+points>frameLength)loopflag=false;
			}
			
		}
	
	
	
	protected int getSixteenBitSample(int high, int low) {
		return (high << 8) + (low & 0x00ff);
	}

	
	
	public int[][] getSample()
	{
		return samples;
	}
	
	public int[][] getLowPass()
	{
		return lowPassSamples;
	}
	
	
	public int sampleSize()
	{
		return (int)frameLength;
	}
	
	
	public double getMaxvol()
	{
		return maxvol;
	}


	public int getSteps() {
		return steps;
	}
}
