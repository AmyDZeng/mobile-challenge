package com.fivehundredpx.amy.mobilechallenge;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private ArrayList<Photo> mPhotoArray;

    private Context mContext;
    private int mScreenWidth, mScreenHeight;

    public ImageAdapter(Context c) {
        mContext = c;
        mPhotoArray = new ArrayList<>();

        // Grab dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    public void add(Photo photo) {
        mPhotoArray.add(photo);
    }

    public Bitmap getBitmap(int position) {
        return mPhotoArray.get(position).getBitmap();
    }

    public Photo getPhoto(int position) {
        return mPhotoArray.get(position);
    }

    public int getCount() {
        return mPhotoArray.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(mScreenWidth/3, mScreenWidth/3));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(mPhotoArray.get(position).getBitmap());
        return imageView;
    }
}