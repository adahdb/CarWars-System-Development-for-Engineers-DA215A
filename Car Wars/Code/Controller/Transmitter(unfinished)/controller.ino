#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

// Define button pins
const int btnPin1 = 21;  // Button 1 (Forward)
const int btnPin2 = 22;  // Button 2 (Backward)
const int btnPin3 = 26;  // Button 3 (Left)
const int btnPin4 = 27;  // Button 4 (Right)

// Radio setup
RF24 radio(4, 5); // CE, CSN pins
const byte address[6] = "00001"; // Receiver address
uint8_t btnStatus = 0b00000000;  // 8-bit byte to store button states (0 for not pressed, 1 for pressed)

// Task handles
TaskHandle_t btnReadTask1Handle;
TaskHandle_t btnReadTask2Handle;
TaskHandle_t radioSendTaskHandle;

void setup() {
  Serial.begin(9600);

  // Set button pins as inputs with pull-up resistors
  pinMode(btnPin1, INPUT_PULLUP);
  pinMode(btnPin2, INPUT_PULLUP);
  pinMode(btnPin3, INPUT_PULLUP);
  pinMode(btnPin4, INPUT_PULLUP);

  // Initialize the radio
  if (!radio.begin()) {
    Serial.println("Radio hardware not responding!");
    while (1);
  }

  if (!radio.isChipConnected()) {
    Serial.println("nRF24L01 not connected properly.");
    while (1);
  }

  radio.setDataRate(RF24_250KBPS);
  radio.setPALevel(RF24_PA_LOW);
  radio.setRetries(15, 15);
  radio.openWritingPipe(address);
  radio.stopListening(); // Set to transmitter mode

  Serial.println("Transmitter ready.");
  radio.printDetails();

  // Create tasks
  xTaskCreatePinnedToCore(
    readButtonStates1,   // Task function
    "Read Button States 1", // Task name
    2000,               // Stack size
    NULL,               // Parameters
    1,                  // Priority
    &btnReadTask1Handle, // Task handle
    0                   // Core 0
  );

  xTaskCreatePinnedToCore(
    readButtonStates2,   // Task function
    "Read Button States 2", // Task name
    2000,               // Stack size
    NULL,               // Parameters
    1,                  // Priority
    &btnReadTask2Handle, // Task handle
    1                   // Core 1
  );

  xTaskCreatePinnedToCore(
    sendRadioData,      // Task function
    "Send Radio Data",  // Task name
    2000,               // Stack size
    NULL,               // Parameters
    1,                  // Priority
    &radioSendTaskHandle, // Task handle
    0                   // Core 0 (or Core 1)
  );
}

void loop() {
  // Nothing to do here, tasks are running concurrently
}

// Task 1: Read button 1 and button 2 states (Core 0)
void readButtonStates1(void *pvParameters) {
  for (;;) {
          //Serial.println("heja");

    btnStatus &= 0b00111111; // Reset button states

    // Read button states and update btnStatus byte
    if (digitalRead(btnPin1) == LOW) { // Button 1 (Forward)
      btnStatus |= (1 << 7);
    }
    if (digitalRead(btnPin2) == LOW) { // Button 2 (Backward)
      btnStatus |= (1 << 6);
      Serial.print("backward");
    }

    vTaskDelay(100 / portTICK_PERIOD_MS); // Delay to prevent excessive reading
  }
}

// Task 2: Read button 3 and button 4 states (Core 1)
void readButtonStates2(void *pvParameters) {
  for (;;) {
         // Serial.println("då");

    btnStatus &= 0b11000000; // Reset button states

    // Read button states and update btnStatus byte
    if (digitalRead(btnPin3) == LOW) { // Button 3 (Left)
      btnStatus |= (1 << 3);
    }
    if (digitalRead(btnPin4) == LOW) { // Button 4 (Right)
      btnStatus |= (1 << 2);
    }

    vTaskDelay(100 / portTICK_PERIOD_MS); // Delay to prevent excessive reading
  }
}

// Task 3: Send the button states to the receiver
void sendRadioData(void *pvParameters) {
  for (;;) {
    if (radio.write(&btnStatus, sizeof(btnStatus))) {
      Serial.print("Sent button status: ");
      Serial.println(btnStatus, BIN);
    } else {
      Serial.println("Failed to send data");
    }

    vTaskDelay(100 / portTICK_PERIOD_MS); // Delay to avoid sending too often
  }
}
