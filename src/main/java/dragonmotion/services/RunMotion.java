package dragonmotion.services;

import java.util.ArrayList;

import dragonmotion.DragonConnect;
import dragonmotion.DragonMotion;
import dragonmotion.DragonUDP;
import dragonmotion.SingleTrack;

public class RunMotion implements Runnable {
	
	ArrayList<SingleTrack> trackList=new ArrayList<>();
	
	//float interval=DragonMotion.interval;
	int steps;
	boolean runFlag=false;
	
	int slist[]=new int[16];
	
	DragonConnect network=DragonUDP.getService();
	
	public RunMotion(int port)
	{

	}
	
	
	
	
	public void run() {
		runFlag=true;
		int numOfTracks=trackList.size();
		
		// Reset all the tracks to the begin 
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		
		// Thr mail loop
		System.out.println("Start the run");
		for(int tel=0;tel<DragonMotion.steps;tel++)
		{
			System.out.printf("step  %d:\t",tel);
			for(int trackCount=0;trackCount<numOfTracks;trackCount++)		// Loop langs elke track
			{
				int servoValue=trackList.get(trackCount).getNextReal();		// haal de servo waarde op
				trackList.get(trackCount).setLooppoint(tel);
				System.out.printf("%d\t", servoValue);
				
				slist[trackCount]=servoValue;								// Voeg de servo waarde aan de lijst to
				if(runFlag!=true)											// Als de runflag false is stop dan alles
					{
						for(int counter=0;counter<numOfTracks;counter++)
						{
							trackList.get(counter).reset();
						}
						System.out.println("Loop ended manually");
						return;
					}
			}
			network.setAllServos(slist);									// Set the servo's 
			
			//System.out.printf("\n");
			
			try {
				//Thread.sleep((int)DragonMotion.interval*1000);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		
		System.out.println("Loop ended");
	}

	
	public void clearTrack()
	{
		trackList.clear();
	}
	
	public void addTrack(SingleTrack track)
	{
		trackList.add(track);
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
}
