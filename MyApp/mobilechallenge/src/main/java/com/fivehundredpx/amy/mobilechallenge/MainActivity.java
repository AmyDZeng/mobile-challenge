package com.fivehundredpx.amy.mobilechallenge;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static java.security.AccessController.getContext;

public class MainActivity extends Activity {

    private String DEBUG_JSON = "DEBUG_JSON/";
    private ImageAdapter mImageAdapter;
    private RelativeLayout mFullScreenLayout;
    private ImageView mFullScreenImageView;
    private TextView mFullScreenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mFullScreenLayout = (RelativeLayout) findViewById(R.id.photoView);
        mFullScreenImageView = (ImageView) findViewById(R.id.fullImage);
        mFullScreenTextView = (TextView) findViewById(R.id.textDescription);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(mImageAdapter);

        getAllPopularPhotos();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO: update with info as well
                populateFullScreen(position);
                mFullScreenLayout.setVisibility(View.VISIBLE);

            }
        });

        mFullScreenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: reset image as well?
                // TODO: disable touch events? necessary?
                mFullScreenLayout.setVisibility(View.GONE);
            }
        });
    }

    public void populateFullScreen(int position) {
        mFullScreenImageView.setImageBitmap(mImageAdapter.getBitmap(position));
        mImageAdapter.getPhoto(position).updateFullScreenInfo(mFullScreenTextView);
    }

    public void getAllPopularPhotos() /* throws JSONException */ {

        RequestParams params = new RequestParams();
        params.put("consumer_key", "3xpH2xqdDBbJFSAjVuQCIma2RvOyWFOusJvY61RW");
        params.put("feature", "popular");
        params.put("sort", "created_at");
        params.put("image_size", "3");
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
            public void onSuccess(int statusCode, Header[] headers, JSONArray photos) {
                // Won't be called
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
            Toast.makeText(getBaseContext(), "Download Complete", Toast.LENGTH_SHORT).show();
        }
    }
}

/* TODO list:
 *
 * P1:
 *      Pagination
 *          grid and full screen? Probably both
 *      screen rotation
 * P2:
 *      Truncate excess description text
 *      re-try on load failure
 */