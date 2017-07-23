package com.example.android.booklist8;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private Button SearchBtn;
    private String userBookSearch;
    private TextView emptyTextView;
    private int length;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView) findViewById(R.id.list);
        userInput = (EditText) findViewById(R.id.editText);
        SearchBtn = (Button) findViewById(R.id.searchBtn);

        //set empty state TextView
        emptyTextView = (TextView)findViewById(R.id.text_empty_list);
        bookListView.setEmptyView(emptyTextView);

        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userBookSearch = userInput.getText().toString().replace(" ", "+");

                if (userBookSearch.trim().length() <= 0 || userBookSearch.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Type your search", Toast.LENGTH_LONG).show();
                } else {
                    checkConnectivity();
                }
            }
        });
    }

    /**chech the internet connection and execute BookAsyncTask  */
    private void checkConnectivity() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            BookAsyncTask task = new BookAsyncTask();
            task.execute();
        } else
        // Otherwise, display error
        Toast.makeText(this,"sorry, NO INTERNET connectivity",Toast.LENGTH_LONG).show();
    }


    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {
        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {
            URL url = null;
            url = createUrl(userBookSearch.trim());

            String jsonResponce = "";
            try {
                jsonResponce = makeHttpRequest(url);
            } catch (IOException e) {
                // "In DoInBackground Override Method";
            }

            ArrayList<Book> books = extractBookFromJson(jsonResponce);

            userBookSearch = "";

            return books;

        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            if (books == null) {
                return;
            }
            UpdateUi(books);
        }
    }


    /**
     * #1 create URL
     */
    private URL createUrl(String SearchItem) {
        String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=";
        String completeUrl = baseUrl + SearchItem.replace(" ", "%20");
        URL url = null;
        try {
            url = new URL(completeUrl);
        } catch (MalformedURLException e) {
        }
        return url;
    }

    /**
     * #2 create http request
     */
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
            }
        } catch (IOException e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * #2.2 readFromStream for jsonResponse
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder OutputString = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            line = reader.readLine();
            while (line != null) {
                OutputString.append(line);
                line = reader.readLine();
            }
        }
        return OutputString.toString();
    }

    /**
     * #3 the fetching - extractBookFromJson
     */
    private ArrayList<Book> extractBookFromJson(String bookJson) {
        if (TextUtils.isEmpty(bookJson)) {
            return null;
        }

        ArrayList<Book> books = new ArrayList<>();

        try {
            JSONObject baseJsonObject = new JSONObject(bookJson);
            /** check if there is "item" in the JSONOnject*/
            if (baseJsonObject.has("items")) {

                JSONArray bookArray = baseJsonObject.getJSONArray("items");
                int length = bookArray.length();

                for (int i = 0; i < length; i++) {

                    String category = "";
                    double rating;
                    String urlJsonLink = "";

                    JSONObject bookObject = bookArray.getJSONObject(i);
                    JSONObject bookInfo = bookObject.getJSONObject("volumeInfo");
                    JSONObject bookPictures = bookInfo.getJSONObject("imageLinks");
                    String picture = bookPictures.getString("thumbnail");

                    /** check the title */
                    String title;
                    if (bookInfo.has("title")) {
                        title = bookInfo.getString("title");
                    } else {
                        title = "No Title";
                    }

                    /** check rating */
                    if (bookInfo.isNull("averageRating")) {
                        rating = 0;
                    } else {
                        rating = bookInfo.getDouble("averageRating");
                    }

                    urlJsonLink = bookInfo.getString("previewLink");


                    /** authors again*/
                    JSONArray authorsArray;
                    ArrayList<String> authors = new ArrayList<String>();

                    if (bookInfo.has("authors")) {
                        authorsArray = bookInfo.getJSONArray("authors");
                        for (int j = 0; j < authorsArray.length(); j++) {
                            authors.add(authorsArray.getString(j));
                        }
                    } else {
                        authors.add("Uknown Author");
                    }


                    JSONArray categories = bookInfo.getJSONArray("categories");
                    if (categories.length() > 0) {
                        for (int j = 0; j < categories.length(); j++) {
                            category += categories.optString(j) + " ";
                        }
                    }
                    books.add(new Book(rating, title, authors.toString(), category, picture, urlJsonLink));
                }
            }
            } catch(JSONException e){
            }
            return books;
        }


    /**
     * #4 update UI
     */
    private void UpdateUi(ArrayList<Book> books) {

        ListView bookListView = (ListView) findViewById(R.id.list);
        BookAdapter adapter = new BookAdapter(this, books);

        /** If there is no result, send a message */
        if (books.isEmpty()){
            Toast.makeText(this, "no result, Try it again", Toast.LENGTH_LONG).show();
            emptyTextView.setText("No result found!");
        }
        bookListView.setAdapter(adapter);
    }
}




