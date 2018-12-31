# What Is It?
Tostr is a project powered by a PHP web server, a Raspberry Pi and an Android companion application (what this project is) and they communicate between each other using a service called PubNub. We have an ordinary toaster modified to detect when the toast goes down and when it pops. When either of these events occur, a message is sent to the server.

## Typical Usage Flow
Create an alarm in the Android application. When the alarm goes off, a message is sent to the web server which records the alarm time. To stop the alarm, you have to push the handle down on the toaster. Otherwise, force quit the app (or press the 'Stop Alarm' button in the application as of right now). After pushing the handle down on the toaster, a Python script on the Raspberry Pi will detect the handle being pushed down using a button. It will send a message to the web server to stop the alarm. This message is then forwarded to the Android application which it will then stop the alarm on the phone.

This is all possible in a simple manner because of PubNub.

<p align="center">
  <img src="https://i.imgur.com/nhdnNkO.jpg" alt="Tostr Promo Picture"/>
</p>

## Why?
Devyn and I were in our Internet of Things course at university and needed to come up with a project that could be created and demoed by the end of the semester. Now, we both found it difficult to get up early in the morning to catch class and thought we could design something that would automatically make some quick toast for us. That's where Tostr was born.
