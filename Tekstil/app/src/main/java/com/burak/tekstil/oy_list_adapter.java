package com.burak.tekstil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class oy_list_adapter extends BaseAdapter {

    Context mContext;
    List<oy_adapter> mAdapter;

    public oy_list_adapter(Context mContext, List<oy_adapter> mAdapter) {
        this.mContext = mContext;
        this.mAdapter = mAdapter;
    }

    @Override
    public int getCount() {
        return mAdapter.size();
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.oy_list_item,null);

        TextView txtBaslik = v.findViewById(R.id.txtOyBaslik);
        TextView txtKacOy = v.findViewById(R.id.txtKacOy);

        v.setTag(mAdapter.get(position).getmKime());

//Puanları hesaplıyoruz.
        int cuval_miktar = Integer.parseInt(mAdapter.get(position).getmCuvalMiktar());
        int cuval_secimi = Integer.parseInt(mAdapter.get(position).getmCuvalSecimi());
        int oy = Integer.parseInt(mAdapter.get(position).getmKacOy());

        int puan = cuval_miktar * cuval_secimi;


        oy *= 10;

        puan += oy;

        txtBaslik.setText("İşçi Adı : " + mAdapter.get(position).getmKime());



        txtKacOy.setText(puan + " Puan");


        return v;
    }
}
