# ToolCase App

## Features

### Dashboard
A easy access dashboard to allow you to navigate between the different tools.

### Wi-Fi Connectivity:
Connects directly to your Raspberry Pi Pico W over Wi-Fi as a data source.
API Integration: Makes API calls with the data received from your Raspberry Pi Pico W.

### Temperature
The app allows users to Get real-time temperature data directly from your Raspberry Pi Pico W.

## Features

- **Temperature Page:** Displays temperature data received from Raspberry Pi Pico and provides options to view temperature in Celsius or Fahrenheit.


## Screenshots

# Temperature Page
![Temperature Page](screenshots/temperature_page.png)


## Usage

- **Temperature Page:**
    - Open the app and navigate to the Temperature Page.
    - Ensure that the Raspberry Pi Pico is connected and providing temperature data.
    - Choose between Celsius and Fahrenheit to view temperature readings.

### Range Finder App
The app allows users to measure distances accurately using the range finder connected to your Raspberry Pi Pico W.

## Features
- **Range Finder Page:** Measures distances using a connected range finder. Users can input measurements for height, width, or length and calculate volume (cubic yards or cubic feet) or area (square feet).

## Screenshots

# Range Finder Page
![Range Finder Page](screenshots/range_finder_page.png)



## Usage
- **Range Finder Page:**
    - Navigate to the Range Finder Page.
    - Connect the range finder to the Raspberry Pi Pico.
    - Use the range finder to measure distances for height, width, or length.
    - Input measurements and select desired calculations for volume or area.

## Contributors
- Eric Izekor
- Justin McMullens

## License


## Acknowledgments


### Built-in Bubble Level:
Utilize the integrated level for your construction or DIY projects, featuring code from an open-source project.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Bubble1.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/Bubble2.png" width="150"/>

With the level you can measure angles and inclination. Simply align one of your phone's four sides with an object or place it on a level surface to determine its orientation. The code for a ruler in cm and inches is also in the code but currently commented out.

## Getting Started

### Prerequisites
This app is designed to be paired with the tool case that has the raspberry Pi Pico W built into the case with a power source, thermometer and rangefinder. If you have this case, all you will need is asmartphone capable of installing the ToolCase App and connecting to the Wi-Fi.

If you do not have the specifically designed case the following would be needed:

-Raspberry Pi Pico W setup with temperature and range finder sensors.

-The Pico W should be configured to send data over Wi-Fi in a JSON format.

### Installation
To use this app, follow these steps:
1. Clone this repository to your local machine.
2. Open the project in Android Studio.
3. Connect your Android device to your computer.
4. Build and run the app on your device.


#### ToolCase App Installation:
Download and install the ToolCase App through the following link:

** ADD APK DOWNLOAD FILE LINK ***

#### Raspberry Pi Pico W Setup (If you do not have the case):
Ensure your Raspberry Pi Pico W is correctly set up with the temperature sensor and range finder connected, and it's broadcasting data over a Wi-Fi network.

### Connecting to Raspberry Pi Pico W
Connect your mobile device to the Raspberry Pi Pico W as a Wi-Fi network through the settings app.if the Pico W has been programmed correctly, the app will automatically pull the data from the Pico W once the Wi-Fi connection has been made.

## License

RulerView taken from https://github.com/SecUSo/privacy-friendly-ruler, published under GPL3.0 license

```
An Android Bubble Level application.

Copyright (C) 2014  Antoine Vianey
Copyright (C) 2021- woheller69

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Level is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Level. If not, see <http://www.gnu.org/licenses/>
```
