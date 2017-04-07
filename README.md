# ScreenDemo
利用ShareREC实现录屏录音并能播放等功能。  
  
问题1：结束录屏后实际录制的视频文件最后会丢失1-2秒。  
解决方法：由于`recorder.setForceSoftwareEncoding(true, true);`强制使用了软件编码器对视频进行了编码压缩，导致录屏文件最后1-2秒会丢失。都设置为false就不会丢失了。`recorder.setForceSoftwareEncoding(false, false);`
