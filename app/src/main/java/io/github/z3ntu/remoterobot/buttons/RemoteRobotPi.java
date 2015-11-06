package io.github.z3ntu.remoterobot.buttons;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Timer;
import java.util.TimerTask;


public class RemoteRobotPi extends AppCompatActivity {

    public Timer timer;

    private Vibrator vibrator;

    public static String CONTROL_URL;
    public static String DISTANCE_URL;
    public static boolean UPDATE_DISTANCE;

    private boolean inSettings = false;

    private SharedPreferences sharedPrefs;

    public enum Mode {
        FORWARD("FORWARD"), BACKWARD("BACKWARD"), LEFT("RIGHT"), RIGHT("LEFT"), ROTATE_LEFT("ROTATE_LEFT"), ROTATE_RIGHT("ROTATE_RIGHT"), STOP("RESET"), AUTO("");

        private String command;

        Mode(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, io.github.z3ntu.remoterobot.R.xml.preferences, false);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        CONTROL_URL = sharedPrefs.getString("control_addr", "httpbin.org/get");
        DISTANCE_URL = sharedPrefs.getString("distance_addr", "httpbin.org/ip");
        UPDATE_DISTANCE = sharedPrefs.getBoolean("distance", false);

        setContentView(io.github.z3ntu.remoterobot.R.layout.activity_remote_robot_pi);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        final Button stop = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.STOP);
            }
        });

        final Button forward = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.FORWARD);
            }
        });

        final Button backward = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.back);
        backward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.BACKWARD);
            }
        });

        final Button left = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.LEFT);
            }
        });

        final Button right = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.RIGHT);
            }
        });

        final Button rotate_left = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.rotate_left);
        rotate_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.ROTATE_LEFT);
            }
        });

        final Button rotate_right = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.rotate_right);
        rotate_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.ROTATE_RIGHT);
            }
        });

        final Button auto = (Button) findViewById(io.github.z3ntu.remoterobot.R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.AUTO);
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDistance();
            }
        }, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(io.github.z3ntu.remoterobot.R.menu.menu_remote_robot_pi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == io.github.z3ntu.remoterobot.R.id.action_settings) {
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

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + CONTROL_URL + "?parameter=" + mode.getCommand();
//        String url ="http://httpbin.org/get?parameter="+mode.getCommand();


// Request a string response from the provided CONTROL_URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Response is: " + response);
                System.out.println("RESPONSE!!! -------------------------------------------------");
                vibrator.vibrate(50);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("VOLLEYERROR!!! -------------------------------------------------");

                long[] pattern = {0, 100, 300, 100};
                vibrator.vibrate(pattern, -1);

                if (volleyError.networkResponse == null) {

                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                    errorDialogFragment.setText(volleyError.toString());
                    errorDialogFragment.show(getFragmentManager(), "error");

                    /*
                    if (volleyError.getClass().equals(TimeoutError.class)) {
                        // Show timeout error message
                        Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();
                    }
                    */

                } else {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                    errorDialogFragment.setText(
                            "Statuscode " + volleyError.networkResponse.statusCode + "\n" +
                                    "Content: " + new String(volleyError.networkResponse.data));
                    errorDialogFragment.show(getFragmentManager(), "error");
                }
/*
//                mTextView.setText(new String(error.networkResponse.data));
                mTextView.setText(volleyError.toString());
//                System.out.println(error.getMessage());
                TimeoutError timeoutError = (TimeoutError)volleyError;
                        NetworkResponse response = volleyError.networkResponse;

                    NetworkResponse networkResponse = volleyError.networkResponse;
                    if(networkResponse != null && networkResponse.data != null){
                        mTextView.append(networkResponse.statusCode+"");
                    } else {
                        mTextView.append("something is null");
                    }


                new ErrorDialogFragment().show(getFragmentManager(), "error");
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG);*/

            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
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
        System.out.println("updating distance");

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://" + DISTANCE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final TextView distanceView = (TextView) findViewById(io.github.z3ntu.remoterobot.R.id.distance);
                distanceView.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                timer.cancel();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("distance", false);
                editor.apply();
                if (volleyError.networkResponse == null) {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                    errorDialogFragment.setText(volleyError.toString());
                    errorDialogFragment.show(getFragmentManager(), "error");
                } else {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                    errorDialogFragment.setText(
                            "Statuscode " + volleyError.networkResponse.statusCode + "\n" +
                                    "Content: " + new String(volleyError.networkResponse.data));
                    errorDialogFragment.show(getFragmentManager(), "error");
                }
            }
        });
        queue.add(stringRequest);

        //            final TextView mTextView = (TextView) findViewById(R.id.text);

    }

}
