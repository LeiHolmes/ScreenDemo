package com.sherlockxu.screendemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class ShowMediaActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private Button start, end, pause, restart;
    private VideoView videoview;
    private MediaController mediaController;
    private MediaPlayer mPlayer = null;
    private String soundFileName = "";
    private File videoFile;
    private boolean isHaveVideo = false; //是否有录屏文件
    private boolean isHaveSound = false; //是否有录音文件
    private int videoDuration = 0; //录屏播放进度
    private int soundDuration = 0; //录音播放进度
    private boolean isStopped = true; //录屏播放是否是停止状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_media);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        start = (Button) findViewById(R.id.start);
        end = (Button) findViewById(R.id.end);
        pause = (Button) findViewById(R.id.pause);
        restart = (Button) findViewById(R.id.restart);

        videoview = (VideoView) findViewById(R.id.videoview);
        mediaController = new MediaController(this);
        videoview.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoview);
    }

    private void initData() {
        soundFileName = Environment.getExternalStorageDirectory().getPath() + MainActivity.SOUND_PATH + "/sound1.3gp";
        videoFile = getVideoFile();

        if (new File(soundFileName).exists())//音频文件是否存在
            isHaveSound = true;
        else
            isHaveSound = false;

        if (videoFile != null)//录屏文件是否存在
            isHaveVideo = true;
        else
            isHaveVideo = false;

    }

    private void initListener() {
        start.setOnClickListener(this);
        end.setOnClickListener(this);
        pause.setOnClickListener(this);
        restart.setOnClickListener(this);
        videoview.setOnCompletionListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start: //播放
                //播放录屏
                if (isHaveVideo && videoview != null) {
                    videoview.setVideoPath(videoFile.getPath());
                    videoview.requestFocus();
                    videoview.start();
                    isStopped = false;
                } else {
                    Log.e("show_media", "录屏播放失败，文件不存在");
                }
                //播放录音
                if (isHaveSound) {
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(soundFileName);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e("show_media", "录音播放失败");
                    }
                }
                break;
            case R.id.end: //停止
                if (isHaveVideo && videoview != null) {
                    //没有找到videoview停止方法故如此实现停止功能
                    videoview.seekTo(0);
                    videoview.pause();
                    isStopped = true;
                }
                if (isHaveSound && mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                }
                break;
            case R.id.pause: //暂停
                if (isHaveVideo && videoview != null && videoview.isPlaying()) {
                    videoview.pause();
//                    videoDuration = videoview.getDuration();
                }
                if (isHaveSound && mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.pause();
//                    soundDuration = mPlayer.getCurrentPosition();
                }
                break;
            case R.id.restart: //恢复
                if (!isStopped) {//已经停止后不让点恢复
                    if (isHaveVideo && videoview != null && !videoview.isPlaying()) {
                        videoview.start();
                    }
                    if (isHaveSound && mPlayer != null && !mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                }
                break;
        }
    }

    /**
     * 录屏播放完成回调
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 录屏播放失败回调
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Toast.makeText(this, "录屏播放失败", Toast.LENGTH_SHORT).show();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        return false;
    }

    /**
     * 获取保存的录屏文件
     */
    public File getVideoFile() {
        File videoFile = null;
        File sdcardDir = Environment.getExternalStorageDirectory();
        String file_path = sdcardDir.getPath() + MainActivity.VIDEO_PATH;
        File file = new File(file_path);
        if (file.exists()) {
            File[] file_list = file.listFiles();
            if (file_list.length != 0) {
                videoFile = file_list[0];
            }
        }
        return videoFile;
    }
}
