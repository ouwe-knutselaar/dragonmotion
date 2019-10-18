package dragonmotion;

public class ServoTest {

	public static void main(String argc[])
	{
		DragonConnect connect=DragonUDP.getService();
		int offset=200;
		int max=500;
		int servo=8;
		

		
		System.out.println("Turn servo "+servo+" from "+offset+" to "+max);
		for(int tel=offset;tel<max;tel++)
		{
			
			connect.setServo(servo, tel);
			System.out.print(tel+" ");
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\nFinished");
		
	}
	
}
