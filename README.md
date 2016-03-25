# MilkVrBatch
Android APK - Create batch .mvrl files for MilkVR playback through Universal Media Server DLNA

Note: This app parses HTML from the UMS web interface to locate all video files. With a couple tweaks it should also work for other dlna servers with a web interface that can be parsed easily. Any future changes made to the UMS web interface could affect this apps functionality. In the future I might convert this to fully browse DLNA using an Android DLNA api instead of parsing HTML, but it works with UMS now the way it is.

This application requires Universal Media Server and the web interface running.

Universal Media Server should also have transcoding disabled to allow seeking functionality

in UMS android devices can be detected as chromeCast. Disable chromeCast renderer in UMS to avoid this, or ensure proper android rendered is used. Restart of UMS may be needed

Required files:

MilkVRBatch.apk
MilkVR (Gear Vr app)
Universal Media Server - http://www.universalmediaserver.com/ (current working version 6.2.0)
Android DLNA App: Example BubbleUpnp (to initialize proper connection with Universal Media Server)
  (https://play.google.com/store/apps/details?id=com.bubblesoft.android.bubbleupnp)


Steps:

1. Install MilkVRBatch.apk app
2. Install BubbleUpnp app from playstore (May not be required if step 12 isn't needed as FIX)
3. Ensure UMS is running + web interface (http://www.universalmediaserver.com/)
4. Setting in UMS Navigation Share/Settings : Disable 'Hide file extensions' (you want to know its a .mp4)
5. Setting in UMS Navigation Share/Settings : Enable "hide Empty folder', 'hide the cache folder', 'hide the transcode folder', 'hide     the new media folder'
6. connect to UMS web interface through android web browser (Default address= http://<serverIP>:9001/)
7. Browse until you find the desired media folder (example: http://<serverIP>:9001/browse/2918) and copy URL
8. Open MilkVRBatch android app copy/paste URL into app 
9. Ensure DLNA Port is set (default 5001)
10. click "Generate .mvrl.." button
11. Files are now in sdcard/MilkVR
12.
THIS STEP MAY NOT BE REQUIRED. But is a fix to ensure playback works on MilkVR
  a) Run BubbleUpnp.
  b) Opening BubbleUpnp will initialize the proper settings between UMS and your android/IP for DLNA playback. You will see an android      renderer show up in your Universal Media Server list on the server. You can now leave BubbleUpnp open or close it
  c) Final test - not needed: Open a video through BubbleUpnp + Any Video App will ensure playback works. Close video

11. Start MilkVR and run .mvrl files from sideloaded
