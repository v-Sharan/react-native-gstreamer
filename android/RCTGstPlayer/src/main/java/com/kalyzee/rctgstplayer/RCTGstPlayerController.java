package com.kalyzee.rctgstplayer;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.kalyzee.rctgstplayer.utils.EaglUIView;
import com.kalyzee.rctgstplayer.utils.RCTGstConfigurationCallable;
import com.kalyzee.rctgstplayer.utils.RCTGstConfiguration;

// GStreamer import removed — loaded via System.loadLibrary below

@SuppressWarnings("deprecation")  // ← ADDED for RCTEventEmitter
public class RCTGstPlayerController implements RCTGstConfigurationCallable, SurfaceHolder.Callback {

    private static final String LOG_TAG = "RCTGstPlayer";
    private boolean isInited = false;
    private RCTGstConfiguration configuration;
    private EaglUIView view;
    private ReactContext context;

    private native String nativeRCTGstGetGStreamerInfo();
    private native void nativeRCTGstSetDrawableSurface(Surface drawableSurface);
    private native void nativeRCTGstSetUri(String uri);
    private native void nativeRCTGstSetAudioLevelRefreshRate(int audioLevelRefreshRate);
    private native void nativeRCTGstSetDebugging(boolean isDebugging);
    private native void nativeRCTGstSetPipelineState(int state);
    private native void nativeRCTGstInitAndRun(RCTGstConfiguration configuration);

    @Override
    public void onInit() {
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onPlayerInit", null);
    }

    @Override
    public void onStateChanged(int old_state, int new_state) {
        WritableMap event = Arguments.createMap();
        event.putInt("old_state", old_state);
        event.putInt("new_state", new_state);
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onStateChanged", event);
    }

    @Override
    public void onVolumeChanged(double rms, double peak, double decay) {
        WritableMap event = Arguments.createMap();
        event.putDouble("rms", rms);
        event.putDouble("peak", peak);
        event.putDouble("decay", decay);
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onVolumeChanged", event);
    }

    @Override
    public void onUriChanged(String new_uri) {
        WritableMap event = Arguments.createMap();
        event.putString("new_uri", new_uri);
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onUriChanged", event);
    }

    @Override
    public void onEOS() {
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onEOS", null);
    }

    @Override
    public void onElementError(String source, String message, String debug_info) {
        WritableMap event = Arguments.createMap();
        event.putString("source", source);
        event.putString("message", message);
        event.putString("debug_info", debug_info);
        context.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onElementError", event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isInited) {
            this.configuration.setInitialDrawableSurface(holder.getSurface());
            nativeRCTGstInitAndRun(this.configuration);
            this.isInited = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        nativeRCTGstSetDrawableSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    public RCTGstPlayerController(ReactContext context) {
        this.context = context;

        // GStreamer.init() removed — init happens via nativeRCTGstInitAndRun
        // Libraries loaded below via System.loadLibrary

        String version = nativeRCTGstGetGStreamerInfo();
        Log.d(LOG_TAG, version);

        this.view = new EaglUIView(this.context, this);
        this.configuration = new RCTGstConfiguration(this);
    }

    View getView() { return this.view; }

    void setRctGstUri(String uri) { nativeRCTGstSetUri(uri); }
    void setRctGstAudioLevelRefreshRate(int rate) { nativeRCTGstSetAudioLevelRefreshRate(rate); }
    void setRctGstDebugging(boolean isDebugging) { nativeRCTGstSetDebugging(isDebugging); }
    void setRctGstState(int state) { nativeRCTGstSetPipelineState(state); }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("rctgstplayer");
    }
}