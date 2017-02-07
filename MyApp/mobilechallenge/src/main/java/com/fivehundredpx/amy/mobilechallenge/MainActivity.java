package com.fivehundredpx.amy.mobilechallenge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends FragmentActivity {

    private final String DEBUG_JSON = "DEBUG_JSON/";
    private final int RESULTS_PER_PAGE = 18;

    private ImageAdapter mImageAdapter;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Instantiate a ViewPager and a PagerAdapter.
        mViewPager = (ViewPager) findViewById(R.id.fullScreenPager);
        mPagerAdapter = new FullScreenPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        // Instantiate gridview and adapter
        GridView gridview = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(mImageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mViewPager.setVisibility(View.VISIBLE);
            }
        });

        getAllPopularPhotos(1);
    }

    // TODO: re-write this to close view pager
    @Override
    public void onBackPressed() {
        if (mViewPager.getVisibility() == View.GONE) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setVisibility(View.GONE);
        }
    }

    public void getAllPopularPhotos(int pageNum) /* throws JSONException */ {

        RequestParams params = new RequestParams();
        params.put("consumer_key", "3xpH2xqdDBbJFSAjVuQCIma2RvOyWFOusJvY61RW");
        params.put("feature", "popular");
        params.put("sort", "created_at");
        params.put("image_size", "4"); // Set to max image size
        params.put("page", Integer.toString(pageNum));
        params.put("rpp", Integer.toString(RESULTS_PER_PAGE));
        params.put("include_store", "store_download");
        params.put("include_states", "voted");

        RestClient.get("/v1/photos", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    // Parse the response into list of photo objects -- passes that on post to image adapter.
                    JSONArray photos = response.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        new ParseAndDownloadImageTask().execute(photos.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    System.out.println(DEBUG_JSON + e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, java.lang.String responseString, java.lang.Throwable throwable) {
                Toast.makeText(getBaseContext(), "Error Downloading, check internet connection", Toast.LENGTH_SHORT).show();
                System.out.println(DEBUG_JSON + statusCode);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, java.lang.Throwable throwable, JSONObject response) {
                Toast.makeText(getBaseContext(), "Error Downloading, check internet connection", Toast.LENGTH_SHORT).show();
                System.out.println(DEBUG_JSON + statusCode);
            }
        });
    }

    private class ParseAndDownloadImageTask extends AsyncTask<JSONObject, Void, Photo> {

        public ParseAndDownloadImageTask() {
            // Nothing to do
        }

        protected Photo doInBackground(JSONObject... photoInfo) {
            JSONObject photoJSONObj = photoInfo[0];
            Photo photo = null;
            Bitmap bitmap;
            String url, name, desc, uploader;
            int votes;

            try {
                // Bitmap
                url = photoJSONObj.getString("image_url");
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
                if (bitmap == null) return null; // TODO: throw exception?

                // Name
                name = photoJSONObj.isNull("name") ? "" : photoJSONObj.getString("name");
                // Description
                desc = photoJSONObj.isNull("description") ? "" : photoJSONObj.getString("description");
                // Uploader
                uploader = photoJSONObj.getJSONObject("user").isNull("username") ?
                        "Anon" : photoJSONObj.getJSONObject("user").getString("username");
                // Votes
                votes = photoJSONObj.isNull("votes_count") ? 0 : Integer.parseInt(photoJSONObj.getString("votes_count"));

                // Create new object here. Add to list
                photo = new Photo(bitmap, name, desc, uploader, votes);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return photo;
        }

        protected void onPostExecute(Photo result) {
            mImageAdapter.add(result); // TODO: null check result, either here or above
            mImageAdapter.notifyDataSetChanged();
            mPagerAdapter.notifyDataSetChanged(); // Necessary since we're using mImageAdapters data
        }
    }

    private class FullScreenPagerAdapter extends FragmentStatePagerAdapter {


        public FullScreenPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // TODO: re-write for infinite scrolling
        @Override
        public Fragment getItem(int position) {
            FullScreenFragment fragment = new FullScreenFragment();
            // Null check in case of instantiation issues

            if (mImageAdapter != null) {
                fragment.setPhoto(mImageAdapter.getPhoto(position));
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Null check in case of instantiation issues
            return mImageAdapter == null ? 0 : mImageAdapter.getCount();
        }
    }
}

/* TODO list:
 *
 * P1:
 *      Pagination
 *          grid and full screen? Probably both
 *          two components:
 *              replace fullscreen imageview with viewpager to page through -- data remains in imageadapter, no need to consolidate into its own manager
 *              add pager icons to bottom of grid -- reload image content
 *          restrict fullscreen pagination to current gridview page of results -- easier? we can build on it later.
 *          *important -- when flipping through viewpager, must move grid up along to accomodate it.
 *
 *          Might want data adapter. If we can't pull from the image adapter properly ...
 * P2:
 *      Truncate excess description text
 *      re-try on load failure
 *      pull larger res images on tap
 */