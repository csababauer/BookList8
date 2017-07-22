package com.example.android.booklist8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Csaba on 11/07/2017.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, ArrayList<Book> books)
    {
        super(context,0,books);
    }

    @Override
    public View getView(int position, View currentView, ViewGroup parent)
    {
        View listViewItem= currentView;

        if (listViewItem == null)
        {
            listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.activity_list, parent, false);
        }

        Book currentBook = getItem(position);

        ImageView bookImage = (ImageView)listViewItem.findViewById(R.id.book_image);

        /** Picasso picture adapter - simple add 1 dependency to build.gradle (Module:app) file */
        Picasso.with(getContext()).load(currentBook.getmPicture()).into(bookImage);

        TextView bookTitle = (TextView)listViewItem.findViewById(R.id.book_title);
        bookTitle.setText(currentBook.getmTitle());

        TextView bookAuthor = (TextView)listViewItem.findViewById(R.id.book_author);
        bookAuthor.setText(currentBook.getmAuthor());

        TextView bookCategory = (TextView)listViewItem.findViewById(R.id.book_category);
        bookCategory.setText(currentBook.getmCategory());

        RatingBar bookRating = (RatingBar)listViewItem.findViewById(R.id.book_rating);
        bookRating.setRating((float ) currentBook.getmRating());

        return listViewItem;
    }
}
