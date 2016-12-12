package com.rockeagle.phone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.rockeagle.framework.core.files.REFileHandle;


/**
 * TakePhotoActivity
 * @author 日期：2013-4-19下午04:45:25
 * @author 作者：岩鹰
 * @author 邮箱：jyanying@163.com
 * @version 0.1
 * @author (C) Copyright 岩鹰 Corporation 2011 - 2021
 *               All Rights Reserved.
 **/
public class TakePhotoActivity extends Activity {
	// PreviewSurfaceCallback previewSurface;
	private Camera mCamera;
	private Boolean takingPhoto = false;
//	private TakePhotoActivity context;
	private String filePath;



	/** 设置照片的最大像素为200W像素，照片过大容易出现内存溢出  **/
	public static final int PhotoMaxSize = 5000000;

	public class PreviewSurfaceCallback implements SurfaceHolder.Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (mCamera != null) {
				try {
					Camera.Parameters parameters = mCamera.getParameters();

					mCamera.setParameters(parameters);
					mCamera.startPreview();
				} catch (Exception e) {

				}
			}

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera = Camera.open();
				mCamera.setPreviewDisplay(holder);
				ImageButton btnShoot = (ImageButton) findViewById(R.id.ShootButton);
				btnShoot.requestFocus();
				try {
					Camera.Parameters parameters = mCamera.getParameters();
					// 获得当前设备支持的所有拍照尺寸
					List<Size> pic = parameters.getSupportedPictureSizes();
					boolean bool = false;
					Size size2 = null;
					// 设置相机的当前拍照尺寸为预设的范围内
					for (Size size : pic) {
						if(size.width * size.height > PhotoMaxSize) {
							parameters.setPictureSize(size2.width, size2.height);
							bool = true;
							break;
						}
						size2 = size;
					}
					// 如果当前相机的像素小于预设的值，那么选择当前设备支持的最大尺寸进行拍照
					if(!bool) {
						size2 = pic.get(pic.size()-1);
						parameters.setPictureSize(size2.width, size2.height);
					}
					mCamera.setParameters(parameters);
				} catch (Exception e) {
//					Helper.manageException(e, TakePhotoActivity.this);
				}
			} catch (Exception e) {
//				Helper.manageException(e, TakePhotoActivity.this);
				if (mCamera != null) {
					mCamera.release();
					mCamera = null;
				}
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}
	};

	private PreviewSurfaceCallback surfaceCallback = new PreviewSurfaceCallback();

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCamera == null || takingPhoto)
				return;
			takingPhoto = true;
			View iv = findViewById(R.id.ProgressBarPhoto);
			iv.setVisibility(View.VISIBLE);
			// 触发相机的拍照事件
			mCamera.autoFocus(autoFocusCallback);
		}
	};
	
	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			// 拍照成功，生成图像并调用pictureCallback回调方法
			camera.takePicture(null, null, null, pictureCallback);
		}

	};

	private PictureCallback pictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			String fileName = null;
			try {
				fileName = filePath;
				// 保存图像至SD卡中
				REFileHandle.writeFile(data, fileName);
			} catch (FileNotFoundException e) {
				//Helper.manageException(e, TakePhotoActivity.this);
				return;
			} catch (IOException e) {
				//Helper.manageException(e, TakePhotoActivity.this);
				return;
			}
			takingPhoto = false;
			View iv = findViewById(R.id.ProgressBarPhoto);
			iv.setVisibility(View.INVISIBLE);
//			Intent myIntent = new Intent(TakePhotoActivity.this, SendActivity.class);
//			TakePhotoActivity.this.setResult(RESULT_OK, myIntent);
//			TakePhotoActivity.this.finish();
		}

	};
	private OrientationEventListener myOrientationEventListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// remove status bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);

		setContentView(R.layout.activity_camera);

		ImageButton btnShoot = (ImageButton) this
				.findViewById(R.id.ShootButton);

		btnShoot.setOnClickListener(onClickListener);
		SurfaceView sv = (SurfaceView) this.findViewById(R.id.SurfaceView01);
		sv.getHolder().addCallback(surfaceCallback);
		sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		myOrientationEventListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {

			private int mOrientation = -1;
			
			@Override
			public void onOrientationChanged(int orientation) {
				if (orientation == ORIENTATION_UNKNOWN || mCamera == null
						|| takingPhoto)
					return;
				if (orientation > 315 || orientation <= 45)
					orientation = 0;
				else if (orientation > 45 && orientation <= 135)
					orientation = 90;
				else if (orientation > 135 && orientation <= 225)
					orientation = 180;
				else if (orientation > 225 && orientation <= 315)
					orientation = 270;
				if (mOrientation == orientation)
					return;
				mOrientation = orientation;

				try {
					Camera.Parameters parameters = mCamera.getParameters();
					int rotation = (orientation + 90) % 360;
					parameters.setRotation(rotation);
					// 获得当前设备支持的所有拍照尺寸
					List<Size> pic = parameters.getSupportedPictureSizes();
					boolean bool = false;
					Size size2 = null;
					// 设置相机的当前拍照尺寸为预设的范围内
					for (Size size : pic) {
						if(size.width * size.height > PhotoMaxSize) {
							parameters.setPictureSize(size2.width, size2.height);
							bool = true;
							break;
						}
						size2 = size;
					}
					// 如果当前相机的像素小于预设的值，那么选择当前设备支持的最大尺寸进行拍照
					if(!bool) {
						size2 = pic.get(pic.size()-1);
						parameters.setPictureSize(size2.width, size2.height);
					}
					mCamera.setParameters(parameters);
				} catch (Exception e) {
//					Helper.manageException(e, TakePhotoActivity.this);
				}
			}
		};
	}

	@Override
	protected void onPause() {
		myOrientationEventListener.disable();
		super.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		if (myOrientationEventListener.canDetectOrientation())
			myOrientationEventListener.enable();
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			filePath = bundle.getString("filePath");
			if(filePath == null || filePath.equals("")) {
				TakePhotoActivity.this.finish();
			}
		}
	}
}
