package vas.coupgon.rsoccer;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Vas on 11/11/2015.
 */

//More or less basic POJO to hold data for each thread entry
public class ThreadValues {
    public Drawable thumbnail;
    public String title;
    public String author;
    public String date;

    public ThreadValues(Drawable thumbnail, String title, String author, long unixDate) {
        this.title = title;
        this.author = author;
        this.date = convertUnixDate(unixDate);
        this.thumbnail = thumbnail;
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    private String convertUnixDate(long unixDate) {
        Date date = new Date(unixDate * 1000L); //convert to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yy 'at' HH:mm");
        return sdf.format(date);
    }

}
