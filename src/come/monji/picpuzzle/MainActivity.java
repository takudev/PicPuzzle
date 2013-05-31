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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.Chronometer;
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

    private PicView picView;
    private PicPreview picPreview;
    private Chronometer chronometer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        chronometer = (Chronometer)findViewById(R.id.chronometer1);

        Button buttonPreview = (Button)findViewById(R.id.button_preview);

        buttonPreview.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if(event.getAction() == MotionEvent.ACTION_DOWN){
					MainActivity.this.showPicView(picPreview);
				}
				else if(event.getAction() == MotionEvent.ACTION_UP){
					MainActivity.this.showPicView(picView);
				}

		    	return true;
			}
		});
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

        Toast.makeText(this, "シャッフルを開始します。", Toast.LENGTH_LONG).show();

        // フェードアウト
    	AlphaAnimation alpha = new AlphaAnimation(1, 0);
    	alpha.setDuration(getResources().getInteger(R.integer.WAIT_TIME_SHUFFLE_START));
    	picPreview.startAnimation(alpha);

        this.showPicView(this.picView);

        // 特定時間後にシャッフルを開始
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
			public void run() {
				picView.startPieceShuffle();
			}
		}, getResources().getInteger(R.integer.WAIT_TIME_SHUFFLE_START));
    }

    public void onClick_last(View view){
    	picView.moveJustBefore();
    	picView.invalidate();
    }

    public void onClick_save(View view){

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

        PicViewManager picViewManager = new PicViewManager(this, bitmap);

		picView = new PicView(this, picViewManager);
		picPreview = new PicPreview(this, picViewManager);

		this.showPicView(picPreview);
    }

    private void showPicView(View picView) {

        LinearLayout palet = (LinearLayout)findViewById(R.id.linearLayout_palet);
		palet.removeAllViews();
		palet.addView(picView);
        picView.invalidate();
    }


    public void start(){
    	chronometer.start();
    }

    public void clear(){
    	Toast.makeText(this, "正解!", Toast.LENGTH_LONG).show();
    	chronometer.stop();
    }

}