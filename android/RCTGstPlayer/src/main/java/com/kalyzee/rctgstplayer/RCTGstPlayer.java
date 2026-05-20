package com.kalyzee.rctgstplayer;

import androidx.annotation.Nullable;  // ← FIXED
import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.kalyzee.rctgstplayer.utils.manager.Command;

import java.util.Map;

public class RCTGstPlayer extends SimpleViewManager {

    private RCTGstPlayerController playerController;

    @Override
    public String getName() {
        return "RCTGstPlayer";
    }

    @Override
    protected View createViewInstance(ThemedReactContext reactContext) {
        this.playerController = new RCTGstPlayerController(reactContext);
        return this.playerController.getView();
    }

    @ReactProp(name = "uri")
    public void setUri(View controllerView, String uri) {
        this.playerController.setRctGstUri(uri);
    }

    @ReactProp(name = "audioLevelRefreshRate")
    public void setAudioLevelRefreshRate(View controllerView, int audioLevelRefreshRate) {
        this.playerController.setRctGstAudioLevelRefreshRate(audioLevelRefreshRate);
    }

    @ReactProp(name = "isDebugging")
    public void setAudioLevelRefreshRate(View controllerView, boolean isDebugging) {
        this.playerController.setRctGstDebugging(isDebugging);
    }

    @Override
    public void receiveCommand(View view, String commandType, @Nullable ReadableArray args) {
        if ("setState".equals(commandType))
            this.playerController.setRctGstState(args.getInt(0));
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return Command.getCommandsMap();
    }

    @Nullable @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onPlayerInit", MapBuilder.of("registrationName", "onPlayerInit"))
                .put("onStateChanged", MapBuilder.of("registrationName", "onStateChanged"))
                .put("onVolumeChanged", MapBuilder.of("registrationName", "onVolumeChanged"))
                .put("onUriChanged", MapBuilder.of("registrationName", "onUriChanged"))
                .put("onEOS", MapBuilder.of("registrationName", "onEOS"))
                .put("onElementError", MapBuilder.of("registrationName", "onElementError"))
                .build();
    }
}