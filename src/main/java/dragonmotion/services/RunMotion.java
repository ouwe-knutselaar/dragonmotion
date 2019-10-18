package dragonmotion.services;

import java.util.ArrayList;

import dragonmotion.DragonConnect;
import dragonmotion.DragonMotion;
import dragonmotion.DragonUDP;
import dragonmotion.SingleTrack;

public class RunMotion implements Runnable {
	
	ArrayList<SingleTrack> trackList=new ArrayList<>();
	
	float interval=DragonMotion.interval;
	int steps;
	boolean runFlag=false;
	
	int slist[]=new int[16];
	
	DragonConnect network=DragonUDP.getService();
	
	public RunMotion(int port)
	{

		this.steps=steps;
	}
	
	
	
	
	public void run() {
		System.out.println("Start sequence");
		runFlag=true;
		int numOfTracks=trackList.size();
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		
		for(int tel=0;tel<steps;tel++)
		{
			//System.out.printf("step  %d:\t",tel);
			for(int trackCount=0;trackCount<numOfTracks;trackCount++)
			{
				int servoValue=trackList.get(trackCount).getNextReal();
				trackList.get(trackCount).setLooppoint(tel);
				//System.out.printf("%d\t", servoValue);
				
				slist[trackCount]=servoValue;
				if(runFlag!=true)
					{
						for(int counter=0;counter<numOfTracks;counter++)
						{
							trackList.get(counter).reset();
						}
						System.out.println("Interrupt sequence");
						return;
					}
			}
			network.setAllServos(slist);
			
			//System.out.printf("\n");
			
			try {
				Thread.sleep((int)interval*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int trackCount=0;trackCount<numOfTracks;trackCount++)
		{
			trackList.get(trackCount).reset();
		}
		System.out.println("Stop sequence");
	}

	
	public void addTrack(SingleTrack track)
	{
		trackList.add(track);
	}


	public float getInterval() {
		return interval;
	}


	public void setInterval(float interval) {
		this.interval = interval;
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
