package io.github.z3ntu.remoterobot.buttons;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by luca on 24.05.15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RemoteRobotPi remoteRobotPi;

    public void setRemoteRobotPi(RemoteRobotPi remoteRobotPi) {
        this.remoteRobotPi = remoteRobotPi;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(io.github.z3ntu.remoterobot.R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setBackgroundColor(Color.WHITE);
        getView().setClickable(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "control_addr":
                RemoteRobotPi.CONTROL_URL = sharedPreferences.getString("control_addr", "httpbin.org/get");
                break;
            case "distance_addr":
                RemoteRobotPi.DISTANCE_URL = sharedPreferences.getString("distance_addr", "httpbin.org/ip");
                break;
            case "distance":
                if (RemoteRobotPi.UPDATE_DISTANCE) {
                    remoteRobotPi.timer.cancel();
                } else {
                    remoteRobotPi.timer = new Timer();
                    remoteRobotPi.timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            remoteRobotPi.updateDistance();
                        }
                    }, 0, 1000);
                }
                RemoteRobotPi.UPDATE_DISTANCE = sharedPreferences.getBoolean("distance", false);

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
