package ch.milosz.reactnative;

import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import us.zoom.sdk.MobileRTCVideoUnitRenderInfo;
import us.zoom.sdk.MobileRTCVideoView;

public class RNZoomMobileRCTViewManager extends SimpleViewManager<MobileRTCVideoView> {
    public static final String TAG = "RNZoomUs-Video";
    public static final String REACT_CLASS = "RNZoomMobileRCTView";

    private ThemedReactContext reactContext;
    private MobileRTCVideoUnitRenderInfo info;
    private int id = 0;

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected MobileRTCVideoView createViewInstance(@NonNull ThemedReactContext context) {
        reactContext = context;
        info = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
        return new MobileRTCVideoView(context);
    }

    @ReactProp(name = "renderInfo")
    public void setRenderInfo(final MobileRTCVideoView view, final ReadableMap data) {
        try {
            Log.i(TAG, "renderInfo: " + data.toString());
            final int userId = getUserId(data);
            info.aspect_mode = EnumConverter.toAspect(data.getString("videoAspect"));

            if (userId == 0) return;

            if (id != 0 || id != userId) {
                id = userId;
                view.getVideoViewManager().removeAllAttendeeVideoUnit();
                view.getVideoViewManager().addAttendeeVideoUnit(userId, info);
            } else {
                view.getVideoViewManager().updateAttendeeVideoUnit(userId, info);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            view.getVideoViewManager().removeAllVideoUnits();
        }
    }

    private int getUserId(final ReadableMap data) {
        try {
            int result = 0;
            ReadableType type = data.getType("userId");
            if (type == ReadableType.Number) result = data.getInt("userId");
            if (type == ReadableType.String) result = Integer.parseInt(data.getString("userId"));
            return result;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onDropViewInstance(@NonNull MobileRTCVideoView view) {
        Log.i(TAG, "onDropViewInstance");
        try {
            if (view != null && view.getVideoViewManager() != null) {
                view.getVideoViewManager().removeAllVideoUnits();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDropViewInstance(view);
    }
}