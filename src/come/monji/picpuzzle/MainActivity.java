package come.monji.picpuzzle;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 *
 * @author takudev
 *
 */
public class MainActivity extends Activity {

	private static final int REQUEST_PICK_CONTACT = 1;

	private ProgressDialog progressDialog;

    int paletWidth = 380;
    int paletHeight = 450;
    int cellSpacing = 2;

    private PicView picView;
    private PicPreview picPreview;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }


    public void onClick_open(View view) throws IOException{

    	progressDialog = ProgressDialog.show(this, "", "読み込んでいます。");

    	Intent intent = new Intent(Intent.ACTION_PICK);
    	intent.setType("image/*");
    	startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }

    public void onClick_quit(View view){

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("終了しますか？");
    	builder.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
			}
		});
    	builder.setNegativeButton("キャンセル", null);
    	builder.create().show();
    }

    public void onClick_start(View view){
        //-------------------------------
        // 特定時間後にシャッフルを開始
        //-------------------------------
        Toast.makeText(this, "シャッフルを開始します。", Toast.LENGTH_LONG).show();

        // フェードアウト
    	AlphaAnimation alpha = new AlphaAnimation(1, 0);
    	alpha.setDuration(3000);
        LinearLayout palet = (LinearLayout)findViewById(R.id.linearLayout_palet);
        View currentPicView = palet.getChildAt(0);
        currentPicView.startAnimation(alpha);

        this.showPicView(this.picView);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
			public void run() {
				picView.startPieceShuffle();
			}
		}, getResources().getInteger(R.integer.WAIT_TIME_SHUFFLE_START));
    }

    public void onClick_last(View view){

    }

    public void onClick_save(View view){

    }

    public void onClick_preview(View view){

    	LinearLayout palet = (LinearLayout)findViewById(R.id.linearLayout_palet);
    	Object object = palet.getChildAt(0);

    	if (object instanceof PicView) {
    		this.showPicView(picPreview);
    	}
    	else if (object instanceof PicPreview) {
    		this.showPicView(picView);
    	}

    }

    public void onClick_resume(View view){

    }

    /**
     * @override
     */
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

        Bitmap bitmap = null;
        try {
			bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

        PicViewManager picViewManager = this.createPicViewManager(bitmap);

		picView = new PicView(this, picViewManager);
		picPreview = new PicPreview(this, picViewManager);

		this.showPicView(picPreview);

        Log.d("TempLog", "PicView " + picView.getWidth() + ":" + picView.getHeight());
    }

    private void showPicView(View picView) {

        LinearLayout palet = (LinearLayout)findViewById(R.id.linearLayout_palet);
		palet.removeAllViews();
		palet.addView(picView);
        picView.invalidate();
    }

    private PicViewManager createPicViewManager(Bitmap bitmap){

    	WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
    	// ディスプレイのインスタンス生成
    	Display disp = wm.getDefaultDisplay();
    	String width = "Width = " + disp.getWidth();
    	String height = "Height = " + disp.getHeight();


		PicViewManager picViewManager = null;

    	try{
    	    //------------------------
    	    // リサイズした画像を保持
    	    //------------------------
    		{
    		    double imageWidth = bitmap.getWidth();
    		    double imageHeight = bitmap.getHeight();

    		    double rate = 1f;
    		    if(imageWidth > paletWidth || imageHeight > paletHeight){
    		    	// 縮小
    		    	rate = Math.min(Integer.valueOf(paletWidth).doubleValue()/imageWidth,
    		    					Integer.valueOf(paletHeight).doubleValue()/imageHeight);
    		    }
    		    else{
    		    	// 拡大
    		    	rate = Math.min(Integer.valueOf(paletWidth).doubleValue()/imageWidth,
    		    					Integer.valueOf(paletHeight).doubleValue()/imageHeight);
    		    }

    		    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, Double.valueOf(imageWidth*rate).intValue(), Double.valueOf(imageHeight*rate).intValue(), true);
    		    picViewManager = new PicViewManager(newBitmap, cellSpacing);
    		    Log.d("TempLog", "Bitmap " + newBitmap.getWidth() + ":" + newBitmap.getHeight());
    		}

    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}


    	return picViewManager;
    }

    public void clear(){
    	Log.d("TempLog", "clear()");
    	Toast.makeText(this, "正解!", Toast.LENGTH_LONG).show();
    }

}