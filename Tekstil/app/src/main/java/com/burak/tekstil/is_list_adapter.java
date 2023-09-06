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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class is_list_adapter  extends BaseAdapter {

    Context mContext;
    List<is_adapter> mAdapter;

    String userId;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myKayitRef;

    public is_list_adapter(Context mContext, List<is_adapter> mAdapter) {
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
//is_list_item.xml de oluşturduğum iş listeleme şablonuna başlık kayıt tarihi düzenle ve sil butonu tanımlıyorum
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.is_list_item,null);

        TextView txtBaslik = v.findViewById(R.id.txtKayitBaslik);
        TextView txtCTime = v.findViewById(R.id.txtKayitCTime);

        Button btnDuzenle = v.findViewById(R.id.btnKayitDuzenle);
        Button btnSil = v.findViewById(R.id.btnKayitSil);

        v.setTag(mAdapter.get(position).getmIsKodu());

        txtBaslik.setText("İş Kodu : " + mAdapter.get(position).getmIsKodu()); //kaç tane ekleme gerekiyorsa onu kontrol etmek için şuankinin positionını alıyoruz
        txtCTime.setText(mAdapter.get(position).getmCTime().split(" ")[0] +"\n"+ mAdapter.get(position).getmCTime().split(" ")[1]);

        btnDuzenle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//Düzenleme için düzenleme ekranına yönlendiriyoruz...
                Intent i = new Intent(v.getRootView().getContext(),IsKaydet.class);
                i.putExtra("id",mAdapter.get(position).mId); //güncelleme için hangi kısımdaki düzenle butonun basılırsa o kaydın idsini gönderip bilgileri alıyoruz
                mContext.startActivity(i); //adapter olduğu için context ile başlatıyoruz.
            }
        });

        btnSil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Dialog ekranı oluşturup emin olup olmadığını soruyoruz...

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() { //sil kısmına bastığımızda elimiz yanlışlıkla basmış olabilir bu yüzden eminmisiniz kısmını DialogInterface ile oluşturuyoruz.
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE: //eğer silmek istediğinize eminmisiniz kısmına evet dersek yani positifse aşağıdaki nesne ve değişkenleri alıyoruz.(94,96,98. satırlar) kayitrefi alıyoruz(103.satır) o tıkladığımız kısmın id sini alıyoruz removeValue yapıyoruz(105.satır)

                                mAuth = FirebaseAuth.getInstance();

                                mUser = mAuth.getCurrentUser();

                                userId = mUser.getUid();

                                database = FirebaseDatabase.getInstance();

                                // Veritabanından erişim sağlamak istediğimiz bölümü referansımıza atıyoruz...
                                myKayitRef = database.getReference("kayitlar");
//Eminse siliyoruz...
                                myKayitRef.child(userId).child(mAdapter.get(position).mId).removeValue();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setMessage("Silmek istediğinize emin misiniz?").setPositiveButton("Evet", dialogClickListener)
                        .setNegativeButton("Hayır", dialogClickListener).show();
            }
        });



        return v;
    }
}
