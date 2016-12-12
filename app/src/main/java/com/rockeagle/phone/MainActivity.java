package com.rockeagle.phone;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rockeagle.framework.core.files.REFileHandle;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<String> selectedPicture;

    // 存放用户拍照或者选中的图片路径
    private Map<Long, String> imgMap;
    private Map<Long, View> imgViews;
    private View btnAddImg;

    private View modelView;
    private View showView;	// 用于弹出PopupWindow
    private DialogSelectImg dialogSelectImg;
    private Long curTime;
    private String curImgPath;

    private LayoutInflater inflater;
    private LinearLayout viewPager;

    public static final String Cache_Path = getInnerSDCardPath() + "/xuefeng_photo_cache/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (LinearLayout) findViewById(R.id.send_imgsViewPager);
        inflater = LayoutInflater.from(MainActivity.this);
        btnAddImg = inflater.inflate(R.layout.send_add_image, null);
        btnAddImg.setOnClickListener(clickListener);
        viewPager.addView(btnAddImg);
        modelView = findViewById(R.id.send_view_Model);
        showView = findViewById(R.id.send_showing_View);
        dialogSelectImg = new DialogSelectImg(MainActivity.this, dialogCall);
        init();
    }

    private void init() {
        imgMap = new HashMap<Long, String>();
        imgViews = new HashMap<Long, View>();

        File file = new File(Cache_Path);
        file.mkdirs();
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v == btnAddImg) {
//				startActivityForResult(new Intent(SendActivity.this, SelectPictureActivity.class), REQUEST_PICK);
                if(modelView.getVisibility() == View.GONE) {
                    dialogSelectImg.show(showView);
                    modelView.setVisibility(View.VISIBLE);
                } else {
                    modelView.setVisibility(View.GONE);
                }
            }
        }
    };

    private Handler dialogCall = new Handler () {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    // 拍照
                    modelView.setVisibility(View.GONE);
                    gotoCamera();
                    break;

                case 2:
                    // 选择图片
                    modelView.setVisibility(View.GONE);
                    selectedImg();
                case 0:
                    // 取消
                    modelView.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    };

    /**
     * 获取内置SD卡路径
     * @return
     */
    public static String getInnerSDCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    /**
     * 选择图片
     */
    private void selectedImg() {
        // 建立"选择档案Action" 的Intent
        Intent intent = new Intent(Intent.ACTION_PICK);
        // 过滤档案格式
        intent.setType("image/*");
        // 建立"档案选择器" 的Intent (第二个参数: 选择器的标题)
        Intent destIntent = Intent.createChooser(intent, "选择文件");
        // 切换到档案选择器(它的处理结果, 会触发onActivityResult 事件)
        startActivityForResult(destIntent, 0);
    }

    /**
     * 调用系统相机拍照
     */
    private void gotoCamera() {
//		// 建立"选择档案Action" 的Intent
//		Intent intent = new Intent(Intent.ACTION_PICK);
//		// 过滤档案格式
//		intent.setType("image/*");
        // 建立"档案选择器" 的Intent (第二个参数: 选择器的标题)
        curTime = System.currentTimeMillis();
        curImgPath = Cache_Path + curTime + "temp.jpg";

//        Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
//        intent.putExtra("filePath", curImgPath);
//        startActivityForResult(intent, 1);


        File mPhotoFile = new File(Cache_Path, curTime + "temp.jpg");
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = Uri.fromFile(mPhotoFile);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(captureIntent, 1);
    }

//    Uri fileUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 有选择档案
        if (resultCode == RESULT_OK) {
            if(requestCode == 0) {
                // 取得档案的Uri
                Uri uri = data.getData();
                if (uri != null) {
                    Long l = System.currentTimeMillis();
                    View view = inflater.inflate(R.layout.send_img_item, null);
                    // 通过URI获取到绝对路径
                    ImageView imageView = (ImageView) view.findViewById(R.id.send_item_imgs);
                    imageView.setBackgroundColor(0xffdfdfdf);
                    imageView.setImageURI(uri);
                    imageView.setOnClickListener(imgClickListener);
                    imageView.setTag(l);

                    ImageButton btnView = (ImageButton) view.findViewById(R.id.send_item_delete);
                    btnView.setOnClickListener(imgClickListener);
                    btnView.setTag(l);

                    imgMap.put(l, getAbsoluteImagePath(uri));

                    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(btnAddImg.getWidth(), btnAddImg.getHeight());
                    layoutParams.setMargins(10, 0, 10, 0);
                    viewPager.addView(view, layoutParams);

                    imgViews.put(l, view);
                } else {
                    // 无效的图片
                }
            } else if (requestCode == 1) {
//				Uri uri = data.getData();
//				System.out.println(curImgPath);
                // 取得档案的Uri
                String imgPath = curImgPath;

                View view = inflater.inflate(R.layout.send_img_item, null);
                // 通过URI获取到绝对路径
                ImageView imageView = (ImageView) view.findViewById(R.id.send_item_imgs);
                imageView.setBackgroundColor(0xffdfdfdf);
                // 压缩图片
                File file = new File(imgPath);
                BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                // 只读取图片的大小，不读取像素
                bitmapFactoryOptions.inJustDecodeBounds = true;
                Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapFactoryOptions);
                // 计算缩放比例
                int be = (int)(bitmapFactoryOptions.outWidth / (float)100);
                if (be <= 0)
                    be = 1;
                bitmapFactoryOptions.inSampleSize = be;
                bitmapFactoryOptions.inJustDecodeBounds = false;
                bm = REFileHandle.getBitmapFD(file.getAbsolutePath(), bitmapFactoryOptions);

                imageView.setImageBitmap(bm);
                imageView.setOnClickListener(imgClickListener);
                imageView.setTag(curTime);

                ImageButton btnView = (ImageButton) view.findViewById(R.id.send_item_delete);
                btnView.setOnClickListener(imgClickListener);
                btnView.setTag(curTime.longValue());

                ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(btnAddImg.getWidth(), btnAddImg.getHeight());
                layoutParams.setMargins(10, 0, 10, 0);
                viewPager.addView(view, layoutParams);
                // 取得档案的Uri
                imgMap.put(curTime.longValue(), imgPath.toString());
                imgViews.put(curTime.longValue(), view);
            }
        } else {
            // 失败，请重新操作
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    private View.OnClickListener imgClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v instanceof ImageButton) {
                Long tag = (Long) v.getTag();
                View parent = imgViews.remove(tag);
                viewPager.removeView(parent);
                imgMap.remove(tag);
            }
        }
    };

    /**
     * 通过uri获取文件的绝对路径
     *
     * @param uri
     * @return
     */
    protected String getAbsoluteImagePath(Uri uri) {
        // can post image
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
}
