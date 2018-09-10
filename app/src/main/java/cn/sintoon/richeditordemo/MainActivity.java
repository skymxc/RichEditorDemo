package cn.sintoon.richeditordemo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.List;

import cn.sintoon.richeditordemo.listener.CompressListener;
import cn.sintoon.richeditordemo.listener.ExportListener;
import cn.sintoon.richeditordemo.task.CompressAsyncTask;
import cn.sintoon.richeditordemo.task.ExportHtmlAsyncTask;
import cn.sintoon.richeditordemo.util.MyGlideEngine;
import cn.sintoon.richeditordemo.util.SDCardUtils;
import cn.sintoon.richeditordemo.widget.ColorPickerView;
import cn.sintoon.richeditordemo.widget.RichEditor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_CODE_CHOOSE = 23;//定义请求码常量

    private static final String TAG_WAIT_FRAGMENT = "tag_wait_fragment";
    /**
     * 编辑器
     */
    protected RichEditor mEditor;

    /**
     * 图片
     */
    protected TextView mImage;

    /**
     * 加粗
     */
    protected ImageView mBold;

    /**
     * 颜色
     */
    protected ImageView mTextColor;

    /**
     * 预览按钮
     */
    protected TextView mPreView;
    /**
     * 有序
     */
    protected ImageView mListOL;

    /**
     * 无序
     */
    protected ImageView mListUL;

    /**
     * 下划线
     */
    protected ImageView mLean;

    /**
     * 倾斜
     */
    protected ImageView mItalic;
    /**
     * 字体左对齐
     */
    private ImageView mAlignLeft;
    /**
     * 字体右对齐
     */
    private ImageView mAlignRight;
    /**
     * 字体居中对齐
     */
    private ImageView mAlignCenter;
    /**
     * 字体缩进
     */
    private ImageView mIndent;
    /**
     * 字体较少缩进
     */
    private ImageView mOutdent;
    /**
     * 字体中划线（删除线）
     */
    private ImageView mStrikethrough;

    /**
     * 颜色选择
     */
    protected LinearLayout llColorView;
    /********************boolean开关**********************/
    //是否加粗
    boolean isClickBold = false;
    //是否正在执行动画
    boolean isAnimating = false;
    //是否按ol排序
    boolean isListOl = false;
    //是否按ul排序
    boolean isListUL = false;
    //是否下划线字体
    boolean isTextLean = false;
    //是否下倾斜字体
    boolean isItalic = false;
    //是否左对齐
    boolean isAlignLeft = false;
    //是否右对齐
    boolean isAlignRight = false;
    //是否中对齐
    boolean isAlignCenter = false;
    //是否缩进
    boolean isIndent = false;
    //是否较少缩进
    boolean isOutdent = false;
    //是否索引
    boolean isBlockquote = false;
    //字体中划线
    boolean isStrikethrough = false;
    //字体上标
    boolean isSuperscript = false;
    //字体下标
    boolean isSubscript = false;
    /********************变量**********************/
    //折叠视图的宽高
    private int mFoldedViewMeasureHeight;


    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
            }
        }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        mScreenWidth = metrics.widthPixels;
        Log.e(MainActivity.class.getSimpleName() + "", "onCreate->" + mScreenWidth + "," + mScreenHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEditor.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditor.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditor.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                String html = mEditor.getHtml();
                showProgress("正在导出");
                new ExportHtmlAsyncTask(new ExportListener() {
                    @Override
                    public void onError(Exception exception) {
                        hideProgress();
                        toast(exception.getMessage());
                    }

                    @Override
                    public void onComplete(File exportFile) {
                        hideProgress();
                        exported(exportFile);
                    }
                }).execute(html);
                break;
            case R.id.menu_import:
                importHtml();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void pasteHtml() {

    }

    //这个是为了测试能否再次编辑。
    private void importHtml() {
        File htmlMkdir = SDCardUtils.getHtmlMkdir();
        File[] files = htmlMkdir.listFiles();
        if (null != files && files.length > 0) {
            String[] list = htmlMkdir.list();
            for (String name : list) {
                Log.e(MainActivity.class.getSimpleName() + "", "importHtml->" + name);
            }
            readHtml(files[0]);
        } else {
            toast("最近没有导出过HTML文档");
        }
    }

    private void readHtml(File file) {
        String s = SDCardUtils.readFile(file);
        mEditor.setHtml(s);
    }

    protected void initView() {
        initEditor();
        initMenu();
        initColorPicker();
        initClickListener();
    }

    protected void initEditor() {
        mEditor = findViewById(R.id.rich_editor);
        mEditor.setEditorFontSize(18);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("请输入编辑内容");

//        mEditor.setBackground("http://blog.skymxc.com/images/user.jpg");

    }

    /**
     * 初始化菜单按钮
     */
    private void initMenu() {
        mBold = findViewById(R.id.button_bold);
        mTextColor = findViewById(R.id.button_text_color);
        llColorView = findViewById(R.id.ll_main_color);
        mPreView = findViewById(R.id.tv_main_preview);
        mImage = findViewById(R.id.button_image);
        mListOL = findViewById(R.id.button_list_ol);
        mListUL = findViewById(R.id.button_list_ul);
        mLean = findViewById(R.id.button_underline);
        mItalic = findViewById(R.id.button_italic);
        mAlignLeft = findViewById(R.id.button_align_left);
        mAlignRight = findViewById(R.id.button_align_right);
        mAlignCenter = findViewById(R.id.button_align_center);
        mIndent = findViewById(R.id.button_indent);
        mOutdent = findViewById(R.id.button_outdent);
        mStrikethrough = findViewById(R.id.action_strikethrough);
        getViewMeasureHeight();
    }

    /**
     * 获取控件的高度
     */
    private void getViewMeasureHeight() {
        //获取像素密度
        float mDensity = getResources().getDisplayMetrics().density;
        //获取布局的高度
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        llColorView.measure(w, h);
        int height = llColorView.getMeasuredHeight();
        mFoldedViewMeasureHeight = (int) (mDensity * height + 0.5);
    }

    private void initClickListener() {
        mBold.setOnClickListener(this);
        mTextColor.setOnClickListener(this);
        mPreView.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mListOL.setOnClickListener(this);
        mListUL.setOnClickListener(this);
        mLean.setOnClickListener(this);
        mItalic.setOnClickListener(this);
        mAlignLeft.setOnClickListener(this);
        mAlignRight.setOnClickListener(this);
        mAlignCenter.setOnClickListener(this);
        mIndent.setOnClickListener(this);
        mOutdent.setOnClickListener(this);
        mStrikethrough.setOnClickListener(this);
        findViewById(R.id.button_undo).setOnClickListener(this);
        findViewById(R.id.button_redo).setOnClickListener(this);
        findViewById(R.id.button_link).setOnClickListener(this);
    }

    /**
     * 初始化颜色选择器
     */
    private void initColorPicker() {
        ColorPickerView right = findViewById(R.id.cpv_main_color);
        right.setPosition(0, 0);
        right.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                mEditor.focusEditor();
//                mTextColor.setBackgroundColor(color);
                mEditor.setTextColor(color);
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_bold) {//字体加粗

            mEditor.setBold();
        } else if (id == R.id.button_text_color) {//设置字体颜色
            //如果动画正在执行,直接return,相当于点击无效了,不会出现当快速点击时,
            // 动画的执行和ImageButton的图标不一致的情况
            if (isAnimating) return;
            //如果动画没在执行,走到这一步就将isAnimating制为true , 防止这次动画还没有执行完毕的
            //情况下,又要执行一次动画,当动画执行完毕后会将isAnimating制为false,这样下次动画又能执行
            isAnimating = true;

            if (llColorView.getVisibility() == View.GONE) {
                //打开动画
                animateOpen(llColorView);
            } else {
                //关闭动画
                animateClose(llColorView);
            }
        } else if (id == R.id.button_image) {//插入图片
            //这里的功能需要根据需求实现，通过insertImage传入一个URL或者本地图片路径都可以，这里用户可以自己调用本地相
            //或者拍照获取图片，传图本地图片路径，也可以将本地图片路径上传到服务器（自己的服务器或者免费的七牛服务器），
            //返回在服务端的URL地址，将地址传如即可（我这里传了一张写死的图片URL，如果你插入的图片不现实，请检查你是否添加
            // 网络请求权限<uses-permission android:name="android.permission.INTERNET" />）
//            mEditor.insertImage("http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
//                    "图片");
            callGallery();
        } else if (id == R.id.button_list_ol) {

            mEditor.focusEditor();
            mEditor.setNumbers();
        } else if (id == R.id.button_list_ul) {

            mEditor.setBullets();
        } else if (id == R.id.button_underline) {

            mEditor.setUnderline();
        } else if (id == R.id.button_italic) {

            mEditor.setItalic();
        } else if (id == R.id.button_align_left) {

            mEditor.setAlignLeft();
        } else if (id == R.id.button_align_right) {

            mEditor.setAlignRight();
        } else if (id == R.id.button_align_center) {

            mEditor.setAlignCenter();
        } else if (id == R.id.button_indent) {

            mEditor.focusEditor();
            mEditor.setIndent();
        } else if (id == R.id.button_outdent) {

            mEditor.focusEditor();
            mEditor.setOutdent();
        } else if (id == R.id.action_strikethrough) {

            mEditor.focusEditor();
            mEditor.setStrikeThrough();
        } else if (id == R.id.button_undo) {
            mEditor.undo();
        } else if (id == R.id.button_redo) {
            mEditor.redo();
        }
        //H1--H6省略，需要的自己添加

        else if (id == R.id.tv_main_preview) {//预览
            String html = mEditor.getHtml();
            PreviewActivity.start(this, html);
        }else if (id==R.id.button_link){
            showInsertLinkDialog();
        }
    }

    private android.app.AlertDialog linkDialog;

    /**
     * 插入链接Dialog
     */
    private void showInsertLinkDialog() {

        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(this);
        linkDialog = adb.create();

        View view = getLayoutInflater().inflate(R.layout.dialog_insertlink, null);

        final EditText et_link_address = (EditText) view.findViewById(R.id.et_link_address);
        final EditText et_link_title = (EditText) view.findViewById(R.id.et_link_title);

        Editable etext = et_link_address.getText();
        Selection.setSelection(etext, etext.length());

        //点击确实的监听
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String linkAddress = et_link_address.getText().toString();
                String linkTitle = et_link_title.getText().toString();

                if (linkAddress.endsWith("http://") || TextUtils.isEmpty(linkAddress)) {
                    Toast.makeText(MainActivity.this, "请输入超链接地址", Toast.LENGTH_SHORT);
                } else if (TextUtils.isEmpty(linkTitle)) {
                    Toast.makeText(MainActivity.this, "请输入超链接标题", Toast.LENGTH_SHORT);
                } else {
                    mEditor.focusEditor();
                    mEditor.insertLink(linkAddress, linkTitle);
                    linkDialog.dismiss();
                }
            }
        });
        //点击取消的监听
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkDialog.dismiss();
            }
        });
        linkDialog.setCancelable(false);
        linkDialog.setView(view, 0, 0, 0, 0); // 设置 view
        linkDialog.show();
    }

    /**
     * 调用图库选择
     */
    private void callGallery() {

        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(1)//最大选择数量为9
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true, "cn.sintoon.richeditordemo.fileprovider"))//存储到哪里
                .forResult(REQUEST_CODE_CHOOSE);//请求码
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1) {
                    //处理调用系统图库
                } else if (requestCode == REQUEST_CODE_CHOOSE) {

                    List<String> list = Matisse.obtainPathResult(data);
                    if (list.size() > 0) {
                        showProgress("图片压缩中");
                        new CompressAsyncTask(new CompressListener() {
                            @Override
                            public void onComplete(File file) {
                                hideProgress();
                                mEditor.focusEditor();
                                Log.e(MainActivity.class.getSimpleName() + "", "onComplete->file://" + file.getAbsolutePath());
                                mEditor.insertImage("file://" + file.getAbsolutePath(), "图片"+ "\" style=\"max-width:100%");
                            }

                            @Override
                            public void onError(Exception exception) {
                                hideProgress();
                                toast(exception.getMessage());
                            }
                        },  mEditor.getWidth()).execute(list.get(0));
                    }
                }
            }
        }
    }

    /**
     * 开启动画
     *
     * @param view 开启动画的view
     */
    private void animateOpen(LinearLayout view) {
        view.setVisibility(View.VISIBLE);
        ValueAnimator animator = createDropAnimator(view, 0, mFoldedViewMeasureHeight);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        animator.start();
    }

    /**
     * 关闭动画
     *
     * @param view 关闭动画的view
     */
    private void animateClose(final LinearLayout view) {
        int origHeight = view.getHeight();
        ValueAnimator animator = createDropAnimator(view, origHeight, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                isAnimating = false;
            }
        });
        animator.start();
    }


    /**
     * 创建动画
     *
     * @param view  开启和关闭动画的view
     * @param start view的高度
     * @param end   view的高度
     * @return ValueAnimator对象
     */
    private ValueAnimator createDropAnimator(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }


    protected ProgressDialog progressDialog;

    protected void showProgress(String text) {
//        if (null == waitFragment) {
//            waitFragment = WaitFragment.getWaitFragment(text);
//            waitFragment.setCancelable(false);
//        }
//        waitFragment.show(getSupportFragmentManager(), TAG_WAIT_FRAGMENT);
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(text);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void hideProgress() {
//        waitFragment.dismiss();
        progressDialog.dismiss();
    }

    protected void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    protected void exported(final File exportFile) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("导出成功，目录为：\n" + exportFile.getAbsolutePath())
                .setNegativeButton("确定", null)
                .setNeutralButton("在浏览器打开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = FileProvider.getUriForFile(MainActivity.this, "cn.sintoon.richeditordemo.fileprovider", exportFile);
                        intent.setData(uri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

                        MainActivity.this.startActivity(intent);
                    }
                }).show();
    }

}
