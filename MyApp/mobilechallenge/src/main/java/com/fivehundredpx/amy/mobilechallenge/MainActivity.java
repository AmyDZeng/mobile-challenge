package com.fivehundredpx.amy.mobilechallenge;

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
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private String DEBUG_JSON = "DEBUG_JSON/";
    private ImageAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(mImageAdapter);

        getPopularPhotos();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO: Full screen
            }
        });
    }

    public void getPopularPhotos() /* throws JSONException */ {

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
                    // Parse the response, only need photo urls.
                    JSONArray photos = response.getJSONArray("photos");
                    ArrayList<String> urlList = new ArrayList<String>();
                    for (int i = 0; i < photos.length(); i++) {
                        urlList.add(photos.getJSONObject(i).getString("image_url"));
                    }

                    new DownloadImageTask().execute(urlList);

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
                System.out.println(DEBUG_JSON + statusCode);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, java.lang.Throwable throwable, JSONObject response) {
                System.out.println(DEBUG_JSON + statusCode);
            }
        });
    }
// TODO: optimal data structs .. ?
    private class DownloadImageTask extends AsyncTask<ArrayList<String>, Void, ArrayList<Bitmap>> {

        public DownloadImageTask() {
            // Nothing to do
        }

        protected ArrayList<Bitmap> doInBackground(ArrayList<String>... urls) {
            ArrayList<String> urlList = new ArrayList<String>();
            ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
            urlList = urls[0];

            try {
                for (int i = 0; i < urlList.size(); i++) {
                    String url = urlList.get(i);
                    InputStream in = new java.net.URL(url).openStream();
                    bitmapArray.add(BitmapFactory.decodeStream(in));
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmapArray;
        }

        protected void onPostExecute(ArrayList<Bitmap> resultList) {
            mImageAdapter.set(resultList);
            mImageAdapter.notifyDataSetChanged();
            Toast.makeText(getBaseContext(), "Download Complete", Toast.LENGTH_SHORT).show();
        }
    }
}

/* TODO list:
 * Populate adapter with actual pics
 * Only need photo information
 * request for popular photos: "GET /v1/photos?consumer_key=3xpH2xqdDBbJFSAjVuQCIma2RvOyWFOusJvY61RW&feature=popular&sort=created_at&image_size=3&include_store=store_download&include_states=voted HTTP/1.1"
 *
 * Pagination --> design choice?
 */