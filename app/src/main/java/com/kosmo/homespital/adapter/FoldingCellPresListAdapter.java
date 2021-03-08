package com.kosmo.homespital.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kosmo.homespital.R;
import com.kosmo.homespital.model.PrescriptionItem;
import com.kosmo.homespital.model.ReservationItem;
import com.ramotion.foldingcell.FoldingCell;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;

public class FoldingCellPresListAdapter extends ArrayAdapter<PrescriptionItem> {
    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;
    private Context context;

    public FoldingCellPresListAdapter(Context context, List<PrescriptionItem> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // get item for selected view
        PrescriptionItem item = getItem(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.prescription_cell, parent, false);
            // binding view parts to view holder
            viewHolder.TITLE_MEDI_NAME = cell.findViewById(R.id.title_medi_name);
            viewHolder.ITEM_NAME = cell.findViewById(R.id.medicine_item_name);
            viewHolder.ITEM_IMAGE = cell.findViewById(R.id.medicine_item_image);
            viewHolder.ENTP_NAME = cell.findViewById(R.id.medicine_item_entp_name);
            viewHolder.CHART = cell.findViewById(R.id.medicine_item_chart);
            viewHolder.DRUG_SHAPE = cell.findViewById(R.id.medicine_drug_shape);
            viewHolder.MATERIAL_NAME = cell.findViewById(R.id.medicine_material_name);
            viewHolder.STORAGE_METHOD = cell.findViewById(R.id.medicine_storage);
            viewHolder.VALID_TERM = cell.findViewById(R.id.medicine_valid_term);
            viewHolder.pres_date = cell.findViewById(R.id.title_from_date);
            viewHolder.end_date = cell.findViewById(R.id.title_to_date);
            viewHolder.duration = cell.findViewById(R.id.title_pres_count);
            viewHolder.count = cell.findViewById(R.id.title_dailyPres_count);
            viewHolder.addtionalBtn = cell.findViewById(R.id.addtionalBtn);
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
        viewHolder.TITLE_MEDI_NAME.setText(item.getITEM_NAME().substring(0,item.getITEM_NAME().indexOf("(")));
        viewHolder.ITEM_NAME.setText(item.getITEM_NAME());
        viewHolder.ENTP_NAME.setText(item.getENTP_NAME());
        viewHolder.MATERIAL_NAME.setText(item.getMATERIAL_NAME());
        viewHolder.STORAGE_METHOD.setText(item.getSTORAGE_METHOD());
        viewHolder.VALID_TERM.setText(item.getVALID_TERM());
        viewHolder.CHART.setText(item.getCHART());
//        viewHolder.COLOR_CLASS1.setText(item.getCOLOR_CLASS1());
        viewHolder.DRUG_SHAPE.setText(item.getDRUG_SHAPE());
        Picasso.get().load(item.getITEM_IMAGE()).into(viewHolder.ITEM_IMAGE);
        viewHolder.pres_date.setText(item.getPres_date());
        viewHolder.end_date.setText(item.getPres_endDate());
        viewHolder.duration.setText(item.getDuration()+"일");
        viewHolder.count.setText(item.getCount()+"번");

        // set custom btn handler for list item from that item
        if (item.getAddtionalBtnClickListener() != null) {
            viewHolder.addtionalBtn.setOnClickListener(item.getAddtionalBtnClickListener());
        } else {
            // (optionally) add "default" handler if no handler found in item
            viewHolder.addtionalBtn.setOnClickListener(defaultRequestBtnClickListener);
        }

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
        TextView TITLE_MEDI_NAME;
        TextView ITEM_NAME;
        TextView ENTP_NAME;
        TextView MATERIAL_NAME;
        TextView STORAGE_METHOD;
        TextView VALID_TERM;
        TextView CHART;
        TextView COLOR_CLASS1;
        TextView DRUG_SHAPE;
        ImageView ITEM_IMAGE;
        TextView pres_date;
        TextView end_date;
        TextView duration;
        TextView count;
        TextView addtionalBtn;

    }
}
