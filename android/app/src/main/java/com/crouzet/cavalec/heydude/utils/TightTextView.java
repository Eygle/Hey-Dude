package com.crouzet.cavalec.heydude.utils;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Johan on 04/03/2015.
 */
public class TightTextView extends TextView {
    private boolean hasMaxWidth;

    public TightTextView(Context context) {
        this(context, null, 0);
    }

    public TightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = getLayout();
        if (layout != null) {
            int width = (int) FloatMath.ceil(getMaxLineWidth(layout))
                    + getCompoundPaddingLeft() + getCompoundPaddingRight();
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }


    @Override
    public void setMaxWidth(int maxpixels) {
        super.setMaxWidth(maxpixels);
        hasMaxWidth = true;
    }

    @Override
    public void setMaxEms(int maxems) {
        super.setMaxEms(maxems);
        hasMaxWidth = true;
    }
}
