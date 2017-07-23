package com.example.android.booklist8;

/**
 * Created by Csaba on 11/07/2017.
 */
public class Book {

    private double mRating;
    private String mTitle;
    private String mAuthor;
    private String mCategory;
    private String mPicture;
    private String ProvidedUrl;

    public Book(double mRating, String mTitle, String mAuthor, String mCategory, String mPicture, String url) {
        this.mRating = mRating;
        this.mTitle = mTitle;
        this.mAuthor = mAuthor;
        this.mCategory = mCategory;
        this.mPicture = mPicture;
        this.ProvidedUrl = url;
    }

    public double getmRating() {
        return mRating;
    }

    public String getPrividedUrl() {
        return ProvidedUrl;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmAuthor() {
        String authors = checkAuthors();
        return authors;
    }

    public String getmCategory() {
        return mCategory;
    }

    public String getmPicture() {
        return mPicture;
    }

    /** if there is more authors */
    public String checkAuthors() {return mAuthor;}
}

