package vas.coupgon.rsoccer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Vas on 11/11/2015.
 */
public class ThreadEntryAdapter extends ArrayAdapter<ThreadValues> {

    private Context context;
    private ThreadValues[] data;

    public ThreadEntryAdapter(Context context, int layoutResourceId, ThreadValues[] data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
    }

    //When our listview requests a thread entry, we have to inflate the row with the layout and populate it with data
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        ImageView thumbnail = (ImageView) rowView.findViewById(R.id.thumbnail);
        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView author = (TextView) rowView.findViewById(R.id.author);
        TextView date = (TextView) rowView.findViewById(R.id.date);

        ThreadValues data = this.data[position];
        Drawable thumbDrawable = data.getThumbnail();
        //sometimes there may be no thumbnail, so we'll only set display it if it exists
        if (thumbDrawable != null) {
            thumbnail.setImageDrawable(thumbDrawable);
        }
        title.setText(data.getTitle());
        author.append(data.getAuthor());
        date.append(data.getDate());

        return rowView;
    }
}
