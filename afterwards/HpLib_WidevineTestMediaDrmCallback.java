package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.annotation.TargetApi;
import android.media.MediaDrm;
import android.media.MediaDrm.KeyRequest;

import com.google.android.exoplayer.drm.MediaDrmCallback;
import com.google.android.exoplayer.util.Util;

import android.media.MediaDrm.ProvisionRequest;
import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by kunal.bhatia on 06-05-2016.
 */

class HpLib_WidevineTestMediaDrmCallback implements MediaDrmCallback {
    private final String defaultUri;
    private static final String WIDEVINE_GTS_DEFAULT_BASE_URI = "https://proxy.uat.widevine.com/proxy";
    public HpLib_WidevineTestMediaDrmCallback(String contentId, String provider) {
        String params = "?video_id=" + contentId + "&provider=" + provider;
        defaultUri = WIDEVINE_GTS_DEFAULT_BASE_URI + params;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
        String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
        return Util.executePost(url, null, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws IOException {
        String url = request.getDefaultUrl();
        if (TextUtils.isEmpty(url)) {
            url = defaultUri;
        }
        return Util.executePost(url, request.getData(), null);
    }
}
