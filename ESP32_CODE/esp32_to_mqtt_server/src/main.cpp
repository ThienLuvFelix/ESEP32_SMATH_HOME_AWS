#include <Arduino.h>
#include "secrets.h"
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "WiFi.h"

#define AWS_IOT_GET_START_TOPIC "$aws/things/ESP32_THING_4LED/shadow/get/accepted"
#define AWS_IOT_SUBSCRIBE_TOPIC "$aws/things/ESP32_THING_4LED/shadow/update/delta"
#define AWS_IOT_PUBLISH_TOPIC "$aws/things/ESP32_THING_4LED/shadow/update"

#define RXD2 16
#define TXD2 17

DynamicJsonDocument doc(2048);
DynamicJsonDocument docReport(2048);
DynamicJsonDocument doc_recv_from_client(2048);

WiFiClientSecure net = WiFiClientSecure();
PubSubClient client(net);

void messageHandler(char* topic, byte* payload, unsigned int length)
{
  // Serial.print("incoming: ");
  // Serial.println(topic);
  deserializeJson(doc, payload);
  //Serial.println("Hiiiiiiiiiiii");
  // serializeJson(doc, Serial);
  // if(isRestart){
    // serializeJson(doc, Serial2);
    // client.unsubscribe(AWS_IOT_GET_START_TOPIC);
    // isRestart = false;
  // }
  // else{
      serializeJson(doc, Serial2);
      Serial2.write('\n');
  // }
}

void connectAWS()
{
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
 
  Serial.println("Connecting to Wi-Fi");
 
  while (WiFi.status() != WL_CONNECTED){
    delay(500);
    //Serial.print(".");
  }
 
  // Configure WiFiClientSecure to use the AWS IoT device credentials
  net.setCACert(AWS_CERT_CA);
  net.setCertificate(AWS_CERT_CRT);
  net.setPrivateKey(AWS_CERT_PRIVATE);
 
  // Connect to the MQTT broker on the AWS endpoint we defined earlier
  client.setServer(AWS_IOT_ENDPOINT, 8883);
 
  // Create a message handler
  client.setCallback(messageHandler);
 
  //Serial.println("Connecting to AWS IOT");
 
  while (!client.connect(THINGNAME)){
    //Serial.print(".");
    delay(100);
  }
 
  if (!client.connected()){
    //Serial.println("AWS IoT Timeout!");
    return;
  }
  // Subscribe to a topic
  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
 
  //Serial.println("AWS IoT Connected!");
}

void setup() {
  // put your setup code here, to run once:
  //Serial.begin(115200);
  Serial2.begin(115200, SERIAL_8N1, RXD2, TXD2);
  connectAWS();
}

void loop() {
  if(Serial2.available()){
    String recv_str_jsondata  = "";
    recv_str_jsondata = Serial2.readStringUntil('\n');
    //Serial.println("Receive:");
    //Serial.println(recv_str_jsondata);
    //char jsonBuffer[1024];
    char jsonBuffer[2048];
    deserializeJson(doc_recv_from_client, recv_str_jsondata);
    serializeJson(doc_recv_from_client, jsonBuffer);
    client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);
  }
  client.loop();
  delay(100);
}