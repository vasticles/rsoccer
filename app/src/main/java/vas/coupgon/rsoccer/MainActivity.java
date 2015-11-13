package vas.coupgon.rsoccer;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private static ListView threadList; //Le listview to show our reddit threads
    private static final String PREFIX = "http://www.reddit.com/r/"; //reddit url
    private static final String SUFFIX = ".json"; //secret reddit api accessor
    private String errorMessage; //we'll use this field to store the halting error, which we will later display in a toast


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SubredditThreadsFragment())
                    .commit();
        }

        //The task to pretty much do everything we need
        ConnectToSubredditAndGetThreadsListingsTask task = new ConnectToSubredditAndGetThreadsListingsTask();
        task.execute(PREFIX+"soccer"+SUFFIX);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //This is our fragment that holds the listview. A bit of an overkill for just a list, but whatevz
    public static class SubredditThreadsFragment extends Fragment {

        public SubredditThreadsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        //Once activity is created, we can find the resources (our listview) within the fragment layout
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            threadList = (ListView) getView().findViewById(R.id.topThreads);
        }
    }

    //Our asynctask to connect to the subreddit, obtain a response, convert it to JSON, parse it,
    //populate the array with the data, and finally pass the data to the adapter and listview
    private class ConnectToSubredditAndGetThreadsListingsTask extends AsyncTask<String, Void, ThreadValues[]> {

        public ConnectToSubredditAndGetThreadsListingsTask() {}

        @Override
        protected ThreadValues[] doInBackground(String... url) {
            URL redditUrl;
            String jsonResponse = null;

            //Create a url object from the passed string
            try {
                redditUrl = new URL(url[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                errorMessage = "Not a proper URL."; //this will never happen since the prefix is already a valid url, but just to cover all bases.
                return null;
            }

            //If it's a valid url (made it this far), let's try and open a connection to get the json response
            try {
                HttpURLConnection connection = (HttpURLConnection) redditUrl.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                //if response code is OK, let's process the inputstream into a workable string
                switch (responseCode) {
                    case 200:
                    case 201:
                        InputStream is = new BufferedInputStream(connection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            sb.append(line);
                        }
                        jsonResponse = sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "Response code not OK. Failed to receive a proper response.";
                return null;
            }

            //we need to parse the string into a json object so that we can work with the data in it
            //the helper method will take a string and return an array of thread entries and the their values
            return parseJsonResponse(jsonResponse);
        }


        //helper method to avoid clutter in our doInBackground()
        private ThreadValues[] parseJsonResponse(String jsonResponse) {
            if (jsonResponse == null) {
                return null;
            } else {
                try {
                    //Have to dig down the response tree to get the data that we need
                    JSONObject json = new JSONObject(jsonResponse);
                    JSONObject data = json.getJSONObject("data");
                    JSONArray children = data.getJSONArray("children");

                    //prep some variables for our data
                    String title;
                    String author;
                    long date;
                    String thumbUrl;
                    Drawable thumbnail = null;

                    //This is the array that we will use for the array adapter to feed into our listview
                    ThreadValues threadValues[] = new ThreadValues[children.length()];

                    //loop through the children array to get data for each thread entry
                    for (int i=0; i<children.length(); i++) {
                        JSONObject threadData = children.getJSONObject(i).getJSONObject("data");
                        thumbUrl = threadData.getString("thumbnail");
                        title = threadData.getString("title");
                        author = threadData.getString("author");
                        date = threadData.getLong("created");

                        //Since reddit provides a url to the thumbnail, we need to connect to the url and create a drawable from the inputstream
                        //Better do it in the same asynctask to avoid unnecessary overhead
                        if (thumbUrl != null || thumbUrl.length() > 0) {
                            InputStream is = null;
                            try {
                                is = (InputStream) new URL(thumbUrl).getContent();
                                thumbnail = Drawable.createFromStream(is, null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        //Now that we have all the data, create a ThreadValues object and add it to array
                        threadValues[i] = new ThreadValues(thumbnail, title, author, date);
                    }
                    return threadValues;
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorMessage = "Failed to parse JSON."; //in case there were any errors parsing json
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(ThreadValues[] threadValues) {
            //finally take the array of thread values, create an adapter and feed it to our listview
            if (threadValues != null) {
                ThreadEntryAdapter adapter = new ThreadEntryAdapter(MainActivity.this, -1, threadValues);
                threadList.setAdapter(adapter);
            } else {
                //if the array came in null, show a toast notifying the user
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
