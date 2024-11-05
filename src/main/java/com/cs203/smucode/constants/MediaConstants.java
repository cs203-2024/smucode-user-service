package com.cs203.smucode.constants;

import java.util.List;

public final class MediaConstants {

    public static final List<String> SUPPORTED_MEDIA
            = List.of("image/png", "image/jpeg", "image/gif");
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_GIF = "image/gif";

    private MediaConstants() {
        throw new IllegalStateException("Constant class");
    }
}
