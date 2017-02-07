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
import android.widget.Button;
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
    private final int RESULTS_PER_PAGE = 15;

    private int mCurrentPage = 1;
    private int mNumberPages = 1;

    private ImageAdapter mImageAdapter;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private GridView mGridview;
    private Button mLeftButton;
    private Button mRightButton;

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
        mGridview = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        mGridview.setAdapter(mImageAdapter);

        // Instantiate buttons
        mLeftButton = (Button) findViewById(R.id.leftButton);
        mRightButton = (Button) findViewById(R.id.rightButton);
        mLeftButton.setText("<");
        mRightButton.setText(">");

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back
                if (mCurrentPage > 1) {
                    // TODO: need to have data cache -- at least 2 pages ahead of current. When to re-enable button? at end of cache page function ... ?
                    mLeftButton.setEnabled(false);
                    mCurrentPage -= 1;
                    mImageAdapter.invalidateData();
                    getAllPopularPhotos();
                } else {
                    Toast.makeText(getBaseContext(), "Already at first page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go forward
                if (mCurrentPage <= mNumberPages) {
                    mRightButton.setEnabled(false);
                    mCurrentPage += 1;
                    mImageAdapter.invalidateData();
                    getAllPopularPhotos();
                } else {
                    Toast.makeText(getBaseContext(), "Already at last page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // We reset viewpager contents so screen won't flash between old fragments
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setCurrentItem(position);
                mViewPager.setVisibility(View.VISIBLE);
            }
        });

        getAllPopularPhotos();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getVisibility() == View.GONE) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, close the viewpager
            mViewPager.setVisibility(View.GONE);
            mGridview.setSelection(mViewPager.getCurrentItem());
        }
    }
// TODO: modularize data fetch, parse, and display. pass in "display" parameter?
    public void getAllPopularPhotos() /* throws JSONException */ {

        RequestParams params = new RequestParams();
        params.put("consumer_key", "3xpH2xqdDBbJFSAjVuQCIma2RvOyWFOusJvY61RW");
        params.put("feature", "popular");
        params.put("sort", "created_at");
        params.put("image_size", "4"); // Set to max image size
        params.put("page", Integer.toString(mCurrentPage));
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
                    // Update total pages. TODO: null check
                    mNumberPages = response.getInt("total_pages");

                    // Parse and display fetched photos
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
                if (bitmap == null) throw new Exception("Failed loading Bitmap");

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
            if (result != null) {
                // TODO: add in if we're on current page, display. Otherwise just cache?
                mImageAdapter.add(result);
                mImageAdapter.notifyDataSetChanged();
                mPagerAdapter.notifyDataSetChanged(); // Necessary since we're using mImageAdapters data
            }
            if (mImageAdapter.getCount() >= RESULTS_PER_PAGE) {
                // We're finished loading the current page, re-enable buttons
                mLeftButton.setEnabled(true);
                mRightButton.setEnabled(true);
            }
        }
    }

    private class FullScreenPagerAdapter extends FragmentStatePagerAdapter {


        public FullScreenPagerAdapter(FragmentManager fm) {
            super(fm);
        }

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
 *          *important -- when flipping through viewpager, must move grid up along to accomodate it.
 *
 *          Might want data adapter. If we can't pull from the image adapter properly ...
 *
 *          Need to data cache for grid pagination
 * P2:
 *      Truncate excess description text
 *      re-try on load failure
 * Potential bugs:
 *      atm our grid column width works perf for the LG G4, but since the imageadapter scales images based on screen width and height we might have an issue on other devices.
 */