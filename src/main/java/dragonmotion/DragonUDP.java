package dragonmotion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Erwin on 20-8-2019.
 */

public class DragonUDP implements DragonConnect {


	DatagramSocket socket;
    int port;    
    InputStream inStream;
    OutputStream outStream;
    byte inbuffer[]=new byte[10];
    byte sendBuffer[]=new byte[256];
    int sendsize;
    InetAddress address;
    
    private static DragonUDP INSTANCE;
    

    private DragonUDP()
    {
    	try {
			socket=new DatagramSocket();
			this.port=DragonMotion.port;
			address = InetAddress.getByName(DragonMotion.ipadres);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

    
    
    public void setAllServos(int s[])
    {    	
    	String sendstr=String.format("a%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d",s[0],s[1],s[2],s[3],s[4],s[5],s[6],s[7],s[8],s[9],s[10],s[11],s[12],s[13],s[14],s[15]);
    	
            try {
                //System.out.printf("SetAllServos() UDP Socket no connected, make new connection to %s at port %d \n",address.getHostAddress(),port);                                         
                //socket.setSoTimeout(50);
                
                sendBuffer=sendstr.getBytes();
                sendsize=sendstr.length();
                
                DatagramPacket sendPacket=new DatagramPacket(sendBuffer,sendsize,address,port);
                socket.send(sendPacket);             
                                
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }            	    	
    }
    
    
	public String setServo(int servoNumber, int value) {

		String sendstr = String.format("s%04d%04d", servoNumber, value);
		try {
            System.out.printf("SetServo() UDP Socket no connected, make new connection to %s at port %d to set servo %d on value %d\n",address.getHostAddress(),port,servoNumber,value);                                         
            //socket.setSoTimeout(50);
            
            sendBuffer=sendstr.getBytes();
            sendsize=sendstr.length();
            
            DatagramPacket sendPacket=new DatagramPacket(sendBuffer,sendsize,address,port);
            socket.send(sendPacket);             
                            
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }    

		

		return "OK";
	}

    
    public int getPort() {
        return port;
    }

    


    
    public String getIpadress() {
    	
    	return address.getHostAddress();        
    }

    
    public void setIpadress(String ipadress) {
    	try {
			address = InetAddress.getByName(ipadress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
    }


	public static DragonConnect getService() {
		if(INSTANCE==null)
		{
			INSTANCE=new DragonUDP();
		}
		return INSTANCE;
	}
}
