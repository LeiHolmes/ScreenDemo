package com.sherlockxu.screendemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

import cn.sharerec.recorder.MediaOutput;
import cn.sharerec.recorder.Recorder;
import cn.sharerec.recorder.impl.SystemRecorder;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final String VIDEO_PATH = "/ShareREC/video";
    public static final String SOUND_PATH = "/ShareREC/sound";
    public static final String APP_KEY = "1bd81b4808ec4";
    public static final String APP_SECRET = "91a1c41c80cbc11aba920eaa83992ffb";

    private Button start, end, show;
    private SystemRecorder recorder;
    private MediaRecorder mRecorder = null;
    private String soundFileName = "";
    private boolean isStart = false;
    // 要申请的权限
    private String[] permissions = {Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recorder = new SystemRecorder(this, APP_KEY, APP_SECRET);
//        recorder = new ViewRecorder(findViewById(R.id.action_container), "1bd81b4808ec4", "91a1c41c80cbc11aba920eaa83992ffb");
        // 设置视频的最大尺寸
        recorder.setMaxFrameSize(Recorder.LevelMaxFrameSize.LEVEL_1920_1080);
        // 设置视频的质量（高、中、低）
        recorder.setVideoQuality(Recorder.LevelVideoQuality.LEVEL_VERY_HIGH);
        // 设置视频的最短时长
        recorder.setMinDuration(3 * 1000);
        // 设置视频的输出路径
        recorder.setCacheFolder("/sdcard" + VIDEO_PATH);
        // 设置是否强制使用软件编码器对视频进行编码（兼容性更高）
        recorder.setForceSoftwareEncoding(true, true);
        // 设置监听回调 有问题：会导致文件打不开
//        recorder.setMediaOutput(output);
        requestPermission();//请求权限

        initView();
        initSoundData();
        initListener();
    }

    private void initSoundData() {
        soundFileName = Environment.getExternalStorageDirectory().getPath() + SOUND_PATH;
        File file = new File(soundFileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        soundFileName += "/sound1.3gp";
    }

    private void initView() {
        start = (Button) findViewById(R.id.start);
        end = (Button) findViewById(R.id.end);
        show = (Button) findViewById(R.id.show);
    }

    private void initListener() {
        start.setOnClickListener(this);
        end.setOnClickListener(this);
        show.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                if (!isStart) {//不可连续点多次开始录屏
                    //清除缓存
                    deleteCache();
                    //开始录屏
                    if (recorder.isAvailable()) {
                        recorder.start();
//                    recorder.startRecorder();
                    } else {
                        Toast.makeText(MainActivity.this, "录屏isNotAvailable", Toast.LENGTH_SHORT).show();
                    }
                    //开始录音
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setOutputFile(soundFileName);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.e("show_media", "prepare() failed");
                    }
                    mRecorder.start();
                    Log.e("show_media", "录屏录音开始");
                    isStart = true;
                }
                break;
            case R.id.end:
                //停止录屏
                if (isStart) {
                    if (recorder.isAvailable()) {
//                        start.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
                        recorder.stop();
//                    recorder.stopRecorder();
//                    recorder.showShare();  
//                            }
//                        }, 1000);
                    } else {
                        Toast.makeText(MainActivity.this, "录屏isNotAvailable", Toast.LENGTH_SHORT).show();
                    }

                    //停止录音
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    Log.e("show_media", "录屏录音停止");
                    isStart = false;
                }
                break;
            case R.id.show:
                startActivity(new Intent(this, ShowMediaActivity.class));
                break;
        }
    }

    /**
     * 保存视频音频前清空文件夹
     */
    public void deleteCache() {
        //判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdcardDir = Environment.getExternalStorageDirectory();

            String file_path = sdcardDir.getPath() + VIDEO_PATH;
            File file = new File(file_path);
            if (!file.exists()) {
                file.mkdirs();
            } else {
                File[] file_list = file.listFiles();
                for (int i = 0; i < file_list.length; i++) {
                    file_list[i].delete();
                }
            }

            String file_path1 = sdcardDir.getPath() + SOUND_PATH;
            File file1 = new File(file_path1);
            if (!file1.exists()) {
                file1.mkdirs();
            } else {
                File[] file_list = file1.listFiles();
                for (int i = 0; i < file_list.length; i++) {
                    file_list[i].delete();
                }
            }
        }
    }

    MediaOutput output = new MediaOutput() {
        @Override
        public void onStart() {
            Log.e("file_path", "录像开始");
        }

        @Override
        public void onPause() {
            Log.e("file_path", "录像暂停");
        }

        @Override
        public void onResume() {
            Log.e("file_path", "录像恢复");
        }

        @Override
        public void onStop() {
            Log.e("file_path", "录像停止");
        }

        @Override
        public void onAudio(Buffer buffer, int i, long l, int i1) {

        }

        @Override
        public void onVideo(Buffer buffer, int i, long l, int i1) {

        }
    };

    /**
     * 6.0以上动态申请权限
     */
    public void requestPermission() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                int permissionStatus = ContextCompat.checkSelfPermission(this, permissions[i]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有授予该权限，就去提示用户请求
                    ActivityCompat.requestPermissions(this, permissions, 110);
                    break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110) {

        }
    }
}
