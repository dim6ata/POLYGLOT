/**
 * REFERENCES:
 * <p>
 * https://stackoverflow.com/questions/44193509/android-toast-set-gravity-causing-app-to-crash - how to create a custom Toast (Ahamed, 2017)
 */

package com.example.polyglot;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A custom made Toast class that displays an image as well as customised fonts to notify user when needed.
 * @author dim6ata
 */
public class ToastedToast {

    private Toast toast;
    private Context context;
    private int offset;
    private ImageView imageView;
    TextView textView;

    /**
     * Constructor for toasted toast.
     *
     * @param context provides the context, i.e activity which calls this class.
     * @param yOffset provides the offset in y direction.
     */
    public ToastedToast(Context context, int yOffset) {

        this.context = context;
        this.offset = yOffset;
        toast = new Toast(context);

    }

    /**
     * sets the custom toast values.
     *
     * @param text     the text that will be displayed.
     * @param duration the duration that the text would be displayed.
     */
    public void setToast(String text, int duration) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        textView = layout.findViewById(R.id.customToast);
        imageView = layout.findViewById(R.id.img_custom_toast);
        textView.setText(text);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, offset);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();

    }


}
