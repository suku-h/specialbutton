package in.jiyofit.specialbutton;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.Button;

import java.util.ArrayList;

public class RoundedButton extends Button {
    private Context ctx;
    private GradientDrawable defaultDrawable;
    private int buttonWidth;
    private int buttonHeight;
    private int usableButtonWidth;
    private int usableButtonHeight;
    private float aspectRatio;
    // # Background Attributes
    private int mDefaultBackgroundColor = Color.TRANSPARENT;
    private int mFocusBackgroundColor = Color.LTGRAY;

    // # Icon Attributes
    private Drawable mIconResource = null;
    private int mIconPosition = 1;

    private int mButtonPaddingLeft = 0;
    private int mButtonPaddingRight = 0;
    private int mButtonPaddingTop = 0;
    private int mButtonPaddingBottom = 0;

    private int mDistanceTextFromIcon = 0;

    private int mBorderColor = Color.TRANSPARENT;
    private int mBorderThickness = 0;

    private int mRadius = 0;

    public RoundedButton(Context context) {
        super(context);
        ctx = context;
    }

    public RoundedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.RoundedButton, 0, 0);
        initAttributesArray(attrsArray);
        attrsArray.recycle();
        initializeRoundedButton();
    }

    private void initAttributesArray(TypedArray attrsArray) {

        mDefaultBackgroundColor = attrsArray.getColor(R.styleable.RoundedButton_defaultColor, mDefaultBackgroundColor);
        mFocusBackgroundColor = attrsArray.getColor(R.styleable.RoundedButton_focusColor, mFocusBackgroundColor);

        // if default color is set then the icon's color is the same (the default for icon's color)
        mBorderThickness = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_borderThickness, mBorderThickness);
        mBorderColor = attrsArray.getColor(R.styleable.RoundedButton_borderColor, mBorderColor);

        mRadius = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_radius, mRadius);

        mButtonPaddingLeft = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_iconPaddingLeft, mButtonPaddingLeft);
        mButtonPaddingRight = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_iconPaddingRight, mButtonPaddingRight);
        mButtonPaddingTop = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_iconPaddingTop, mButtonPaddingTop);
        mButtonPaddingBottom = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_iconPaddingBottom, mButtonPaddingBottom);
        mIconPosition = attrsArray.getInt(R.styleable.RoundedButton_iconPosition, mIconPosition);

        mDistanceTextFromIcon = attrsArray.getDimensionPixelSize(R.styleable.RoundedButton_distanceTextFromIcon, mDistanceTextFromIcon);
    }

    private void initializeRoundedButton() {
        this.setBackgroundColor(Color.TRANSPARENT);
        //need to setBackground otherwise getBackground will not work even if it is put in attrs
        this.setBackground(ContextCompat.getDrawable(ctx, R.drawable.button_border));
        defaultDrawable = (GradientDrawable) this.getBackground().mutate();
        setFocusDrawable();

        this.setDefaultBackgroundColor(mDefaultBackgroundColor);
        this.setFocusable(true);
        this.setClickable(true);
        this.setAllCaps(false);

        if(mRadius != 0){
            setRadius(mRadius);
        }
    }

    public void setAutoscaledIcon(final RoundedButton rb, final int drawableResourceId, final int iconPosition) {
        mIconPosition = iconPosition;
        removeIcon();

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();

                mIconResource = ContextCompat.getDrawable(getContext(), drawableResourceId);
                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                usableButtonWidth = buttonWidth - mButtonPaddingLeft - mButtonPaddingRight;
                usableButtonHeight = buttonHeight - mButtonPaddingTop - mButtonPaddingBottom;

                int finalUsableButtonWidth;
                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        //drawableleft & drawableRight always has gravity center_vertical
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, buttonHeight);
                        mIconResource.setBounds(0, 0, finalUsableButtonWidth, (int) (finalUsableButtonWidth / aspectRatio));
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, usableButtonHeight);
                        mIconResource.setBounds(0, 0, finalUsableButtonWidth, (int) (finalUsableButtonWidth / aspectRatio));
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        //drawableTop & drawableBottom always has gravity center_vertical
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setAutoscaledIcon(final RoundedButton rb, Bitmap bitmap, final int iconPosition) {
        mIconResource = new BitmapDrawable(getResources(), bitmap);
        mIconPosition = iconPosition;

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();

                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                usableButtonWidth = buttonWidth - mButtonPaddingLeft - mButtonPaddingRight;
                usableButtonHeight = buttonHeight - mButtonPaddingTop - mButtonPaddingBottom;

                int finalUsableButtonWidth;
                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        //drawableleft & drawableRight always has gravity center_vertical
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, usableButtonHeight);
                        mIconResource.setBounds(0, 0, finalUsableButtonWidth, (int) (finalUsableButtonWidth / aspectRatio));
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, usableButtonHeight);
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        //drawableTop & drawableBottom always has gravity center_vertical
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setAutoscaledIcon(final RoundedButton rb, Drawable drawable, final int iconPosition) {
        mIconResource = drawable;
        mIconPosition = iconPosition;

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();

                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                usableButtonWidth = buttonWidth - mButtonPaddingLeft - mButtonPaddingRight;
                usableButtonHeight = buttonHeight - mButtonPaddingTop - mButtonPaddingBottom;;

                int finalUsableButtonWidth;
                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        //drawableleft & drawableRight always has gravity center_vertical
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, usableButtonHeight);
                        mIconResource.setBounds(0, 0, finalUsableButtonWidth, (int) (finalUsableButtonWidth / aspectRatio));
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        finalUsableButtonWidth = Math.min(usableButtonWidth - getTextWidth() - mDistanceTextFromIcon, usableButtonHeight);
                        mIconResource.setBounds(0, 0, finalUsableButtonWidth, (int) (finalUsableButtonWidth / aspectRatio));
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        //drawableTop & drawableBottom always has gravity center_vertical
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        usableButtonHeight = Math.min(usableButtonHeight - getTextHeight() - mDistanceTextFromIcon, usableButtonWidth);
                        mIconResource.setBounds(0, 0, (int) (usableButtonHeight * aspectRatio), usableButtonHeight);
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setIconWithScaling(final RoundedButton rb, int  drawableResourceId, final int scalePercent, int iconPosition){
        mIconResource = ContextCompat.getDrawable(getContext(), drawableResourceId);
        mIconPosition = iconPosition;

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();
                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                if(buttonWidth > buttonHeight){
                    usableButtonHeight = (int)((float) (scalePercent * buttonHeight) / (float) 100);
                    usableButtonWidth =  (int) (usableButtonHeight * aspectRatio);
                } else {
                    usableButtonWidth = (int)((float) (scalePercent * buttonWidth) / (float) 100);
                    usableButtonHeight =  (int) (usableButtonWidth * aspectRatio);
                }

                mIconResource.setBounds(0, 0, usableButtonWidth , usableButtonHeight);

                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setIconWithScaling(final RoundedButton rb, Bitmap bitmap, final int scalePercent, int iconPosition){
        mIconResource = new BitmapDrawable(getResources(), bitmap);
        mIconPosition = iconPosition;

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();
                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                if(buttonWidth > buttonHeight){
                    usableButtonHeight = (int)((float) (scalePercent * buttonHeight) / (float) 100);
                    usableButtonWidth =  (int) (usableButtonHeight * aspectRatio);
                } else {
                    usableButtonWidth = (int)((float) (scalePercent * buttonWidth) / (float) 100);
                    usableButtonHeight =  (int) (usableButtonWidth * aspectRatio);
                }

                mIconResource.setBounds(0, 0, usableButtonWidth , usableButtonHeight);

                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setIconWithScaling(final RoundedButton rb, Drawable drawable, final int scalePercent, int iconPosition){
        mIconResource = drawable;
        mIconPosition = iconPosition;

        rb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonWidth = rb.getWidth();
                buttonHeight = rb.getHeight();
                int drawableWidth = mIconResource.getIntrinsicWidth();
                int drawableHeight = mIconResource.getIntrinsicHeight();
                aspectRatio = (float) drawableWidth / (float) drawableHeight;

                if(buttonWidth > buttonHeight){
                    usableButtonHeight = (int)((float) (scalePercent * buttonHeight) / (float) 100);
                    usableButtonWidth =  (int) (usableButtonHeight * aspectRatio);
                } else {
                    usableButtonWidth = (int)((float) (scalePercent * buttonWidth) / (float) 100);
                    usableButtonHeight =  (int) (usableButtonWidth * aspectRatio);
                }

                mIconResource.setBounds(0, 0, usableButtonWidth , usableButtonHeight);

                switch (mIconPosition) {
                    case ICON_POSITION.LEFT:
                        rb.setCompoundDrawables(mIconResource, null, null, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.RIGHT:
                        rb.setCompoundDrawables(null, null, mIconResource, null);
                        rb.setGravity(Gravity.CENTER_VERTICAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.TOP:
                        rb.setCompoundDrawables(null, mIconResource, null, null);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                    case ICON_POSITION.BOTTOM:
                        rb.setCompoundDrawables(null, null, null, mIconResource);
                        rb.setGravity(Gravity.CENTER_HORIZONTAL);
                        rb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        break;
                }

                rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
                rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
            }
        });
    }

    public void setIconWithDimensions(int  drawableResourceId, int iconWidthDp, int iconHeightDp, int iconPosition){
        mIconResource = ContextCompat.getDrawable(getContext(), drawableResourceId);
        mIconResource.setBounds(0, 0, Utils.dpToPx(ctx, iconWidthDp) , Utils.dpToPx(ctx, iconHeightDp));

        mIconPosition = iconPosition;
        switch (mIconPosition) {
            case ICON_POSITION.LEFT:
                this.setCompoundDrawables(mIconResource, null, null, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.RIGHT:
                this.setCompoundDrawables(null, null, mIconResource, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.TOP:
                this.setCompoundDrawables(null, mIconResource, null, null);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case ICON_POSITION.BOTTOM:
                this.setCompoundDrawables(null, null, null, mIconResource);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
        }
        this.setCompoundDrawablePadding(mDistanceTextFromIcon);
        this.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
    }

    public void setIconWithDimensions(Bitmap bitmap, int iconWidthDp, int iconHeightDp, int iconPosition){
        mIconResource = new BitmapDrawable(getResources(), bitmap);
        mIconResource.setBounds(0, 0, Utils.dpToPx(ctx, iconWidthDp) , Utils.dpToPx(ctx, iconHeightDp));

        mIconPosition = iconPosition;
        switch (mIconPosition) {
            case ICON_POSITION.LEFT:
                this.setCompoundDrawables(mIconResource, null, null, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.RIGHT:
                this.setCompoundDrawables(null, null, mIconResource, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.TOP:
                this.setCompoundDrawables(null, mIconResource, null, null);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case ICON_POSITION.BOTTOM:
                this.setCompoundDrawables(null, null, null, mIconResource);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
        }
        this.setCompoundDrawablePadding(mDistanceTextFromIcon);
        this.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
    }

    public void setIconWithDimensions(Drawable drawable, int iconWidthDp, int iconHeightDp, int iconPosition){
        mIconResource = drawable;
        mIconResource.setBounds(0, 0, Utils.dpToPx(ctx, iconWidthDp) , Utils.dpToPx(ctx, iconHeightDp));

        mIconPosition = iconPosition;
        switch (mIconPosition) {
            case ICON_POSITION.LEFT:
                this.setCompoundDrawables(mIconResource, null, null, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.RIGHT:
                this.setCompoundDrawables(null, null, mIconResource, null);
                this.setGravity(Gravity.CENTER_VERTICAL);
                break;
            case ICON_POSITION.TOP:
                this.setCompoundDrawables(null, mIconResource, null, null);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case ICON_POSITION.BOTTOM:
                this.setCompoundDrawables(null, null, null, mIconResource);
                this.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
        }
        this.setCompoundDrawablePadding(mDistanceTextFromIcon);
        this.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
    }

    public void removeIcon() {
        mIconResource = null;
        this.setGravity(Gravity.CENTER);
        this.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
        this.setCompoundDrawables(null, null, null, null);
    }

    private int getTextHeight() {
        if (this.getText().length() != 0) {
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(this.getTextSize());
            Typeface typeface = this.getTypeface();
            Typeface styledTypeface = Typeface.create(typeface, typeface.getStyle());
            textPaint.setTypeface(styledTypeface);
            StaticLayout staticLayout = new StaticLayout(this.getText(), textPaint, usableButtonWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            return staticLayout.getHeight();
        } else {
            return 0;
        }
    }

    private int getTextWidth() {
        if (this.getText().length() != 0) {
            Paint paint = new Paint();
            Rect bounds = new Rect();
            paint.setTextSize(this.getTextSize());
            Typeface typeface = this.getTypeface();
            Typeface styledTypeface = Typeface.create(typeface, typeface.getStyle());
            paint.setTypeface(styledTypeface);
            String buttonText = this.getText().toString();
            paint.getTextBounds(buttonText, 0, buttonText.length(), bounds);

            int availableWidth = usableButtonWidth - mDistanceTextFromIcon;

            if (bounds.width() > availableWidth) {
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(this.getTextSize());
                textPaint.setTypeface(styledTypeface);
                StaticLayout staticLayout = new StaticLayout(this.getText(), textPaint, availableWidth,
                        Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                int textLineCount = (int) Math.ceil(staticLayout.getHeight() / bounds.height());
                int multiLineTextWidth = availableWidth;
                while ((int) Math.ceil(staticLayout.getHeight() / bounds.height()) == textLineCount && multiLineTextWidth > 1) {
                    multiLineTextWidth--;
                    staticLayout = new StaticLayout(this.getText(), textPaint, multiLineTextWidth,
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                return multiLineTextWidth + 1;
            } else {
                return bounds.width() + 1;
            }
        } else {
            return 0;
        }
    }

    public void setButtonPadding(RoundedButton rb, int paddingLeftDp, int paddingTopDp, int paddingRightDp, int paddingBottomDp) {
        mButtonPaddingLeft = Utils.dpToPx(ctx, paddingLeftDp);
        mButtonPaddingTop = Utils.dpToPx(ctx, paddingTopDp);
        mButtonPaddingRight = Utils.dpToPx(ctx, paddingRightDp);
        mButtonPaddingBottom = Utils.dpToPx(ctx, paddingBottomDp);

        if (mIconResource != null) {
            setAutoscaledIcon(rb, mIconResource, mIconPosition);
        } else {
            rb.setPadding(mButtonPaddingLeft, mButtonPaddingTop, mButtonPaddingRight, mButtonPaddingBottom);
        }
    }

    public ArrayList<Integer> getButtonPadding() {
        ArrayList<Integer> result = new ArrayList<>();
        result.add(Utils.pxToDp(ctx, mButtonPaddingLeft));
        result.add(Utils.pxToDp(ctx, mButtonPaddingTop));
        result.add(Utils.pxToDp(ctx, mButtonPaddingRight));
        result.add(Utils.pxToDp(ctx, mButtonPaddingBottom));
        return result;
    }

    public void setDistanceTextFromIcon(RoundedButton rb, int distanceDp) {
        mDistanceTextFromIcon = Utils.dpToPx(ctx, distanceDp);
        if (mIconResource != null) {
            setAutoscaledIcon(rb, mIconResource, mIconPosition);
        } else {
            rb.setCompoundDrawablePadding(mDistanceTextFromIcon);
        }
    }

    public int getDistanceTextFromIcon() {
        return Utils.pxToDp(ctx, mDistanceTextFromIcon);
    }

    public void setRadius(int radiusDp) {
        mRadius = Utils.dpToPx(ctx, radiusDp);
        defaultDrawable.setCornerRadius(mRadius);
        defaultDrawable.invalidateSelf();
    }

    public int getRadius() {
        return Utils.pxToDp(ctx, mRadius);
    }

    public void setBorder(int borderThicknessDp, int borderColor) {
        mBorderThickness = Utils.dpToPx(ctx, borderThicknessDp);
        mBorderColor = borderColor;
        initializeRoundedButton();
        defaultDrawable.setStroke(mBorderThickness, mBorderColor);
        defaultDrawable.invalidateSelf();
    }

    public void removeBorder() {
        defaultDrawable.setStroke(0, mBorderColor);
        defaultDrawable.invalidateSelf();
    }

    public int getBorderThickness() {
        return Utils.pxToDp(ctx, mBorderThickness);
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setDefaultBackgroundColor(int defaultBackgroundColor) {
        mDefaultBackgroundColor = defaultBackgroundColor;
        if(defaultDrawable == null){
            initializeRoundedButton();
        }
        defaultDrawable.setColor(mDefaultBackgroundColor);
        defaultDrawable.invalidateSelf();
        //without this line the modified defaultDrawable is not set as background
        //if the button is pressed, the old drawable is seen, I don't know why
        this.setBackground(defaultDrawable);
    }

    public int getDefaultBackgroundColor() {
        return mDefaultBackgroundColor;
    }

    public void setFocusBackgroundColor(int focusBackgroundColor) {
        mFocusBackgroundColor = focusBackgroundColor;
        setFocusDrawable();
    }

    public int getFocusableBackgroundColor() {
        return mFocusBackgroundColor;
    }

    private void setFocusDrawable() {
        GradientDrawable disableDrawable = new GradientDrawable();
        disableDrawable.setColor(Color.LTGRAY);
        disableDrawable.setCornerRadius(mRadius);
        if (mBorderColor != 0) {
            disableDrawable.setStroke(mBorderThickness, Color.GRAY);
        }

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);

        if (mFocusBackgroundColor != 0) {
            GradientDrawable focusDrawable = new GradientDrawable();
            focusDrawable.setColor(mFocusBackgroundColor);
            focusDrawable.setCornerRadius(mRadius);
            if (mBorderColor != 0) {
                focusDrawable.setStroke(mBorderThickness, mBorderColor);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                states.addState(new int[]{android.R.attr.state_focused}, getRippleDrawable(defaultDrawable, focusDrawable));
                states.addState(new int[]{android.R.attr.state_pressed}, getRippleDrawable(defaultDrawable, focusDrawable));
            } else {
                states.addState(new int[]{android.R.attr.state_pressed}, focusDrawable);
                states.addState(new int[]{android.R.attr.state_focused}, focusDrawable);
            }
            states.addState(new int[]{}, defaultDrawable);
            this.setBackground(states);
        } else {
            states.addState(new int[]{}, defaultDrawable);
            this.setBackground(states);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable getRippleDrawable(Drawable defaultDrawable, Drawable focusDrawable) {
        return new RippleDrawable(ColorStateList.valueOf(mFocusBackgroundColor), defaultDrawable, focusDrawable);
    }

    public interface ICON_POSITION {
        int LEFT = 1;
        int RIGHT = 2;
        int TOP = 3;
        int BOTTOM = 4;
    }
}
