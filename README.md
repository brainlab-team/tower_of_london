# Tower Of London

## Prerequisites
- Python 2.7
- Windows 7 o 8.1 or Ubuntu >=14.04 or Mac OS X 10.11 El Capitan
- Conda virtual environment (optional)

## Initial Setup
1. Make sure you have Python 2.7 installed on your system. If not, download and install Python 2.7 from the official website: [Python 2.7 Download](https://www.python.org/downloads/release/python-279/)
2. If your python version is different of python 2.7 create a Conda virtual environment (optional):
    ```bash
    conda create --name environment_name python=2.7
    ```

## Aldebaran SDK Installation for Python

1. Download the SDK from the official website: [Aldebaran SDK Install Guide](http://doc.aldebaran.com/2-5/dev/python/install_guide.html)
2. Follow the installation instructions for your operating system.
3. Set up the SDK as described in the documentation.

## Android Studio Configuration

1. Install Android Studio on your system. You can download it from: [Android Studio Download](https://developer.android.com/studio?hl=it)
2. Open the project file in Android Studio and navigate to `TowerOfLondon/app/src/main/java/com/example/toweroflondon/MainActivity.kt`
3. Before proceeding, find the IP address of your machine. 

    On Ubuntu, you can use the following command:
    ```bash
    ifconfig
    ```

    On Windows, you can use the following command:
    ```cmd
    ipconfig
    ```

    On MacOS, you can use the following command:
    ```bash
    ifconfig
    ```
    Note down the IP address associated with your network interface (e.g., eth0, wlan0).


4. In sections related to servers, make sure to configure the correct IP address:
    ```kotlin
    val url = URL("Inser your ID")
    ```

5. Build the APK from Android Studio: 
    ```cmd
    Build-> Build Bundle(s) / APK(s) -> Build APK(s)   
    ```

6. Transfer it to Pepper's tablet with Telegram or Drive, then download it on that device.

7. Make sure the app works.

## Running the Code
1. Open a terminal.
2. Activate the virtual environment if it has been created, otherwise, make sure to use Python 2.7.
3. Execute the Python code.
    ```bash
    python main.py
    ```
