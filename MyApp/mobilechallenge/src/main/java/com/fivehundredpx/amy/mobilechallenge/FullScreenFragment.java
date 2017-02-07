package com.fivehundredpx.amy.mobilechallenge;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Malzberry on 2/7/2017.
 */

public class FullScreenFragment extends Fragment {

    Photo mPhoto;

    public void setPhoto(Photo photo) {
        mPhoto = photo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_full_screen, container, false);
        // Configure view here
        ImageView imageView = (ImageView) rootView.findViewById(R.id.fullImage);
        TextView textView = (TextView) rootView.findViewById(R.id.textDescription);

        // Views could be uninstantiated???
        if (imageView != null) {
            imageView.setImageBitmap(mPhoto.getBitmap());
        }

        if (textView != null) {
            textView.setText(mPhoto.getFullScreenInfo());
        }

        return rootView;
    }
}
