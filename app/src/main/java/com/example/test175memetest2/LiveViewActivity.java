package com.example.test175memetest2;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;

import java.io.IOException;
import java.util.List;

import jp.ogwork.camerafragment.camera.CameraFragment;
import jp.ogwork.camerafragment.camera.CameraSurfaceView;

public class LiveViewActivity extends AppCompatActivity {

    // TODO : Replace APP_ID and APP_SECRET
    private static final String APP_ID = "";
    private static final String APP_SECRET = "";

    private FrameLayout cameraLayout;

    private LinearLayout countDownLayout;
    private TextView countDownText;

    private SoundPool soundPool;
    private String[] audioFileNames = {"music2.mp3", "music3.mp3", "music4.mp3", "music5.mp3", "music.mp3"};
    private int[] drumMusicFileRefs = {R.raw.drum_cymbal, R.raw.drum_snare, R.raw.drum_hihat, R.raw.drum_snare};
    private int[] waza1FileNames = {R.raw.waza_aura, R.raw.waza_punch_high, R.raw.waza_highspeed, R.raw.waza_whip_attack};
    private int[] waza2FileNames = {R.raw.waza_sword_clash, R.raw.waza_knife_stab, R.raw.waza_sword_gesture, R.raw.waza_katana_slash};
    private int[] specialFileNames = {R.raw.sp_shine, R.raw.sp_pocopoco, R.raw.sp_tin, R.raw.sp_touch};
    private int[] pianoFileNames = {R.raw.piano1_1do, R.raw.piano1_2re, R.raw.piano1_3mi, R.raw.piano1_4fa};
    private int[] guitarFileNames = {R.raw.guitar01, R.raw.guitar04, R.raw.guitar06, R.raw.guitar09};
    private int[] bassFileNames = {R.raw.bass01, R.raw.bass03, R.raw.bass05, R.raw.bass10};
    private int[] sounds = new int[5];
    private int[] drums = new int[4];
    private int[] waza1s = new int[4];
    private int[] waza2s = new int[4];
    private int[] specials = new int[4];
    private int[] pianos = new int[4];
    private int[] guitars = new int[4];
    private int[] basses = new int[4];

    private int bgm = 0;
    private int kubifuriSound = 0;
    private int shisenSound = 0;
    private boolean isPlayAllowed = false;
    private boolean audioPlayingFlag = false;
    private boolean playingChecker = true;

    private MediaPlayer mediaPlayer;

    private int[] backgroundMusicRefs = {R.raw.first_highhat, R.raw.sakanakushon5};
    private int[] backgroundMusics = new int[backgroundMusicRefs.length];
    private ImageView[] effectViews = new ImageView[16];

    private MemeLib memeLib;

    private CameraFragment cameraFragment;
    private MyPreferencesActivity myPreferencesActivity = new MyPreferencesActivity();

    final private MemeConnectListener memeConnectListener = new MemeConnectListener() {
        @Override
        public void memeConnectCallback(boolean b) {
            //describe actions after connection with JINS MEME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeViewStatus(true);
                }
            });
        }

        @Override
        public void memeDisconnectCallback() {
            //describe actions after disconnection from JINS MEME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeViewStatus(false);
                    Toast.makeText(LiveViewActivity.this, "DISCONNECTED", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_view);
        init(savedInstanceState);
        soundPool();
        getValues();
        startCountDown();
    }

    @Override
    protected void onPause(){
        super.onPause();
        soundPool.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Sets MemeConnectListener to get connection result.
        memeLib.setMemeConnectListener(memeConnectListener);

        changeViewStatus(memeLib.isConnected());

        //Starts receiving realtime data if MEME is connected
        if (memeLib.isConnected()) {
            memeLib.startDataReport(memeRealtimeListener);
        }
    }

    private void init(Bundle savedInstanceState) {
        //Authentication and authorization of App and SDK
        MemeLib.setAppClientID(getApplicationContext(), APP_ID, APP_SECRET);
        memeLib = MemeLib.getInstance();


        changeViewStatus(memeLib.isConnected());
        cameraLayout = findViewById(R.id.framelayout_camera);

        //カメラサイズを決定→フラグメントを設定
        if (savedInstanceState == null) {
            cameraLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    cameraLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = cameraLayout.getWidth();
                    int height = cameraLayout.getHeight();

                    addCameraFragment(width, height, R.id.framelayout_camera);
                }
            });
        }
        countDownLayout = findViewById(R.id.layout_countdown_container);
        countDownText = findViewById(R.id.text_countdown);

        effectViews[0] = findViewById(R.id.effect1_image_0);
        effectViews[1] = findViewById(R.id.effect1_image_1);
        effectViews[2] = findViewById(R.id.effect1_image_2);
        effectViews[3] = findViewById(R.id.effect1_image_3);
        effectViews[4] = findViewById(R.id.effect2_image_0);
        effectViews[5] = findViewById(R.id.effect2_image_1);
        effectViews[6] = findViewById(R.id.effect2_image_2);
        effectViews[7] = findViewById(R.id.effect2_image_3);
        effectViews[8] = findViewById(R.id.effect3_image_0);
        effectViews[9] = findViewById(R.id.effect3_image_1);
        effectViews[10] = findViewById(R.id.effect3_image_2);
        effectViews[11] = findViewById(R.id.effect3_image_3);
        effectViews[12] = findViewById(R.id.effect4_image_0);
        effectViews[13] = findViewById(R.id.effect4_image_1);
        effectViews[14] = findViewById(R.id.effect4_image_2);
        effectViews[15] = findViewById(R.id.effect4_image_3);
        effectViews[0].setVisibility(View.INVISIBLE);
        effectViews[1].setVisibility(View.INVISIBLE);
        effectViews[2].setVisibility(View.INVISIBLE);
        effectViews[3].setVisibility(View.INVISIBLE);
        effectViews[4] = findViewById(View.INVISIBLE);
        effectViews[5] = findViewById(View.INVISIBLE);
        effectViews[6] = findViewById(View.INVISIBLE);
        effectViews[7] = findViewById(View.INVISIBLE);
        effectViews[8] = findViewById(View.INVISIBLE);
        effectViews[9] = findViewById(View.INVISIBLE);
        effectViews[10] = findViewById(View.INVISIBLE);
        effectViews[11] = findViewById(View.INVISIBLE);
        effectViews[12] = findViewById(View.INVISIBLE);
        effectViews[13] = findViewById(View.INVISIBLE);
        effectViews[14] = findViewById(View.INVISIBLE);
        effectViews[15] = findViewById(View.INVISIBLE);
    }

    private void changeViewStatus(boolean connected) {
        if (connected) {

        } else {

        }
    }


    private final MemeRealtimeListener memeRealtimeListener = new MemeRealtimeListener() {
        @Override
        public void memeRealtimeCallback(final MemeRealtimeData memeRealtimeData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMemeData(memeRealtimeData);
                }
            });
        }
    };

    private void updateMemeData(MemeRealtimeData d) {

        // for blink
        //Log.d("LiveViewActivity", "Blink Speed:" + d.getBlinkSpeed());
        if (d.getBlinkSpeed() > 0) {
            setEffect(10);

        }
        int dataUp = d.getEyeMoveUp();
        int dataDown = d.getEyeMoveDown();
        int dataLeft = d.getEyeMoveLeft();
        int dataRight = d.getEyeMoveRight();
        float dataRoll = d.getRoll();
        float dataPitch = d.getPitch();
        float dataYaw = d.getYaw();
        float dataAccZ = d.getAccZ();
        if (d.getBlinkStrength() > 200){
            setEffect(11);
        }
        if (d.getBlinkStrength() < 50){
            setEffect(12);
        }
        if (dataUp > 1){
            Log.d("error1", "up");
            //soundPool.play(sounds[0], 1.0f, 1.0f, 0, 0, 1);
            playMusic(0, 1);
            setEffect(3);
        }
        if (dataDown > 1){
            Log.d("error1", "down");
            //soundPool.play(sounds[1], 1.0f, 1.0f, 0, 0, 1);
            playMusic(1, 1);
            setEffect(1);
        }
        if (dataLeft > 1){
            Log.d("error1", "left");
            //soundPool.play(sounds[2], 1.0f, 1.0f, 0, 0, 1);
            playMusic(2, 1);
            setEffect(2);
        }
        if (dataRight > 1){
            Log.d("error1", "right");
            //soundPool.play(sounds[3], 1.0f, 1.0f, 0, 0, 1);
            playMusic(3, 1);
            setEffect(0);
        }
        if (dataAccZ > -6.0f){
            setEffect(8);
        } else if (dataAccZ < -26.0f){
            setEffect(9);
        } else if (dataPitch > 50.0f){
            setEffect(13);
            playMusic(0, 2);
        } else if (dataRoll > 30.0f){
            setEffect(4);
            playMusic(1, 2);
        } else if (dataPitch < -50.0f){
            setEffect(14);
            playMusic(2, 2);
        } else if (dataRoll < -30.0f){
            setEffect(6);
            playMusic(3, 2);
        } else if (dataPitch > 30.0f){
            setEffect(7);
        } else if (dataPitch < -30.0f){
            setEffect(5);
        }

        //Log.d("error1", "pitch: " + dataPitch);
        //Log.d("error1", "yaw: " + dataYaw);
        Log.d("error1", "accZ: " + dataAccZ);


        // for body (Y axis rotation)
        double radian = Math.atan2(d.getAccX(), d.getAccZ());
        rotate(Math.toDegrees(-radian)); // for mirroring display(radian x -1)
    }

    private void rotate(double degree) {

    }

    private void soundPool(){
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(10)
                .build();
        sounds[0] = soundPool.load(this, R.raw.music2, 1);
        sounds[1] = soundPool.load(this, R.raw.music3, 1);
        sounds[2] = soundPool.load(this, R.raw.music4, 1);
        sounds[3] = soundPool.load(this, R.raw.music5, 1);
        backgroundMusics[0] = soundPool.load(this, backgroundMusicRefs[0], 1);
        backgroundMusics[1] = soundPool.load(this, backgroundMusicRefs[1], 1);
        for (int i = 0; i < 4; i++){
            drums[i] = soundPool.load(this, drumMusicFileRefs[i], 1);
            waza1s[i] = soundPool.load(this, waza1FileNames[i], 1);
            waza2s[i] = soundPool.load(this, waza2FileNames[i], 1);
            specials[i] = soundPool.load(this, specialFileNames[i], 1);
            pianos[i] = soundPool.load(this, pianoFileNames[i], 1);
            guitars[i] = soundPool.load(this, guitarFileNames[i], 1);
            basses[i] = soundPool.load(this, bassFileNames[i], 1);
        }
    }

    public void addCameraFragment(final int viewWidth, final int viewHeight, int containerViewId){
        cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt(CameraFragment.BUNDLE_KEY_CAMERA_FACING,
                Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraFragment.setArguments(args);

        cameraFragment.setOnPreviewSizeChangeListener(new CameraSurfaceView.OnPreviewSizeChangeListener() {
            @Override
            public Camera.Size onPreviewSizeChange(List<Camera.Size> var1) {
                return cameraFragment.choosePreviewSize(var1, 0, 0, viewWidth, viewHeight);
            }

            @Override
            public void onPreviewSizeChanged(Camera.Size previewSize) {
                float viewAspectRatio = (float) viewHeight / previewSize.width;
                int height = viewHeight;
                int width = (int)(viewAspectRatio * previewSize.height);

                if (width < viewWidth){
                    width = viewWidth;
                    height = (int)(viewAspectRatio * previewSize.width);
                }

                cameraFragment.setLayoutBounds(width, height);
                return;
            }
        });

        getSupportFragmentManager().beginTransaction().add(containerViewId, cameraFragment, "camera")
                .commit();
    }

    //設定した値を取得
    private void getValues(){
        bgm = myPreferencesActivity.getCurrentBGM(LiveViewActivity.this);
        kubifuriSound = myPreferencesActivity.getKubifuriInstrument(LiveViewActivity.this);
        shisenSound = myPreferencesActivity.getShisenInstrument(LiveViewActivity.this);
    }

    private void startBGM(){
        soundPool.play(backgroundMusics[bgm],1.0f, 1.0f, 1, -1, 1);
    }

    private void startCountDown(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownText.setText("2");
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownText.setText("1");
            }
        }, 2000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownText.setText("0");
                AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                animation.setFillAfter(true);
                countDownLayout.startAnimation(animation);
                isPlayAllowed = true;
                startBGM();
            }
        }, 3000);
    }

    //視点移動: 1
    private void playMusic(int direction, int type){
        if (playingChecker){
            int target = 0;
            int priority = 0;
            switch (type){
                case 1:
                    priority = 0;
                    switch (shisenSound){
                        case 0:
                            target = pianos[direction];
                            break;
                        case 1:
                            target = guitars[direction];
                            break;
                        case 2:
                            target = basses[direction];
                            break;
                    }
                    break;
                case 2:
                    priority = 0;
                    switch (kubifuriSound){
                        case 0:
                            target = drums[direction];
                            break;
                        case 1:
                            target = waza1s[direction];
                            break;
                        case 2:
                            target = waza2s[direction];
                            break;
                        case 3:
                            target = specials[direction];
                            break;
                    }
                    break;
                case 3:
                    break;
            }

            soundPool.play(target, 1.0f, 1.0f, priority, 0, 1);
            playingChecker = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playingChecker = true;
                }
            }, 500);
        }
    }

    private void setEffect(int direction){
        effectViews[direction].setBackgroundResource(R.drawable.effect1);
        effectViews[direction].setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(900);
        animation.setFillAfter(false);
        effectViews[direction].startAnimation(animation);
        effectViews[direction].setVisibility(View.GONE);
    }

    private void makeToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}