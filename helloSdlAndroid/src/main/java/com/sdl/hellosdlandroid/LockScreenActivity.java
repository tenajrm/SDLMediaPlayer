package com.sdl.hellosdlandroid;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.smartdevicelink.proxy.rpc.enums.LockScreenStatus;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LockScreenActivity extends Activity {

    String TAG = LockScreenActivity.class.getSimpleName();

    // This will be set to true if there is any activity running
    // onResume will set this variable to true
    // onPause will set this variable to false
    // As a fallback to old API levels this will be set to true forever
    private static boolean     ACTIVITY_RUNNING;
    // This will hold the activity instance of the lock screen if created
    // onCreate will set this variable to the current lock screen instance
    // onDestroy will set this variable to null
    private static Activity         LOCKSCREEN_INSTANCE;
    // This will hold the current lock screen status
    private static LockScreenStatus LOCKSCREEN_STATUS;
    // This will ensure that the lifecycle is registered only once
    private static boolean          ACTIVITY_LIFECYCLE_REGISTERED;
    // This will hold the lifecycle callback object
    private static ActivityLifecycleCallbacks ACTIVITY_LIFECYCLE_CALLBACK;
    // This will hold the instance of the application object
    private static Application APPLICATION;

    static {
        ACTIVITY_RUNNING = false;
        LOCKSCREEN_INSTANCE = null;
        LOCKSCREEN_STATUS = LockScreenStatus.OFF;

        ACTIVITY_LIFECYCLE_REGISTERED = false;
    }

    public static void registerActivityLifecycle(Application application) {
        // register only once
        if (ACTIVITY_LIFECYCLE_REGISTERED == false) {
            ACTIVITY_LIFECYCLE_REGISTERED = true;

            // check if API level is >= 14
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // create the callback
                ACTIVITY_LIFECYCLE_CALLBACK = new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityResumed(Activity activity) {
                        ACTIVITY_RUNNING = true;
                        // recall this method so the lock screen comes up when necessary
                        updateLockScreenStatus(LOCKSCREEN_STATUS);
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        ACTIVITY_RUNNING = false;
                    }

                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                };

                APPLICATION = application;

                // do the activity registration
                application.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE_CALLBACK);
            } else {
                // fallback and assume we always have an activity
                ACTIVITY_RUNNING = true;
            }
        }
    }

    public static void updateLockScreenStatus(LockScreenStatus status) {
        LOCKSCREEN_STATUS = status;

        if (status.equals(LockScreenStatus.OFF)) {
            // do we have a lock screen? if so we need to remove it
            if (LOCKSCREEN_INSTANCE != null) {
                LOCKSCREEN_INSTANCE.finish();
            }
        } else {
            // do we miss a lock screen and app is in foreground somehow? if so we need to lock it
            if (LOCKSCREEN_INSTANCE == null && ACTIVITY_RUNNING == true) {
                Intent intent = new Intent(APPLICATION, LockScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

                APPLICATION.startActivity(intent);
            }
        }
    }

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        LOCKSCREEN_INSTANCE = this;

        // redo the checkup
        updateLockScreenStatus(LOCKSCREEN_STATUS);

        //final String url = "https://dl.dropboxusercontent.com/u/10281242/sample_audio.mp3";
        //final String url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
        final String url = "http://www.samisite.com/sound/flowersclip.mp3";

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        // Listen for if the audio file can't be prepared
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // ... react appropriately ...
                // The MediaPlayer has moved to the Error state, must be reset!
                Log.d(TAG, "Streaming setOnErrorListener " + what + extra);

                return false;
            }
        });
        // Attach to when audio file is prepared for playing
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "Streaming onPrepared");
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Completation then play again
                Log.d(TAG, "Streaming complete stop reset release");

                //mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();

                int delay = 5000;// in ms

                Timer timer = new Timer();

                timer.schedule( new TimerTask(){
                    public void run() {
                        Log.d(TAG, "Wait, what..:");

                        repeat();
                    }
                }, delay);

            }
        });

        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                //Completation then play again
                Log.d(TAG, "Streaming setOnSeekCompleteListener");
                //mediaPlayer.stop();
                //mediaPlayer.reset();
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new  MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                //do something
                Log.d(TAG, "OnBufferingUpdateListener duration "+ mp.getDuration()+ "current position "+ mp.getCurrentPosition());
            }
        });



        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START ");
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END ");
                        break;
                }
                return false;
            }
        });

        // Set the data source to the remote URL
        // Trigger an async preparation which will file listener when completed
        try {
            int delay = 5000;// in ms

            Timer timer = new Timer();

            timer.schedule( new TimerTask(){
                public void run() {
                    Log.d(TAG, "Wait, what..:");
                    try {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }, delay);


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }




    public void repeat() {

        //final String url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
        //final String url = "https://dl.dropboxusercontent.com/u/10281242/sample_audio.mp3";
        //final String url = "http://www.samisite.com/sound/shades.m3u";
        final String url = "http://www.samisite.com/sound/flowersclip.mp3";

        Log.d(TAG, "REPEAT");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // must call prepare first
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start(); // then start

    }

    @Override
    protected void onDestroy() {
        LOCKSCREEN_INSTANCE = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}
