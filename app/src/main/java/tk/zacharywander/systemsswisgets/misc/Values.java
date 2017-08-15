package tk.zacharywander.systemsswisgets.misc;

import java.util.ArrayList;

public class Values
{
    public static final String MUSIC_INTENT_ACTION = "music_action";
    public static final String TOGGLE_INTENT_ACTION = "toggle_action";
    public static final String FLASHLIGHT_ENABLED = "flashlight_enabled";

    public static final int MUSIC_BACK = -1;
    public static final int MUSIC_PLAYPAUSE = 0;
    public static final int MUSIC_NEXT = 1;
    public static final int MUSIC_OPEN = 2;

    public static final int SOUND_ID = 0;
    public static final int WIFI_ID = 1;
    public static final int FLASHLIGHT_ID = 2;
    public static final int BT_ID = 3;
    public static final int AIRPLANE_ID = 4;

    public static final String SOUND_TOGGLE = "sound";
    public static final String WIFI_TOGGLE = "wifi";
    public static final String FLASHLIGHT_TOGGLE = "flashlight";
    public static final String BLUETOOTH_TOGGLE = "bluetooth";
    public static final String AIRPLANE_TOGGLE = "airplane";

    public static final String SOUND_COLOR = "sound_color";
    public static final String WIFI_COLOR = "wifi_color";
    public static final String FLASHLIGHT_COLOR = "flash_color";
    public static final String BT_COLOR = "bt_color";
    public static final String AIRPLANE_COLOR = "airplane_color";

    public static final ArrayList<String> defaultToggleOrder = new ArrayList<String>() {{
        add(SOUND_TOGGLE);
        add(WIFI_TOGGLE);
        add(FLASHLIGHT_TOGGLE);
        add(BLUETOOTH_TOGGLE);
        add(AIRPLANE_TOGGLE);
    }};
}
