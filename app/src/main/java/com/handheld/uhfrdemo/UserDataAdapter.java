package com.handheld.uhfrdemo;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handheld.uhfr.R;

public class UserDataAdapter extends BaseAdapter {
    private List<UserDataModel> list ;
    private Context context ;

    public UserDataAdapter(Context context , List<UserDataModel> list){
        this.context = context ;
        this.list = list ;
    }
    @Override

    public int getCount() {
        return list.size() ;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            UserDataAdapter.ViewHolder holder;
            if (convertView == null) {
                holder = new UserDataAdapter.ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_user_date, null);
                holder.tvTagEPC = (TextView) convertView.findViewById(R.id.textView_epc);
                holder.tvNfcUid = (TextView) convertView.findViewById(R.id.textView_nfc_uid);
                holder.tvStartTime = (TextView) convertView.findViewById(R.id.textView_start_time);
                holder.tvTemperaure = (TextView) convertView.findViewById(R.id.textView_temperature);
                holder.tvCount = (TextView) convertView.findViewById(R.id.textView_count);
                holder.tvInterval = (TextView) convertView.findViewById(R.id.textView_interval);
                holder.tvReadCount = (TextView) convertView.findViewById(R.id.textview_readcount);
                holder.tvOutOfLimit = (TextView) convertView.findViewById(R.id.textView_outof_limit);
                //holder.tvMiniLimit = (TextView) convertView.findViewById(R.id.textView_minLimit);
               // holder.tvMaxLimit = (TextView) convertView.findViewById(R.id.textView_maxLimit);
                //holder.tvCurrenTime = (TextView) convertView.findViewById(R.id.textView_currentime);
                convertView.setTag(holder);
            } else {
                holder = (UserDataAdapter.ViewHolder) convertView.getTag();
            }
            if (list != null && !list.isEmpty()) {
                int id = position + 1;
                holder.tvTagEPC.setText(list.get(position).tagEPC);
                //holder.tvTagEPC.setText(list.get(position).lastDataByte);
                holder.tvNfcUid.setText(list.get(position).nfcUid);
                holder.tvStartTime.setText(list.get(position).startTime);
                holder.tvTemperaure.setText("" + list.get(position).temperaure);
                holder.tvCount.setText(list.get(position).count);
                holder.tvInterval.setText(list.get(position).interval);
                holder.tvOutOfLimit.setText(list.get(position).outOfLimit);
                holder.tvReadCount.setText(Integer.toString(list.get(position).readCount));
                //holder.tvMiniLimit.setText(list.get(position).miniLimit);
                //holder.tvMaxLimit.setText("" + list.get(position).maxLimit);
                //holder.tvCurrenTime.setText(list.get(position).currenTime);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {
        TextView tvTagEPC;
        TextView tvNfcUid ;
        TextView tvStartTime ;
        TextView tvTemperaure ;
        TextView tvCount;
        TextView tvInterval ;
        TextView tvMiniLimit ;
        TextView tvMaxLimit ;
        TextView tvCurrenTime;
        TextView tvReadCount;
        TextView tvOutOfLimit;
    }
}

