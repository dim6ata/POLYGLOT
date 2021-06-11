package com.example.polyglot;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter class that is used to set up list views.
 * By default the list view lines are white, but when selected they change to black.
 * This is handled by selectedPosition which is updated every time a new item is selected.
 * isSelected boolean is responsible for handling the first time the table is loaded.
 * @author dim6ata
 */
public class CustomAdapter extends ArrayAdapter<String> {

    ArrayList<String> list;
    int selectedPosition;
    Context context;
    boolean isSelected;

    public CustomAdapter(Context context, int resource, ArrayList<String> list) {
        super(context, resource, list);
        this.context = context;
        this.list = list;
        isSelected = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_layout_list, null);

        }
        TextView tv = view.findViewById(R.id.list_text_id);
        tv.setText(list.get(position));


        if (isSelected) {

            if (this.selectedPosition == position) {//when selected the background is changed to black.
                tv.setBackgroundColor(Color.BLACK);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundColor(Color.WHITE);//when not selected the background is changed to white
                tv.setTextColor(Color.BLACK);
            }

        } else {//when nothing is selected background is white.
            tv.setBackgroundColor(Color.WHITE);
            tv.setTextColor(Color.BLACK);
        }
        return view;
    }

    /**
     * used to set the position, so that selection in different list views can be changed.
     *
     * @param selectedPosition new selected position.
     */
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
