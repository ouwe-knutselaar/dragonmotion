#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

bool debug = true;
bool setupBusy = true;
uint16_t port=1080;

WiFiUDP server;

// called this way, it uses the default address 0x40
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

uint8_t readBuffer[70];
uint8_t dump[70];
int pmin = 60;

int servo[16] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};;

void setup() {
  Serial.begin(115200);

  // First start the PWM driver
  if (debug)Serial.printf("Start servo driver\nServo PWM frequentie minimum is %d\n", pmin);
  pwm.begin();
  pwm.setPWMFreq(60);  // This is the maximum PWM frequency
  

  // Then start the network
  if (debug)Serial.printf("Start network\n");
  WiFi.begin("TP-LINK_A4EC02", "Breinaald");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED) { // Wait for the Wi-Fi to connect: scan for Wi-Fi networks, and connect to the strongest of the networks above
    delay(250);
    if (debug)Serial.printf(".");
  }
  if (debug)Serial.printf("\n");
  if (debug)Serial.printf("Connected to ");
  if (debug)Serial.printf("%s\n", WiFi.SSID().c_str());            // Tell us what network we're connected to
  if (debug)Serial.printf("IP address:\t");
  if (debug)Serial.printf("%d.%d.%d.%d\n", WiFi.localIP()[0], WiFi.localIP()[1], WiFi.localIP()[2], WiFi.localIP()[3]);          // Send the IP address of the ESP8266 to the computer

  // Start the telnet server
  if (debug)Serial.printf("Start UDP server on port %d\n",port);
  server.begin(port);
  //server.setNoDelay(true);
  setupBusy = false;
}

void loop() {

  if (setupBusy)return;

  int cb=server.parsePacket();
  if (cb)
  {
    
    server.read(readBuffer, 70);
    
    //if(debug)Serial.printf("read bytes: %d\n",result);

    if (readBuffer[0] == 'a')
    {
      if (debug)Serial.printf("Set all ports\n");
      String readString = (char *)readBuffer;

      int step;
      for (int tel = 0; tel < 16; tel++)
      {
        step = 4 * tel;
        servo[tel] = readString.substring(1 + step, 5 + step).toInt();
        if (debug)Serial.printf("Servo %d is at %d\n", tel, servo[tel]);
        pwm.setPWM(tel, 0, servo[tel]);
      }
      
      return;
    }

    if (readBuffer[0] == 's')
    {
      if (debug)Serial.printf("Set single port\n");
      String readString = (char *)readBuffer;
      int servo = readString.substring(1, 5).toInt();
      int value = readString.substring(5, 9).toInt();
      if (debug)Serial.printf("Servo %d is at %d\n", servo, value);
      pwm.setPWM(servo, 0, value);
      return;
    }

    if (readBuffer[0] == 'i')
    {
      if (debug)Serial.print("Get IP adres\n");
      if (debug)Serial.print("package from "+server.remoteIP().toString()+" and remote port "+server.remotePort()+"\n");
      
      // send a reply, to the IP address and port that sent us the packet we received      
      server.beginPacket(server.remoteIP(), port);
      char sendbuf[]="dragonresponse";
      server.write(sendbuf,sizeof(sendbuf));
      server.endPacket();
      return;
    }
    
   if (readBuffer[0] == 'r')
    {
      if (debug)Serial.print("Reset the board");
      pwm.reset();
    }
  }
}




int hex2dec(unsigned char h, unsigned char l)
{
  int value = 0;
  if (h == '0')value = 0;
  if (h == '1')value = 16;
  if (h == '2')value = 32;
  if (h == '3')value = 48;
  if (h == '4')value = 64;
  if (h == '5')value = 80;
  if (h == '6')value = 96;
  if (h == '7')value = 112;
  if (h == '8')value = 128;
  if (h == '9')value = 144;
  if (h == 'a')value = 160;
  if (h == 'b')value = 176;
  if (h == 'c')value = 192;
  if (h == 'd')value = 208;
  if (h == 'e')value = 224;
  if (h == 'f')value = 240;

  if (l == '0')value = value + 0;
  if (l == '1')value = value + 1;
  if (l == '2')value = value + 2;
  if (l == '3')value = value + 3;
  if (l == '4')value = value + 4;
  if (l == '5')value = value + 5;
  if (l == '6')value = value + 6;
  if (l == '7')value = value + 7;
  if (l == '8')value = value + 8;
  if (l == '9')value = value + 9;
  if (l == 'a')value = value + 10;
  if (l == 'b')value = value + 11;
  if (l == 'c')value = value + 12;
  if (l == 'd')value = value + 13;
  if (l == 'e')value = value + 14;
  if (l == 'f')value = value + 15;
  return value;
}
