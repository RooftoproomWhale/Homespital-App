package com.kosmo.homespital.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kosmo.homespital.R;
import com.kosmo.homespital.model.ReservationItem;
import com.ramotion.foldingcell.FoldingCell;

import java.util.HashSet;
import java.util.List;

public class FoldingCellListAdapter extends ArrayAdapter<ReservationItem> {
    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;

    public FoldingCellListAdapter(Context context, List<ReservationItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // get item for selected view
        ReservationItem item = getItem(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.reservation_cell, parent, false);
            // binding view parts to view holder
            viewHolder.hosp_name = cell.findViewById(R.id.title_hospname);
            viewHolder.dept_name = cell.findViewById(R.id.title_dept);
            viewHolder.res_date = cell.findViewById(R.id.title_date_label);
            viewHolder.res_time = cell.findViewById(R.id.title_time_label);
            viewHolder.sel_symp = cell.findViewById(R.id.title_symp);
            viewHolder.approved = cell.findViewById(R.id.title_approved);
            viewHolder.address = cell.findViewById(R.id.title_address);
            viewHolder.apply_date = cell.findViewById(R.id.title_apply);
            cell.setTag(viewHolder);
        } else {
            // for existing cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }

        if (null == item)
            return cell;

        // bind data from selected element to view through view holder
        viewHolder.hosp_name.setText(item.getHosp_name());
        viewHolder.dept_name.setText(item.getDept_name());
        viewHolder.res_date.setText(item.getRes_date());
        viewHolder.res_time.setText(item.getRes_time());
        viewHolder.sel_symp.setText(item.getSel_symp());
        viewHolder.approved.setText(item.getApproved());
        viewHolder.address.setText(item.getAddress());
        viewHolder.apply_date.setText(item.getApply_date());

        /*// set custom btn handler for list item from that item
        if (item.getRequestBtnClickListener() != null) {
            viewHolder.contentRequestBtn.setOnClickListener(item.getRequestBtnClickListener());
        } else {
            // (optionally) add "default" handler if no handler found in item
            viewHolder.contentRequestBtn.setOnClickListener(defaultRequestBtnClickListener);
        }*/

        return cell;
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView hosp_name;
        TextView dept_name;
        TextView res_date;
        TextView res_time;
        TextView sel_symp;
        TextView approved;
        TextView address;
        TextView apply_date;
    }
}
