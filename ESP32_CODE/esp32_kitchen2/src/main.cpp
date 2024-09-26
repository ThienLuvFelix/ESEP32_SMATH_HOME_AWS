#include <Arduino.h>
#include <esp_now.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Adafruit_Sensor.h>


#define DHTTYPE DHT11
#define buttonFan   19
#define buttonLamp  18
#define dht_pin     21 // LM35 temperature sensor connected to A0
#define GAS_PIN    4  // Gas sensor connected to GPIO 15
#define lamp_pin    22
#define fan_pin     23
#define BUZZER_PIN  13// Buzzer control pin

DHT dht(dht_pin, DHTTYPE);
/******************Variable for State**********************************/
unsigned long crurentTime;
unsigned long crurentTime2;
int   lampStatus = 0 , fanStatus = 0;
int   cur_lampStatus = 0 , cur_fanStatus = 0;
bool isChangeLamp = false, isChangeFan = false;
float temperature = 0, humidity  = 0.0f;
bool isHasGas = false;
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
    if(docDelta.containsKey("KitchenLight")){
      cur_lampStatus = docDelta["KitchenLight"];
    }
    if(docDelta.containsKey("KitchenFan")){
      cur_fanStatus = docDelta["KitchenFan"];
    }
  }
  else {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }
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
void IRAM_ATTR den(){
  cur_lampStatus = !cur_lampStatus;
}
void IRAM_ATTR quat(){
  cur_fanStatus = !cur_fanStatus;
}
void IRAM_ATTR hasGas(){
 if(digitalRead(GAS_PIN)){
  cur_fanStatus = 0;
  // digitalWrite(BUZZER_PIN, LOW);
  isHasGas = false;
 }
 else{
  cur_fanStatus = 1;
  isHasGas = true;
  // digitalWrite(BUZZER_PIN, HIGH); 
 }
}

void setup_wifi_esp_now(){
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
}

void setup(){
  
  Serial.begin(115200);
  setup_wifi_esp_now();

  pinMode(buttonFan, INPUT);
  pinMode(buttonLamp, INPUT);
  pinMode(GAS_PIN, INPUT);

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(fan_pin, OUTPUT);
  pinMode(lamp_pin, OUTPUT);

  attachInterrupt(buttonFan, quat, RISING);
  attachInterrupt(buttonLamp, den, RISING);  
  attachInterrupt(GAS_PIN, hasGas, CHANGE);
  dht.begin();
  delay(1000);
}

void loop(){
 
  if(millis() - crurentTime > 3000){
    crurentTime = millis();
    humidity = dht.readHumidity();
    temperature = dht.readTemperature();
  }

  if(cur_fanStatus != fanStatus){
    fanStatus = cur_fanStatus;
    isChangeFan = true;
  }
  if(cur_lampStatus != lampStatus){
    lampStatus = cur_lampStatus;
    isChangeLamp = true;
  }
  if(isChangeFan){
    digitalWrite(fan_pin, fanStatus);
    SendUpDate("KitchenFan", fanStatus);
    isChangeFan = false;
  }
  if(isChangeLamp){
    digitalWrite(lamp_pin,lampStatus);
    SendUpDate("KitchenLight", lampStatus);
    isChangeLamp = false;
  }
   if(millis() - crurentTime2 > 2000){
    crurentTime2 = millis();
    String jsonStrToUpdate;
    DynamicJsonDocument docMsgUpdate(2048);
    DynamicJsonDocument docReport(2048);
    docReport["KitchenHumidity"]  = humidity;
    docReport["KitchenTemperature"]  = temperature;
    docReport["KitchenFan"]  = fanStatus;
    docReport["KitchenLight"]  = lampStatus;
    JsonObject state = docMsgUpdate.createNestedObject("state");
    state["reported"] = docReport.as<JsonObject>();
    serializeJson(docMsgUpdate, jsonStrToUpdate);
    esp_now_send(gatewayAddress, (uint8_t *) jsonStrToUpdate.c_str(), jsonStrToUpdate.length());
  }
  if(temperature > 40 || isHasGas){
    digitalWrite(BUZZER_PIN, HIGH);
  } 
  else {
    digitalWrite(BUZZER_PIN, LOW);
  }
  
}

