package come.monji.picpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int REQUEST_PICK_CONTACT = 1;
	
	private ProgressDialog progressDialog;
	
    int paletWidth = 380;
    int paletHeight = 450;
    
    private PicView picView;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    
    public void onClick_open(View view){

    	// TODO
    	progressDialog = ProgressDialog.show(this, "", "読み込んでいます。");
//        this.setBitmapToCanvas(Uri.parse("content://media/external/images/media/22"));
        this.picView = this.setBitmapToCanvas(Uri.parse("content://media/external/images/media/1"));
        progressDialog.dismiss();
        picView.invalidate();
        
        
//    	progressDialog = ProgressDialog.show(this, "", "読み込んでいます。");
//    	
//    	Intent intent = new Intent(Intent.ACTION_PICK);
//    	intent.setType("image/*");
//    	startActivityForResult(intent, REQUEST_PICK_CONTACT);
        
        
        //-------------------------------
        // 特定時間後にシャッフルを開始
        //-------------------------------
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
//			@Override
			public void run() {
				picView.startPieceShuffle();
			}
		}, getResources().getInteger(R.integer.WAIT_TIME_SHUFFLE_START));
    }

    public void onClick_quit(View view){
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("終了しますか？");
    	builder.setPositiveButton("OK", new OnClickListener() {
			
//			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
			}
		});
    	builder.setNegativeButton("キャンセル", null);
    	builder.create().show();
    }
    
    public void onClick_save(View view){
    	
    }
    
    public void onClick_preview(View view){
    	
    }

    public void onClick_resume(View view){
    	
    }
    
    public void onActivityResult (int requestCode, int resultCode, Intent intent){
    	super.onActivityResult(requestCode, resultCode, intent);

    	if(progressDialog != null && progressDialog.isShowing()){
    		progressDialog.dismiss();
    	}
    	
    	
    	if(requestCode != REQUEST_PICK_CONTACT){
    		return;
    	}
    	
    	if(intent == null){
    		return;
    	}
    	
        // 画像URIを取得
        Uri photoUri = intent.getData();
        
        if(photoUri == null){
        	return;
        }
        
        this.setBitmapToCanvas(photoUri);
        
    }
    
    
    private PicView setBitmapToCanvas(Uri photoUri){

    	PicView picView = null;
    	
        // 画像を取得
    	try{
    		// ビットマップ画像を取得
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
            LinearLayout palet = (LinearLayout)findViewById(R.id.linearLayout_palet);
    		palet.removeAllViews();
    		
    		
    	    //------------------------
    	    // リサイズした画像を保持
    	    //------------------------
    		PicViewManager picViewManager = null;
    		{
    		    double imageWidth = bitmap.getWidth();
    		    double imageHeight = bitmap.getHeight();
    		    
    		    double rate = 1f;
    		    if(imageWidth > paletWidth || imageHeight > paletHeight){
    		    	// 縮小
    		    	rate = Math.min(new Integer(paletWidth).doubleValue()/imageWidth,
    		    					new Integer(paletHeight).doubleValue()/imageHeight);
    		    }
    		    else{
    		    	// 拡大
    		    	rate = Math.min(new Integer(paletWidth).doubleValue()/imageWidth,
    						new Integer(paletHeight).doubleValue()/imageHeight);
    		    }
    		    
    		    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, new Double(imageWidth*rate).intValue(), new Double(imageHeight*rate).intValue(), true);
    		    picViewManager = new PicViewManager(newBitmap, 2);
    		    Log.d("TempLog", "Bitmap " + newBitmap.getWidth() + ":" + newBitmap.getHeight());
    		}
    		
    		picView = new PicView(this, picViewManager);
    		picView.setBackgroundColor(Color.CYAN);
    		palet.addView(picView);
    		Log.d("TempLog", "PicView " + picView.getWidth() + ":" + picView.getHeight());
    		
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	return picView;
    }
    
    public void clear(){
    	Log.d("TempLog", "clear()");
    	Toast.makeText(this, "正解!", Toast.LENGTH_LONG).show();
    }

}