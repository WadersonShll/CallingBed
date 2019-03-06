package com.wdkl.callingbed.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdkl.callingbed.R;
import com.wdkl.callingbed.entity.CostDataEntity.CostArray;

import java.util.List;


/**
 * Created by 胡博文 on 2017/9/6.
 */

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.ViewHolder> {
    public List<CostArray> list;

    public CostAdapter(List<CostArray> list) {
        this.list = list;
    }

    public void upDateList(List<CostArray> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cost_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (null != list) {
            if(position==0){
                viewHolder.tvCostDate.setText("日期");
                viewHolder.tvCostContent.setText("费用名称");
                viewHolder.tvCostCost.setText("金额");

                viewHolder.rlTitle.setBackgroundResource(R.color.cost_list_title);
            }else{
                if((position-1)%2==0){
                    viewHolder.rlTitle.setBackgroundResource(R.color.cost_item);
                }else{
                    viewHolder.rlTitle.setBackgroundResource(R.color.cost_item);
                }
                viewHolder.tvCostDate.setText(list.get(position-1).getDate());
                viewHolder.tvCostContent.setText(list.get(position-1).getCostName());
                viewHolder.tvCostCost.setText(list.get(position-1).getMoney());
            }
        }
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
            return list.size()+1;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCostDate;
        public TextView tvCostContent;
        public TextView tvCostCost;
        public RelativeLayout rlTitle;

        public ViewHolder(View view) {
            super(view);
            tvCostDate = view.findViewById(R.id.item_cost_layout_tv_date);
            tvCostContent = view.findViewById(R.id.item_cost_layout_tv_content);
            tvCostCost = view.findViewById(R.id.item_cost_layout_tv_cost);
            rlTitle = view.findViewById(R.id.item_cost_layout_rl_title);
        }
    }
}
