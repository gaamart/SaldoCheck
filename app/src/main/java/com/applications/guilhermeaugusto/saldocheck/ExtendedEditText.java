/**
 * Created by guilhermeaugusto on 16/06/2014.
 */
package com.applications.guilhermeaugusto.saldocheck;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by guilhermeaugusto on 16/06/2014.
 */
public class ExtendedEditText extends EditText {

    public interface KeyPreImeListener { public void onKeyPreImeAccured(); }
    private KeyPreImeListener eventListener;
    private Paint mTestPaint;

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise();

    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public ExtendedEditText(Context context) {
        super(context);
        initialise();
    }

    public void setEventListener(KeyPreImeListener eventListener) {
        this.eventListener = eventListener;
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
    }

    private void refitText(String text, int textWidth)
    {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = 200;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dispatchKeyEvent(event);
            if (this.eventListener != null) {
                this.eventListener.onKeyPreImeAccured();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}