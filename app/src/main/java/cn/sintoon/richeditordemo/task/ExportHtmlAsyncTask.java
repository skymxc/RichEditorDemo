package cn.sintoon.richeditordemo.task;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import cn.sintoon.richeditordemo.listener.ExportListener;
import cn.sintoon.richeditordemo.util.SDCardUtils;


/**
 * Created by mxc on 2018/9/7.
 * description:
 */
public class ExportHtmlAsyncTask extends AsyncTask<String, Void, Boolean> {


    protected ExportListener listener;
    protected IOException exception;
    private File exportFile ;

    public ExportHtmlAsyncTask(ExportListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String html = strings[0];
        try {
             exportFile = new File(SDCardUtils.getHtmlMkdir(), getHtmlName());
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(exportFile);
            fileWriter.write(html);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.exception = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean.booleanValue()){
            listener.onComplete(exportFile);
        }else{
            listener.onError(exception);
        }
    }

    private String getHtmlName() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return day + "_" + hour + "_" + minute + "_" + second + ".html";
    }
}
