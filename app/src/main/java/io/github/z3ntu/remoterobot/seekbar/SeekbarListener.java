package io.github.z3ntu.remoterobot.seekbar;

import android.os.Message;
import android.widget.SeekBar;

import java.text.DecimalFormat;

/**
 * Created by luca on 19.06.15.
 */
public class SeekbarListener implements SeekBar.OnSeekBarChangeListener {

    private ConnectionHandler connectionHandler;
    private Side side;
    public SeekbarListener(ConnectionHandler connectionHandler, Side side) {
        this.connectionHandler = connectionHandler;
        this.side = side;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if(!fromUser)
//            return;
        if (connectionHandler.isSocketActive()) {
            Message message = null;
            String direction = "0";
            String value = "00";

            if (side != Side.SERVO) {
                if (progress < 99) {
                    direction = "B";
                    value = new DecimalFormat("00").format((100 - progress));
                } else if (progress == 100) {
                    direction = "F";
                    value = "00";
                } else if (progress > 100) {
                    direction = "F";
                    value = new DecimalFormat("00").format(progress - 100);
                }
            } else {
                value = new DecimalFormat("000").format(progress);
            }

            System.out.println(value);

            switch (side) {
                case LEFT:
//                message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, "FL" + (progress < 10 ? "0" + progress : progress) + '\r');
                    message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, direction + "L" + value + '\r');
                    break;
                case RIGHT:
//                    message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, "FR" + (progress < 10 ? "0" + progress : progress) + '\r');
                    message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, direction + "R" + value + '\r');
                    break;
                case SERVO:
                    message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, "Y" + value + '\r');
                    break;
            }
            connectionHandler.sendMessage(message);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        System.out.println("Progress: " + seekBar.getProgress());
//        if(seekBar.getProgress() > 90 && seekBar.getProgress() < 110){
//            System.out.println("reset to 100");
//            seekBar.setProgress(100);
//        }
    }

    public enum Side {
        LEFT, SERVO, RIGHT
    }
}
