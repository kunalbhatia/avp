package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
public class HungamaVideoPlayer extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener,
        ExoPlayer.Listener, View.OnClickListener,SeekBar.OnSeekBarChangeListener,
        HlsSampleSource.EventListener, BandwidthMeter.EventListener,
        StreamingDrmSessionManager.EventListener,DashChunkSource.EventListener,
        ChunkSampleSource.EventListener, MediaCodecVideoTrackRenderer.EventListener,
        MediaCodecAudioTrackRenderer.EventListener,MediaController.MediaPlayerControl {
    private static final String TAG = "HungamaVideoPlayer";
    public static final int RENDERER_COUNT = 2;
    private final Runnable screen_controls_runnable;
    private ExoPlayer player;
    private PlayerControl playerControl;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private AudioManager am;
    private SurfaceView surface;
    private String url;
    private BandwidthMeter bandwidthMeter;
    private TrackRenderer videoRenderer;
    private CodecCounters codecCounters;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_METADATA = 3;
    private ManifestFetcher<HlsPlaylist> playlistFetcher;
    private ImageButton btn_play;
    private ImageButton btn_pause;
    private ImageButton btn_fwd;
    private ImageButton btn_rev;
    private ImageButton btn_settings;
    private ImageButton btn_lock;
    private ImageButton btn_next;
    private ImageButton btn_prev;
    private ImageButton btn_unlock;
    private ImageButton btn_back;
    private MediaRouteButton btn_cast;
    private Button btn_subs;
    private TextView txt_title, txt_ct, txt_td;
    private RelativeLayout loadingPanel;
    private SeekBar seekBar;
    private Runnable updateData;
    private View decorView;
    private int uiImmersiveOptions;
    private LinearLayout all_controls, top_controls, middle_panel, unlock_panel, bottom_controls,volumeBarContainer, brightnessBarContainer, seekBar_center_text, onlySeekbar, brightness_center_text, vol_center_text,only_controls;
    private Handler controls_handler,playTime_Handler,pauseTime_Handler;
    private boolean immersiveMode, intLeft, intRight, intTop, intBottom, finLeft, finRight, finTop, finBottom;
    private Display display;
    private Point size;
    private int sWidth,sHeight;
    private float baseX, baseY;
    private long diffX, diffY;
    private int calculatedTime;
    private String seekDur;
    private double seekSpeed = 0;
    private AudioManager audioManager;
    private int controlsHideDuration = 5000;
    private Format videoFormat;
    private ProgressBar volumeBar, brightnessBar;
    private TextView txt_seek_currTime, txt_seek_secs, vol_perc_center_text, brigtness_perc_center_text;
    private ImageView volIcon, brightnessIcon, vol_image, brightness_image;
    private String video_url, video_title, video_type,contentId,provider = null;
    private Handler mainHandler;
    private static final int RENDERER_BUILDING_STATE_IDLE = 1;
    private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
    private static final int RENDERER_BUILDING_STATE_BUILT = 3;
    private int rendererBuildingState,device_height,device_width,brightness, mediavolume,playTime,pauseTime;
    private HpLib_RendererBuilder hpLibRendererBuilder;
    private ContentResolver cResolver;
    private Window window;
    private static final int MIN_DISTANCE = 150;
    private Boolean tested_ok = false;
    private Boolean screen_swipe_move = false;
    private HpLib_Listener hplib_event;
    private String applicationId="4F8B3483";
    private Hplib_Tracker hplib_tracker;
    private String content_id;
    private String content_viewing;
    private String property;
    private Handler bufferHandler;
    private int bufferTime;
    private int seekCount;
    private int pauseCount;
    private String subStatus;
    private String subcategory;
    private String category;
    private String albumid;
    private String user_email;
    private String userid;
    private String vendor;
    private String genre;
    private String singer;
    private String albumname;
    private String language;
    private int ltime;
    private Handler ltimeHandler;
    private String errmsg;


    private MediaRouteButton mMediaRouteButton;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private PlaybackState mPlaybackState;
    private SessionManager mSessionManager;
    private MediaItem mSelectedMedia;
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
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
        if (null != mSelectedMedia) {

            if (mPlaybackState == PlaybackState.PLAYING) {

                return;
            } else {
                mPlaybackState = PlaybackState.IDLE;

            }
        }
        loadRemoteMedia(0,true);
    }
    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);


        mSelectedMedia = new MediaItem();
        mSelectedMedia.setUrl(video_url);
        mSelectedMedia.setContentType(video_type);
        mSelectedMedia.setTitle(video_title);

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
        remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
    }

    private void toggle_controls_visibility(){
        if(!immersiveMode) {
            if(top_controls.getVisibility()==View.GONE && immersiveMode==false){
                top_controls.setVisibility(View.VISIBLE);
                onlySeekbar.setVisibility(View.VISIBLE);
                only_controls.setVisibility(View.VISIBLE);
                controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
            }
        }else if(unlock_panel.getVisibility()==View.GONE && immersiveMode==true){
            // Log.d(TAG,"ok");
            unlock_panel.setVisibility(View.VISIBLE);
            controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
        }
    }
    {
        updateData = new Runnable() {
            @Override
            public void run() {
                String text = "";
                switch (player.getPlaybackState()) {
                    case ExoPlayer.STATE_BUFFERING:
                        text += "buffering";
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_ENDED:
                        text += "ended";
                        finish();
                        break;
                    case ExoPlayer.STATE_IDLE:
                        text += "idle";
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        text += "preparing";
                        loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    case ExoPlayer.STATE_READY:
                        text += "ready";
                        loadingPanel.setVisibility(View.GONE);
                        break;
                    default:
                        text += "unknown";
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
                if(!screen_swipe_move)
                    seekBar.setProgress((int) player.getCurrentPosition());
                btn_play.postDelayed(updateData, 200);
            }
        };
    }
    {
        screen_controls_runnable = new Runnable() {
            @Override
            public void run() {
                decorView.setSystemUiVisibility(uiImmersiveOptions);
                if(!immersiveMode) {
                    top_controls.setVisibility(View.GONE);
                    onlySeekbar.setVisibility(View.GONE);
                    only_controls.setVisibility(View.GONE);
                }else{
                    unlock_panel.setVisibility(View.GONE);
                }
            }
        };
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player != null) {
            switch (event.getAction()) {
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
                    return true;
                case MotionEvent.ACTION_MOVE:
                    screen_swipe_move=true;
                    if (immersiveMode == false) {
                        top_controls.setVisibility(View.GONE);
                        onlySeekbar.setVisibility(View.GONE);
                        only_controls.setVisibility(View.GONE);
                        diffX = (long) (Math.ceil(event.getX() - baseX));
                        diffY = (long) Math.ceil(event.getY() - baseY);
                        //Log.d(TAG, "diffX: " + diffX);
                        double brightnessSpeed = 0.01;
                        if (Math.abs(diffY) > MIN_DISTANCE) {
                            tested_ok = true;
                        }
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (Math.abs(diffX) > (MIN_DISTANCE + 100)) {
                                tested_ok = true;
                                seekBar_center_text.setVisibility(View.VISIBLE);
                                onlySeekbar.setVisibility(View.VISIBLE);
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
                        } else if (Math.abs(diffY) > Math.abs(diffX)) {
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
                            } else if (intRight) {
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
                        }
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    screen_swipe_move=false;
                    //TOUCH COMPLETED
                    if(seekBar_center_text.getVisibility()==View.VISIBLE) {
                        onlySeekbar.setVisibility(View.GONE);
                    }

                    if(tested_ok==false){
                        toggle_controls_visibility();
                    }
                    tested_ok = false;

                    brightness_center_text.setVisibility(View.GONE);
                    vol_center_text.setVisibility(View.GONE);
                    seekBar_center_text.setVisibility(View.GONE);
                    brightnessBarContainer.setVisibility(View.GONE);
                    volumeBarContainer.setVisibility(View.GONE);
                    calculatedTime = (int) (player.getCurrentPosition() + (calculatedTime));
                    player.seekTo(calculatedTime);
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hplib_activity_video_player);

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

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        device_height = displaymetrics.heightPixels;
        device_width = displaymetrics.widthPixels;
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_pause = (ImageButton) findViewById(R.id.btn_pause);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_fwd = (ImageButton) findViewById(R.id.btn_fwd);
        btn_rev = (ImageButton) findViewById(R.id.btn_rev);
        btn_lock = (ImageButton) findViewById(R.id.btn_lock);
        btn_unlock = (ImageButton) findViewById(R.id.btn_unlock);
        btn_settings = (ImageButton) findViewById(R.id.btn_settings);
        btn_prev = (ImageButton) findViewById(R.id.btn_prev);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        vol_perc_center_text = (TextView) findViewById(R.id.vol_perc_center_text);
        brigtness_perc_center_text = (TextView) findViewById(R.id.brigtness_perc_center_text);
        volumeBar = (ProgressBar) findViewById(R.id.volume_slider);
        brightnessBar = (ProgressBar) findViewById(R.id.brightness_slider);
        volumeBarContainer = (LinearLayout) findViewById(R.id.volume_slider_container);
        brightnessBarContainer = (LinearLayout) findViewById(R.id.brightness_slider_container);
        seekBar_center_text = (LinearLayout) findViewById(R.id.seekbar_center_text);
        onlySeekbar = (LinearLayout) findViewById(R.id.seekbar_time);
        only_controls = (LinearLayout) findViewById(R.id.controls);
        brightness_center_text = (LinearLayout) findViewById(R.id.brightness_center_text);
        vol_center_text = (LinearLayout) findViewById(R.id.vol_center_text);
        txt_seek_currTime = (TextView) findViewById(R.id.txt_seek_currTime);
        txt_seek_secs = (TextView) findViewById(R.id.txt_seek_secs);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_ct = (TextView) findViewById(R.id.txt_currentTime);
        txt_td = (TextView) findViewById(R.id.txt_totalDuration);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        surface = (SurfaceView) findViewById(R.id.surface_view);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingVPanel);
        all_controls = (LinearLayout) findViewById(R.id.root);
        top_controls = (LinearLayout) findViewById(R.id.top);
        middle_panel = (LinearLayout) findViewById(R.id.middle);
        //bottom_controls = (LinearLayout) findViewById(R.id.bottom);
        unlock_panel = (LinearLayout) findViewById(R.id.unlock_panel);
        volIcon = (ImageView) findViewById(R.id.volIcon);
        brightnessIcon = (ImageView) findViewById(R.id.brightnessIcon);
        vol_image = (ImageView) findViewById(R.id.vol_image);
        brightness_image = (ImageView) findViewById(R.id.brightness_image);

        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_fwd.setOnClickListener(this);
        btn_rev.setOnClickListener(this);
        btn_lock.setOnClickListener(this);
        btn_unlock.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
        btn_prev.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        loadingPanel.setVisibility(View.VISIBLE);
        decorView = getWindow().getDecorView();
        immersiveMode = false;
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;
        controls_handler = new Handler();
        controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Intent intent = getIntent();
        video_url = intent.getStringExtra("video_url");
        video_title = intent.getStringExtra("video_title");
        video_type = intent.getStringExtra("video_type");
        content_id = intent.getStringExtra("content_id");
        content_viewing = intent.getStringExtra("content_viewing");
        property = intent.getStringExtra("property");
        subStatus = intent.getStringExtra("sub_status");
        subcategory = intent.getStringExtra("subcategory");
        category = intent.getStringExtra("category");
        albumid = intent.getStringExtra("albumid");
        user_email = intent.getStringExtra("user_email");
        userid = intent.getStringExtra("userid");
        vendor = intent.getStringExtra("vendor");
        genre = intent.getStringExtra("genre");
        singer = intent.getStringExtra("singer");
        albumname = intent.getStringExtra("albumname");
        language = intent.getStringExtra("language");
        hplib_event = (HpLib_Listener) intent.getSerializableExtra("interface");

        if(video_type==null){
            //video_type="others";
            video_type=HpLib_Constants.MEDIA_TYPE_HLS;
            //video_type=HpLib_Constants.MEDIA_TYPE_DASH;
        }
        if (video_url == null) {
            //video_url = "https://85-content.hungama.com/1218/4/FF-2014-00000642/stream.mpd?https://wv.service.expressplay.com/hms/wv/rights/?ExpressPlayToken=AQAAAA9JKcEAAABQ0TzVN8BfBuuemyNm4uZLHlZXme0x1Qaqf6lbUm-Bk52mztzfC8HLtA_tzihZC7sYxd5jythYWzGgJ7Hgg1S-YYah-olULq-3IJnIJhJhZfo0O0Wm1dcC3pUlWgC3D-SUJX0XmQ";
            //video_url = "http://content.hungama.com/movie/video%20multi%20drm%20content/mpd/FF-2013-00000478/stream.mpd";

//            video_url = "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?"
//                    + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
//                    + "ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7."
//                    + "8506521BFC350652163895D4C26DEE124209AA9E&key=ik0";
            video_url = "http://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8";
            //video_url = "file:///sdcard/Download/ruhi.mp4"; //playing
            //video_url = "http://player.hungama.com/mp3/a.mkv"; //playing
            //video_url = "http://player.hungama.com/mp3/a.mp4"; //playing
            //video_url = "http://player.hungama.com/mp3/drop.avi"; //not playing
            //video_url = "http://player.hungama.com/mp3/b.avi"; //not playing
            //video_url = "http://player.hungama.com/mp3/c.3gp";
        }

        if (video_title == null) {
            video_title = "Tears";
            //video_title = "Google";
        }
        mainHandler = new Handler();
        /*---CHROMECAST CODE--*/
        /*mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mSelector = new MediaRouteSelector.Builder()
                // These are the framework-supported intents
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build();*/
        playTime_Handler = new Handler();
        pauseTime_Handler = new Handler();
        bufferHandler = new Handler();
        ltimeHandler = new Handler();
        execute();
    }
    public void execute() {
        ltime = 0;
        playTime = 0;
        pauseTime = 0;
        bufferTime = 0;
        seekCount = 0;
        pauseCount = 0;
        errmsg = "";
        hplib_tracker = new Hplib_Tracker();
        if(hplib_event!=null)
            hplib_event.player_created(this);
        am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        txt_title.setText(video_title);
        contentId = video_title;
        provider="";
        player=ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        playerControl = new PlayerControl(player);
        if(player!=null) {
            hpLibRendererBuilder = getHpLibRendererBuilder();
            hpLibRendererBuilder.buildRenderers(this);
        }
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
    }
    void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
        Log.d(TAG,"onRenderers");
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i] == null) {
                renderers[i] = new DummyTrackRenderer();
            }
        }
        // Complete preparation.
        this.videoRenderer = renderers[TYPE_VIDEO];
        this.codecCounters = videoRenderer instanceof MediaCodecTrackRenderer
                ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
                : renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer
                ? ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters : null;
        this.bandwidthMeter = bandwidthMeter;
        pushSurface(false);
        loadingPanel.setVisibility(View.VISIBLE);
        player.prepare(renderers);
        Log.d(TAG,"preparing");
        playerControl = new PlayerControl(player);
        if (requestFocus()) {
            Log.d(TAG,"requestfocus");
            player.setPlayWhenReady(true);
            btn_play.postDelayed(updateData, 200);
        }
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
    }
    /* package */ Handler getMainHandler() {
        return mainHandler;
    }
    /* package */ Looper getPlaybackLooper() {
        return player.getPlaybackLooper();
    }
    void onRenderersError(Exception e) {
        errmsg = e.getMessage();
    }
    private HpLib_RendererBuilder getHpLibRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "HpLib");
        switch (video_type){
            case HpLib_Constants.MEDIA_TYPE_DASH:
                return new HpLib_DashHpLibRendererBuilder(this,userAgent,video_url,
                        new HpLib_WidevineTestMediaDrmCallback(contentId, provider));
            case HpLib_Constants.MEDIA_TYPE_HLS:
                return new HpLib_HlsHpLibRendererBuilder(this, userAgent, video_url);
            case HpLib_Constants.MEDIA_TYPE_OTHERS:
                return new HpLib_ExtractorHpLibRendererBuilder(this,userAgent,Uri.parse(video_url));
            case HpLib_Constants.MEDIA_TYPE_SS:
            default:
                throw new IllegalStateException("Unsupported type: " + video_url);
        }
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
    @Override
    public void onClick(View v) {
        controls_handler.removeCallbacks(screen_controls_runnable);
        int i1 = v.getId();
        if (i1 == R.id.btn_pause) {
            if (playerControl.isPlaying()) {
                playerControl.pause();
                pauseCount++;
                playTime_Handler.removeCallbacksAndMessages(null);
                pauseTime_Handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pauseTime++;
                    }
                },1000);
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
            }
        } else if (i1 == R.id.btn_play) {
            if (!playerControl.isPlaying()) {
                playerControl.start();
                playTime_Handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playTime++;
                    }
                },1000);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
            }
        } /*else if (i1 == R.id.btn_cast) {
            if(hplib_event!=null)
                hplib_event.click_cast();
        }*/ else if (i1 == R.id.btn_back) {
            player.release();
            finish();
        } else if (i1 == R.id.btn_fwd) {
            player.seekTo(player.getCurrentPosition() + 30000);
        } else if (i1 == R.id.btn_rev) {
            player.seekTo(player.getCurrentPosition() - 30000);
        } else if (i1 == R.id.btn_lock) {
            top_controls.setVisibility(View.GONE);
            onlySeekbar.setVisibility(View.GONE);
            only_controls.setVisibility(View.GONE);
            unlock_panel.setVisibility(View.VISIBLE);
            controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
            immersiveMode = true;
        } else if (i1 == R.id.btn_unlock) {
            top_controls.setVisibility(View.VISIBLE);
            onlySeekbar.setVisibility(View.VISIBLE);
            only_controls.setVisibility(View.VISIBLE);
            unlock_panel.setVisibility(View.GONE);
            controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
            immersiveMode = false;
        } else if (i1 == R.id.btn_next) {
            collectStats();
            if(hplib_event!=null)
                hplib_event.click_next(hplib_tracker);
            //player.release();
        } else if (i1 == R.id.btn_prev) {
            collectStats();
            if(hplib_event!=null)
                hplib_event.click_previous(hplib_tracker);
            //player.release();
        } else if (i1 == R.id.btn_settings) {
            PopupMenu popup = new PopupMenu(HungamaVideoPlayer.this, v);
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
        controls_handler.postDelayed(screen_controls_runnable, controlsHideDuration);
    }

    private void collectStats() {
        hplib_tracker.setHplib_errormessage(errmsg);
        hplib_tracker.setHplib_ltime(String.valueOf(ltime));
        hplib_tracker.setHplib_language(language);
        hplib_tracker.setHplib_albumname(albumname);
        hplib_tracker.setHplib_singer(singer);
        hplib_tracker.setHplib_genre(genre);
        hplib_tracker.setHplib_vendor(vendor);
        hplib_tracker.setHplib_userid(userid);
        hplib_tracker.setHplib_email(user_email);
        hplib_tracker.setHplib_category(category);
        hplib_tracker.setHplib_albumid(albumid);
        hplib_tracker.setHplib_subcategory(subcategory);
        hplib_tracker.setHplib_substatus(subStatus);
        hplib_tracker.setHplib_pausecount(String.valueOf(pauseCount));
        hplib_tracker.setHplib_seekcount(String.valueOf(seekCount));
        hplib_tracker.setHplib_property(property);
        hplib_tracker.setHplib_content_viewing(content_viewing);
        hplib_tracker.setHplib_content_id(content_id);
        hplib_tracker.setHplib_content_title(video_title);
        hplib_tracker.setHplib_content_url(video_url);
        hplib_tracker.setHplib_content_type(video_type);
        hplib_tracker.setHplib_playtime(String.valueOf(playTime));
        hplib_tracker.setHplib_pausetime(String.valueOf(pauseTime));
        hplib_tracker.setHplib_buffertime(String.valueOf(bufferTime));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            controls_handler.removeCallbacksAndMessages(null);
            btn_play.removeCallbacks(updateData);
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekCount++;
        player.seekTo(seekBar.getProgress());
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
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_PREPARING:
                ltimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ltime++;
                    }
                },1000);
                break;
            case ExoPlayer.STATE_BUFFERING:
                bufferHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bufferTime++;
                    }
                },1000);
                break;
            case ExoPlayer.STATE_READY:
                ltimeHandler.removeCallbacksAndMessages(null);
                bufferHandler.removeCallbacksAndMessages(null);
                break;
        }
    }
    @Override
    public void onPlayWhenReadyCommitted() {
    }
    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }
    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }
    private void killPlayer(){
        if (player != null) {
            player.release();
            controls_handler.removeCallbacksAndMessages(null);
            btn_play.removeCallbacks(updateData);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        killPlayer();
    }


    /*HLS EVENTS*/
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
    /*HLS EVENTS ENDS*/
    /*DASH EVENTS*/
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
    }
    @Override
    public void onDrmKeysLoaded() {

    }
    @Override
    public void onDrmSessionManagerError(Exception e) {

    }
    @Override
    public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {

    }
    @Override
    public void onDroppedFrames(int count, long elapsed) {
    }
    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    }
    @Override
    public void onDrawnToSurface(Surface surface) {
    }
    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
    }
    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
    }
    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {

    }
    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    }
    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
    }
    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
    }
    /*DASH EVENTS ENDS*/
    /*MEDIA PLAYER CONTROLS EVENTS*/
    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    /*MEDIA PLAYER CONTROLS EVENTS ENDS*/
}

