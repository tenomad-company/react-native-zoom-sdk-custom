package ch.milosz.reactnative;

import us.zoom.sdk.MobileRTCVideoUnitAspectMode;

public class EnumConverter {
    static int toAspect(String aspect) {
        if (aspect == null) return MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_ORIGINAL;

        switch (aspect) {
            case "letterBox":
                return MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_LETTER_BOX;
            case "panAndScan":
                return MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN;
            case "fullFilled":
                return MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_FULL_FILLED;
            case "original":
            default:
                return MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_ORIGINAL;
        }
    }
}
