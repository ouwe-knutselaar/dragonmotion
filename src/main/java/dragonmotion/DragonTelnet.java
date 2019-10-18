package dragonmotion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Erwin on 20-8-2019.
 */

public class DragonTelnet implements DragonConnect {


    Socket socket;
    int port;
    String ipAdres;
    InputStream inStream;
    OutputStream outStream;
    byte inbuffer[]=new byte[10];
    

    public DragonTelnet()
    {

    }

    
    public void setAllServos(int s[])
    {    	
    	String sendstr=String.format("a%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d%04d",s[0],s[1],s[2],s[3],s[4],s[5],s[6],s[7],s[8],s[9],s[10],s[11],s[12],s[13],s[14],s[15]);
    	
            try {
                System.out.printf("SetAllServos() Socket no connected, make new connection to %s at port %d \n",ipAdres,port);
                socket=new Socket(ipAdres,port);
                socket.setSoTimeout(50);
                
                System.out.println("Get inputstream");
                inStream=socket.getInputStream();
                outStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        
    	
    	try {        	
        	System.out.println("Send "+sendstr);
        	outStream.write(sendstr.getBytes());
            //inStream.read(inbuffer,0,10);
            //System.out.println("Read:"+inbuffer);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
	public String setServo(int servoNumber, int value) {

		try {
			System.out.printf("SetServer() Socket no connected, make new connection to %s at port %d",ipAdres,port);
			socket = new Socket(ipAdres, port);
			socket.setSoTimeout(20);

			System.out.println("Get inputstream");
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
			String sendString = String.format("s%04d%04d", servoNumber, value);
			System.out.println("Send " + sendString);
			outStream.write(sendString.getBytes());
			//inStream.read(inbuffer, 0, 10);
			//System.out.println("Read:" + inbuffer);
			socket.close();
		} catch (SocketException e) {
			System.out.println("Error on socket:" + socket + "\nerror is " + e.getMessage());

			e.printStackTrace();
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

    
    public int getPort() {
        return port;
    }

    
    public void setPort(int port) {
        this.port=port;
    }

    
    public String getIpadress() {
        return ipAdres;
    }

    
    public void setIpadress(String ipadress) {
        this.ipAdres=ipadress;
    }
}
