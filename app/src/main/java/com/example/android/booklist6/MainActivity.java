package com.example.android.booklist6;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView) findViewById(R.id.list);
        userInput = (EditText) findViewById(R.id.editText);
        SearchBtn = (Button) findViewById(R.id.searchBtn);

        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userBookSearch = userInput.getText().toString().replace(" ", "+");

                if (userBookSearch.trim().length() <= 0 || userBookSearch.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Type your search", Toast.LENGTH_LONG).show();
                } else {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                }
            }
        });

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
                //Log.e(LOG_TAG, "In DoInBackground Override Method: ",e);
            }

            ArrayList<Book> books = extractBookFromJson(jsonResponce);

            userBookSearch = "";

            return books;
            //   return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
//            super.onPostExecute(books);
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
                //       Log.e(LOG_TAG, "Response code: "+urlConnection.getResponseCode());
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                //       Log.e(LOG_TAG,"Error Response Code: "+urlConnection.getResponseCode()+ "URl COnnection: "+url.toString());
            }
        } catch (IOException e) {
            //     Log.e(LOG_TAG, "Error Retrieving Book JSON Results", e);
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
            JSONArray bookArray = baseJsonObject.getJSONArray("items");
            int length = bookArray.length();

            for (int i = 0; i < length; i++) {
                String author = "Author: ";
                String category = "";
                // String publisher="Publisher: ";
                double rating;
                String urlJsonLink = "";

                JSONObject bookObject = bookArray.getJSONObject(i);
                JSONObject bookInfo = bookObject.getJSONObject("volumeInfo");
                JSONObject bookPictures = bookInfo.getJSONObject("imageLinks");
                String picture = bookPictures.getString("thumbnail");

                String title = bookInfo.getString("title");
                // publisher += bookInfo.getString("publisher");
                if (bookInfo.isNull("averageRating")) {
                    rating = 5;
                } else {
                    rating = bookInfo.getDouble("averageRating");
                }
                urlJsonLink = bookInfo.getString("previewLink");

                JSONArray authorJson = bookInfo.getJSONArray("authors");

                if (authorJson.length() > 0) {
                    for (int j = 0; j < authorJson.length(); j++) {
                        author += authorJson.optString(j) + " ";
                    }
                }

                JSONArray categories = bookInfo.getJSONArray("categories");
                if (categories.length() > 0) {
                    for (int j = 0; j < categories.length(); j++) {
                        category += categories.optString(j) + " ";
                    }
                }
                books.add(new Book(rating, title, author, category, picture, urlJsonLink));
            }
        } catch (JSONException e) {
        }
        return books;
    }

    /**
     * update UI
     */
    private void UpdateUi(ArrayList<Book> books) {
        //ArrayList tempBookArrayList = books;
        ListView bookListView = (ListView) findViewById(R.id.list);
        BookAdapter adapter = new BookAdapter(this, books);
        bookListView.setAdapter(adapter);


    }

}




