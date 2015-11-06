package io.github.z3ntu.remoterobot.seekbar;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.Timer;

import io.github.z3ntu.remoterobot.R;


public class RemoteRobotPiSocket extends AppCompatActivity {

    public static String CONTROL_URL;
    public static String DISTANCE_URL;
    public static boolean UPDATE_DISTANCE;
    public Timer timer;
    private boolean inSettings = false;

    private SharedPreferences sharedPrefs;
    private ConnectionHandler connectionHandler;
    private HandlerThread connectionHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        CONTROL_URL = sharedPrefs.getString("control_addr", "httpbin.org/get");
        DISTANCE_URL = sharedPrefs.getString("distance_addr", "httpbin.org/ip");
        UPDATE_DISTANCE = sharedPrefs.getBoolean("distance", false);

        setContentView(R.layout.activity_sliders);

        /*final Button auto = (Button) findViewById(R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Side.AUTO);
            }
        });*/

        connectionHandlerThread = new HandlerThread("ConnectionThread");
        connectionHandlerThread.start();

        connectionHandler = new ConnectionHandler(connectionHandlerThread.getLooper(), sharedPrefs.getString("raw_addr", null), getFragmentManager());

        Message connect_message = Message.obtain(connectionHandler);
        connect_message.what = ConnectionHandler.MessageCode.CLASS_CONNECTION; // EventClass CONNECTION
        connect_message.arg1 = ConnectionHandler.MessageCode.CONNECTION_CONNECT; // EventAction CONNECT

        connectionHandler.sendMessage(connect_message);

        VerticalSeekbar sr = (VerticalSeekbar) findViewById(R.id.rightSeekBar);
        sr.setOnSeekBarChangeListener(new SeekbarListener(connectionHandler, SeekbarListener.Side.RIGHT));

        VerticalSeekbar sl = (VerticalSeekbar) findViewById(R.id.leftSeekBar);
        sl.setOnSeekBarChangeListener(new SeekbarListener(connectionHandler, SeekbarListener.Side.LEFT));

        SeekBar servo = (SeekBar) findViewById(R.id.servo);
        servo.setOnSeekBarChangeListener(new SeekbarListener(connectionHandler, SeekbarListener.Side.SERVO));

        Button reset_speed = (Button) findViewById(R.id.reset_speed);
        reset_speed.setOnClickListener(new ButtonListener(sl, sr, servo, ButtonListener.Type.SPEED));

        Button reset_all = (Button) findViewById(R.id.reset_all);
        reset_all.setOnClickListener(new ButtonListener(sl, sr, servo, ButtonListener.Type.ALL));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remote_robot_pi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void request(Mode mode) {
        Message message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, mode.getCommand());
        connectionHandler.sendMessage(message);
    }

    public void showSettingsDialog() {
        if (inSettings)
            return;
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        SettingsFragment mSettingsFragment = new SettingsFragment();

        mSettingsFragment.setRemoteRobotPi(this);

        mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        mFragmentTransaction.replace(android.R.id.content, mSettingsFragment);

        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
        inSettings = true;
    }

    @Override
    public void onBackPressed() {
        System.out.println("onBackPressed");
        if (inSettings) {
            backFromSettingsFragment();
            return;
        }
        super.onBackPressed();
    }

    private void backFromSettingsFragment() {
        inSettings = false;
        getFragmentManager().popBackStack();
    }

    public void updateDistance() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connectionHandler.isSocketActive())
            request(Mode.DISCONNECT);
    }


    public enum Mode {
        FORWARD("F099"), BACKWARD("B099"), LEFT("FR99"), RIGHT("FL99"), ROTATE_LEFT("RL99"), ROTATE_RIGHT("RR99"), STOP("0000"), AUTO("AUTO"), DISCONNECT("C001"), EXIT("EXIT");

        private String command;

        Mode(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command + "\r";
        }
    }
}
