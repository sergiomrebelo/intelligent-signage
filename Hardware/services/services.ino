/*services*/

const int PORT = 9600;
boolean start = false;
int inst = 1;
String type = "SER";
String val;
boolean pinActive [14];
boolean pinActiveCompare [14];
//*diferents color by institution*//


void setup () {
  Serial.begin (PORT);
  while (!Serial) {}

  for (int i = 0; i < 14; i++) {
    pinMode (i, OUTPUT);
    pinActive[i]=false;
  }
}

void loop () {
 
  if (!start) {
    startMessage();
    start = true;
  }
  
  
 
 
 
  if (Serial.available()) {
    for (int i=0; i<sizeof(pinActiveCompare);i++) pinActiveCompare[i] = false;
    val = Serial.readString();
    Serial.print("ARDUINO");
    Serial.println(val);
    int i = 0;
    while (i < val.length()) {
      if (val.charAt(i) == 'N') {
       if (i + 1 < val.length()) {
          char c = val.charAt(i + 1);
          int pin = c - '0';
          if (pin == 1) {
            c = val.charAt(i + 2);
            pin = c - '0';
            pin+=10;
            pinActiveCompare[pin] = true;
          } else  {
            pinActiveCompare[pin] = true;
          }
          
        }
      }
      i++;
    }
  }
  for (int i=0; i<sizeof(pinActive);i++)  {
    if (pinActive[i]!=pinActiveCompare[i]) {
      pinActive[i]=pinActiveCompare[i];
      digitalWrite (i+2, pinActive[i]);
    }
  }
  delay(3000);
  Serial.println(type);
}


void startMessage () {
  Serial.print("START: ");
  Serial.print("INST:");
  Serial.print (inst);
  Serial.print (" ");
  Serial.println();
}
