package com.wdkl.callingbed.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wdkl.callingbed.R;
import com.wdkl.callingbed.entity.DoctorDataEntity.DoctorChargeArray;

import java.util.List;


/**
 * Created by 胡博文 on 2017/9/6.
 */

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
    public List<DoctorChargeArray> list;

    public DoctorAdapter(List<DoctorChargeArray> list) {
        this.list = list;
    }

    public void upDateList(List<DoctorChargeArray> list){
        this.list = list;
        notifyDataSetChanged();
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_doctor_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(null!=list){
            viewHolder.tvItemNum.setText(position+1+"");
            viewHolder.tvItemContent.setText(list.get(position).getDoctorCharge());
        }
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return list.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvItemNum;
        public TextView tvItemContent;

        public ViewHolder(View view) {
            super(view);
            tvItemNum = view.findViewById(R.id.item_doctor_layout_num);
            tvItemContent = view.findViewById(R.id.item_doctor_layout_content);
        }
    }
}