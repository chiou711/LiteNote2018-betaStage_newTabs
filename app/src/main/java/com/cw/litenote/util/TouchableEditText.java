package com.cw.litenote.util;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatEditText;

/**
 * Created by cw on 2018/2/6.
 */

public class TouchableEditText extends AppCompatEditText {

    public TouchableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        // do what you want
        return true;
    }
}
