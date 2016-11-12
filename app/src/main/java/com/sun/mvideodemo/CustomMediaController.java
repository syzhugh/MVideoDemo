package com.sun.mvideodemo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sun.mvideodemo.view.LightTest;
import com.sun.mvideodemo.view.VolumeTest;

import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by Yaozong on 2016/9/17.
 */

public class CustomMediaController extends MediaController {

    private static final String TAG = "CustomMediaController";

    private static final int HIDEFRAM = 0;//控制提示窗口的显示

    /*
    * xml要求：
    * back filename pause
    * textView : current duration 控件id必须与源码要求一致（或者自行更改）
    *
    * */

    /*核心*/
    private VideoView videoView;
    /*-------------top--------------*/
    private ImageButton img_back;//返回按钮
    private TextView mFileName;//文件名

    private String videoname;//视频名称
    private int controllerWidth = 0;//设置mediaController高度为了使横屏时top显示在屏幕顶端

    /*-------------弹出窗口--------------*/
//    private View mVolumeBrightnessLayout;//提示窗口
//    private ImageView mOperationBg;//提示图片
//    private TextView mOperationTv;//提示文字

    private GestureDetector detector;

    private RelativeLayout volumeOrLight;
    private VolumeTest volume;
    private LightTest light;
//    private VolumeTest volume;

    /*-------------其它--------------*/
    /*辅助*/
    private Context context;
    private AppCompatActivity activity;
    private AudioManager audioManager;
    private Window window;

    /*音量*/
    private int mMaxVolume;
    private int mVolume = -1;
    /*亮度*/
    private float mBrightness = -1f;

    /*-------------控制窗口（有待开发）--------------*/

//    private SeekBar progress;
//    private boolean mDragging;
//    private MediaPlayerControl player;
//    private boolean dragging;


    /**
     * @param context
     * @param videoView extends from Surface and defined in Vitamio
     * @param activity  used to quit current Activity
     */
    public CustomMediaController(Context context, VideoView videoView, AppCompatActivity activity) {
        super(context);
        this.context = context;
        this.videoView = videoView;
        this.activity = activity;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        controllerWidth = wm.getDefaultDisplay().getWidth();
        detector = new GestureDetector(context, new MyGestureListener());

    }

    /*
    * 注意：
    * 1.viewgroup不能为null，CustomMediaController extends from FrameLayout作为父容器。不然，事务分发控件接收不到
    * 2.控件ID一定与源码内一致（或自行修改）
    * */

    @Override
    protected View makeControllerView() {
        Log.i("info", "CustomMediaController:makeControllerView----------------------");
//        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("mcontroller", "layout", getContext().getPackageName()), this);
        View v = activity.getLayoutInflater().inflate(R.layout.mcontroller, this);
        v.setMinimumHeight(controllerWidth);
        //获取控件
//        img_back = (ImageButton) v.findViewById(getResources().getIdentifier("mcontroller_top_back", "id", context.getPackageName()));
        img_back = (ImageButton) v.findViewById(R.id.mcontroller_top_back);
//        mFileName = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_file_name", "id", context.getPackageName()));
        mFileName = (TextView) v.findViewById(R.id.mediacontroller_file_name);
        if (mFileName != null) {
            mFileName.setText(videoname);
        }

        volumeOrLight = (RelativeLayout) v.findViewById(R.id.operation_volume_brightness);
        volume = (VolumeTest) v.findViewById(R.id.volume);
        light = (LightTest) v.findViewById(R.id.lighttest);

        window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        light.init((int) (lp.screenBrightness * light.getRadius()));


        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        volume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setCurrent(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        //注册事件监听
        img_back.setOnClickListener(backListener);
        return v;
    }


    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case HIDEFRAM://隐藏提示窗口
                    volumeOrLight.setVisibility(View.GONE);
                    volume.setVisibility(View.GONE);
                    light.setVisibility(View.GONE);
                    break;
            }
        }
    };


    /*-------------手势控制--------------*/
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * 因为使用的是自定义的mediaController 当显示后，mediaController会铺满屏幕，
         * 所以VideoView的点击事件会被拦截，所以重写控制器的手势事件，
         * 将全部的操作全部写在控制器中，
         * 因为点击事件被控制器拦截，无法传递到下层的VideoView，
         * 所以 原来的单机隐藏会失效，作为代替，
         * 在手势监听中onSingleTapConfirmed（）添加自定义的隐藏/显示，
         *
         * @param e
         * @return
         */

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //当手势结束，并且是单击结束时，控制器隐藏/显示
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        //滑动事件监听
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawX();
            Display disp = activity.getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();
            if (mOldX > windowWidth * 3.0 / 4.0) {// 右边滑动 屏幕 3/4
                onVolumeSlide((mOldY - y) / windowHeight);
            } else if (mOldX < windowWidth * 1.0 / 4.0) {// 左边滑动 屏幕 1/4
                onBrightnessSlide((mOldY - y) / windowHeight);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            playOrPause();
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }


    /*detector 辅助方法*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //手势操作交给mGestureDetector处理
        if (detector.onTouchEvent(event))
            return true;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
        // 隐藏
        myHandler.removeMessages(HIDEFRAM);
        myHandler.sendEmptyMessageDelayed(HIDEFRAM, 500);
    }


    private void onVolumeSlide(float percent) {
        volumeOrLight.setVisibility(VISIBLE);
        light.setVisibility(GONE);
        volume.setVisibility(VISIBLE);

        Log.i("info", ":" + percent);
        float temp = volume.getCurrent() / volume.getMax() + percent * 0.05f;

        if (temp > 1)
            temp = 1;
        else if (temp < 0)
            temp = 0;

        volume.setCurrent(volume.getMax() * temp);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (volume.getMax() * temp), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

    }

    private void onBrightnessSlide(float percent) {
        volumeOrLight.setVisibility(VISIBLE);
        volume.setVisibility(GONE);
        light.setVisibility(VISIBLE);
        WindowManager.LayoutParams lp = window.getAttributes();

        Log.i("info", ":" + lp.screenBrightness);


        float temp = light.getCurrent() * 1.0f / light.getRadius() + percent*0.05f;
        if (temp > 1)
            temp = 1;
        else if (temp < 0)
            temp = 0;
        light.setCurrent((int) (temp * light.getRadius()));


        lp.screenBrightness = temp;
        window.setAttributes(lp);
    }

    public void setVideoName(String name) {
        videoname = name;
        if (mFileName != null) {
            mFileName.setText(name);
        }
    }

    /* 隐藏或显示*/
    private void toggleMediaControlsVisiblity() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /*播放/暂停*/
    private void playOrPause() {
        if (videoView != null)
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
    }

    /*-------------接口--------------*/
    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (activity != null) {
                activity.finish();
            }
        }
    };
}
