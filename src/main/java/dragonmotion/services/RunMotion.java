package dragonmotion.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import dragonmotion.DragonConnect;
import dragonmotion.DragonMotion;
import dragonmotion.DragonUDP;
import dragonmotion.SingleTrack;
import dragonmotion.WaveTrack;


public class RunMotion implements Runnable,DragonEvent {
	
	Logger log=Logger.getLogger(RunMotion.class);
	
	ArrayList<SingleTrack> trackList=new ArrayList<>();
	WaveTrack waveTrack;
	
	//float interval=DragonMotion.interval;
	int steps;
	boolean runFlag=false;
	
	int slist[]=new int[16];
	
	DragonConnect network=DragonUDP.getService();
	
	List<DragonEvent> eventHandlerList=new ArrayList<>();
	
	public RunMotion(int steps)
	{
		this.steps=steps;
	}
	
	
	
	// The movement thread
	public void run() {
		runFlag=true;
		int numOfTracks=trackList.size();
		
		WaveService waveService=WaveService.getInstance();
		
		// Reset all the tracks to the begin 
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		
		// Thr mail loop
		log.info("Start the run");
		waveService.playWave();
		for(int tel=0;tel<steps;tel++)
		{
			log.debug(String.format("step  %d:\t",tel));
			for(int trackCount=0;trackCount<numOfTracks;trackCount++)		// Loop langs elke track om de servo waardes op te halen
			{
				int servoValue=trackList.get(trackCount).getNextReal();		// haal de servo waarde op
				int servo=trackList.get(trackCount).getServo();				// Haal de servo op
				
				slist[servo]=servoValue;									// Voeg de servo waarde aan de lijst to
				if(runFlag!=true)											// Als de runflag false is stop dan alles
					{
						for(int counter=0;counter<numOfTracks;counter++)
						{
							trackList.get(counter).reset();
						}
						waveService.stopWave();
						log.info("Loop ended manually");
						handle("Loop ended manually",1,0);
						return;
					}
			}
			//waveTrack.setLooppoint(tel);									// Zet het looppoint zodat ook hier de rode lijn verschuift
			
			network.setAllServos(slist);									// Set the servo's 
			handle("loopstep",2,tel);						// Send an event to update the GUI
			
			try {															// Sleep for 50 seconds
				//Thread.sleep((int)DragonMotion.interval*1000);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		}
		
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		
		log.info("Loop ended");
		handle("Loop ended normally",0,0);
	}

	
	public void clearTrack()
	{
		trackList.clear();
	}
	
	public void addTrack(SingleTrack track)
	{
		trackList.add(track);
	}

	
	public void setWaveTrack(WaveTrack waveTrack)
	{
		this.waveTrack=waveTrack;
	}
	

	public int getSteps() {
		return steps;
	}


	public void setSteps(int steps) {
		this.steps = steps;
	}
	
	public void stopThread()
	{
		runFlag=false;
	}


	
	// Event handlers
	public final void setOnActionEvent(DragonEvent value)
	{
		eventHandlerList.add(value);
	}



	@Override
	public void handle(String msg,int val1,int val2) {

		for(DragonEvent value:eventHandlerList)
		{
			value.handle(msg,val1,val2);
		}
	}
	


}
