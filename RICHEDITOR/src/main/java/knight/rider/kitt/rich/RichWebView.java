package knight.rider.kitt.rich;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichWebView extends FrameLayout {

    private final CoreWebView mEditor;

    private String htmlText = "";

    private OnClickListener onClickListener;

    public RichWebView(Context context) {
        this(context, null);
    }

    public RichWebView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public RichWebView(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        // 获取屏幕像素高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        float density = dm.density;       // 屏幕密度（像素比例：0.75/1.0/1.5/2.0,3.0,4等）

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.kitt_rich_web_view, this, true);
        mEditor = ((CoreWebView) this.findViewById(R.id.rootLayout));

        mEditor.setFontSize(3);
        mEditor.setEditorFontColor(Color.parseColor("#999999"));

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichWebView);
        float padding = ta.getDimension(R.styleable.RichWebView_ui_rich_content_padding, 0);
        float paddingLeft = ta.getDimension(R.styleable.RichWebView_ui_rich_content_paddingLeft, 0);
        float paddingRight = ta.getDimension(R.styleable.RichWebView_ui_rich_content_paddingRight, 0);
        float paddingTop = ta.getDimension(R.styleable.RichWebView_ui_rich_content_paddingTop, 0);
        float paddingBottom = ta.getDimension(R.styleable.RichWebView_ui_rich_content_paddingBottom, 0);

        if (ta.getText(R.styleable.RichWebView_ui_rich_content_padding) != null && ta.getText(R.styleable.RichWebView_ui_rich_content_padding).toString().endsWith("dip")) {
            padding = padding / density;
        }

        if (ta.getText(R.styleable.RichWebView_ui_rich_content_paddingLeft) != null && ta.getText(R.styleable.RichWebView_ui_rich_content_paddingLeft).toString().endsWith("dip")) {
            paddingLeft = paddingLeft / density;
        }

        if (ta.getText(R.styleable.RichWebView_ui_rich_content_paddingRight) != null && ta.getText(R.styleable.RichWebView_ui_rich_content_paddingRight).toString().endsWith("dip")) {
            paddingRight = paddingRight / density;
        }

        if (ta.getText(R.styleable.RichWebView_ui_rich_content_paddingTop) != null && ta.getText(R.styleable.RichWebView_ui_rich_content_paddingTop).toString().endsWith("dip")) {
            paddingTop = paddingTop / density;
        }

        if (ta.getText(R.styleable.RichWebView_ui_rich_content_paddingBottom) != null && ta.getText(R.styleable.RichWebView_ui_rich_content_paddingBottom).toString().endsWith("dip")) {
            paddingBottom = paddingBottom / density;
        }


        ta.recycle();

        if (padding != 0) {
            paddingLeft = padding;
            paddingRight = padding;
            paddingTop = padding;
            paddingBottom = padding;
        }

        mEditor.setPadding((int) paddingLeft, (int) paddingTop, (int) paddingRight, (int) paddingBottom);

        mEditor.setOnTouchListener(new OnTouchListener() {

            private long downTime = 0;

            private float downX = 0;
            private float downY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downTime = System.currentTimeMillis();
                        downX = motionEvent.getX();
                        downY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() < downTime + 200 && onClickListener != null) {
                            onClickListener.onClick(RichWebView.this);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float absMx = Math.abs(downX - motionEvent.getX());
                        float absMy = Math.abs(downY - motionEvent.getY());
                        if (absMx > 50 && absMy > 50) {
                            downTime = 0;//移动了
                        }
                        break;
                }

                return false;
            }
        });
    }


    /**
     * 设置点击的监听
     *
     * @param listener The callback that will run.
     */
    public final void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }


    /**
     * 设置webView无数据时显示的提示内容
     *
     * @param hint the text to be displayed when the text of the TextView is empty.
     */
    public final void setHint(String hint) {
        mEditor.setPlaceholder(hint);
    }


    /**
     * 转换Email回复格式
     *
     * @param quotations the quotations text. eg. 发件人：1334@qq.com\n收件人:xxx@qq.com\n日期:...
     * @param emailHtml  the email html text.
     */
    public static String covertEmailHtml(String quotations, String emailHtml) {
        quotations = quotations.replaceAll("\\\\n", "<br>");
        quotations = quotations.replaceAll("\n", "<br>");
        quotations = quotations.replaceAll("\\\\r", "<br>");
        quotations = quotations.replaceAll("\r", "<br>");
        quotations = "<br><br><br><br><font color=\"#282828\" size=\"2\"><b>--原始内容--<br><br></b></font><span style=\"background-color: #F1F1F1;width:calc(100% - 24px);display:-moz-inline-box;display:inline-block;border-radius:5px;padding:10px 5px 15px 20px;line-height:23px;font-size:13px;\">" + quotations;
        quotations = quotations + "</span><br><br><br><br>";
        quotations = quotations + emailHtml;

        return quotations;
    }


    /**
     * 转换超文本转换为普通文字
     *
     * @param htmlStr the html text.
     * @return the text without tag.
     */
    public static String convertHTMLToText(String htmlStr) {

        //先将换行符保留，然后过滤标签
        Pattern p_enter = Pattern.compile("<br/>", Pattern.CASE_INSENSITIVE);
        Matcher m_enter = p_enter.matcher(htmlStr);
        htmlStr = m_enter.replaceAll("\n");

        //过滤html标签
        Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        return m_html.replaceAll("");
    }

    /**
     * 获取WebView数据
     *
     * @return the html text.
     */
    public final String getHtmlText() {
        return htmlText;
    }


    /**
     * 设置WebView数据
     *
     * @param htmlText the html text.
     */
    public final void setContent(String htmlText) {

        if (htmlText == null)
            htmlText = "";

        htmlText = changeImgWidth(htmlText);

        mEditor.setHtml(htmlText);
        this.htmlText = htmlText;
    }

    // 图片不超过一屏幕
    private String changeImgWidth(String htmlContent) {
        Document doc_Dis = Jsoup.parse(htmlContent);
        Elements ele_Img = doc_Dis.getElementsByTag("img");
        if (ele_Img.size() != 0) {
            for (Element e_Img : ele_Img) {
                String maxWidth = e_Img.attributes().get("max-width");
                if (maxWidth.length() <= 0) {
                    e_Img.attr("style", "max-width:100%");
                }
            }
        }
        return doc_Dis.toString();
    }
}
