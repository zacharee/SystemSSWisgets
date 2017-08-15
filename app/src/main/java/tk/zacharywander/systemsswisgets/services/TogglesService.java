package tk.zacharywander.systemsswisgets.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;

import tk.zacharywander.systemsswisgets.misc.FlashlightController;
import tk.zacharywander.systemsswisgets.misc.Values;
import tk.zacharywander.systemsswisgets.widgets.Toggles;

public class TogglesService extends IntentService
{
    public TogglesService() {
        super("TogglesService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(Values.TOGGLE_INTENT_ACTION)) {
                int extra = intent.getIntExtra(Values.TOGGLE_INTENT_ACTION, -2);

                switch (extra) {
                    case -2:
                        return;
                    case Values.WIFI_ID:
                        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                        wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
                        break;
                    case Values.SOUND_ID:
                        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
                        switch (am.getRingerMode()) {
                            case AudioManager.RINGER_MODE_SILENT:
                                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                break;
                            case AudioManager.RINGER_MODE_VIBRATE:
                                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                break;
                            case AudioManager.RINGER_MODE_NORMAL:
                                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                break;
                        }
                        break;
                    case Values.FLASHLIGHT_ID:
                        FlashlightController flashlightController = Toggles.controller;
                        flashlightController.setFlashlight(!flashlightController.isEnabled());
                        break;
                    case Values.BT_ID:
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled()) {
                            bluetoothAdapter.disable();
                        } else {
                            bluetoothAdapter.enable();
                        }
                        break;
                    case Values.AIRPLANE_ID:
                        final ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        boolean enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;

                        try {
                            Method setAirplaneMode = ConnectivityManager.class.getMethod("setAirplaneMode", boolean.class);
                            setAirplaneMode.invoke(mgr, !enabled);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }
}
