/**   
 * Copyright © 2014 All rights reserved.
 * 
 * @Title: MertoItemView.java 
 * @Prject: MetroMain_2
 * @Package: com.example.metromain.view 
 * @Description: TODO
 * @author: raot raotao.bj@cabletech.com.cn 
 * @date: 2014年9月30日 上午9:47:34 
 * @version: V1.0   
 */
package org.tmind.kiteui.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

import org.tmind.kiteui.R;


/**
 * @ClassName: MertoItemView
 * @Description: TODO
 * @author: raot raotao.bj@cabletech.com.cn
 * @date: 2014年9月30日 上午9:47:34
 */
public class MertoItemView extends View implements OnTouchListener,
		OnClickListener, OnLongClickListener {

	public interface OnMertoItemViewListener {

		public void onClick(MertoItemView v);

		public boolean onMove(MertoItemView v, MotionEvent e1, MotionEvent e2);

		public void onLongClick(MertoItemView v);

		public void onUp(MertoItemView v);

	}

	private OnMertoItemViewListener onMertoItemViewListener = new OnMertoItemViewListener() {

		@Override
		public void onClick(MertoItemView v) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean onMove(MertoItemView v, MotionEvent e1, MotionEvent e2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongClick(MertoItemView v) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUp(MertoItemView v) {
			// TODO Auto-generated method stub

		}

	};

	/**
	 * @param onMertoItemViewListener
	 *            the onMertoItemViewListener to set
	 */
	public void setOnMertoItemViewListener(
			OnMertoItemViewListener onMertoItemViewListener) {
		this.onMertoItemViewListener = onMertoItemViewListener;
	}

	private MotionEvent event1;
	private MotionEvent event2;
	private int textColor = Color.WHITE;
	private float textSize = 30;
	private String text = "";
	private Drawable icon;

	public MertoItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setOnTouchListener(this);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	@SuppressLint("Recycle")
	public MertoItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.MertoItemView);
		textColor = typedArray.getColor(R.styleable.MertoItemView_textColor,
				Color.WHITE);
		textSize = typedArray.getDimension(R.styleable.MertoItemView_textSize,
				30);
		text = typedArray.getString(R.styleable.MertoItemView_text);
		if (StringIsEmpty(text)) {
			text = "";
		}
		icon = typedArray.getDrawable(R.styleable.MertoItemView_icon);
		setOnTouchListener(this);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
		invalidate();
	}

	public void setIcon(int icon) {
		this.icon = getResources().getDrawable(icon);
		invalidate();
	}

	public Drawable getIcon() {
		return icon;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		invalidate();
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
		invalidate();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		invalidate();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		TextPaint textPaint = new TextPaint();
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setAntiAlias(true);
		StaticLayout layout = null;
		layout = new StaticLayout(text, textPaint, width - 10,
				Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
		if (icon != null) {
			Paint paint = new Paint();
			float iconWidth = 0;
			float iconHeight = 0;
			if (width > height - layout.getHeight()) {
				iconHeight = (height - layout.getHeight() - 5) / 2;
				iconWidth = iconHeight;
			} else {
				iconWidth = width / 2;
				iconHeight = iconWidth;
			}
			float iconX = (width - iconWidth) / 2;
			float iconY = (height - layout.getHeight() - iconHeight) / 2;
			Rect iconRect = new Rect((int) iconX, (int) iconY,
					(int) (iconX + iconWidth), (int) (iconY + iconHeight));
			canvas.translate(0, 0);
			canvas.drawBitmap(((BitmapDrawable) icon).getBitmap(), null,
					iconRect, paint);
			paint.reset();
		}
		canvas.translate((width - layout.getWidth()) / 2,
				height - layout.getHeight() - 10);
		layout.draw(canvas);
		textPaint.reset();
	}

	private boolean StringIsEmpty(String string) {
		if (string == null || ("").equals(string.trim())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		onMertoItemViewListener.onLongClick(this);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		onMertoItemViewListener.onClick(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			event1 = MotionEvent.obtain(event);
			return false;
		case MotionEvent.ACTION_MOVE:
			event2 = MotionEvent.obtain(event);
			Log.i("Log", event1.equals(event2) + "");
			return onMertoItemViewListener.onMove(this, event1, event2);
		case MotionEvent.ACTION_UP:
			onMertoItemViewListener.onUp(this);
			return false;
		default:
			return false;
		}

	}
}
