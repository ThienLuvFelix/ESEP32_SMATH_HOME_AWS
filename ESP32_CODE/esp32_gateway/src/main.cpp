#include <Arduino.h>
#include <ArduinoJson.h>
#include <esp_now.h>
#include <WiFi.h>
#define RXD2 16
#define TXD2 17

// uint8_t livingRoomAddress[] = {0xC8,0xF0,0x9E,0x75,0x09,0xE0}; 
uint8_t livingRoomAddress[] = {0xff,0xff,0xff,0xff,0xff,0xff}; 
DynamicJsonDocument doc_from_espnow(2048); // JSON Doc for Receiving data from ESPNOW Devices
DynamicJsonDocument doc_to_espnow(2048); // JSON Doc for Transmitting data to ESPNOW Devices
String recv_jsondata;
String send_jsondata;

void OnDataSent(const uint8_t *mac_addr, esp_now_send_status_t status)
{
  Serial.print("\r\nLast Packet Send Status:\t");
  Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Delivery Success" : "Delivery Fail");
}


void OnDataRecv(const uint8_t * mac, const uint8_t *incomingData, int len) {

  char* buff = (char*) incomingData;
  recv_jsondata = "";
  recv_jsondata = String(buff);
  //Serial.print("Recieved "); Serial.println(recv_jsondata);
  //DeserializationError error = deserializeJson(doc_from_espnow, recv_jsondata);
  //if (!error) {
  DynamicJsonDocument doc(2048);
  deserializeJson(doc, recv_jsondata);

  serializeJson(doc, Serial2);
  Serial.write('\n');
  //}

  //else {
    // Serial.print(F("deserializeJson() failed: "));
    // Serial.println(error.f_str());
    // return;
  //}

}
void setup() {
  // put your setup code here, to run once:
  //delay(5000);
  Serial.begin(115200);
  Serial2.begin(115200, SERIAL_8N1, RXD2, TXD2);

  WiFi.mode(WIFI_STA);
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }

  // Defining Callback Functions
  
  esp_now_register_send_cb(OnDataSent);

  // Adding Peer Devices
  esp_now_peer_info_t peerInfo;
  memset(&peerInfo, 0, sizeof(peerInfo));
  for (int ii = 0; ii < 6; ++ii )
  {
    peerInfo.peer_addr[ii] = (uint8_t) livingRoomAddress[ii];
  }
  peerInfo.channel = 0;
  peerInfo.encrypt = false;

  if (esp_now_add_peer(&peerInfo) != ESP_OK) {
    Serial.println("Failed to add peer");
    return;
  }
  esp_now_register_recv_cb(OnDataRecv);

  
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial2.available()){
    char dataToSend[2048];
    // String recv_str_jsondata = Serial2.readStringUntil('\n');
    // Serial.println("Receive:");
    // Serial.println(recv_str_jsondata);

    // recv_str_jsondata.toCharArray(dataToSend,2048);
    // esp_err_t result = esp_now_send(livingRoomAddress, (uint8_t *)dataToSend, recv_str_jsondata.length()+1);
    // if (result == ESP_OK)
    // {
    //   Serial.println("Sent with success");
    // }
    // else
    // {
    //   Serial.println(result);
    // }

    String recv_str_jsondata = Serial2.readStringUntil('\n');
    Serial.println("Receive:");
    Serial.println(recv_str_jsondata);

    size_t len = recv_str_jsondata.length();
    if (len > sizeof(dataToSend)) len = sizeof(dataToSend);
    memcpy(dataToSend, recv_str_jsondata.c_str(), len);

    
    esp_err_t result = esp_now_send(livingRoomAddress, (uint8_t *) dataToSend, len);
    if (result == ESP_OK)
    {
      Serial.println("Sent with success");
    }
    else
    {
      Serial.println(result);
    }   
    
  }
}