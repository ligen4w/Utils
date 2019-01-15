package com.lg.utilsdemo.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lg.utilsdemo.R;

import java.util.List;

/**
 * Created by ligen on 2018/8/29.
 */

public class StringAdapter extends BaseQuickAdapter<String,BaseViewHolder> {
    public StringAdapter(@LayoutRes int layoutResId) {
        super(layoutResId);
    }

    public StringAdapter(@LayoutRes int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_item,item);
    }
}
