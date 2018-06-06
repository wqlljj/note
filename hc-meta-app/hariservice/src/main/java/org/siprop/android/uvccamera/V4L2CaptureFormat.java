package org.siprop.android.uvccamera;

public class V4L2CaptureFormat {
    public final int width;
    public final int height;
    public final int maxFramerate;
    public final int minFramerate;
    public final String imageFormat;

    public V4L2CaptureFormat(int width, int height, int minFramerate, 
    		                int maxFramerate,String imageFormat) {
      this.width = width;
      this.height = height;
      this.minFramerate = minFramerate;
      this.maxFramerate = maxFramerate;
      this.imageFormat = imageFormat;
    }
}
