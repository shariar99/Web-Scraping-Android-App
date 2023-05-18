package com.example.webscraping;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentLayout = findViewById(R.id.parentLayout);

        // Start the AsyncTask to perform web scraping
        new WebScrapingTask().execute();
    }

    private class WebScrapingTask extends AsyncTask<Void, Void, List<MovieData>> {

        @Override
        protected List<MovieData> doInBackground(Void... voids) {
            List<MovieData> movieDataList = new ArrayList<>();

            try {
                // Replace "YOUR_WEBSITE_URL" with the actual URL to scrape data from
                Document document = Jsoup.connect("https://www.themoviedb.org/movie").get();

                // Extract the desired data from the HTML using CSS selectors
                Elements movieElements = document.select("div.card.style_1");

                for (Element movieElement : movieElements) {
                    String movieTitle = movieElement.select("h2 a").text();
                    String releaseDate = movieElement.select("p").text();
                    String imageUrl = movieElement.select("img.poster").attr("src");

                    // Construct the absolute image URL
                    String baseUrl = "https://www.themoviedb.org";
                    String absoluteImageUrl = baseUrl + imageUrl;

                    // Download the movie image as Bitmap
                    Bitmap movieImage = downloadImage(absoluteImageUrl);

                    // Create a MovieData object to hold the data
                    MovieData movieData = new MovieData(movieTitle, releaseDate, movieImage);
                    movieDataList.add(movieData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return movieDataList;
        }

        @Override
        protected void onPostExecute(List<MovieData> movieDataList) {
            super.onPostExecute(movieDataList);

            if (movieDataList != null && !movieDataList.isEmpty()) {
                for (MovieData movieData : movieDataList) {
                    addMovieView(movieData);
                }
            } else {
                TextView textView = new TextView(MainActivity.this);
                textView.setText("Failed to fetch data.");
                parentLayout.addView(textView);
            }
        }
    }

    private Bitmap downloadImage(String imageUrl) {
        try {
            InputStream inputStream = new URL(imageUrl).openStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addMovieView(MovieData movieData) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16);

        LinearLayout movieLayout = new LinearLayout(MainActivity.this);
        movieLayout.setOrientation(LinearLayout.HORIZONTAL);
        movieLayout.setLayoutParams(layoutParams);

        ImageView movieImageView = new ImageView(MainActivity.this);
        movieImageView.setImageBitmap(movieData.getImage());
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                220,
                330
        );
        imageLayoutParams.setMarginEnd(16);
        movieImageView.setLayoutParams(imageLayoutParams);
        movieLayout.addView(movieImageView);

        TextView movieTextView = new TextView(MainActivity.this);
        movieTextView.setText("Title: " + movieData.getTitle() + "\nRelease Date: " + movieData.getReleaseDate());
        movieLayout.addView(movieTextView);

        parentLayout.addView(movieLayout);
    }

    private static class MovieData {
        private String title;
        private String releaseDate;
        private Bitmap image;

        public MovieData(String title, String releaseDate, Bitmap image) {
            this.title = title;
            this.releaseDate = releaseDate;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public Bitmap getImage() {
            return image;
        }
    }
}
