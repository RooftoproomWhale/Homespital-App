package com.kosmo.homespital.model;

import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PrescriptionItem {

    private String ITEM_NAME;
    private String ENTP_NAME;
    private String MATERIAL_NAME;
    private String STORAGE_METHOD;
    private String VALID_TERM;
    private String CHART;
    private String COLOR_CLASS1;
    private String DRUG_SHAPE;
    private String ITEM_IMAGE;
    private String EFFECT;
    private String USAGE;
    private String CAREFUL;
    private String pres_date;
    private String duration;
    private String count;

    private View.OnClickListener addtionalBtnClickListener;


    public PrescriptionItem() {
    }

    public PrescriptionItem(String ITEM_NAME, String ENTP_NAME, String MATERIAL_NAME, String STORAGE_METHOD, String VALID_TERM, String CHART, String COLOR_CLASS1, String DRUG_SHAPE, String ITEM_IMAGE,  String EFFECT, String USAGE, String CAREFUL, String pres_date, String duration, String count) {
        this.ITEM_NAME = ITEM_NAME;
        this.ENTP_NAME = ENTP_NAME;
        this.MATERIAL_NAME = MATERIAL_NAME;
        this.STORAGE_METHOD = STORAGE_METHOD;
        this.VALID_TERM = VALID_TERM;
        this.CHART = CHART;
        this.COLOR_CLASS1 = COLOR_CLASS1;
        this.DRUG_SHAPE = DRUG_SHAPE;
        this.ITEM_IMAGE = ITEM_IMAGE;
        this.EFFECT = EFFECT;
        this.USAGE = USAGE;
        this.CAREFUL = CAREFUL;
        this.pres_date = pres_date;
        this.duration = duration;
        this.count = count;
    }

    public String getEFFECT() {
        return EFFECT;
    }

    public void setEFFECT(String EFFECT) {
        this.EFFECT = EFFECT;
    }

    public String getUSAGE() {
        return USAGE;
    }

    public void setUSAGE(String USAGE) {
        this.USAGE = USAGE;
    }

    public String getCAREFUL() {
        return CAREFUL;
    }

    public void setCAREFUL(String CAREFUL) {
        this.CAREFUL = CAREFUL;
    }

    public String getITEM_NAME() {
        return ITEM_NAME;
    }

    public void setITEM_NAME(String ITEM_NAME) {
        this.ITEM_NAME = ITEM_NAME;
    }

    public String getENTP_NAME() {
        return ENTP_NAME;
    }

    public void setENTP_NAME(String ENTP_NAME) {
        this.ENTP_NAME = ENTP_NAME;
    }

    public String getMATERIAL_NAME() {

        MATERIAL_NAME = MATERIAL_NAME.replace("|","\r\n");
        return MATERIAL_NAME;
    }

    public void setMATERIAL_NAME(String MATERIAL_NAME) {
        this.MATERIAL_NAME = MATERIAL_NAME;
    }

    public String getSTORAGE_METHOD() {
        return STORAGE_METHOD;
    }

    public void setSTORAGE_METHOD(String STORAGE_METHOD) {
        this.STORAGE_METHOD = STORAGE_METHOD;
    }

    public String getVALID_TERM() {
        return VALID_TERM;
    }

    public void setVALID_TERM(String VALID_TERM) {
        this.VALID_TERM = VALID_TERM;
    }

    public String getCHART() {
        return CHART;
    }

    public void setCHART(String CHART) {
        this.CHART = CHART;
    }

    public String getCOLOR_CLASS1() {
        return COLOR_CLASS1;
    }

    public void setCOLOR_CLASS1(String COLOR_CLASS1) {
        this.COLOR_CLASS1 = COLOR_CLASS1;
    }

    public String getDRUG_SHAPE() {
        return DRUG_SHAPE;
    }

    public void setDRUG_SHAPE(String DRUG_SHAPE) {
        this.DRUG_SHAPE = DRUG_SHAPE;
    }

    public String getITEM_IMAGE() {
        return ITEM_IMAGE;
    }

    public void setITEM_IMAGE(String ITEM_IMAGE) {
        this.ITEM_IMAGE = ITEM_IMAGE;
    }

    public String getPres_date() {
        return pres_date;
    }

    public String getPres_endDate()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
        try {
            cal.setTime(format.parse(pres_date));
            cal.add(Calendar.DATE, Integer.parseInt(getDuration())-1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return format.format(cal.getTime());
    }

    public void setPres_date(String pres_date) {
        this.pres_date = pres_date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public View.OnClickListener getAddtionalBtnClickListener() {
        return addtionalBtnClickListener;
    }

    public void setAddtionalBtnClickListener(View.OnClickListener addtionalBtnClickListener) {
        this.addtionalBtnClickListener = addtionalBtnClickListener;
    }
}
