#include <Bluepad32.h>
#include <Wire.h>
#include <WiFi.h>
#include <math.h>
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>
#include <freertos/semphr.h>

// ADXL345 Constants
#define ADXL345_ADDR 0x53
#define POWER_CTL 0x2D
#define DATA_FORMAT 0x31
#define DATAX0 0x32

// WiFi & Server settings 
WiFiClient client;
const char* ssid = "-";
const char* password = "-";  
const char* server = "-";
const int port = 6000;
boolean responseStatus = false;
String startInstruction;

// Motor pins
int motorPin2 = 12;
int motorPin1 = 27;
int motorPin3 = 16;
int motorPin4 = 17;

// Motor control deadzone
const int DEADZONE = 100;

// Bluepad32 controllers
ControllerPtr myControllers[BP32_MAX_GAMEPADS];
SemaphoreHandle_t controllerMutex;

// Collision detection globals
float previousMagnitude = 0;
const float collisionThreshold = 2.0; // Gs

// Function declarations
void SteeringTask(void *pvParameters);
void CollisionTask(void *pvParameters);
void readXYZ(int16_t *x, int16_t *y, int16_t *z);

// Controller connection callbacks
void onConnectedController(ControllerPtr ctl) {
    for (int i = 0; i < BP32_MAX_GAMEPADS; i++) {
        if (myControllers[i] == nullptr) {
            myControllers[i] = ctl;
            Serial.printf("Controller connected at index %d\n", i);
            break;
        }
    }
}

void onDisconnectedController(ControllerPtr ctl) {
    for (int i = 0; i < BP32_MAX_GAMEPADS; i++) {
        if (myControllers[i] == ctl) {
            Serial.printf("Controller for CAR2 disconnected from index %d\n", i);
            client.println("Controller for CAR2 disconnected");
            myControllers[i] = nullptr;
            break;
        }
    }
}

void setup() {
    Serial.begin(115200);

    //Connect the Esp32 to wifi
  WiFi.mode(WIFI_STA);  // Station mode (recommended)
  Serial.println("connecting to wifi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("Wifi connected");
  
  //Connect the Esp32 to the server
  Serial.println("connecting to server");
  boolean connectionStatus = client.connect(server, port);
  while(!connectionStatus){
     delay(500);
    Serial.print(".");
  }
  Serial.println("connected to wifi");
  client.println("hej från bil1"); //test

  
  Serial.println("Setting Up Motor Pins");

    // Setup motor pins
    pinMode(motorPin1, OUTPUT);
    pinMode(motorPin2, OUTPUT);
    pinMode(motorPin3, OUTPUT);
    pinMode(motorPin4, OUTPUT);

    digitalWrite(motorPin1, LOW);
    digitalWrite(motorPin2, LOW);
    digitalWrite(motorPin3, LOW);
    digitalWrite(motorPin4, LOW);

    // ADXL345 setup
    Wire.begin();
    Wire.beginTransmission(ADXL345_ADDR);
    Wire.write(POWER_CTL);
    Wire.write(0x08); // Measurement mode
    Wire.endTransmission();

    Wire.beginTransmission(ADXL345_ADDR);
    Wire.write(DATA_FORMAT);
    Wire.write(0x0B); // Full res, ±16g
    Wire.endTransmission();

    delay(100);

    // Bluepad32 setup
    BP32.setup(&onConnectedController, &onDisconnectedController);
    BP32.enableVirtualDevice(false);
    BP32.forgetBluetoothKeys();

    controllerMutex = xSemaphoreCreateMutex();

    if (client.connected() && client.available()) {

    while (startInstruction != "start") {
      startInstruction = client.readStringUntil('\n');
      if (startInstruction == "start") {
        Serial.println("started match");
        // Create tasks
        xTaskCreatePinnedToCore(
            SteeringTask,
            "Steering",
            4096,
            NULL,
            2,
            NULL,
            0
        );
    
        xTaskCreatePinnedToCore(
            CollisionTask,
            "Collision Detection",
            2048,
            NULL,
            3,
            NULL,
            1
        );
      }
    } 
  }
}

void loop() {
    // Everything is handled by tasks
}

void SteeringTask(void *pvParameters) {
    for (;;) {
        BP32.update();

        if (xSemaphoreTake(controllerMutex, pdMS_TO_TICKS(10))) {
            for (auto ctl : myControllers) {
                if (ctl && ctl->isConnected() && ctl->hasData() && ctl->isGamepad()) {
                    int leftY = ctl->axisY();    // Forward/backward
                    int rightX = ctl->axisRX();  // Left/right

                    Serial.printf("[Steering] LeftY: %d | RightX: %d\n", leftY, rightX);

                    // Motor 1 & 2: Left track
                    if (leftY < -DEADZONE) {
                        Serial.println("[Motor] Left track: FORWARD");
                        digitalWrite(motorPin1, LOW);
                        digitalWrite(motorPin2, HIGH);
                    } else if (leftY > DEADZONE) {
                        Serial.println("[Motor] Left track: BACKWARD");
                        digitalWrite(motorPin1, HIGH);
                        digitalWrite(motorPin2, LOW);
                    } else {
                        digitalWrite(motorPin1, LOW);
                        digitalWrite(motorPin2, LOW);
                    }

                    // Motor 3 & 4: Right track
                    if (rightX < -DEADZONE) {
                        Serial.println("[Motor] Right track: LEFT TURN");
                        digitalWrite(motorPin3, LOW);
                        digitalWrite(motorPin4, HIGH);
                    } else if (rightX > DEADZONE) {
                        Serial.println("[Motor] Right track: RIGHT TURN");
                        digitalWrite(motorPin4, LOW);
                        digitalWrite(motorPin3, HIGH);
                    } else {
                        digitalWrite(motorPin3, LOW);
                        digitalWrite(motorPin4, LOW);
                    }
                }
            }
            xSemaphoreGive(controllerMutex);
        }

        vTaskDelay(pdMS_TO_TICKS(50)); // 20Hz update
    }
}


void CollisionTask(void *pvParameters) {
    for (;;) {
        int16_t x, y, z;
        readXYZ(&x, &y, &z);

        float ax = x * 0.0039;
        float ay = y * 0.0039;
        float az = z * 0.0039;

        float magnitude = sqrt(ax * ax + ay * ay + az * az);
        float delta = fabs(magnitude - previousMagnitude);

        if (delta > collisionThreshold) {
            Serial.println("  <-- COLLISION DETECTED! Stopping motors...");
            client.println("BIL2:KROCK");

          
        }

        previousMagnitude = magnitude;
        vTaskDelay(pdMS_TO_TICKS(50));
    }
}


void readXYZ(int16_t *x, int16_t *y, int16_t *z) {
    Wire.beginTransmission(ADXL345_ADDR);
    Wire.write(DATAX0);
    Wire.endTransmission(false);
    Wire.requestFrom(ADXL345_ADDR, 6);

    if (Wire.available() == 6) {
        *x = Wire.read() | (Wire.read() << 8);
        *y = Wire.read() | (Wire.read() << 8);
        *z = Wire.read() | (Wire.read() << 8);
    }
}
