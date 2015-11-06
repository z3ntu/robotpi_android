package io.github.z3ntu.remoterobot.socket;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import io.github.z3ntu.remoterobot.R;

/**
 * Created by luca on 24.05.15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RemoteRobotPiSocket remoteRobotPi;

    public void setRemoteRobotPi(RemoteRobotPiSocket remoteRobotPi) {
        this.remoteRobotPi = remoteRobotPi;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
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
                RemoteRobotPiSocket.CONTROL_URL = sharedPreferences.getString("control_addr", "httpbin.org/get");
                break;
            case "distance_addr":
                RemoteRobotPiSocket.DISTANCE_URL = sharedPreferences.getString("distance_addr", "httpbin.org/ip");
                break;
            case "distance":
                if (RemoteRobotPiSocket.UPDATE_DISTANCE) {
                    remoteRobotPi.timer.cancel();
                } else {
                    // RemoteRobotPiSocket.timer = new Timer();
                    //RemoteRobotPiSocket.timer.scheduleAtFixedRate(new TimerTask() {
//                    @Override
//                    public void run() {
//                        remoteRobotPi.updateDistance();
//                    }
//                }, 0, 1000);
                }
                RemoteRobotPiSocket.UPDATE_DISTANCE = sharedPreferences.getBoolean("distance", false);

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
