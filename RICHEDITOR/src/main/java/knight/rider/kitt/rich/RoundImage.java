package knight.rider.kitt.rich;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

class RoundImage extends RelativeLayout {

    private int bgColor = 0xffff0000;

    public RoundImage(Context context) {
        this(context, null);
    }

    public RoundImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundImage);
        bgColor = ta.getColor(R.styleable.RoundImage_temp_round_color, bgColor);
        ta.recycle();

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.kitt_rich_round_layout, this, true);

        this.findViewById(R.id.kitt_rich_round_img).setBackgroundColor(bgColor);
    }


    public int getBgColor() {
        return bgColor;
    }
}

