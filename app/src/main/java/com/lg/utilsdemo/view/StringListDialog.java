package com.lg.utilsdemo.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.lg.utils.ScreenUtil;
import com.lg.utilsdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ligen on 2017/11/29.
 */

public class StringListDialog extends AppCompatDialog {

    @BindView(R.id.lv_dialog)
    ListView lvDialog;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    private List<String> mOptionsList;
    private DialogInterface.OnCancelListener mCancelListener;

    public StringListDialog(Context context) {
        super(context);
    }

    public StringListDialog(Context context, int theme) {
        super(context, theme);
    }

    public StringListDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mCancelListener = cancelListener;
    }

    public StringListDialog(Context context, List<String> optionsList) {
        super(context, R.style.dialog_base);
        mOptionsList = optionsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        int[] screenSize = ScreenUtil.getScreenSize(getContext());
        // 设置弹框位置、大小和动画
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(screenSize[0], ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setWindowAnimations(R.style.dialog_animation);

        //将弹框上的系统蓝色线条设为透明
        int divierId = getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = findViewById(divierId);
        divider.setBackgroundColor(Color.TRANSPARENT);

        if (mOptionsList != null && mOptionsList.size() > 0) {
            lvDialog.setAdapter(new ListDialogAdapter());
            lvDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("对话框选项列表不能为空！");
        }
    }

    @OnClick(R.id.tv_cancel)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                if (mCancelListener != null) {
                    setOnCancelListener(mCancelListener);
                } else {
                    dismiss();
                }
                break;
        }
    }

    class ListDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mOptionsList == null?0:mOptionsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mOptionsList == null?"":mOptionsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mOptionsList == null?0:position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_dialog, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.option = (TextView) convertView.findViewById(R.id.tv_item_list_dialog);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.option.setText((String) getItem(position));
            return convertView;
        }
    }

    static class ViewHolder{
        TextView option;
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
