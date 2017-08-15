package tk.zacharywander.systemsswisgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

import tk.zacharywander.systemsswisgets.R;
import tk.zacharywander.systemsswisgets.misc.FlashlightController;
import tk.zacharywander.systemsswisgets.misc.Util;
import tk.zacharywander.systemsswisgets.misc.Values;
import tk.zacharywander.systemsswisgets.services.TogglesService;

import static tk.zacharywander.systemsswisgets.misc.Values.AIRPLANE_COLOR;
import static tk.zacharywander.systemsswisgets.misc.Values.AIRPLANE_ID;
import static tk.zacharywander.systemsswisgets.misc.Values.AIRPLANE_TOGGLE;
import static tk.zacharywander.systemsswisgets.misc.Values.BLUETOOTH_TOGGLE;
import static tk.zacharywander.systemsswisgets.misc.Values.BT_COLOR;
import static tk.zacharywander.systemsswisgets.misc.Values.BT_ID;
import static tk.zacharywander.systemsswisgets.misc.Values.FLASHLIGHT_COLOR;
import static tk.zacharywander.systemsswisgets.misc.Values.FLASHLIGHT_ENABLED;
import static tk.zacharywander.systemsswisgets.misc.Values.FLASHLIGHT_ID;
import static tk.zacharywander.systemsswisgets.misc.Values.FLASHLIGHT_TOGGLE;
import static tk.zacharywander.systemsswisgets.misc.Values.SOUND_COLOR;
import static tk.zacharywander.systemsswisgets.misc.Values.SOUND_ID;
import static tk.zacharywander.systemsswisgets.misc.Values.SOUND_TOGGLE;
import static tk.zacharywander.systemsswisgets.misc.Values.TOGGLE_INTENT_ACTION;
import static tk.zacharywander.systemsswisgets.misc.Values.WIFI_COLOR;
import static tk.zacharywander.systemsswisgets.misc.Values.WIFI_ID;
import static tk.zacharywander.systemsswisgets.misc.Values.WIFI_TOGGLE;

public class Toggles extends AppWidgetProvider
{
    private AppWidgetManager mManager;
    private int[] mIds;
    private Context mContext;

    private RemoteViews mView;
    private ContentObserver observer;
    private BroadcastReceiver receiver;
    public static FlashlightController controller;

    private int wifi;
    private int sound;
    private int flash;
    private int bluetooth;
    private int airplane;

    private boolean mFlashlightEnabled = false;

    private static final String TAG = "SSSystem";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mView = new RemoteViews(context.getPackageName(), R.layout.layout_toggles);
        mContext = context;

        mManager = appWidgetManager;
        mIds = appWidgetIds;

        controller = new FlashlightController(mContext);

        addInSetOrder();

        addTouchListeners();
        registerReceiverAndObserver();

        mManager.updateAppWidget(mIds, mView);
    }

    private void addInSetOrder() {
        ArrayList<String> keySet = Util.parseSavedToggleOrder(mContext, Values.defaultToggleOrder);

        mView.removeAllViews(R.id.toggles);

        for (String key : keySet) {
            switch (key)
            {
                case WIFI_TOGGLE:
                    RemoteViews wifi = new RemoteViews(mContext.getPackageName(), R.layout.toggle_wifi);
                    this.wifi = wifi.getLayoutId();
                    mView.addView(R.id.toggles, wifi);
                    setWifiState();
                    setWifiColor();
                    break;
                case SOUND_TOGGLE:
                    RemoteViews sound = new RemoteViews(mContext.getPackageName(), R.layout.toggle_sound);
                    this.sound = sound.getLayoutId();
                    mView.addView(R.id.toggles, sound);
                    setSoundState();
                    setSoundColor();
                    break;
                case FLASHLIGHT_TOGGLE:
                    RemoteViews flash = new RemoteViews(mContext.getPackageName(), R.layout.toggle_flashlight);
                    this.flash = flash.getLayoutId();
                    mView.addView(R.id.toggles, flash);
                    setFlashlightState();
                    setFlashlightColor();
                    break;
                case BLUETOOTH_TOGGLE:
                    RemoteViews bt = new RemoteViews(mContext.getPackageName(), R.layout.toggle_bluetooth);
                    this.bluetooth = bt.getLayoutId();
                    mView.addView(R.id.toggles, bt);
                    setBluetoothState();
                    setBluetoothColor();
                    break;
                case AIRPLANE_TOGGLE:
                    RemoteViews airplane = new RemoteViews(mContext.getPackageName(), R.layout.toggle_airplane);
                    this.airplane = airplane.getLayoutId();
                    mView.addView(R.id.toggles, airplane);
                    setAirplaneState();
                    setAirplaneColor();
                    break;
            }
        }

        mManager.updateAppWidget(mIds, mView);
    }

    private void addTouchListeners() {
        Intent wifiPress = new Intent(mContext, TogglesService.class);
        wifiPress.setAction(Values.TOGGLE_INTENT_ACTION);
        wifiPress.putExtra(Values.TOGGLE_INTENT_ACTION, WIFI_ID);

        Intent soundPress = new Intent(mContext, TogglesService.class);
        soundPress.setAction(Values.TOGGLE_INTENT_ACTION);
        soundPress.putExtra(Values.TOGGLE_INTENT_ACTION, SOUND_ID);

        Intent flashPress = new Intent(mContext, TogglesService.class);
        flashPress.setAction(TOGGLE_INTENT_ACTION);
        flashPress.putExtra(TOGGLE_INTENT_ACTION, FLASHLIGHT_ID);

        Intent btPress = new Intent(mContext, TogglesService.class);
        btPress.setAction(TOGGLE_INTENT_ACTION);
        btPress.putExtra(TOGGLE_INTENT_ACTION, BT_ID);

        Intent airplanePress = new Intent(mContext, TogglesService.class);
        airplanePress.setAction(TOGGLE_INTENT_ACTION);
        airplanePress.putExtra(TOGGLE_INTENT_ACTION, AIRPLANE_ID);

        PendingIntent wifi = PendingIntent.getService(mContext, WIFI_ID, wifiPress, 0);
        PendingIntent sound = PendingIntent.getService(mContext, SOUND_ID, soundPress, 0);
        PendingIntent flash = PendingIntent.getService(mContext, FLASHLIGHT_ID, flashPress, 0);
        PendingIntent bt = PendingIntent.getService(mContext, BT_ID, btPress, 0);
        PendingIntent airplane = PendingIntent.getService(mContext, AIRPLANE_ID, airplanePress, 0);

        mView.setOnClickPendingIntent(R.id.wifi, wifi);
        mView.setOnClickPendingIntent(R.id.sound, sound);
        mView.setOnClickPendingIntent(R.id.flashlight, flash);
        mView.setOnClickPendingIntent(R.id.bluetooth, bt);
        mView.setOnClickPendingIntent(R.id.airplane, airplane);
    }

    private void registerReceiverAndObserver() {
        observer = new ContentObserver(null)
        {
            @Override
            public void onChange(boolean selfChange, Uri uri)
            {
                Log.e("RECEIVED", uri.toString());

                Uri wifi = Settings.Global.getUriFor(WIFI_COLOR);
                Uri sound = Settings.Global.getUriFor(SOUND_COLOR);
                Uri flash = Settings.Global.getUriFor(FLASHLIGHT_COLOR);
                Uri bt = Settings.Global.getUriFor(BT_COLOR);
                Uri airplane = Settings.Global.getUriFor(AIRPLANE_COLOR);

                Uri wifiShown = Settings.Global.getUriFor(WIFI_TOGGLE + "_shown");
                Uri soundShown = Settings.Global.getUriFor(SOUND_TOGGLE + "_shown");
                Uri flashShown = Settings.Global.getUriFor(FLASHLIGHT_TOGGLE + "_shown");
                Uri btShown = Settings.Global.getUriFor(BLUETOOTH_TOGGLE + "_shown");
                Uri airplaneShown = Settings.Global.getUriFor(AIRPLANE_TOGGLE + "_shown");

                Uri toggleOrder = Settings.Global.getUriFor("toggle_order");

                if (uri.equals(wifi)) {
                    setWifiColor();
                }

                if (uri.equals(sound)) {
                    setSoundColor();
                }

                if (uri.equals(flash)) {
                    setFlashlightColor();
                }

                if (uri.equals(bt)) {
                    setBluetoothColor();
                }

                if (uri.equals(airplane)) {
                    setAirplaneColor();
                }

                if (uri.equals(wifiShown)
                        || uri.equals(soundShown)
                        || uri.equals(flashShown)
                        || uri.equals(btShown)
                        || uri.equals(airplaneShown)
                        || uri.equals(toggleOrder)) {
                    addInSetOrder();
                }
            }
        };

        mContext.getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, observer);

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) ||
                        action.equals("android.net.wifi.WIFI_STATE_CHANGED") ||
                        action.equals("android.net.wifi.STATE_CHANGED")) {
                    setWifiState();
                }

                if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                    setSoundState();
                }

                if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) ||
                        action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    setBluetoothState();
                }

                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    setWifiState();
                    setAirplaneState();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");

        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        mContext.getApplicationContext().registerReceiver(receiver, filter);

        controller.addListener(new FlashlightController.FlashlightListener()
        {
            @Override
            public void onFlashlightChanged(boolean enabled)
            {
                setFlashlightState();
            }

            @Override
            public void onFlashlightError()
            {

            }

            @Override
            public void onFlashlightAvailabilityChanged(boolean available)
            {

            }
        });
    }

    private void setWifiState() {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int resId;

        if (manager.isWifiEnabled()) {
            resId = R.drawable.ic_signal_wifi_4_bar_black_24dp;
        } else {
            resId = R.drawable.ic_signal_wifi_off_black_24dp;
        }

        mView.setImageViewResource(R.id.wifi, resId);
        mManager.updateAppWidget(mIds, mView);

    }

    private void setSoundState() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int resId;

        switch (manager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                resId = R.drawable.ic_volume_mute_black_24dp;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                resId = R.drawable.ic_vibration_black_24dp;
                break;
            default:
                resId = R.drawable.ic_volume_up_black_24dp;
                break;
        }

        mView.setImageViewResource(R.id.sound, resId);
        mManager.updateAppWidget(mIds, mView);

    }

    private void setFlashlightState() {
        mFlashlightEnabled = controller.isEnabled();
        int resId;

        if (mFlashlightEnabled) {
            resId = R.drawable.ic_flash_on_black_24dp;
        } else {
            resId = R.drawable.ic_flash_off_black_24dp;
        }

        mView.setImageViewResource(R.id.flashlight, resId);
        mManager.updateAppWidget(mIds, mView);

    }

    private void setBluetoothState() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int resId;

        if (adapter.isEnabled()) {
            resId = R.drawable.ic_bluetooth_black_24dp;
        } else {
            resId = R.drawable.ic_bluetooth_disabled_black_24dp;
        }

        mView.setImageViewResource(R.id.bluetooth, resId);
        mManager.updateAppWidget(mIds, mView);

    }

    private void setAirplaneState() {
        boolean enabled = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        int resId;

        if (enabled) {
            resId = R.drawable.ic_airplanemode_active_black_24dp;
        } else {
            resId = R.drawable.ic_airplanemode_inactive_black_24dp;
        }

        mView.setImageViewResource(R.id.airplane, resId);
        mManager.updateAppWidget(mIds, mView);

    }

    private void setWifiColor() {
        Log.e(TAG, "setWifiColor()");
        mView.setInt(R.id.wifi, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), WIFI_COLOR, Color.WHITE));
        mManager.updateAppWidget(mIds, mView);

    }

    private void setSoundColor() {
        Log.e(TAG, "setSoundColor()");
        mView.setInt(R.id.sound, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), SOUND_COLOR, Color.WHITE));
        mManager.updateAppWidget(mIds, mView);

    }

    private void setFlashlightColor() {
        Log.e(TAG, "setFlashlightColor()");
        mView.setInt(R.id.flashlight, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), FLASHLIGHT_COLOR, Color.WHITE));
        mManager.updateAppWidget(mIds, mView);

    }

    private void setBluetoothColor() {
        Log.e(TAG, "setBluetoothColor()");
        mView.setInt(R.id.bluetooth, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), BT_COLOR, Color.WHITE));
        mManager.updateAppWidget(mIds, mView);

    }

    private void setAirplaneColor() {
        Log.e(TAG, "setAirplaneColor()");
        mView.setInt(R.id.airplane, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), AIRPLANE_COLOR, Color.WHITE));
        mManager.updateAppWidget(mIds, mView);

    }
}
