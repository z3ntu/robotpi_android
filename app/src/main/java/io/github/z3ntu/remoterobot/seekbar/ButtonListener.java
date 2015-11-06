package io.github.z3ntu.remoterobot.seekbar;

import android.view.View;
import android.widget.SeekBar;

/**
 * Created by luca on 24.06.15.
 */
public class ButtonListener implements View.OnClickListener {

    private Type type;
    private VerticalSeekbar sl;
    private VerticalSeekbar sr;
    private SeekBar servo;

    public enum Type {
        ALL, SPEED
    }

    public ButtonListener(VerticalSeekbar sl, VerticalSeekbar sr, SeekBar servo, Type type){
        this.sl = sl;
        this.sr = sr;
        this.servo = servo;
        this.type = type;
    }

    @Override
    public void onClick(View v) {
        switch(type){
            case ALL:
                sl.setProgress(100);
                sr.setProgress(100);
                servo.setProgress(0);
                break;
            case SPEED:
                sl.setProgress(100);
                sr.setProgress(100);
                break;
        }
    }
}
