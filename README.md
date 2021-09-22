# :mag_right: FaceLab - A simple face analysis application using AI :mag_right:
<img alt="Logo" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="80">

<a href='https://play.google.com/store/apps/details?id=com.FaceLab.facelab'>
<img src='https://simplemobiletools.com/assets/images/google-play.png' alt='Get it on Google Play' height=45/></a>

## :wave: Introduction
This is a simple face analysis application using AI
1. You can use images from an album or take pictures.
2. You can get results from the following list:
- Sex : Male / Female
- Age : baby / children / elementary school student / middle school student / high school student / 20s / 30s / 40s / 50s / over 60s
- Emotion : Anger / Disgust / Fear / Happy / Sad / Surprise / Neutral
3. You can share the results on SNS directly.
4. You can save the results as a picture.

## :camera: Screenshots
<img alt="Screenshot1" src="images/screenshot1.JPG" width="30%"> <img alt="Screenshot2" src="images/screenshot2.JPG" width="30%">

<img alt="Screenshot3" src="images/screenshot3.JPG" width="30%"> <img alt="Screenshot4" src="images/screenshot4.JPG" width="30%">

## :art: Program Structure
|         Activity         |                         Description                          |
| :----------------------: | ---------------------------------------------------------- |
|      `MainActivity`      |  The screen where you select a picture  |
|      `TestActivity`      |  The screen that displays cropped pictures and moves to the next for analysis  |
|      `ResultActivity`    |  The screen that proceeds with the analysis and shows the results  |
|      `InfoActivity`      |  The screen showing developer information  |

## :hammer: Development Environment
- Kotlin
- Android Studio - Arctic Fox | 2020.3.1
- minSdkVersion : 21
- targetSdkVersion : 31

## :books: Libraries Used
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- [TensorFlow Lite](https://github.com/tensorflow/tflite-support)
- [TedPermission](https://github.com/ParkSangGwon/TedPermission)
- [Mobile Ads](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start)
- [android-crop](https://github.com/jdamcd/android-crop)
