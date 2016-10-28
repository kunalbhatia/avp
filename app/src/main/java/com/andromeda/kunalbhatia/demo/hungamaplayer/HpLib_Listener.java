package com.andromeda.kunalbhatia.demo.hungamaplayer;

import java.io.Serializable;

/**
 * Created by kunal.bhatia on 06-05-2016.
 */
public interface HpLib_Listener extends Serializable {
    void player_created(VideoPlayer videoPlayer);
    //void click_next(Hplib_Tracker hplib_tracker);
    //void click_previous(Hplib_Tracker hplib_tracker);
    void click_cast();
}
