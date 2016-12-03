package me.loganfuller.tostr;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AlarmList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] web;
    private final Integer[] imageId;

    public AlarmList(Activity context, String[] web, Integer[] imageId) {
        super(context, R.layout.item_alarm, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View itemView = inflater.inflate(R.layout.item_alarm, null, true);

        TextView txtAlarmTime = (TextView) itemView.findViewById(R.id.txtAlarmTime);
        TextView txtAlarmDays = (TextView) itemView.findViewById(R.id.txtAlarmDays);

        //txtAlarmTime.setText();

        return itemView;
    }

}
