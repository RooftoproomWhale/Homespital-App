package com.kosmo.homespital.model;

import android.view.View;

public class ReservationItem {

    private String hosp_name;
    private String dept_name;
    private String res_date;
    private String res_time;
    private String sel_symp;
    private String approved;
    private String address;
    private String apply_date;

    private View.OnClickListener requestBtnClickListener;

    public ReservationItem() {
    }

    public ReservationItem(String hosp_name, String dept_name, String res_date, String res_time, String sel_symp, String approved, String address, String apply_date) {
        this.hosp_name = hosp_name;
        this.dept_name = dept_name;
        this.res_date = res_date;
        this.res_time = res_time;
        this.sel_symp = sel_symp;
        this.approved = approved;
        this.address = address;
        this.apply_date = apply_date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApply_date() {
        return apply_date;
    }

    public void setApply_date(String apply_date) {
        this.apply_date = apply_date;
    }

    public String getHosp_name() {
        return hosp_name;
    }

    public void setHosp_name(String hosp_name) {
        this.hosp_name = hosp_name;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getRes_date() {
        return res_date;
    }

    public void setRes_date(String res_date) {
        this.res_date = res_date;
    }

    public String getRes_time() {
        return res_time;
    }

    public void setRes_time(String res_time) {
        this.res_time = res_time;
    }

    public String getSel_symp() {
        return sel_symp;
    }

    public void setSel_symp(String sel_symp) {
        this.sel_symp = sel_symp;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public View.OnClickListener getRequestBtnClickListener() {
        return requestBtnClickListener;
    }

    public void setRequestBtnClickListener(View.OnClickListener requestBtnClickListener) {
        this.requestBtnClickListener = requestBtnClickListener;
    }
}
