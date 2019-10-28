package dragonmotion;

/**
 * Created by Erwin on 20-8-2019.
 */
public interface DragonConnect {

    public String setServo(int servoNumber,int value);

    public int getPort();
   // public void setPort(int port);
    public String getIpadress();
    public void setIpadress(String ipadress);
    public void setAllServos(int s[]);
    public String scanForDragon(int port,String base);
    public void close();
   
}
