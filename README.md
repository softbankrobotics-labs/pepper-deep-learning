# Object recognition on Pepper with Deep Learning


This application demonstrates how the Pepper robot can recognize objects using deep learning algorithms.
It comes with a pre-trained deep learning model that allows Pepper to recognize up to 80 different objects with its camera.

# Video

Please see the following video on YouTube for some more information about how this project works in practice: https://www.youtube.com/watch?v=93M1oY2VJfE

## 1. Hardware & software compatibility

This app is compatible with Pepper, and [Naoqi 2.9.5](https://developer.softbankrobotics.com/blog/pepper-qisdk-os-295-release-note)

## 2. Supported language

This app is available in:

* French
* English

The application will automatically switch to English or French language when you change the language on Pepper tablet.

## 3. Install the app

### 3.1. Dependencies

#### 3.1.1. Language french

If you want to use the Deep Pepper app in French, you need to have the French language installed on your robot. If it is not installed by default, follow [this guide](https://developer.softbankrobotics.com/blog/lets-make-pepper-and-nao-speak-second-language) to find out how to install a second language on Pepper.

### 3.2. Install the app using Android Studio

To install this application on your Pepper, you need first to [install Android Studio and the Pepper QiSDK](https://developer.softbankrobotics.com/pepper-qisdk/getting-started/installing-pepper-sdk-plug).

Once this is done, open this project in Android studio, and [run this application on your Pepper](https://developer.softbankrobotics.com/pepper-qisdk/getting-started/running-application#running-an-application-on-a-real-robot)

## 4. Use the app

### 4.1. Start the app

When your application has been installed on Pepper, you don't need Android Studio anymore, and can directly start it using Pepper tablet and clicking on the app icon (search for the *DeepPepper* app).

### 4.2. Recognized objects

We provide a default deep learning model that was trained on [COCO Dataset](https://cocodataset.org/). Using this model, Pepper can recognize up to 80 different objects.

You can view the complete list of recognized object in [this file](app/src/main/assets/word_list_en.txt) (or [this one](app/src/main/assets/word_list_fr.txt) for the same list in French language). Open it with a text editor.


### 4.3. Voice interaction

When the Deep Pepper application is running, Pepper will listen for and react to specific sentences.
You can say to Pepper:

* *"What do you see"* (*"Qu'est ce que tu vois?"*): Pepper will tell you what objects it currently detect and recognize
* *"Tell me everything you see"* (*"Dis moi tout ce que tu vois"*): Pepper will keep telling you what objects it detects and recognizes. It will also look left and right to find more objects to recognize. Say *"Stop"* when you want Pepper to stop.
* *"Look around you"* (*"Regarde autour de toi"*): Pepper will look around it, and tell you what objects it recognize
* *"Search for a/an <Insert here the name one of the object Pepper can recognise, see section 4.2>"* (*"Cherches un(e) <Nom d'un objet, voir section 4.2>"*): Pepper will look for that specific object, and if it finds it, will tell you where it sees it.
* *"Help"*: Display a help screen with the sentences Pepper reacts to.




