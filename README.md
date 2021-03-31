# :mag_right: FaceLab - A simple face analysis application using AI :mag_right:
<img alt="Logo" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="80">

<a href='https://play.google.com/store/apps/details?id=com.FaceLab.facelab'>
<img src='https://simplemobiletools.com/assets/images/google-play.png' alt='Get it on Google Play' height=45/></a>

## :wave: Introduction
This is a simple face analysis application using AI
1. You can use images from an album or take pictures.
2. You can obtain results from the following list:
- Sex : Male / Female
- Age : baby / children / elementary school student / middle school student / high school student / 20s / 30s / 40s / 50s / over 60s
- Emotion : Anger / Disgust / Fear / Happy / Sad / Surprise / Neutral
3. You can share the results on SNS directly.
4. You can save the results as a picture.

## :hammer: Development Environment
- Android Studio @4.0.1

## :bookmark: Application Version
- minSdkVersion : 15
- targetSdkVersion : 29

## :art: Program Structure
|         Activity         |                         Description                          |
| :----------------------: | ---------------------------------------------------------- |
|      `TestActivity`      |  The screen that displays cropped pictures and moves to the next for analysis  |
|        `realMain`        |  The screen that proceeds with the analysis and shows the results  |
|        `loading`         |  The screen where you select a picture  |
|       `information`      |  The screen showing developer information  |

## :books: Libraries Used
```
implementation 'androidx.appcompat:appcompat:1.0.2'
implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
implementation 'com.google.android.material:material:1.0.0'
testImplementation 'junit:junit:4.12'
androidTestImplementation 'androidx.test:runner:1.2.0'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation 'com.android.support:support-v4:26.1.0'
implementation 'com.android.support:design:26.1.0'
implementation 'com.android.support:cardview-v7:28.0.0'
implementation 'org.tensorflow:tensorflow-lite:+'
implementation "gun0912.ted:tedpermission:2.1.0"
implementation 'com.google.android.gms:play-services-ads:18.0.0'
implementation 'com.soundcloud.android:android-crop:1.0.1@aar'
```
