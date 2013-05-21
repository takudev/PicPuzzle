package come.monji.picpuzzle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class PicView extends View{

	private MainActivity activity;

	private PicViewManager picViewManager;

	private Point prevBlankBoxIndex;

	private boolean isMoving = false;

	// ブランクに対して移動元となる画像の位置
	private static HashMap<Point, Point[]> movePointMap = new HashMap<Point, Point[]>();

	private int shuffleCount = 0;

	// 移動中のピースの透過値 0..255(小さく透過)
	private int moving_piece_alpha = 150;


	static{
		movePointMap.put(new Point(0,0), new Point[]{new Point(1,0), new Point(0,1)});
		movePointMap.put(new Point(1,0), new Point[]{new Point(0,0), new Point(2,0), new Point(1,1)});
		movePointMap.put(new Point(2,0), new Point[]{new Point(1,0), new Point(2,1)});
		movePointMap.put(new Point(0,1), new Point[]{new Point(0,0), new Point(1,1), new Point(0,2)});
		movePointMap.put(new Point(1,1), new Point[]{new Point(1,0), new Point(0,1), new Point(2,1), new Point(1,2)});
		movePointMap.put(new Point(2,1), new Point[]{new Point(2,0), new Point(1,1), new Point(2,2)});
		movePointMap.put(new Point(0,2), new Point[]{new Point(0,1), new Point(1,2)});
		movePointMap.put(new Point(1,2), new Point[]{new Point(1,1), new Point(0,2), new Point(2,2)});
		movePointMap.put(new Point(2,2), new Point[]{new Point(2,1), new Point(1,2)});
	}

	public PicView(MainActivity activity, PicViewManager picViewManager) {
		super(activity);

		setLayoutParams(new LinearLayout.LayoutParams(picViewManager.getTotalWidth(), picViewManager.getTotalHeight()));

		this.activity = activity;
		this.picViewManager = picViewManager;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		Bitmap[][] currentPieceMap = picViewManager.getCurrentPieceMap();
		Point workBoxIndex = picViewManager.getWorkBoxIndex();

		for(int index_x = 0; index_x < currentPieceMap.length; index_x++){
			for(int index_y = 0; index_y < currentPieceMap.length; index_y++){
				if(currentPieceMap[index_x][index_y] == null){
					continue;
				}

				// 移動中のピースの場合
				Paint paint = new Paint();
				if(workBoxIndex != null && workBoxIndex.x == index_x && workBoxIndex.y == index_y){
					paint.setAlpha(this.moving_piece_alpha);
				}

				Bitmap pieceBitmap = currentPieceMap[index_x][index_y];
				Rect pieceRect = picViewManager.getPieceRect(pieceBitmap);
				canvas.drawBitmap(pieceBitmap, pieceRect.left, pieceRect.top, paint);
			}
		}
	}


	public void startPieceShuffle(){

		if(getResources().getInteger(R.integer.INITIAL_SHUFFLE_COUNT) == shuffleCount){
			return;
		}


		Point blankBoxIndex = picViewManager.getBlankBoxIndex();

		for(Point key:movePointMap.keySet()){
			if(key.equals(blankBoxIndex.x, blankBoxIndex.y)){
				Point[] fromIndexs = movePointMap.get(key);

				// 前回の移動で埋まった場所は移動の起点にしない（前回の移動で埋まった場所を動かした動きはUndo処理と同様）
				if(prevBlankBoxIndex != null){
					Point[] reviceFromIndexs = new Point[fromIndexs.length -1];
					int insIndex = 0;
					for(int i=0; i<fromIndexs.length; i++){
						if(fromIndexs[i].equals(prevBlankBoxIndex.x, prevBlankBoxIndex.y)){
							continue;
						}
						reviceFromIndexs[insIndex] = fromIndexs[i];
						insIndex ++;
					}

					fromIndexs = reviceFromIndexs;
				}

				// 移動元をランダムに決定
				int randInt = new Random().nextInt(fromIndexs.length);


				// 移動実行
				int initShuffleTimeOneByOne = getResources().getInteger(R.integer.INITIAL_SHUFFLE_TIME_ONE_BY_ONE);
				MoveTimer moveTimer = new MoveTimer(this, fromIndexs[randInt], blankBoxIndex, initShuffleTimeOneByOne);
				moveTimer.doMovePiece();

				// 前回のブランク＝今回埋まった場所を保持
				prevBlankBoxIndex = blankBoxIndex;

				shuffleCount++;
			}
		}
	}

	/**
	 *
	 *
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){

		final float point_x = event.getX();
		final float point_y = event.getY();


		if(event.getAction() == MotionEvent.ACTION_DOWN){
			isMoving = picViewManager.isExistsPiece((int)point_x, (int)point_y);
			picViewManager.initPiecePoint((int)point_x, (int)point_y);
			invalidate();
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE){

			if(isMoving){

				Point workBoxIndex = picViewManager.getWorkBoxIndex();
				if(!picViewManager.isAroundBlankBox(workBoxIndex.x, workBoxIndex.y)){
					return true;
				}

				picViewManager.updatePiecePoint((int)point_x, (int)point_y);
				invalidate();
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			if(isMoving){
				isMoving = false;
				picViewManager.decidePiecePoint();
				invalidate();

				if(picViewManager.isCorrect()){
					activity.clear();
				}
			}
		}

		// イベント消化
		return true;
	}


	public void moveJustBefore(){
		picViewManager.moveJustBefore();
	}

	class MoveTimer extends Timer{

		// 1枠移動する際に刻む数（多い値ほど滑らかだが、処理が重くなる。）
		private int stepCount = 5;
		private int scheduleTime = 10;

		private PicView picView;
		private MoveTimerTask task;

		public MoveTimer(PicView picView, Point fromIndex, Point toIndex, int initShuffleTimeOneByOne){

			Rect fromRect = picViewManager.getBoxRect(fromIndex.x, fromIndex.y);
			Rect toRect = picViewManager.getBoxRect(toIndex.x, toIndex.y);

			int distanceX = toRect.centerX() - fromRect.centerX();
			int distanceY = toRect.centerY() - fromRect.centerY();

			this.picView = picView;
			Handler handler = new Handler();
			task = new MoveTimerTask(handler, fromIndex, toIndex, distanceX, distanceY);

			// 1枠の移動に対する、刻む数と指定所要時間から1刻み時間間隔を計算
			this.scheduleTime = initShuffleTimeOneByOne / stepCount;
		}

		public void doMovePiece(){
			schedule(task, 0, scheduleTime);
		}

		class MoveTimerTask extends TimerTask{

			private int stepIndex = 0;
			private Point fromIndex;
			private Point toIndex;
			private int[] stepList_x;
			private int[] stepList_y;

			private Handler handler;

			public MoveTimerTask(Handler handler, Point fromIndex, Point toIndex, int distance_x, int distance_y){


				this.handler = handler;
				this.fromIndex = fromIndex;
				this.toIndex = toIndex;

				//-----------------------------
				// 移動距離リストを作成
				// 端数を考慮しステップ毎に移動させる距離を保持する。
				// 端数がある場合は最終ステップで調整する。
				//-----------------------------
				stepList_x = new int[stepCount];
				stepList_y = new int[stepCount];

				int step_x = distance_x / stepCount;
				int step_y = distance_y / stepCount;
				Arrays.fill(stepList_x, step_x);
				Arrays.fill(stepList_y, step_y);

				// 端数の調整
				if(distance_x % stepCount != 0){
					stepList_x[stepCount -1] += distance_x % stepCount;
				}
				if(distance_y % stepCount != 0){
					stepList_y[stepCount -1] += distance_y % stepCount;
				}
			}


			@Override
			public void run() {

				handler.post(new Runnable() {

					public void run(){

						// cancelが追いつかない場合があるためここでreturnしておく
						if(stepIndex >= stepList_x.length){
							return;
						}

						picViewManager.offsetPieceRect(fromIndex.x,fromIndex.y,stepList_x[stepIndex],stepList_y[stepIndex]);
						invalidate();

						stepIndex ++;
						if(stepIndex == stepCount){
							cancel();
							picViewManager.decidePiecePoint(fromIndex, toIndex);
							picView.startPieceShuffle();
						}
					}
				});
			}
		}
	}
}
