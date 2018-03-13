package com.cw.litenote.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by cw on 2018/2/6.
 */

public class TouchableEditText extends EditText {

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
