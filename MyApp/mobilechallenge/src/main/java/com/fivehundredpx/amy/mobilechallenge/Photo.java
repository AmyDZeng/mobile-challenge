package com.fivehundredpx.amy.mobilechallenge;

import android.graphics.Bitmap;
import android.widget.TextView;

/**
 * Created by MacAir on 2017-02-06.
 */
public class Photo {
    private Bitmap mBitmap;
    private String mName;
    private String mDescription;
    private String mUploader;
    private int mVotes;

    public Photo(Bitmap bitmap, String name, String description, String uploader, int votes) {
        mBitmap = bitmap;
        mName = name;
        mDescription = description;
        mUploader = uploader;
        mVotes = votes;
    }
// TODO: mvc?
    public void updateFullScreenInfo(TextView textView) {
        String myText = "";
        myText += mName;
        myText += "\nBy: " + mUploader;
        myText += "\n" + mDescription;
        myText += "\n\nVotes: " + mVotes;

        textView.setText(myText);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
