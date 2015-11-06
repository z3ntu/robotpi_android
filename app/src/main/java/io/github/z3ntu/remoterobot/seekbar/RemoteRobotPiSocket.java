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


/*
        final Button auto = (Button) findViewById(R.id.auto);
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

//        SeekbarListener seekbarListener = new SeekbarListener(connectionHandler);

        VerticalSeekbar sr = (VerticalSeekbar) findViewById(R.id.rightSeekBar);
        sr.setOnSeekBarChangeListener(new SeekbarListener(connectionHandler, SeekbarListener.Side.RIGHT));

         /*new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(connectionHandler.isSocketActive()) {
                    Message message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, "FR" + (progress < 10 ? "0" + progress : progress) + '\r');
                    connectionHandler.sendMessage(message);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        }*/

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote_robot_pi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            final TextView mTextView = (TextView) findViewById(R.id.text);
//            Toast.makeText(getApplicationContext(),
//                    "Should open settings! ;)",
//                    Toast.LENGTH_LONG).show();
            showSettingsDialog();
            //TODO: SHOW PREFERENCES
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

/*        mFragmentTransaction.setCustomAnimations(R.anim.animation_test, 0);
        mFragmentTransaction.show(mSettingsFragment);*/
//        mFragmentTransaction.add(mSettingsFragment, "settings");
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

        request(Mode.DISCONNECT);
//        request(Side.EXIT);

//        Message message = Message.obtain(connectionHandler);
//        message.what = ConnectionHandler.MessageCode.CLASS_CONNECTION; // EventClass CONNECTION
//        message.arg1 = -1; // EventAction CONNECT
//
//        connectionHandler.sendMessage(message);
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
