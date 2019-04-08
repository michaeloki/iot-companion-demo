# iot-companion-demo

# Project Setup
This is a demo Android app for controlling your Smart Home

To run this project on Android Studio do the following:

1. Clone this repository on your machine
2. Extract the folder to your development directory
3. Open Android Studio and click on File>>Open
4. Choose the directory in step 2 
5. Ensure you have Internet connection so that the gradle files for the third party libraries can be downloaded
6. Connect your device to the PC and grant permission when prompted. (Ensure Developer option is enabled on your phone)
7. Click on Run and select your device from the popup window.

# App Build

To create an apk file,do these:
1. Click on the Build from the menu at the top of Android Studio
2. Select Debug

# Tech Decision
I used an activity and four fragments,namely: Home,Bedroom,Living Room and Kitchen.
Since the rooms are part of the home it makes sense to use fragments in lieu of activities.
Intents were used to share data across the fragments and activity.
A background intent service was used to ping the weatherAPI every three minutes. The intent service doesn't run on the same thread as the activity, hence, the app won't hang and the system resource won't be exhausted. 

Retrofit library was used to make the API calls while the POJO files in the models directory had the data structure for the various endpoints.

Robolectric library was also used for creating the unit tests.

