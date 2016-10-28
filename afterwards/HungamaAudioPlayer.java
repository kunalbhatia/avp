package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HungamaAudioPlayer extends AppCompatActivity implements View.OnClickListener, ExoPlayer.Listener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "HungamaAudioPlayer";
    private ExoPlayer player;
    private PlayerControl playerControl;
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageView artwork;
    private Boolean isRepeat;
    private Boolean isShuffle;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private RelativeLayout loadingPanel;
    private Bitmap bitmap;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private AudioManager am;
    private String song_url,song_title,song_image,song_year,song_album_name,song_singer_name,song_album_id,song_song_id,song_lyrics=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hplib_activity_main);

        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        artwork = (ImageView) findViewById(R.id.artwork);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);

        btnPlay.setOnClickListener(this);
        songProgressBar.setOnSeekBarChangeListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnPlaylist.setOnClickListener(this);
        isRepeat = false;
        isShuffle = false;
        Intent intent = getIntent();

        song_url = intent.getStringExtra("song_url");
        if(song_url==null){
            song_url="http://player.hungama.com/mp3/Gerua.mp3";
        }
        song_album_id = intent.getStringExtra("song_album_id");
        song_album_name = intent.getStringExtra("song_album_name");
        song_image = intent.getStringExtra("song_image");
        if(song_image==null){
            song_image="http://player.hungama.com/mp3/ai.jpg";
        }
        song_lyrics = intent.getStringExtra("song_lyrics");
        song_song_id = intent.getStringExtra("song_song_id");
        song_singer_name = intent.getStringExtra("song_singer_name");
        song_title = intent.getStringExtra("song_title");
        if(song_title==null){
            song_title="Gerua";
        }
        song_year = intent.getStringExtra("song_year");
        am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        loadingPanel.setVisibility(View.VISIBLE);
        preparePlayer();
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!playerControl.isPlaying()) {
                    playerControl.start();
                }
                player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                if (!playerControl.isPlaying()) {
                    playerControl.start();
                }
                player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (!playerControl.isPlaying()) {
                    playerControl.start();
                }
                player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0.2f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (playerControl.canPause()) {
                    playerControl.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (playerControl.canPause()) {
                    playerControl.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0.5f);
                break;
        }

    }

    private void preparePlayer() {
        Uri uri = Uri.parse(song_url);
        player = ExoPlayer.Factory.newInstance(1);
        playerControl = new PlayerControl(player);
        DataSource dataSource = new DefaultUriDataSource(this, TAG);
        ExtractorSampleSource extractorSampleSource = new ExtractorSampleSource(uri, dataSource, new DefaultAllocator(64 * 1024), 64 * 1024 * 256);
        audioRenderer = new MediaCodecAudioTrackRenderer(extractorSampleSource, MediaCodecSelector.DEFAULT);
        player.prepare(audioRenderer);
        songTitleLabel.setText(song_title);
        new LoadImage().execute(song_image);
        songTitleLabel.postDelayed(updateData, 200);
        if(requestFocus()){
            player.setPlayWhenReady(true);
            loadingPanel.setVisibility(View.GONE);
        }
    }
    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }
    public void setMute(boolean toMute){
        if(toMute){
            player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
        } else {
            player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingPanel.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... args) {

            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                artwork.setImageBitmap(image);
                artwork.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                Toast.makeText(HungamaAudioPlayer.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
            loadingPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.btnPlay) {
            if (!playerControl.isPlaying()) {
                playerControl.start();
            } else {
                if (playerControl.canPause()) {
                    playerControl.pause();
                }
            }

        } else if (i1 == R.id.btnForward) {
            player.seekTo(player.getCurrentPosition() + 30000);

        } else if (i1 == R.id.btnBackward) {
            player.seekTo(player.getCurrentPosition() - 30000);

        } else if (i1 == R.id.btnRepeat) {
            if (isRepeat) {
                isRepeat = false;
                btnRepeat.setImageResource(R.drawable.hplib_btn_repeat);
            } else {
                isRepeat = true;
                btnRepeat.setImageResource(R.drawable.hplib_btn_repeat_focused);
            }

        } else if (i1 == R.id.btnShuffle) {
            if (isShuffle) {
                isShuffle = false;
                btnShuffle.setImageResource(R.drawable.hplib_btn_shuffle);
            } else {
                isShuffle = true;
                btnShuffle.setImageResource(R.drawable.hplib_btn_shuffle_focused);
            }

        } else if (i1 == R.id.btnNext) {


        } else if (i1 == R.id.btnPrevious) {


        } else if (i1 == R.id.btnPlaylist) {

        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {


    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d(TAG, "error: " + error.getMessage());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekTo(seekBar.getProgress());
    }

    private ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();
    private Runnable updateData;

    {
        updateData = new Runnable() {
            @Override
            public void run() {
                String text = "";
                switch (player.getPlaybackState()) {
                    case ExoPlayer.STATE_BUFFERING:
                        text += "buffering";
                        break;
                    case ExoPlayer.STATE_ENDED:
                        text += "ended";
                        if (isRepeat) {
                            player.seekTo(0);
                        }else{

                        }
                        break;
                    case ExoPlayer.STATE_IDLE:
                        text += "idle";
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        text += "preparing";
                        break;
                    case ExoPlayer.STATE_READY:
                        text += "ready";
                        break;
                    default:
                        text += "unknown";
                        break;
                }
                if (playerControl.isPlaying()) {
                    btnPlay.setImageResource(R.drawable.hplib_btn_pause);
                } else {
                    btnPlay.setImageResource(R.drawable.hplib_btn_play);
                }
                String totDur = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getDuration()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getDuration()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getDuration())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
                String curDur = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition())));
                songTotalDurationLabel.setText("" + totDur);
                songCurrentDurationLabel.setText("" + curDur);
                songProgressBar.setMax((int) player.getDuration());
                songProgressBar.setProgress((int) player.getCurrentPosition());
                songTitleLabel.postDelayed(updateData, 200);
            }
        };
    }

    private Future longRunningTaskFuture = threadPoolExecutor.submit(updateData);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player!=null){
            player.release();
        }
    }
}