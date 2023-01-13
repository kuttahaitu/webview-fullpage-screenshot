package com.cri.wsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.webkit.WebView;
import android.widget.Button;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.view.View;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import java.io.File;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import java.io.OutputStream;
import java.io.FileOutputStream;
import android.view.View.MeasureSpec;


public class MainActivity extends AppCompatActivity {
    WebView webview;
	ProgressBar progressBar;
	Button screenshotClickBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		webview=findViewById(R.id.activity_mainWebView);
		progressBar=findViewById(R.id.activitymainProgressBar1);
		screenshotClickBtn=findViewById(R.id.activitymainButton1);
		WebView.enableSlowWholeDocumentDraw();
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView web,String url,Bitmap favicon){
				progressBar.setVisibility(View.VISIBLE);
				super.onPageStarted(web,url,favicon);
			}
			@Override
			public void onPageFinished(WebView web,String url){
				progressBar.setVisibility(View.GONE);
				super.onPageFinished(web,url);
			}
		});
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl("https://www.google.com");
		screenshotClickBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1) {
					clickScreenshot(webview);
				}
				
			
		});
		
        
    }
	//this for checking storage permission
	private static final int MY_PERMISION_REQ_CODE=123;
	protected void checkPermission(){
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
			PackageManager.PERMISSION_GRANTED){
				if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
					){
						AlertDialog.Builder builder=new AlertDialog.Builder(this);
						builder.setMessage("Write external strage permission required");
						builder.setTitle("Permission Required");
					 builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

							 @Override
							 public void onClick(DialogInterface p1, int p2) {
								 ActivityCompat.requestPermissions(MainActivity.this,new String[]{
									 Manifest.permission.WRITE_EXTERNAL_STORAGE
								 },MY_PERMISION_REQ_CODE);
							 }
							 
							
						});
						builder.setNeutralButton("Cancel",null);
						AlertDialog dialog=builder.create();
						dialog.show();
						
					}else{
						ActivityCompat.requestPermissions(MainActivity.this,new String[]{
					 Manifest.permission.WRITE_EXTERNAL_STORAGE
				  },MY_PERMISION_REQ_CODE);
					
					}
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch(requestCode){
			case MY_PERMISION_REQ_CODE:{
				if(grantResults.length>0&&grantResults[0]==PackageManager
				. PERMISSION_GRANTED){
					Toast.makeText(getApplicationContext(),"Permission Granted",
					Toast.LENGTH_LONG).show();
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkPermission();
	}

	@Override
	public void onBackPressed() {
		
		if(webview.canGoBack()){
			webview.goBack();
		}else{
			super.onBackPressed();
		}
	}
	private void clickScreenshot(final WebView webview){
		new AsyncTask(){
			ProgressDialog  progressdialog;
			Exception exception;
			File file;
			@Override
			protected Object doInBackground(Object[] p1) {
				Bitmap bitmap=Bitmap.createBitmap(webview.getMeasuredHeight(),
				webview.getMeasuredWidth(),Bitmap.Config.ARGB_8888);
				Canvas canvas=new Canvas(bitmap);
				Paint paint=new Paint();
				int iHeight=bitmap.getHeight();
				canvas.drawBitmap(bitmap,0,iHeight,paint);
				webview.draw(canvas);
				if(bitmap!=null){
					try{
						File path=new File(Environment.
						getExternalStorageDirectory(),"DCIM");
						OutputStream fileOut=null;
						file=new File(path,String.format("web_%s.png",
						System.currentTimeMillis()));
						fileOut=new FileOutputStream(file);
						bitmap.compress(Bitmap.CompressFormat.PNG,50,fileOut);
						fileOut.flush();
						fileOut.close();
						bitmap.recycle();
					}catch(Exception e){
						exception=e;
					}
				}else{
					exception=new Exception("Can not draw image because bitmap is null");
					Toast.makeText(getApplicationContext(),exception.toString(),
					Toast.LENGTH_LONG).show();
				}
				return null;
			}
			@Override
			protected void onPreExecute(){
				webview.measure(MeasureSpec.makeMeasureSpec(
				MeasureSpec.UNSPECIFIED,MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
				webview.setDrawingCacheEnabled(true);
				webview.buildDrawingCache();
				progressdialog=ProgressDialog.show(MainActivity.this,null,"Please Wait.."
				, true,false);
			}
			@Override
			protected void onPostExecute(Object obj){
				progressdialog.dismiss();
				if(exception==null){
					Toast.makeText(getApplicationContext(),
					"Image Saved"+file.getParent(),0).show();
				}else{
					Toast.makeText(getApplicationContext(),
					"Image Could not Save because : "+
					exception.getMessage(),0).show();
				}
			}
		}.execute();
	}
    
}

