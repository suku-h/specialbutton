package in.jiyofit.specialbutton;

import android.content.Context;

public class Utils {

    public static int pxToSp(final Context context, final float px) {
        return Math.round(px / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static int spToPx(final Context context, final float sp) {
        return Math.round(sp * context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static int dpToPx(Context context, int dp){
        return (int)((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static int pxToDp(Context context, int px){
        return (int) ((px/context.getResources().getDisplayMetrics().density)+0.5);
    }
}
