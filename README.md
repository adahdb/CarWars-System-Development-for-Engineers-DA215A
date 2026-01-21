# CarWars - Interactive RC Car Battle Game

## Overview

CarWars is an interactive battle game where remote-controlled (RC) cars fight each other by colliding. The cars are equipped with accelerometers to detect impacts, and the game tracks the health of each vehicle. When a car's health reaches 0%, it is eliminated. The last remaining car wins. This project was designed as a smaller version of Robot Wars, with RC cars replacing robots, combining hardware, software, and real-time interaction to create a competitive game.

## Purpose

The goal of CarWars was to develop a physical multiplayer game that blends electronics, software, and real-time interaction. The idea was to use NRF24L01 modules to wirelessly control the cars, with both the controller and car systems implemented from scratch. However, due to time constraints, we adapted the project by using a PlayStation 4 controller to control the car. The project also initially planned to 3D print the chassis, but the tight deadlines led to purchasing pre-built RC cars, which were stripped of their internals and replaced with our custom circuitry.

This project was completed as part of the course Systemutveckling och projekt för ingenjörer, where we applied engineering concepts in system development and project management.

## Features
- Wireless RC Car Control: Initially designed to use NRF24L01 for communication between the car and controller, we ended up using a PlayStation 4 controller for ease of implementation.
- Collision Detection: The cars use built-in accelerometers (ADXL345) to detect impacts.
- Health Tracking: Each car has health points that decrease with every collision. When a car's health reaches 0%, it’s eliminated from the game.
- Leaderboard: A simple terminal leaderboard shows the real-time status of each car.
- Game Restart: The game can be easily restarted after each match.

## Hardware Used
- RC Cars: Purchased RC cars that were stripped of their internals and replaced with custom-designed PCBs (ESP32, NRF24L01, ADXL345).
- Controller: A PlayStation 4 controller used to control the cars due to time constraints (originally planned to use NRF24L01).
- Server: A central server (Java-based) that tracks collisions and player status.
- Wi-Fi & Communication: ESP32 handles Wi-Fi communication to the server.

## Group Members

- **Adnan Alahdab** – [LinkedIn Profile](https://www.linkedin.com/in/adnan-alahdab-076056281/)
- **Joshua Owald** – [LinkedIn Profile](https://www.linkedin.com/in/joshua-owald-337759271/)
- **Omar Yassin** – [LinkedIn Profile](https://www.linkedin.com/in/omar-yassin-6b24ba273/)
- **Rachid Kontakgi** – [LinkedIn Profile](https://www.linkedin.com/in/rachid-kontakgi-87b810225/)
