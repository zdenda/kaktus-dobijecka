package eu.zkkn.android.kaktus;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class SemaphoreView extends AppCompatTextView {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OK, INFO, ERROR})
    public @interface Type {}
    public static final int OK = 0;
    public static final int INFO = 1;
    public static final int ERROR = 2;

    //keep colors synchronized with the @Type (maybe add test for size)
    private static final String[] COLORS = {"#4caf50", "#ffeb3b", "#f44336"};


    public SemaphoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setOk(@StringRes int resId) {
        setText(OK, resId);
    }

    public void setInfo(@StringRes int resId) {
        setText(INFO, resId);
    }

    public void setError(@StringRes int resId) {
        setText(ERROR, resId);
    }

    public void setText(@Type int type, @StringRes int resId) {
        setText(type, getContext().getResources().getText(resId));
    }

    public void setText(@Type int type, CharSequence text) {
        super.setText(Helper.formatHtml(
                "<font color=\"%1$s\">\u25CF</font> %2$s", COLORS[type%COLORS.length], text));
    }

}
