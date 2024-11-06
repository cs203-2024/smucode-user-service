package com.cs203.smucode.constants;

import java.util.List;

public final class MediaConstants {

    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_GIF = "image/gif";
    public static final List<String> SUPPORTED_MEDIA
            = List.of(IMAGE_PNG, IMAGE_JPEG, IMAGE_GIF);

    private MediaConstants() {
        throw new IllegalStateException("Constant class");
    }
}
