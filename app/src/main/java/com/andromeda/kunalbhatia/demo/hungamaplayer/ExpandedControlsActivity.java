package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.SeekBar;

import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

/**
 * Created by kunal.bhatia on 10/5/2016.
 */

public class ExpandedControlsActivity extends ExpandedControllerActivity {
    private SeekBar seekbar;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //seekbar = (SeekBar) this.getSeekBar();
        //CastButtonFactory.setUpMediaRouteButton(this,);
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent();
        int currDur = seekbar.getProgress();
        intent.putExtra("currTime",currDur);
        setResult(200,intent);
        finish();
    }*/
}
