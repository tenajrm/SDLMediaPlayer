package com.sdl.hellosdlandroid;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class AudioActivity extends Activity {

    String TAG = AudioActivity.class.getSimpleName();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    public void onPlayLocalAudio(View v) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.mpthreetest);
        mediaPlayer.start();
    }


    MediaPlayer mediaPlayer;

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

    public void onStreamAudio(View v) {

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

    MediaRecorder mediaRecorder;
    String mFileName;

    public void onRecordAudio(View v) {
        // Verify that the device has a mic
        PackageManager pmanager = this.getPackageManager();
        if (!pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            Toast.makeText(this, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
            return;
        }

        Button btnRecord = (Button) v;
        // Start the recording
        if (v.getTag() == "start" || v.getTag() == null) {
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/audiorecordtest.3gp";
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(mFileName);
            v.setTag("stop");
            btnRecord.setText("Stop");


            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (v.getTag() == "stop") {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            v.setTag("start");
            btnRecord.setText("Record Audio");
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(mFileName);
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
    }

}
