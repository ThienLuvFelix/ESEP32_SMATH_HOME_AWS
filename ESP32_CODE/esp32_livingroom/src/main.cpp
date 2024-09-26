#include <Arduino.h>
#include <esp_now.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Adafruit_Sensor.h>
#include <SPI.h>
#include <MFRC522.h>
#include <ESP32Servo.h>

#define DHTTYPE DHT11

#define SS_PIN    21  // ESP32 pin GIOP5 
#define RST_PIN   22 // ESP32 pin GIOP27 
#define SERVO     17 // ESP32 pin GIOP22 connects to relay

#define buttonDoor        33
#define sensorTemperature 32
#define fan               13
#define sensorLight       4
#define lamp              12
#define buttonFan         27
#define buttonLamp        26
#define button            25

unsigned long crurentTime;
unsigned long crurentTime2;

const unsigned long delayTime = 3000;

portMUX_TYPE mux = portMUX_INITIALIZER_UNLOCKED;
/***************************************/
DHT dht(sensorTemperature, DHTTYPE);
/***************************************/

/******************Variable for RFID**********************************/

MFRC522 rfid(SS_PIN, RST_PIN);
byte keyTagUID[4] = {0xA9, 0x96, 0x1A, 0x0D};
Servo myServo;

/******************Variable for RFID**********************************/

/******************Variable for espNOW**********************************/

uint8_t gatewayAddress[] = {0xE0,0x5A,0x1B,0xA1,0xE7,0x00};
String recv_jsondata;
String send_jsondata;
DynamicJsonDocument doc_to_espnow(2048);  
DynamicJsonDocument doc_from_espnow(2048);  // for data < 1KB

/******************Variable for espNOW**********************************/

/******************Variable for State**********************************/

int   lampStatus = 0 , fanStatus = 0, autoMode = 0, doorStatus = 0;
int   cur_lampStatus = 0 , cur_fanStatus = 0, cur_autoMode = 0, cur_doorStatus = 0;
bool isChangeLamp = false, isChangeFan = false, isChangeDoor = false, isChangeAutoMode = false;
float temperature = 0, humidity  = 0.0f;
int   isLight = 1;
int testSTT = 0;
/******************Variable for State**********************************/


void OnDataSent(const uint8_t *mac_addr, esp_now_send_status_t status)
{
  // Serial.print("\r\nLast Packet Send Status:\t");
  // Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Delivery Success" : "Delivery Fail");
}

void OnDataRecv(const uint8_t * mac, const uint8_t *incomingData, int len) {
  recv_jsondata = String((char*)incomingData);
  
  Serial.print("Recieved "); Serial.println(recv_jsondata);
  DeserializationError error = deserializeJson(doc_from_espnow, recv_jsondata);
  if (!error) {
    JsonObject docDelta = doc_from_espnow["state"]; 
    if(docDelta.containsKey("LivingRoomLight")){
      cur_lampStatus = docDelta["LivingRoomLight"];
          testSTT = !testSTT;
      digitalWrite(2, testSTT);
    }
    if(docDelta.containsKey("LivingRoomDoor")){
      cur_doorStatus = docDelta["LivingRoomDoor"];
    }
    if(docDelta.containsKey("LivingRoomFan")){
      cur_fanStatus = docDelta["LivingRoomFan"];
    }
    if(docDelta.containsKey("LivingRoomAutoMode")){
      cur_autoMode = docDelta["LivingRoomAutoMode"];
    }
  }
  else {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }
}

void IRAM_ATTR buttonDoorPush(){
  //portENTER_CRITICAL_ISR(&mux);
  while (digitalRead(buttonDoor) == 0);
  cur_doorStatus = !doorStatus;
  //  portEXIT_CRITICAL_ISR(&mux);
}

void IRAM_ATTR buttonLampPush(){
  //portENTER_CRITICAL_ISR(&mux);
  while (digitalRead(buttonLamp) == 0);
  if(autoMode == 0)
    cur_lampStatus = !lampStatus;
  //  portEXIT_CRITICAL_ISR(&mux);
}

void IRAM_ATTR buttonFanPush(){
  //portENTER_CRITICAL_ISR(&mux);
  while (digitalRead(buttonFan) == 0);
  if(autoMode == 0)
    cur_fanStatus = !fanStatus;
  //  portEXIT_CRITICAL_ISR(&mux);
}

void IRAM_ATTR buttonAutoModePush(){
  //portENTER_CRITICAL_ISR(&mux);
  while (digitalRead(button) == 0);
  cur_autoMode = !cur_autoMode;
  //  portEXIT_CRITICAL_ISR(&mux);
}
void IRAM_ATTR buttonTestPush(){
  //portENTER_CRITICAL_ISR(&mux);
  cur_fanStatus = !cur_fanStatus;
  //  portEXIT_CRITICAL_ISR(&mux);
}

void SendUpDate(String nameChange, int value){
  String jsonStrToUpdate;
  DynamicJsonDocument docMsgUpdate(2048);
  DynamicJsonDocument docReport(2048);
  DynamicJsonDocument docDesired(2048);
  docReport[nameChange]  = value;
  docDesired[nameChange] = nullptr; 
  JsonObject state = docMsgUpdate.createNestedObject("state");
  state["reported"] = docReport.as<JsonObject>();
  state["desired"]  = docDesired.as<JsonObject>();
  serializeJson(docMsgUpdate, jsonStrToUpdate);
  esp_now_send(gatewayAddress, (uint8_t *) jsonStrToUpdate.c_str(), jsonStrToUpdate.length());
}
void SendUpDate(String nameChange, float value, String nameChange2, float value2){
  String jsonStrToUpdate;
  DynamicJsonDocument docMsgUpdate(2048);
  DynamicJsonDocument docReport(2048);
  docReport[nameChange]  = value;
  docReport[nameChange2]  = value2;
  JsonObject state = docMsgUpdate.createNestedObject("state");
  state["reported"] = docReport.as<JsonObject>();
  serializeJson(docMsgUpdate, jsonStrToUpdate);
  esp_now_send(gatewayAddress, (uint8_t *) jsonStrToUpdate.c_str(), jsonStrToUpdate.length());
}
void setup() {
  
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }
  esp_now_register_recv_cb(OnDataRecv);
  esp_now_register_send_cb(OnDataSent);

  // Adding Peer Devices
  esp_now_peer_info_t peerInfo;
  memset(&peerInfo, 0, sizeof(peerInfo));
  for (int ii = 0; ii < 6; ++ii )
  {
    peerInfo.peer_addr[ii] = (uint8_t) gatewayAddress[ii];
  }
  peerInfo.channel = 0;
  peerInfo.encrypt = false;

  if (esp_now_add_peer(&peerInfo) != ESP_OK) {
    Serial.println("Failed to add peer");
    return;
  }
  esp_now_register_recv_cb(OnDataRecv);

  pinMode(sensorTemperature, INPUT);
  pinMode(fan, OUTPUT);
  pinMode(sensorLight, INPUT);
  pinMode(lamp, OUTPUT);
  pinMode(buttonFan, INPUT);
  pinMode(buttonLamp, INPUT);
  pinMode(buttonDoor, INPUT);
  pinMode(button, INPUT); 
  pinMode(0, INPUT);
  pinMode(2, OUTPUT);
  attachInterrupt(digitalPinToInterrupt(0), buttonTestPush,FALLING);
  attachInterrupt(digitalPinToInterrupt(buttonLamp), buttonLampPush, FALLING);
  attachInterrupt(digitalPinToInterrupt(buttonFan),  buttonFanPush, FALLING);
  attachInterrupt(digitalPinToInterrupt(buttonDoor), buttonDoorPush, FALLING);
  attachInterrupt(digitalPinToInterrupt(button), buttonAutoModePush, FALLING);

  SPI.begin(); // init SPI bus
  rfid.PCD_Init(); // init MFRC522
  dht.begin(); 
  myServo.attach(SERVO);
  myServo.write(0);
  delay(500);
}

void loop() {
  if(millis() - crurentTime2 > 3000){
    crurentTime2 = millis();
    humidity    = dht.readHumidity();
    temperature = dht.readTemperature();
  }
  isLight     = digitalRead(sensorLight);

  if(autoMode != cur_autoMode){
    autoMode = cur_autoMode;
    isChangeAutoMode = true;
  }
  if(autoMode == 1){
    if(temperature > 25)
      cur_fanStatus = 1;
    else{
      cur_fanStatus = 0;
    }
    if(isLight){
      cur_lampStatus = 1;
    }
    else{
      cur_lampStatus = 0;
    }
  }
  if (rfid.PICC_IsNewCardPresent()) { // new tag is available
    if (rfid.PICC_ReadCardSerial()) { // NUID has been readed
      MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);
      if (rfid.uid.uidByte[0] == keyTagUID[0] &&
          rfid.uid.uidByte[1] == keyTagUID[1] &&
          rfid.uid.uidByte[2] == keyTagUID[2] &&
          rfid.uid.uidByte[3] == keyTagUID[3] )
      {
        cur_doorStatus = !cur_doorStatus;
      }
      rfid.PICC_HaltA(); // halt PICC
      rfid.PCD_StopCrypto1(); // stop encryption on PCD
    }
  }
  if(doorStatus != cur_doorStatus){
    doorStatus = cur_doorStatus;
    isChangeDoor = true;
  }
  if(fanStatus != cur_fanStatus){
    fanStatus = cur_fanStatus;
    isChangeFan = true;
  }
  if(lampStatus != cur_lampStatus){
    lampStatus = cur_lampStatus;
    isChangeLamp = true;
  }
  if(isChangeAutoMode){
    SendUpDate("LivingRoomAutoMode", autoMode);
    isChangeAutoMode = false;
  }
  if(isChangeDoor){
    myServo.write(doorStatus == 1 ? 90 : 0);
    SendUpDate("LivingRoomDoor", doorStatus);
    isChangeDoor = false;
  }
  if(isChangeFan){
    digitalWrite(fan, fanStatus);
    SendUpDate("LivingRoomFan", fanStatus);
    isChangeFan = false;
  }
  if(isChangeLamp){
    digitalWrite(lamp, lampStatus);
    SendUpDate("LivingRoomLight", lampStatus);
    isChangeLamp = false;
  }
  
  if(millis() - crurentTime > 1800){
    crurentTime = millis();
    // SendUpDate("LivingRoomHumidity", humidity,"LivingRoomTemperature", temperature);
    String jsonStrToUpdate;
    DynamicJsonDocument docMsgUpdate(512);
    DynamicJsonDocument docReport(512);
    docReport["LivingRoomHumidity"]  = humidity;
    docReport["LivingRoomTemperature"]  = temperature;
    docReport["LivingRoomFan"]  = fanStatus;
    docReport["LivingRoomDoor"]  = doorStatus;
    docReport["LivingRoomLight"]  = lampStatus;
    docReport["LivingRoomAutoMode"]  = autoMode;
    JsonObject state = docMsgUpdate.createNestedObject("state");
    state["reported"] = docReport.as<JsonObject>();
    serializeJson(docMsgUpdate, jsonStrToUpdate);
    esp_now_send(gatewayAddress, (uint8_t *) jsonStrToUpdate.c_str(), jsonStrToUpdate.length());
  }
}