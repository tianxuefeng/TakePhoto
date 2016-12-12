package com.rockeagle.phone;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Created by yifan on 2016-12-11.
 */

public class DialogSelectImg {

    private Context context;

    private Handler handler;

    private PopupWindow popupWindow;

    private Button btnCamera, btnSelectFile, btnCancle;

    public DialogSelectImg(Context context, Handler handler) {
        this.handler = handler;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_img, null);

        btnCamera = (Button) view.findViewById(R.id.btn_select_camera);
        btnSelectFile = (Button) view.findViewById(R.id.btn_select_file);
        btnCancle = (Button) view.findViewById(R.id.btn_select_cancle);

        btnCamera.setOnClickListener(clickListener);
        btnSelectFile.setOnClickListener(clickListener);
        btnSelectFile.setVisibility(View.GONE);
        btnCancle.setOnClickListener(clickListener);

        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
//		ColorDrawable dw = new ColorDrawable(-00000);
//		popupWindow.setBackgroundDrawable(dw);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
            }
        });
    }

    /**
     * 弹出对话框
     * @param parent
     */
    public void show(View parent) {
        if(popupWindow != null && !popupWindow.isShowing()) {
            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }
    }

    public void dismiss() {
        if(popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public boolean isShowing(){
        return popupWindow.isShowing();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_select_camera:
                    dismiss();
                    handler.sendEmptyMessage(1);
                    break;

                case R.id.btn_select_file:
                    dismiss();
                    handler.sendEmptyMessage(2);
                    break;

                case R.id.btn_select_cancle:
                    dismiss();
                    handler.sendEmptyMessage(0);
                    break;

                default:
                    break;
            }
        }
    };
}
