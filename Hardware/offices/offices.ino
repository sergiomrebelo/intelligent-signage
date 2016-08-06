//constrants
const int PORT = 9600;
const String TYPE = "TOF"; //string of request
//all offices [only for testing]
//office 1 [Test office]
const int OFFICES[9] = {1,216,223,242,243,260,1260,2361,2371}; 
//variables
boolean start = false;
int officeNumber=0; //office number
int delayTime=3; //delay of serial requests (in seconds)
int nNiveis=4; //number of teacher in gabinet- MAX:5
int fButton=7;
String c; //Serial String
//on/off lights
boolean pinActive [5];
boolean pinActiveCompare [5];
int previousMillisButton = 0;
int interval = 3000;


void setup() {
  Serial.begin (PORT);
  while (!Serial) {}
  
  for (int i = 0; i < sizeof(pinActive); i++) {
    pinMode (i+2, OUTPUT);
    pinActive[i]=false;
  }
  
  for (int i=fButton; i<fButton+nNiveis; i++) 
    pinMode (i, INPUT);

}

void loop() {
  if (!start) startMessage();
 
  if (Serial.available()) {
    for (int i=0; i<sizeof(pinActiveCompare);i++) pinActiveCompare[i] = false;
    c = Serial.readString();
    
   if (c.indexOf("NPROF:") >=0) {
      //start connections
      //request the number os teachers in office
      nNiveis= charToInt(c.charAt(6));
      start = true;
   } else if (start && c.indexOf("RESET") ==-1) {
      int u=0;
      while (u<c.length()) {
        char ch=c.charAt(u);
        int pin = charToInt (ch);
         pinActiveCompare[pin] = true;
        u++;
      }
    }
  }
  
  updateLights();
  listenButtons();
  
  if ((millis() % (delayTime*1000)==0) && start) {
    Serial.println(TYPE);
    delay(1);
  }
  
  //
  //if (millis() % (delayTime*1000) == 0) 
  //if (start) delay(delayTime*1000);
  //if (start) Serial.println(TYPE);
  //buttons 
  
  
}

//COMUNICATION FUNCTIONS

/*
  updateLights : void
  update the state of lights
*/

void updateLights() {
  if (!start) return;
  for (int i=0; i<sizeof(pinActive);i++)  {
    if (pinActive[i]!=pinActiveCompare[i]) {
      pinActive[i] = pinActiveCompare[i];
      digitalWrite (i+2, pinActive[i]);  //+2, because the gate 0 and 1 aren't used.
    } 
  }
}

/*
 listenButtons: void
 listen if button is pressed and what is the button pressed and communicate with server
*/
void listenButtons () {
  if (!start) return;
  for (int i=fButton; i<(fButton+nNiveis); i++) {
    int sensorValue = digitalRead(i);
    if (sensorValue == 1 && (millis() - previousMillisButton) > interval) {
      Serial.print ("NOT:");
      Serial.println(i-fButton);
      previousMillisButton = millis();
    }
  }
}

/*
  startMessage : void
  function to communicate the first time with Server
  Request the information to start show information
*/
void startMessage () {
  Serial.print("START: ");
  Serial.print("OFF:");
  Serial.print (OFFICES[officeNumber]);
  Serial.print (" ");
  Serial.println();
}

//GLOBAL FUNCTION

/*
  charToInt(char c) : int
  receive a char and convert to a integer
  return this integer
*/
int charToInt (char c) {
  int i = c - '0';
  return i;
}
