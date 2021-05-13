package knight.rider.kitt.rich;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.richeditor.RichEditor;

public class XRichTextEditor extends FrameLayout implements View.OnClickListener {

    private Context mContext;

    private RichEditor mEditor;
    private LinearLayout mLayout;
    private FrameLayout mRootLayout;

    private FrameLayout xBack;
    private FrameLayout xGo;
    private FrameLayout xBold;
    private FrameLayout xUnderLine;
    private FrameLayout xItalic;
    private FrameLayout xSize;
    private FrameLayout xColor;


    private PopupWindow mPopupWindow;
    private PopupWindow mPopupWindow2;
    private View cancle;
    private View cancle2;
    private int screenHeight;
    private String htmlText;
    private RichPicIconClickListener listener;

    private boolean isLoadComplete = false;
    private boolean isRevised = false;

    private int RICK_LAYOUT_HEIGHT = 0;

    private SoftKeyBroadManager mKeyboardListener;

    public XRichTextEditor(Context context) {
        this(context, null);
    }

    public XRichTextEditor(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);


    }

    public XRichTextEditor(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;


        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // 获取屏幕像素高
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
        float density = dm.density;       // 屏幕密度（像素比例：0.75/1.0/1.5/2.0,3.0,4等）

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.kitt_rich_text_editor, this, true);
        mLayout = ((LinearLayout) this.findViewById(R.id.kitt_rich_layout));
        mRootLayout = ((FrameLayout) this.findViewById(R.id.rootLayout));

        // 编辑器
        mEditor = ((RichEditor) this.findViewById(R.id.kitt_rich_editor));
        mEditor.setFontSize(3);
        mEditor.setEditorFontColor(Color.parseColor("#999999"));

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.XRichTextEditor);
        float padding = ta.getDimension(R.styleable.XRichTextEditor_x_rich_padding, 0);
        float paddingLeft = ta.getDimension(R.styleable.XRichTextEditor_x_rich_paddingLeft, 0);
        float paddingRight = ta.getDimension(R.styleable.XRichTextEditor_x_rich_paddingRight, 0);
        float paddingTop = ta.getDimension(R.styleable.XRichTextEditor_x_rich_paddingTop, 0);
        float paddingBottom = ta.getDimension(R.styleable.XRichTextEditor_x_rich_paddingBottom, 0);

        if (ta.getText(R.styleable.XRichTextEditor_x_rich_padding) != null && ta.getText(R.styleable.XRichTextEditor_x_rich_padding).toString().endsWith("dip")) {
            padding = padding / density;
        }

        if (ta.getText(R.styleable.XRichTextEditor_x_rich_paddingLeft) != null && ta.getText(R.styleable.XRichTextEditor_x_rich_paddingLeft).toString().endsWith("dip")) {
            paddingLeft = paddingLeft / density;
        }

        if (ta.getText(R.styleable.XRichTextEditor_x_rich_paddingRight) != null && ta.getText(R.styleable.XRichTextEditor_x_rich_paddingRight).toString().endsWith("dip")) {
            paddingRight = paddingRight / density;
        }

        if (ta.getText(R.styleable.XRichTextEditor_x_rich_paddingTop) != null && ta.getText(R.styleable.XRichTextEditor_x_rich_paddingTop).toString().endsWith("dip")) {
            paddingTop = paddingTop / density;
        }

        if (ta.getText(R.styleable.XRichTextEditor_x_rich_paddingBottom) != null && ta.getText(R.styleable.XRichTextEditor_x_rich_paddingBottom).toString().endsWith("dip")) {
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
        mEditor.focusEditor();

        mEditor.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress != 100) {
                    isLoadComplete = false;
                } else {
                    isLoadComplete = true;
                }
            }
        });


        // 初始化popwindow
        initPopWindow();
        // 初始化popwindow2
        initPopWindow2();
        // 初始化回退以及前进
        initBackAndGo();
        // 初始化字体样式1
        initFontStyle();
        // 初始化字体样式2
        initFontStyle2();


        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {

                htmlText = text;
                isRevised = true;
            }
        });

        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mLayout.setVisibility(b ? VISIBLE : GONE);
            }
        });


        mKeyboardListener = new SoftKeyBroadManager(mContext, this).addSoftKeyboardStateListener(new SoftKeyBroadManager.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx, int heightVisible, int statusBarHeight, int navigationBarHeight) {
                ViewGroup.LayoutParams layoutParams = mRootLayout.getLayoutParams();
                layoutParams.height = RICK_LAYOUT_HEIGHT - keyboardHeightInPx;
                mRootLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void onSoftKeyboardClosed(int heightVisible, int statusBarHeight, int navigationBarHeight) {
                ViewGroup.LayoutParams layoutParams = mRootLayout.getLayoutParams();
                layoutParams.height = RICK_LAYOUT_HEIGHT;
                mRootLayout.setLayoutParams(layoutParams);
                mLayout.setVisibility(VISIBLE);
            }

        });


        this.post(new Runnable() {
            @Override
            public void run() {
                RICK_LAYOUT_HEIGHT = mRootLayout.getHeight();
            }
        });

    }


    private void initPopWindow() {

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.kitt_rich_font_size_layout, null, false);
        mPopupWindow = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        contentView.findViewById(R.id.kitt_rich_1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(1);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(2);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(3);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_4).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(4);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_5).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(5);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_7).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(7);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.kitt_rich_6).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(6);
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.kitt_rich_cancle1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.kitt_rich_cancle2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.kitt_rich_cancle3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });

        cancle = contentView.findViewById(R.id.kitt_rich_cancle4);
        cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
            }
        });
    }


    private void initPopWindow2() {

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.kitt_rich_font_color_layout, null, false);
        mPopupWindow2 = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        mPopupWindow2.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        contentView.findViewById(R.id.kitt_rich_round1).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round2).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round3).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round4).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round5).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round6).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round7).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round8).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round9).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round10).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round11).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round12).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round13).setOnClickListener(this);
        contentView.findViewById(R.id.kitt_rich_round14).setOnClickListener(this);


        contentView.findViewById(R.id.kitt_rich_cancle1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow2 != null)
                    mPopupWindow2.dismiss();
            }
        });

        cancle2 = contentView.findViewById(R.id.kitt_rich_cancle4);
        cancle2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow2 != null)
                    mPopupWindow2.dismiss();
            }
        });
    }


    private void initBackAndGo() {

        // 撤销
        xBack = ((FrameLayout) this.findViewById(R.id.kitt_rich_back));
        xBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });


        // 恢复
        xGo = ((FrameLayout) this.findViewById(R.id.kitt_rich_go));
        xGo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });
    }


    private void initFontStyle() {
        // 加粗
        xBold = ((FrameLayout) this.findViewById(R.id.kitt_rich_bold));
        xBold.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });
        // 下滑线
        xUnderLine = ((FrameLayout) this.findViewById(R.id.kitt_rich_underLine));
        xUnderLine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();

            }
        });
        // 斜体
        xItalic = ((FrameLayout) this.findViewById(R.id.kitt_rich_italic));
        xItalic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });
    }

    private void initFontStyle2() {

        // 字体大小
        xSize = ((FrameLayout) this.findViewById(R.id.kitt_rich_size));
        xSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                mLayout.getLocationOnScreen(location);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cancle.getLayoutParams();

                if (screenHeight - location[1] == dp2px(mContext, 44)) {
                    params.height = dp2px(mContext, 48);
                } else {
                    params.height = screenHeight - location[1] + dp2px(mContext, 4);
                }

                mPopupWindow.showAtLocation(mLayout, Gravity.NO_GRAVITY, 0, 0);
            }
        });


        // 字体颜色
        xColor = ((FrameLayout) this.findViewById(R.id.kitt_rich_color));
        xColor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int[] location = new int[2];
                mLayout.getLocationOnScreen(location);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cancle2.getLayoutParams();

                if (screenHeight - location[1] == dp2px(mContext, 44)) {
                    params.height = dp2px(mContext, 48);
                } else {
                    params.height = screenHeight - location[1] + dp2px(mContext, 4);
                }

                mPopupWindow2.showAtLocation(mLayout, Gravity.NO_GRAVITY, 0, 0);
            }
        });


        // 拍照
        this.findViewById(R.id.kitt_rich_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.clickPicIcon();
            }
        });
    }

    private static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public boolean isRevisedTextContent() {
        return isRevised;
    }

    public void setListener(RichPicIconClickListener listener) {
        this.listener = listener;
    }

    /**
     * TODO 转换Email回复格式
     *
     * @param quotations  引用的String eg. 发件人：1334@qq.com\n收件人:xxx@qq.com\n日期:...
     * @param contentHtml 具体邮件的内容html
     */
    public static String covertEmailHtml(String quotations, String contentHtml) {
        quotations = quotations.replaceAll("\\\\n", "<br>");
        quotations = quotations.replaceAll("\n", "<br>");
        quotations = quotations.replaceAll("\\\\r", "<br>");
        quotations = quotations.replaceAll("\r", "<br>");
        quotations = "<br><br><br><br><font color=\"#282828\" size=\"2\"><b>--原始内容--<br><br></b></font><span style=\"background-color: #F1F1F1;width:calc(100% - 24px);display:-moz-inline-box;display:inline-block;border-radius:5px;padding:10px 5px 15px 20px;line-height:23px;font-size:13px;\">" + quotations;
        quotations = quotations + "</span><br><br><br><br>";
        quotations = quotations + contentHtml;

        return quotations;
    }

    // TODO 转换超文本转换为普通文字
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

    @Override
    public void onClick(View v) {

        if (mPopupWindow2 != null)
            mPopupWindow2.dismiss();

        mEditor.setTextColor(((RoundImage) v).getBgColor());
    }

    public void insertImage(String url) {
        mEditor.insertImage(url, "pic\" style=\"max-width:98%");
    }

    public void insertImage(String url, String alt) {
        mEditor.insertImage(url, alt);
    }

    public String getHtmlText() {
        return htmlText;
    }

    public boolean isLoadComplete() {
        return isLoadComplete;
    }

    public boolean isShowPop() {

        return mPopupWindow.isShowing() || mPopupWindow2.isShowing();
    }

    public void closePop() {

        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        if (mPopupWindow2 != null && mPopupWindow2.isShowing()) {
            mPopupWindow2.dismiss();
        }
    }


    public void setContent(String htmlText) {

        if (htmlText == null)
            htmlText = "";

        htmlText = changeImgWidth(htmlText);

        mEditor.setHtml(htmlText);
    }

    /**
     * @param htmlContent 原来的html
     * @return 改变img标签宽度以后的html
     */
    private String changeImgWidth(String htmlContent) {
        Document doc_Dis = Jsoup.parse(htmlContent);
        Elements ele_Img = doc_Dis.getElementsByTag("img");
        if (ele_Img.size() != 0) {
            for (Element e_Img : ele_Img) {
                String maxWidth = e_Img.attributes().get("max-width");
                if (maxWidth.length() > 0) {
                    String[] split = maxWidth.split("%");
                    try {
                        int i = Integer.parseInt(split[0]);
                        if (i > 98) {
                            e_Img.attr("style", "max-width:98%");
                        }
                    } catch (Exception e) {
                        Log.e(CoreWebView.class.getSimpleName(), "转换Img标签最大宽度", e);
                    }

                } else {
                    e_Img.attr("style", "max-width:98%");
                }
            }
        }
        return doc_Dis.toString();
    }


    // TODO 生命周期进行调用
    public void onResume() {

        if (mKeyboardListener == null)
            return;

        final Rect r = new Rect();
        this.getRootView().getWindowVisibleDisplayFrame(r);

        int height = getRootView().getHeight();


        // 不可见区域
        final int rootHeightDiff = height - (r.bottom - r.top);
        // 可见区域
        int rootHeightVisible = height - rootHeightDiff;

        if (rootHeightDiff > 500) {
            // 当前键盘开启中

            if (this.getHeight() < RICK_LAYOUT_HEIGHT) {
                // 当前控件高度 < 全展开高度时 说明当前布局为键盘开启状态
                mKeyboardListener.setIsSoftKeyboardOpened(true);
            } else {
                // 当前控件为关闭键盘状态
                mKeyboardListener.setIsSoftKeyboardOpened(true);


                ViewGroup.LayoutParams layoutParams = mRootLayout.getLayoutParams();
                layoutParams.height = RICK_LAYOUT_HEIGHT - mKeyboardListener.getRealKeyBoardHeight();
                mRootLayout.setLayoutParams(layoutParams);
            }

        } else {
            // 当前键盘关闭中

            if (this.getHeight() < RICK_LAYOUT_HEIGHT) {
                // 当前控件高度 < 全展开高度时 说明当前布局为键盘开启状态
                mKeyboardListener.setIsSoftKeyboardOpened(false);

                ViewGroup.LayoutParams layoutParams = mRootLayout.getLayoutParams();
                layoutParams.height = RICK_LAYOUT_HEIGHT;
                mRootLayout.setLayoutParams(layoutParams);
                mLayout.setVisibility(VISIBLE);

            } else {
                // 当前控件为关闭键盘状态
                mKeyboardListener.setIsSoftKeyboardOpened(false);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (isShowPop()) {
                closePop();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isShowPop()) {
            closePop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
