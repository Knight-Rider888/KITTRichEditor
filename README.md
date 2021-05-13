# KITTRichEditor

本库 在'jp.wasabeef:richeditor-android:1.2.2'基础上进行了简单的样式更改

一、添加依赖

`root build.gradle `
```
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```
`module build.gradle `
```
implementation 'com.github.fonuhuolian:XRichTextEditor:1.3.1'
```

二、xml

```
<!--可输入-->
<org.fonuhuolian.xrichtexteditor.XRichTextEditor
  android:id="@+id/editor"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:x_rich_padding="10dp"/>
```
```
<!--不可输入-->
<knight.rider.kitt.rich.RichWebView
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  app:ui_rich_content_padding="50dp"/>
```

三、代码

```
// ******可输入******
// 富文本导航栏点击图片按钮的事件
xEditor.setListener(new XRichPicIconClickListener() {
  @Override
  public void clickPicIcon() {
    // 根据实际情况选择图片的逻辑操作
    // 成功后调用方法插入图片
    xEditor.insertImage(url, "pic\" style=\"max-width:98%");
  }
);
// 设置富文本内容
xEditor.setContent();
// 富文本内容是否加载完成
xEditor.isLoadComplete();
// 获取富文本的html文本
xEditor.getHtmlText();
// 转换成Email样式的html文本
XRichTextEditor.covertEmailHtml(String quotations, String contentHtml);
// 超文本转换为普通文字
XRichTextEditor.convertHTMLToText(String htmlStr);
// 请一定在生命周期调用同名方法
@Override
protected void onResume() {
  super.onResume();
  editor.onResume();
}
```

```
// ******不可输入******

// 设置点击的监听
richWebview.setOnClickListener();
// 设置webView无数据时显示的提示内容
richWebview.setHint();
// 设置WebView数据
richWebview.setContent();
// 获取WebView数据
richWebview.getHtmlText();
// 转换成Email样式的html文本
richWebview.covertEmailHtml(String quotations, String emailHtml);
// 超文本转换为普通文字
richWebview.convertHTMLToText(String htmlStr);
```

四、感谢 `wasabeef`提供富文本库



