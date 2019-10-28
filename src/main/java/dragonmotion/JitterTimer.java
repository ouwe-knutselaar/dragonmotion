package dragonmotion;

public class JitterTimer implements Runnable {
	
	
	DragonConnect connect=DragonUDP.getService();

	
	private int timerVal[]=new int[16];
	private boolean timerFlag[]=new boolean[16];			// flag to enable timer
	private boolean timerSignalSend[]=new boolean[16];		// flag to see if a signal is already send
	
	
	private boolean runFlag=true;
	
	public JitterTimer()
	{

		
		for(int tel=0;tel<16;tel++)
		{
			timerVal[tel]=DragonMotion.timerInterval;
			timerFlag[tel]=false;
			timerSignalSend[tel]=false;
		}
	}
	
	
	@Override
	public void run() {
		System.out.println("JitterTimer is started");
		while (runFlag) {
			
			for(int tel=0;tel<16;tel++)
			{
				if(timerFlag[tel])
				{
					if(timerVal[tel]>0)
						{
						  timerVal[tel]=timerVal[tel]-10;
						}
					else
					{
						if(timerSignalSend[tel]!=true)
						{
							connect.setServo(tel, 0);
							timerSignalSend[tel]=true;
							System.out.println("Jitter control for servo "+tel+" is set");
						}
					}
					
				}
				
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void ResetTimer(int timer)
	{
		timerVal[timer]=DragonMotion.timerInterval;
		timerSignalSend[timer]=false;
		System.out.println("Timer for servo "+timer+" is set");
	}
	
	public void enableTimer(int timer)
	{
		timerFlag[timer]=true;
	}
	
	public void disableTimer(int timer)
	{
		timerFlag[timer]=false;
	}

	public void setTimerFlag(int timer,boolean flag)
	{
		timerFlag[timer]=flag;
	}
	
	public void stopJitterTimer()
	{
		runFlag=false;
	}
	
}
