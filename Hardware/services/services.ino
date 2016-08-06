/*services:
1 per instituicion
*/
//constrants
const int PORT = 9600; //serial port
const String TYPE = "SER"; //String of Request
boolean start = false;
int inst = 1; //1-Darq // 2-CA // 3-UC Services
//other instituition only have 6 differentes types os schedules (pins 9/10/11 are ignored)
int maxLevels = 12; //max of diferente timetables for services
int delayTime = 3; //delay of serial requests (in seconds)
String val; //Serial String
//on/off lights
boolean pinActive [14];
boolean pinActiveCompare [14];
int color[3]={100,200,200}; //color of other institution



void setup () {
  Serial.begin (PORT);
  while (!Serial) {}
  
 

  for (int i = 2; i < maxLevels; i++) {
    pinMode (i, OUTPUT);
    pinActive[i] = false;
  }


}


void loop () {
  if (!start)
    startMessage();

  if (Serial.available()) {
    for (int i = 0; i < sizeof(pinActiveCompare); i++) pinActiveCompare[i] = false;
    val = Serial.readString();
    if  (val.indexOf("ON" >= 0))
         deconstructingSerial(val);
  }
  
   if (inst>=3) {
     analogWrite (9, color[0]);
     analogWrite (10, color[1]);
     analogWrite (11, color[2]);
  }
  
  
for (int i = 0; i < sizeof(pinActive); i++)  {
    if (pinActive[i] != pinActiveCompare[i]) {
      pinActive[i] = pinActiveCompare[i];
     digitalWrite (i, pinActive[i]);
    }
  }

  if ((millis() % (delayTime * 1000) == 0) && start) {
    Serial.println(TYPE);
    delay(1);
  }
}

/*
  startMessage : void
  function to communicate the first time with Server
  Request the information to start show information
*/
void startMessage () {
  Serial.print("START: ");
  Serial.print("INST:");
  Serial.print (inst);
  Serial.print (" ");
  Serial.println();
  start = true;
}

/*
  desconstructingSerial (Serial result in String) : void
  Desconstructing the serial string and get the pin in on and pins in off
*/

void deconstructingSerial(String val) {
  int i = 0;
  while (i < val.length()) {
    if (val.charAt(i) == 'N') {
      if (i + 1 < val.length()) {
        char c = val.charAt(i + 1);
        int pin = charToInt (c);
        //if the number is greater than or equal to 10
        if (pin == 1) {
          c = val.charAt(i + 2);
          pin = charToInt (c);
          pin += 10;
          if (inst <3) pinActiveCompare[pin] = true;
          else {
            if (pin==10 || pin==11) pinActiveCompare[pin] = false;
          }
        } else {
          if (inst <3) pinActiveCompare[pin] = true;
          else {
            if (pin== 9) pinActiveCompare[pin] = false;
          }
          
        }
      }
    }
    i++;
  }
}


//GLOBAL FUNCTIONS

/*
  charToInt(char c) : int
  receive a char and convert to a integer
  return this integer
*/
int charToInt (char c) {
  int i = c - '0';
  return i;
}

