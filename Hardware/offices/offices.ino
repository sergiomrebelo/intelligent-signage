const int PORT = 9600;
boolean start = false;
String type = "TOF";
//only for prototype: 1 per office
int offices[8] = {216,223,242,243,260,1260,2361,2371};
int officeNumber=1;
/*
// Ligar os leds
// botao eu estou presente
*/

void setup() {
  Serial.begin (PORT);
  while (!Serial) {}

}

void loop() {
   if (!start) {
    startMessage();
    start = true;
  }

}


void startMessage () {
  Serial.print("START: ");
  Serial.print("OFF:");
  Serial.print (officeNumber);
  Serial.print (" ");
  Serial.println();
}
