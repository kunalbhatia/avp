package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class VideoPlayer extends AppCompatActivity implements HlsSampleSource.EventListener, View.OnClickListener {
    private static final String TAG = "VideoPlayer";

    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_AUDIO = 1;
    private ExoPlayer player;
    private SurfaceView surface;
    private String[] video_url, video_type, video_title;
    private int currentTrackIndex;
    private Handler mainHandler;
    private HpLib_RendererBuilder hpLibRendererBuilder;
    private TrackRenderer videoRenderer;
    private LinearLayout root,top_controls, middle_panel, unlock_panel, bottom_controls,seekBar_center_text,onlySeekbar;
    private double seekSpeed = 0;
    public static final int TYPE_VIDEO = 0;
    private View decorView;
    private int uiImmersiveOptions;
    private RelativeLayout loadingPanel;
    private Runnable updatePlayer,hideControls;

    //Implementing the top bar
    private ImageButton btn_back;
    private TextView txt_title;

    //Implementing Chromecast
    public MediaRouteButton mMediaRouteButton;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private PlaybackState mPlaybackState;
    private SessionManager mSessionManager;
    private MediaItem mSelectedMedia;

    //Implementing current time, total time and seekbar
    private TextView txt_ct,txt_td;
    private SeekBar seekBar;
    private PlayerControl playerControl;


    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
    public enum ControlsMode {
        LOCK, FULLCONTORLS
    }
    private ControlsMode controlsState;

    private ImageButton btn_play;
    private ImageButton btn_pause;
    private ImageButton btn_fwd;
    private ImageButton btn_rev;
    private ImageButton btn_next;
    private ImageButton btn_prev;

    private ImageButton btn_lock;
    private ImageButton btn_unlock;
    private ImageButton btn_settings;


    private Display display;
    private Point size;

    private int sWidth,sHeight;
    private float baseX, baseY;
    private long diffX, diffY;
    private int calculatedTime;
    private String seekDur;
    private Boolean tested_ok = false;
    private Boolean screen_swipe_move = false;
    private boolean immersiveMode, intLeft, intRight, intTop, intBottom, finLeft, finRight, finTop, finBottom;
    private static final int MIN_DISTANCE = 150;
    private ContentResolver cResolver;
    private Window window;
    private LinearLayout volumeBarContainer, brightnessBarContainer,brightness_center_text, vol_center_text;
    private ProgressBar volumeBar, brightnessBar;
    private TextView vol_perc_center_text, brigtness_perc_center_text,txt_seek_secs,txt_seek_currTime;
    private ImageView volIcon, brightnessIcon, vol_image, brightness_image;
    private int brightness, mediavolume,device_height,device_width;
    private AudioManager audioManager;


    private final SessionManagerListener<CastSession> mSessionManagerListener = new SessionManagerListenerImpl();
    private class SessionManagerListenerImpl implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting(CastSession session) {

        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            onApplicationConnected(session);
        }

        @Override
        public void onSessionStartFailed(CastSession session, int i) {

        }

        @Override
        public void onSessionEnding(CastSession session) {

        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            onApplicationConnected(session);
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int i) {

        }

        @Override
        public void onSessionSuspended(CastSession session, int i) {

        }

        @Override
        public void onSessionEnded(CastSession session, int error) {
            finish();
        }

        @Override
        public void onSessionResuming(CastSession session, String s) {

        }
    }
    private void onApplicationConnected(CastSession castSession) {
        mCastSession = castSession;
        loadRemoteMedia(0,true);
    }
    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);


        mSelectedMedia = new MediaItem();
        mSelectedMedia.setUrl(video_url[currentTrackIndex]);
        mSelectedMedia.setContentType(video_type[currentTrackIndex]);
        mSelectedMedia.setTitle(video_title[currentTrackIndex]);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, mSelectedMedia.getTitle());

        return new MediaInfo.Builder(mSelectedMedia.getUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("hls")
                .setMetadata(movieMetadata)
                .setStreamDuration(mSelectedMedia.getDuration() * 1000)
                .build();
    }
    private void loadRemoteMedia(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(VideoPlayer.this, ExpandedControlsActivity.class);
                startActivityForResult(intent,200);
                remoteMediaClient.removeListener(this);
                if(playerControl.isPlaying()){
                    playerControl.pause();
                }
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }
        });
        remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==200){
            int currTime = data.getIntExtra("currTime",0);
            player.seekTo(currTime);
        }
    }
    {
        updatePlayer = new Runnable() {
            @Override
            public void run() {
                switch (player.getPlaybackState()) {
                    case ExoPlayer.STATE_BUFFERING:
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_ENDED:
                        finish();
                        break;
                    case ExoPlayer.STATE_IDLE:
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_READY:
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }

                String totDur = String.format("%02d.%02d.%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getDuration()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getDuration()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getDuration())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
                String curDur = String.format("%02d.%02d.%02d",
                        TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition())));
                txt_ct.setText(curDur);
                txt_td.setText(totDur);
                seekBar.setMax((int) player.getDuration());
                seekBar.setProgress((int) player.getCurrentPosition());

                mainHandler.postDelayed(updatePlayer, 200);
            }
        };
    }
    {
        hideControls = new Runnable() {
            @Override
            public void run() {
                hideAllControls();
            }
        };
    }
    private void hideAllControls(){
        if(controlsState==ControlsMode.FULLCONTORLS){
            if(root.getVisibility()==View.VISIBLE){
                root.setVisibility(View.GONE);
            }
        }else if(controlsState==ControlsMode.LOCK){
            if(unlock_panel.getVisibility()==View.VISIBLE){
                unlock_panel.setVisibility(View.GONE);
            }
        }
        decorView.setSystemUiVisibility(uiImmersiveOptions);
    }
    private void showControls(){
        if(controlsState==ControlsMode.FULLCONTORLS){
            if(root.getVisibility()==View.GONE){
                root.setVisibility(View.VISIBLE);
            }
        }else if(controlsState==ControlsMode.LOCK){
            if(unlock_panel.getVisibility()==View.GONE){
                unlock_panel.setVisibility(View.VISIBLE);
            }
        }
        mainHandler.removeCallbacks(hideControls);
        mainHandler.postDelayed(hideControls, 3000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                tested_ok=false;
                if (event.getX() < (sWidth / 2)) {
                    intLeft = true;
                    intRight = false;
                } else if (event.getX() > (sWidth / 2)) {
                    intLeft = false;
                    intRight = true;
                }
                int upperLimit = (sHeight / 4) + 100;
                int lowerLimit = ((sHeight / 4) * 3) - 150;
                if (event.getY() < upperLimit) {
                    intBottom = false;
                    intTop = true;
                } else if (event.getY() > lowerLimit) {
                    intBottom = true;
                    intTop = false;
                } else {
                    intBottom = false;
                    intTop = false;
                }
                seekSpeed = (TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) * 0.1);
                diffX = 0;
                calculatedTime = 0;
                seekDur = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(diffX) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffX)),
                        TimeUnit.MILLISECONDS.toSeconds(diffX) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffX)));

                //TOUCH STARTED
                baseX = event.getX();
                baseY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                screen_swipe_move=true;
                if(controlsState==ControlsMode.FULLCONTORLS){
                    root.setVisibility(View.GONE);
                    diffX = (long) (Math.ceil(event.getX() - baseX));
                    diffY = (long) Math.ceil(event.getY() - baseY);
                    double brightnessSpeed = 0.05;
                    if (Math.abs(diffY) > MIN_DISTANCE) {
                        tested_ok = true;
                    }
                    if (Math.abs(diffY) > Math.abs(diffX)) {
                        if (intLeft) {
                            cResolver = getContentResolver();
                            window = getWindow();
                            try {
                                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
                            } catch (Settings.SettingNotFoundException e) {
                                e.printStackTrace();
                            }
                            int new_brightness = (int) (brightness - (diffY * brightnessSpeed));
                            if (new_brightness > 250) {
                                new_brightness = 250;
                            } else if (new_brightness < 1) {
                                new_brightness = 1;
                            }
                            double brightPerc = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                            brightnessBarContainer.setVisibility(View.VISIBLE);
                            brightness_center_text.setVisibility(View.VISIBLE);
                            brightnessBar.setProgress((int) brightPerc);
                            if (brightPerc < 30) {
                                brightnessIcon.setImageResource(R.drawable.hplib_brightness_minimum);
                                brightness_image.setImageResource(R.drawable.hplib_brightness_minimum);
                            } else if (brightPerc > 30 && brightPerc < 80) {
                                brightnessIcon.setImageResource(R.drawable.hplib_brightness_medium);
                                brightness_image.setImageResource(R.drawable.hplib_brightness_medium);
                            } else if (brightPerc > 80) {
                                brightnessIcon.setImageResource(R.drawable.hplib_brightness_maximum);
                                brightness_image.setImageResource(R.drawable.hplib_brightness_maximum);
                            }
                            brigtness_perc_center_text.setText(" " + (int) brightPerc);
                            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, (new_brightness));
                            WindowManager.LayoutParams layoutpars = window.getAttributes();
                            layoutpars.screenBrightness = brightness / (float) 255;
                            window.setAttributes(layoutpars);
                        }else if (intRight) {
                            vol_center_text.setVisibility(View.VISIBLE);
                            mediavolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            double cal = (double) diffY * ((double)maxVol/(double)(device_height*4));
                            int newMediaVolume = mediavolume - (int) cal;
                            if (newMediaVolume > maxVol) {
                                newMediaVolume = maxVol;
                            } else if (newMediaVolume < 1) {
                                newMediaVolume = 0;
                            }
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            double volPerc = Math.ceil((((double) newMediaVolume / (double) maxVol) * (double) 100));
                            vol_perc_center_text.setText(" " + (int) volPerc);
                            if (volPerc < 1) {
                                volIcon.setImageResource(R.drawable.hplib_volume_mute);
                                vol_image.setImageResource(R.drawable.hplib_volume_mute);
                                vol_perc_center_text.setVisibility(View.GONE);
                            } else if (volPerc >= 1) {
                                volIcon.setImageResource(R.drawable.hplib_volume);
                                vol_image.setImageResource(R.drawable.hplib_volume);
                                vol_perc_center_text.setVisibility(View.VISIBLE);
                            }
                            volumeBarContainer.setVisibility(View.VISIBLE);
                            volumeBar.setProgress((int) volPerc);
                        }
                    }else if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > (MIN_DISTANCE + 100)) {
                            tested_ok = true;
                            root.setVisibility(View.VISIBLE);
                            seekBar_center_text.setVisibility(View.VISIBLE);
                            onlySeekbar.setVisibility(View.VISIBLE);
                            top_controls.setVisibility(View.GONE);
                            bottom_controls.setVisibility(View.GONE);
                            String totime = "";
                            calculatedTime = (int) ((diffX) * seekSpeed);
                            if (calculatedTime > 0) {
                                seekDur = String.format("[ +%02d:%02d ]",
                                        TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime)),
                                        TimeUnit.MILLISECONDS.toSeconds(calculatedTime) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime)));
                            } else if (calculatedTime < 0) {
                                seekDur = String.format("[ -%02d:%02d ]",
                                        Math.abs(TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime))),
                                        Math.abs(TimeUnit.MILLISECONDS.toSeconds(calculatedTime) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime))));
                            }
                            totime = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition() + (calculatedTime)) -
                                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition() + (calculatedTime))), // The change is in this line
                                    TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition() + (calculatedTime)) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition() + (calculatedTime))));
                            txt_seek_secs.setText(seekDur);
                            txt_seek_currTime.setText(totime);
                            seekBar.setProgress((int) (player.getCurrentPosition() + (calculatedTime)));
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                screen_swipe_move=false;
                tested_ok = false;

                seekBar_center_text.setVisibility(View.GONE);
                brightness_center_text.setVisibility(View.GONE);
                vol_center_text.setVisibility(View.GONE);
                brightnessBarContainer.setVisibility(View.GONE);
                volumeBarContainer.setVisibility(View.GONE);
                onlySeekbar.setVisibility(View.VISIBLE);
                top_controls.setVisibility(View.VISIBLE);
                bottom_controls.setVisibility(View.VISIBLE);
                root.setVisibility(View.VISIBLE);
                calculatedTime = (int) (player.getCurrentPosition() + (calculatedTime));
                player.seekTo(calculatedTime);
                showControls();
                break;

        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hplib_activity_video_player);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;


        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        device_height = displaymetrics.heightPixels;
        device_width = displaymetrics.widthPixels;


        //Chromecast
        LinearLayout cast_container = (LinearLayout) findViewById(R.id.cast_container);
        mMediaRouteButton = new MediaRouteButton(this);
        cast_container.addView(mMediaRouteButton);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);

        uiImmersiveOptions = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiImmersiveOptions);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingVPanel);
        txt_ct = (TextView) findViewById(R.id.txt_currentTime);
        txt_td = (TextView) findViewById(R.id.txt_totalDuration);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });


        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_pause = (ImageButton) findViewById(R.id.btn_pause);
        btn_fwd = (ImageButton) findViewById(R.id.btn_fwd);
        btn_rev = (ImageButton) findViewById(R.id.btn_rev);
        btn_prev = (ImageButton) findViewById(R.id.btn_prev);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_lock = (ImageButton) findViewById(R.id.btn_lock);
        btn_unlock = (ImageButton) findViewById(R.id.btn_unlock);
        btn_settings = (ImageButton) findViewById(R.id.btn_settings);



        txt_seek_secs = (TextView) findViewById(R.id.txt_seek_secs);
        txt_seek_currTime = (TextView) findViewById(R.id.txt_seek_currTime);
        seekBar_center_text = (LinearLayout) findViewById(R.id.seekbar_center_text);
        onlySeekbar = (LinearLayout) findViewById(R.id.seekbar_time);
        top_controls = (LinearLayout) findViewById(R.id.top);
        bottom_controls = (LinearLayout) findViewById(R.id.controls);

        vol_perc_center_text = (TextView) findViewById(R.id.vol_perc_center_text);
        brigtness_perc_center_text = (TextView) findViewById(R.id.brigtness_perc_center_text);
        volumeBar = (ProgressBar) findViewById(R.id.volume_slider);
        brightnessBar = (ProgressBar) findViewById(R.id.brightness_slider);
        volumeBarContainer = (LinearLayout) findViewById(R.id.volume_slider_container);
        brightnessBarContainer = (LinearLayout) findViewById(R.id.brightness_slider_container);
        brightness_center_text = (LinearLayout) findViewById(R.id.brightness_center_text);
        vol_center_text = (LinearLayout) findViewById(R.id.vol_center_text);

        volIcon = (ImageView) findViewById(R.id.volIcon);
        brightnessIcon = (ImageView) findViewById(R.id.brightnessIcon);
        vol_image = (ImageView) findViewById(R.id.vol_image);
        brightness_image = (ImageView) findViewById(R.id.brightness_image);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        btn_back.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_fwd.setOnClickListener(this);
        btn_rev.setOnClickListener(this);
        btn_prev.setOnClickListener(this);
        btn_next.setOnClickListener(this);

        btn_lock.setOnClickListener(this);
        btn_unlock.setOnClickListener(this);
        btn_settings.setOnClickListener(this);

        unlock_panel = (LinearLayout) findViewById(R.id.unlock_panel);



        txt_title = (TextView) findViewById(R.id.txt_title);


        root = (LinearLayout) findViewById(R.id.root);
        root.setVisibility(View.VISIBLE);

        surface = (SurfaceView) findViewById(R.id.surface_view);

        currentTrackIndex=0;

        video_type = new String[]{"hls", "others"};
        video_url = new String[]{"http://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8","http://player.hungama.com/mp3/91508493.mp4"};
        video_title = new String[]{"Big Buck Bunny","Movie Trailer"};

        txt_title.setText(video_title[currentTrackIndex]);
        
        mainHandler = new Handler();



        execute();
    }
    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.btn_back) {
            killPlayer();
            finish();
        }
        if (i1 == R.id.btn_pause) {
            if (playerControl.isPlaying()) {
                playerControl.pause();
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
            }
        }
        if (i1 == R.id.btn_play) {
            if (!playerControl.isPlaying()) {
                playerControl.start();
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
            }
        }
        if (i1 == R.id.btn_fwd) {
            player.seekTo(player.getCurrentPosition() + 30000);
        }
        if (i1 == R.id.btn_rev) {
            player.seekTo(player.getCurrentPosition() - 30000);
        }
        if (i1 == R.id.btn_next) {
            player.release();
            currentTrackIndex++;
            execute();
        }
        if (i1 == R.id.btn_prev) {
            player.release();
            currentTrackIndex--;
            execute();
        }
        if (i1 == R.id.btn_lock) {
            controlsState = ControlsMode.LOCK;
            root.setVisibility(View.GONE);
            unlock_panel.setVisibility(View.VISIBLE);
        }
        if (i1 == R.id.btn_unlock) {
            controlsState = ControlsMode.FULLCONTORLS;
            root.setVisibility(View.VISIBLE);
            unlock_panel.setVisibility(View.GONE);
        }
        if (i1 == R.id.btn_settings) {
            PopupMenu popup = new PopupMenu(VideoPlayer.this, v);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    player.setSelectedTrack(0, (item.getItemId() - 1));
                    return false;
                }
            });
            Menu menu = popup.getMenu();
            menu.add(Menu.NONE, 0, 0, "Video Quality");
            for (int i = 0; i < player.getTrackCount(0); i++) {
                MediaFormat format = player.getTrackFormat(0, i);
                if (MimeTypes.isVideo(format.mimeType)) {
                    if (format.adaptive) {
                        menu.add(1, (i + 1), (i + 1), "Auto");
                    } else {
                        menu.add(1, (i + 1), (i + 1), format.width + "p");
                    }
                }
            }
            menu.setGroupCheckable(1, true, true);
            menu.findItem((player.getSelectedTrack(0) + 1)).setChecked(true);
            popup.show();
        }
    }

    private void execute() {
        player=ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        playerControl = new PlayerControl(player);
        if(currentTrackIndex>=video_title.length){
            currentTrackIndex=(video_title.length-1);
        }else if(currentTrackIndex<=0){
            currentTrackIndex=0;
        }
        txt_title.setText(video_title[currentTrackIndex]);
        if(player!=null) {
            hpLibRendererBuilder = getHpLibRendererBuilder();
            hpLibRendererBuilder.buildRenderers(this);
            loadingPanel.setVisibility(View.VISIBLE);
            mainHandler.postDelayed(updatePlayer, 200);
            mainHandler.postDelayed(hideControls, 3000);
            controlsState = ControlsMode.FULLCONTORLS;
        }
    }

    private HpLib_RendererBuilder getHpLibRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "HpLib");
        switch (video_type[currentTrackIndex]){
            case "hls":
                return new HpLib_HlsHpLibRendererBuilder(this, userAgent, video_url[currentTrackIndex]);
            case "others":
                return new HpLib_ExtractorHpLibRendererBuilder(this,userAgent, Uri.parse(video_url[currentTrackIndex]));
            default:
                throw new IllegalStateException("Unsupported type: " + video_url[currentTrackIndex]);
        }
    }

    Handler getMainHandler() {
        return mainHandler;
    }

    void onRenderersError(Exception e) {
    }

    void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i] == null) {
                renderers[i] = new DummyTrackRenderer();
            }
        }
        // Complete preparation.
        this.videoRenderer = renderers[TYPE_VIDEO];
        pushSurface(false);
        player.prepare(renderers);
        player.setPlayWhenReady(true);
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (videoRenderer == null) {return;}
        if (blockForSurfacePush) {
            player.blockingSendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        } else {
            player.sendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        }
    }

    private void killPlayer(){
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        killPlayer();
    }

    @Override
    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

    }

    @Override
    public void onLoadCanceled(int sourceId, long bytesLoaded) {

    }

    @Override
    public void onLoadError(int sourceId, IOException e) {

    }

    @Override
    public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

    }
}

