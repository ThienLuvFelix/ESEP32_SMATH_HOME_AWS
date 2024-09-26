#include <Arduino.h>
#include <esp_now.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Adafruit_Sensor.h>
#define DHTTYPE DHT11
#define sensorTemperature 4
#define fan 23
#define lamp 22
#define buttonLamp 18
#define buttonFan 19
DHT dht(sensorTemperature, DHTTYPE);

/******************Variable for State**********************************/

float temperature, humidity;
int fanStatus = 0, lampStatus = 0;
int  cur_lampStatus = 0 , cur_fanStatus = 0;
bool isChangeLamp = false, isChangeFan = false;

unsigned long crurentTime;
unsigned long crurentTime2;
/******************Variable for State**********************************/

/******************Variable for espNOW**********************************/

uint8_t gatewayAddress[] = {0xE0,0x5A,0x1B,0xA1,0xE7,0x00};
String recv_jsondata;
String send_jsondata;
DynamicJsonDocument doc_to_espnow(2048);  
DynamicJsonDocument doc_from_espnow(2048);  // for data < 1KB

/******************Variable for espNOW**********************************/
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
    if(docDelta.containsKey("BedroomLight")){
      cur_lampStatus = docDelta["BedroomLight"];
    }
    if(docDelta.containsKey("BedroomFan")){
      cur_fanStatus = docDelta["BedroomFan"];
    }
  }
  else {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }
}
void IRAM_ATTR quat(){
  while(digitalRead(buttonFan) == 0);
  cur_fanStatus = !cur_fanStatus;
}

void IRAM_ATTR den(){
  while(digitalRead(buttonLamp) == 0);
  cur_lampStatus = !cur_lampStatus; // đảo trạng thái led từ LOW -> HIGH  
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

void setup() {
  
  WiFi.mode(WIFI_STA);
  if (esp_now_init() != ESP_OK) {
    //Serial.println("Error initializing ESP-NOW");
    return;
  }
  esp_now_register_recv_cb(OnDataRecv);
  esp_now_register_send_cb(OnDataSent);

  esp_now_peer_info_t peerInfo;
  memset(&peerInfo, 0, sizeof(peerInfo));
  for (int ii = 0; ii < 6; ++ii )
  {
    peerInfo.peer_addr[ii] = (uint8_t) gatewayAddress[ii];
  }
  peerInfo.channel = 0;
  peerInfo.encrypt = false;

  if (esp_now_add_peer(&peerInfo) != ESP_OK) {
    //Serial.println("Failed to add peer");
    return;
  }
  esp_now_register_recv_cb(OnDataRecv);

  pinMode(sensorTemperature, INPUT); 
  pinMode(fan, OUTPUT);
  pinMode(lamp, OUTPUT);
  dht.begin();
  pinMode(buttonFan, INPUT);
  pinMode(buttonLamp, INPUT);
  digitalWrite(fan, LOW); //ban đầu led tắt
  digitalWrite(lamp, LOW); //ban đầu led tắt  
  attachInterrupt(buttonFan, quat, RISING);
  attachInterrupt(buttonLamp, den, RISING);
  delay(1500);
}

void loop() {
  if(millis() - crurentTime2 > 3000){
    crurentTime2 = millis();
    humidity = dht.readHumidity();
    temperature = dht.readTemperature();
  }
  
  if(fanStatus != cur_fanStatus){
    fanStatus = cur_fanStatus;
    isChangeFan = true;
  }
  if(lampStatus != cur_lampStatus){
    lampStatus = cur_lampStatus;
    isChangeLamp = true;
  }

  if(isChangeFan){
    digitalWrite(fan, fanStatus);
    SendUpDate("BedroomFan", fanStatus);
    isChangeFan = false;
  }
  if(isChangeLamp){
    digitalWrite(lamp, lampStatus);
    SendUpDate("BedroomLight", lampStatus);
    isChangeLamp = false;
  }

  if(millis() - crurentTime > 2000){
    crurentTime = millis();
    // SendUpDate("LivingRoomHumidity", humidity,"LivingRoomTemperature", temperature);
    String jsonStrToUpdate;
    DynamicJsonDocument docMsgUpdate(2048);
    DynamicJsonDocument docReport(2048);
    docReport["BedroomHumidity"]  = humidity;
    docReport["BedroomTemperature"]  = temperature;
    docReport["BedroomFan"]  = fanStatus;
    docReport["BedroomLight"]  = lampStatus;
    JsonObject state = docMsgUpdate.createNestedObject("state");
    state["reported"] = docReport.as<JsonObject>();
    serializeJson(docMsgUpdate, jsonStrToUpdate);
    esp_now_send(gatewayAddress, (uint8_t *) jsonStrToUpdate.c_str(), jsonStrToUpdate.length());
  }
}