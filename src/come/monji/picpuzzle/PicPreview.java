package come.monji.picpuzzle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;


public class PicPreview extends View{

	private MainActivity activity;

	private PicViewManager picViewManager;


	public PicPreview(MainActivity activity, PicViewManager picViewManager) {
		super(activity);

		setLayoutParams(new LinearLayout.LayoutParams(picViewManager.getTotalWidth(), picViewManager.getTotalHeight()));

		this.activity = activity;
		this.picViewManager = picViewManager;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawBitmap(this.picViewManager.getBitmap(), 0, 0, new Paint());

	}
}