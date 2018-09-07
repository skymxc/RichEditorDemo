package cn.sintoon.richeditordemo.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.sintoon.richeditordemo.R;

/**
 * Created by mxc on 2018/9/7.
 * description:
 */
public class WaitFragment extends DialogFragment {

    private TextView tvWait;
    public static WaitFragment getWaitFragment(String txt){
        WaitFragment fragment = new WaitFragment();
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(txt)) {
            bundle.putString("text", txt);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public static WaitFragment getWaitFragment(){
       return getWaitFragment(null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String text = getArguments().getString("text",getContext().getString(R.string.loading));
        View inflate = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_dialog_wait, null, false);
        tvWait = inflate.findViewById(R.id.tv_wait);
        tvWait.setText(text);
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(inflate);
        return dialog;
    }


    public void setText(String text){
        if (!TextUtils.isEmpty(text)&&null!=tvWait){
            tvWait.setText(text);
        }
    }


}
