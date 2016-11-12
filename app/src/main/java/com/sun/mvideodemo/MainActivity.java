package com.sun.mvideodemo;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {


    /*资源*/
    private Uri uri;
    private String path = "http://baobab.wdjcdn.com/145076769089714.mp4";

    /* extends from SurfaceView*/
    @Bind(R.id.main_buffer)
    VideoView mainBuffer;

    /*杂项*/
    @Bind(R.id.main_progressbar)
    ProgressBar mainProgressbar;
    @Bind(R.id.main_download_rate)
    TextView mainDownloadRate;
    @Bind(R.id.main_load_rate)
    TextView mainLoadRate;


    /* MediaController from Vitamio and CustomMediaController */
    private MediaController mediaControl;
    private CustomMediaController customController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*设置全屏*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*初始化Vitamio*/
        Vitamio.isInitialized(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
        initData();
    }

    private void initData() {
        uri = Uri.parse(path);
        mainBuffer.setVideoURI(uri);//设置视频播放地址
        mainBuffer.setMediaController(customController);//设置媒体控制器。

        /*设置视频质量。 low medium high*/
        mainBuffer.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        /*设置显示时间，过后隐藏*/
        mediaControl.show(5000);
        /*获取焦点方法，extends from View*/
        mainBuffer.requestFocus();
        /*注册一个回调函数，在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化。*/
        mainBuffer.setOnInfoListener(this);
        /*注册一个回调函数，在网络视频流缓冲变化时调用。*/
        mainBuffer.setOnBufferingUpdateListener(this);
        /* 注册一个回调函数，在视频预处理完成后调用,在视频预处理完成后被调用。
        * 此时视频的宽度、高度、宽高比信息已经获取到，此时可调用seekTo让视频从指定位置开始播放。*/
        mainBuffer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
    }

    private void init() {
        /*用于加载模块*/
        mediaControl = new MediaController(this);
        /*用于控制模块*/
        customController = new CustomMediaController(this, mainBuffer, this);
        customController.setFileName("filename");
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mainBuffer.isPlaying()) {
                    mainBuffer.pause();
                    mainProgressbar.setVisibility(View.VISIBLE);
                    mainDownloadRate.setText("");
                    mainDownloadRate.setVisibility(View.VISIBLE);
                    mainLoadRate.setText("");
                    mainLoadRate.setVisibility(View.VISIBLE);
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mainBuffer.start();
                mainProgressbar.setVisibility(View.GONE);
                mainDownloadRate.setVisibility(View.GONE);
                mainLoadRate.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                mainDownloadRate.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mainLoadRate.setText(percent + "%");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mainBuffer != null) {
            mainBuffer.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
