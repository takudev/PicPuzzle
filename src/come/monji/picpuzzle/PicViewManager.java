package come.monji.picpuzzle;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class PicViewManager {

	private Bitmap bitmap;

	// ボックスに対して正しいピース
	private Bitmap[][] correctPieceMap = new Bitmap[3][3];

	// ボックスに対して入っているピース
	private Bitmap[][] currentPieceMap = new Bitmap[3][3];

	// ボックスの幅
	private int boxWidth;

	// ボックスの高さ
	private int boxHeight;

	// 各ボックスの位置とサイズ
	private Rect[][] boxRects = new Rect[3][3];

	// 移動させるピースが入っているボックスのIndex
	private Point workBoxIndex;

	// 移動開始のため最初に触れた位置
	private Point prevTouchPoint;

	// ボックス間のマージン
	private int cellSpacing;

	private HashMap<Bitmap, Rect> pieceRects = new HashMap<Bitmap, Rect>();


	/**
	 *
	 * @param bitmap
	 */
	public PicViewManager(Bitmap bitmap, int cellSpacing){

		// ボックス間の間隔
		this.cellSpacing = cellSpacing;

		// オリジナル画像を保持
		this.bitmap = bitmap;

		// 画像を分割しサイズを計算
		this.initialize();
	}


	private void initialize(){

	    //------------------------
	    // 画像を9分割
	    //------------------------
	    boxWidth = bitmap.getWidth() / 3;
	    boxHeight = bitmap.getHeight() / 3;

	    Bitmap bitmap_x1_y1 = Bitmap.createBitmap(bitmap, 0          + this.cellSpacing / 2, 0           + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x2_y1 = Bitmap.createBitmap(bitmap, boxWidth   + this.cellSpacing / 2, 0           + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x3_y1 = Bitmap.createBitmap(bitmap, boxWidth*2 + this.cellSpacing / 2, 0           + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x1_y2 = Bitmap.createBitmap(bitmap, 0          + this.cellSpacing / 2, boxHeight   + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x2_y2 = Bitmap.createBitmap(bitmap, boxWidth   + this.cellSpacing / 2, boxHeight   + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x3_y2 = Bitmap.createBitmap(bitmap, boxWidth*2 + this.cellSpacing / 2, boxHeight   + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x1_y3 = Bitmap.createBitmap(bitmap, 0          + this.cellSpacing / 2, boxHeight*2 + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x2_y3 = Bitmap.createBitmap(bitmap, boxWidth   + this.cellSpacing / 2, boxHeight*2 + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);
	    Bitmap bitmap_x3_y3 = Bitmap.createBitmap(bitmap, boxWidth*2 + this.cellSpacing / 2, boxHeight*2 + this.cellSpacing / 2, boxWidth - this.cellSpacing, boxHeight - this.cellSpacing);


	    //------------------------
	    // 正解の配置を保持しておく
	    //------------------------
	    correctPieceMap[0][0] = bitmap_x1_y1;
	    correctPieceMap[1][0] = bitmap_x2_y1;
	    correctPieceMap[2][0] = bitmap_x3_y1;
	    correctPieceMap[0][1] = bitmap_x1_y2;
	    correctPieceMap[1][1] = bitmap_x2_y2;
	    correctPieceMap[2][1] = bitmap_x3_y2;
	    correctPieceMap[0][2] = bitmap_x1_y3;
	    correctPieceMap[1][2] = bitmap_x2_y3;
	    correctPieceMap[2][2] = bitmap_x3_y3;


	    //------------------------
	    // 各ボックスの位置をRectで保持
	    //------------------------
	    for(int x=0; x<3; x++){
	    	for(int y=0; y<3; y++){
	    	    int left = boxWidth * x + cellSpacing / 2;
	    	    int top = boxHeight * y + cellSpacing / 2;
	    	    int right = left + boxWidth - cellSpacing;
	    	    int bottom = top + boxHeight - cellSpacing;
	    	    boxRects[x][y] = new Rect(left, top, right, bottom);
	    	}
	    }

	    //------------------------
	    // 分割画像の位置をRectで保持
	    //------------------------
	    pieceRects.put(bitmap_x1_y1, new Rect(boxRects[0][0]));
	    pieceRects.put(bitmap_x2_y1, new Rect(boxRects[1][0]));
	    pieceRects.put(bitmap_x3_y1, new Rect(boxRects[2][0]));
	    pieceRects.put(bitmap_x1_y2, new Rect(boxRects[0][1]));
	    pieceRects.put(bitmap_x2_y2, new Rect(boxRects[1][1]));
	    pieceRects.put(bitmap_x3_y2, new Rect(boxRects[2][1]));
	    pieceRects.put(bitmap_x1_y3, new Rect(boxRects[0][2]));
	    pieceRects.put(bitmap_x2_y3, new Rect(boxRects[1][2]));
	    pieceRects.put(bitmap_x3_y3, new Rect(boxRects[2][2]));


	    //------------------------
	    // 分割した画像を配置(作業中を保持)
	    //------------------------
	    currentPieceMap[0][0] = bitmap_x1_y1;
	    currentPieceMap[1][0] = bitmap_x2_y1;
	    currentPieceMap[2][0] = bitmap_x3_y1;
	    currentPieceMap[0][1] = bitmap_x1_y2;
	    currentPieceMap[1][1] = bitmap_x2_y2;
	    currentPieceMap[2][1] = bitmap_x3_y2;
	    currentPieceMap[0][2] = bitmap_x1_y3;
	    currentPieceMap[1][2] = bitmap_x2_y3;
	    currentPieceMap[2][2] = bitmap_x3_y3;

	    // ピースを一つ欠けた状態にする
	    pieceRects.remove(bitmap_x3_y3);
	    currentPieceMap[2][2] = null;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public Bitmap getCorrectBitmap(int x, int y) {
		return correctPieceMap[x][y];
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.initialize();
	}


	public int getBoxWidth(){
		return boxWidth;
	}

	public int getBoxHeight(){
		return boxHeight;
	}

	public Rect[][] getBoxRects(){
		return boxRects;
	}

	public Rect getBoxRect(int index_x, int index_y){
		return boxRects[index_x][index_y];
	}

	public boolean isAroundBlankBox(int index_x, int index_y){
		return isLeftBlankBox(index_x, index_y) ||
				isRightBlankBox(index_x, index_y) ||
				isTopBlankBox(index_x, index_y) ||
				isBottomBlankBox(index_x, index_y);
	}

	public boolean isLeftBlankBox(int index_x, int index_y){
		int targetIndex_x = index_x - 1;
		int targetIndex_y = index_y;
		return this.isBlankBox(targetIndex_x, targetIndex_y);
	}

	public boolean isRightBlankBox(int index_x, int index_y){
		int targetIndex_x = index_x + 1;
		int targetIndex_y = index_y;
		return this.isBlankBox(targetIndex_x, targetIndex_y);
	}

	public boolean isTopBlankBox(int index_x, int index_y){
		int targetIndex_x = index_x;
		int targetIndex_y = index_y - 1;
		return this.isBlankBox(targetIndex_x, targetIndex_y);
	}
	public boolean isBottomBlankBox(int index_x, int index_y){
		int targetIndex_x = index_x;
		int targetIndex_y = index_y + 1;
		return this.isBlankBox(targetIndex_x, targetIndex_y);
	}

	public boolean isBlankBox(int targetIndex_x, int targetIndex_y){

		// パラメータの妥当性チェック
		if(targetIndex_x < 0 ||
			targetIndex_y < 0 ||
			targetIndex_x >= correctPieceMap.length ||
			targetIndex_y >= correctPieceMap[0].length){
			return false;
		}

		if(currentPieceMap[targetIndex_x][targetIndex_y] != null){
			return false;
		}

		return true;
	}

	/**
	 *
	 * @param point_x
	 * @param point_y
	 * @return
	 */
	public Point getBoxIndex(int point_x, int point_y){

		Point ret = null;

		loopRoot:
		for(int x=0; x<boxRects.length; x++){
			for(int y=0; y<boxRects.length; y++){
				Rect rect = boxRects[x][y];
				if(rect.contains(point_x, point_y)){
					ret = new Point(x, y);
					break loopRoot;
				}
			}
		}

		return ret;
	}


	public Point getBlankBoxIndex(){

		Point ret = null;

		loopRoot:
		for(int x=0; x<currentPieceMap.length; x++){
			for(int y=0; y<currentPieceMap[x].length; y++){
				if(currentPieceMap[x][y] == null){
					ret = new Point(x, y);
					break loopRoot;
				}
			}
		}

		return ret;
	}

	public void setWorkBoxIndex(int index_x, int index_y){

		workBoxIndex = new Point(index_x, index_y);

	}

	public Point getWorkBoxIndex(){

		return workBoxIndex;
	}

	public boolean initPiecePoint(int point_x, int point_y){

		workBoxIndex = this.getBoxIndex(point_x, point_y);
		if(workBoxIndex == null){
			return false;
		}

		prevTouchPoint = new Point(point_x, point_y);

		return true;
	}


	public boolean updatePiecePoint(int point_x, int point_y){

		if(workBoxIndex == null || prevTouchPoint == null){
			return false;
		}

		// 対象ピース
		Bitmap piece = currentPieceMap[workBoxIndex.x][workBoxIndex.y];
		// 対象ピースの位置
		Rect pieceRect = pieceRects.get(piece);

		// 前回のタッチポイントから移動分（増減）
		int movePoint_x = point_x - prevTouchPoint.x;
		int movePoint_y = point_y - prevTouchPoint.y;

		// 動ける方向によって増減を調整
		if(this.isLeftBlankBox(workBoxIndex.x, workBoxIndex.y)){
			// y軸の移動を許可しない
			movePoint_y = 0;

			// 移動可能範囲を超える場合は無視
			int min = boxRects[workBoxIndex.x-1][workBoxIndex.y].centerX();
			int max = boxRects[workBoxIndex.x][workBoxIndex.y].centerX();
			int buff = pieceRect.centerX() + movePoint_x;
			if(buff < min){
				movePoint_x = pieceRect.centerX() - min;
			}
			else if(max < buff){
				movePoint_x = max - pieceRect.centerX();
			}
		}
		else if(this.isRightBlankBox(workBoxIndex.x, workBoxIndex.y)){
			// y軸の移動を許可しない
			movePoint_y = 0;

			// 移動可能範囲を超える場合は無視
			int min = boxRects[workBoxIndex.x][workBoxIndex.y].centerX();
			int max = boxRects[workBoxIndex.x+1][workBoxIndex.y].centerX();
			int buff = pieceRect.centerX() + movePoint_x;
			if(buff < min){
				movePoint_x = pieceRect.centerX() - min;
			}
			else if(max < buff){
				movePoint_x = max - pieceRect.centerX();
			}
		}
		else if(this.isTopBlankBox(workBoxIndex.x, workBoxIndex.y)){
			// x軸の移動を許可しない
			movePoint_x = 0;

			// 移動可能範囲を超える場合は無視
			int min = boxRects[workBoxIndex.x][workBoxIndex.y-1].centerY();
			int max = boxRects[workBoxIndex.x][workBoxIndex.y].centerY();
			int buff = pieceRect.centerY() + movePoint_y;
			if(buff < min){
				movePoint_y = pieceRect.centerY() - min;
			}
			else if(max < buff){
				movePoint_y = max - pieceRect.centerY();
			}
		}
		else if(this.isBottomBlankBox(workBoxIndex.x, workBoxIndex.y)){
			// x軸の移動を許可しない
			movePoint_x = 0;


			// 移動可能範囲を超える場合は無視
			int min = boxRects[workBoxIndex.x][workBoxIndex.y].centerY();
			int max = boxRects[workBoxIndex.x][workBoxIndex.y+1].centerY();
			int buff = pieceRect.centerY() + movePoint_y;
			if(buff < min){
				movePoint_y = pieceRect.centerY() - min;
			}
			else if(max < buff){
				movePoint_y = max - pieceRect.centerY();
			}
		}

		prevTouchPoint.set(point_x, point_y);
//		prevTouchPoint.offset(movePoint_x, movePoint_y);

		pieceRect.offset(movePoint_x, movePoint_y);
		pieceRects.put(piece, pieceRect);


		return true;
	}



	public void decidePiecePoint(Point fromIndex, Point toIndex){

		Bitmap bitmap = currentPieceMap[fromIndex.x][fromIndex.y];
		currentPieceMap[fromIndex.x][fromIndex.y] = null;
		currentPieceMap[toIndex.x][toIndex.y] = bitmap;

		// pieceRectsを更新
		Rect rect = boxRects[toIndex.x][toIndex.y];
		pieceRects.put(bitmap, new Rect(rect));


		// 初期化
		workBoxIndex = null;
		prevTouchPoint = null;
	}

	/**
	 *
	 *
	 * @return
	 */
	public boolean decidePiecePoint(){

		// currentPieceMapを更新
		Bitmap bitmap = currentPieceMap[workBoxIndex.x][workBoxIndex.y];
		Point nearlyBoxPoint = nearlyBoxPoint(bitmap);
		currentPieceMap[workBoxIndex.x][workBoxIndex.y] = null;
		currentPieceMap[nearlyBoxPoint.x][nearlyBoxPoint.y] = bitmap;

		// pieceRectsを更新
		Rect rect = boxRects[nearlyBoxPoint.x][nearlyBoxPoint.y];
		pieceRects.put(bitmap, new Rect(rect));


		// 初期化
		workBoxIndex = null;
		prevTouchPoint = null;

		return true;
	}


	public boolean isCorrect(){

		for(int x=0; x<currentPieceMap.length; x++){
			for(int y=0; y<currentPieceMap[x].length; y++){
				Bitmap currentBitmap = currentPieceMap[x][y];
				Bitmap correctBitmap = correctPieceMap[x][y];

				if(currentBitmap == null){
					continue;
				}

				// １つでも違えばfalse
				if(currentBitmap != correctBitmap){
					return false;
				}
			}
		}

		return true;
	}

	public Bitmap[][] getCurrentPieceMap(){
		return currentPieceMap;
	}

	public boolean isExistsPiece(int point_x, int point_y){
		Point boxPoint = this.getBoxIndex(point_x, point_y);
		if(boxPoint == null){
			return false;
		}
		if(currentPieceMap[boxPoint.x][boxPoint.y] == null){
			return false;
		}
		return true;
	}


	public Rect getPieceRect(int index_x, int index_y){
		Bitmap bitmap = currentPieceMap[index_x][index_y];
		return this.getPieceRect(bitmap);
	}

	public Rect getPieceRect(Bitmap bitmap){
		return pieceRects.get(bitmap);
	}

	public Point nearlyBoxPoint(Bitmap bitmap){

		Rect rect = pieceRects.get(bitmap);
		int centerX = rect.centerX();
		int centerY = rect.centerY();

		Point point = null;
		for(int x=0; x<boxRects.length; x++){
			for(int y=0; y<boxRects[x].length; y++){
				if(boxRects[x][y].contains(centerX, centerY)){
					point = new Point(x, y);
				}
			}
		}

		// ちょうど境界スペースの場合はnull→元の位置戻す
		if(point == null){
			for(int x=0; x<currentPieceMap.length; x++){
				for(int y=0; y<currentPieceMap[x].length; y++){
					Bitmap buff = currentPieceMap[x][y];
					if(bitmap == buff){
						point = new Point(x, y);
					}
				}
			}
		}

		if(point == null){
			throw new RuntimeException();
		}

		return point;
	}


	public void offsetPieceRect(int index_x, int index_y, int point_x, int point_y){
		Rect rect = pieceRects.get(currentPieceMap[index_x][index_y]);
		rect.offset(point_x, point_y);
	}

	public int getTotalWidth(){
		return boxWidth * 3 + cellSpacing * 2;
	}

	public int getTotalHeight(){
		return boxHeight * 3 + cellSpacing * 2;
	}

	public void moveJustBefore(){

		for(int x=0; x<correctPieceMap.length; x++){
			for(int y=0; y<correctPieceMap[x].length; y++){

				Bitmap bitmap = correctPieceMap[x][y];

				if (x == 2 && y == 2) {
					currentPieceMap[x][y] = null;
				}
				else{
					currentPieceMap[x][y] = bitmap;
					pieceRects.put(bitmap, new Rect(boxRects[x][y]));
				}
			}
		}
	}
}
