package cn.sintoon.richeditordemo.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;

import cn.sintoon.richeditordemo.listener.CompressListener;
import cn.sintoon.richeditordemo.util.ImageUtils;

/**
 * Created by mxc on 2018/9/7.
 * description: 按照屏幕宽高像素比去压缩大小然后迁移至指定目录
 */
public class CompressAsyncTask extends AsyncTask<String, Void, File> {
    private CompressListener listener;
    private int screenWidth;
    private int screenHeight;
    private Exception e;

    public CompressAsyncTask(CompressListener listener, int screenWidth) {
        this.listener = listener;
        this.screenWidth = screenWidth;
    }

    @Override
    protected File doInBackground(String... strings) {
        String s = null;
        try {
            s = ImageUtils.compressFile(strings[0]);
            if (TextUtils.isEmpty(s)){
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e = e;
        }
        return new File(s);
    }

    @Override
    protected void onPostExecute(File file) {
        if (null!=e){
            listener.onError(e);
        }else{
            if (null!=file){
                listener.onComplete(file);
            }else{
                listener.onError(new Exception("压缩失败！"));
            }
        }
    }
}
