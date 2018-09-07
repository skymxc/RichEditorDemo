package cn.sintoon.richeditordemo.listener;

import java.io.File;

/**
 * Created by mxc on 2018/9/7.
 * description:
 */
public interface CompressListener {

    void onComplete(File file);

    void onError(Exception exception);
}
