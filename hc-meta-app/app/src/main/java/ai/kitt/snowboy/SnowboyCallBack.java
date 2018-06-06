package ai.kitt.snowboy;

/**
 * Created by wangqi on 2018/5/22.
 */

public interface SnowboyCallBack {
    enum Event{
        ACTIVE,STARTRECORDING,STOPRECORDING
    }
    void onEvent(Event event);
}
