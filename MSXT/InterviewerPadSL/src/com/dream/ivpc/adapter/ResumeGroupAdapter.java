package com.dream.ivpc.adapter;

import java.util.ArrayList;
import java.util.List;

import com.dream.ivpc.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ResumeGroupAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
//	private static final int[] ids = { R.drawable.cupcake, R.drawable.donut, R.drawable.eclair, R.drawable.froyo,
//			R.drawable.gingerbread, R.drawable.honeycomb, R.drawable.icecream };
	private List<Bitmap> bitmapList = new ArrayList<Bitmap>();

	public ResumeGroupAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public ResumeGroupAdapter(Context context,List<Bitmap> bitmapList) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.bitmapList = bitmapList;
	}

	@Override
	public int getCount() {
//		return ids.length;
		return bitmapList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.candidate_resume_group_item, null);
		}
//		((ImageView) convertView.findViewById(R.id.imgView)).setImageResource(ids[position]);
		((ImageView) convertView.findViewById(R.id.imgView)).setImageBitmap(bitmapList.get(position));
		
		return convertView;
	}
}
