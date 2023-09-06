package com.burak.tekstil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Performansim extends AppCompatActivity {

    private List<oy_adapter> modelList;
    private ListView listView;

    LinearLayout lnrPerf;

    TextView txtMyPerf;

    String userId, isci_uid, cuvalMiktari, cuvalSecimi;

    String[][] isciler;

    boolean isci = false;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myOylarRef, myKullanicilarRef, myKayitRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performansim);

        txtMyPerf = findViewById(R.id.txtMyPerf);
        lnrPerf = findViewById(R.id.lnrPerf);

        listView = findViewById(R.id.lstPerformansim);
        modelList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();

        userId = mUser.getUid();

        database = FirebaseDatabase.getInstance();
        myKullanicilarRef = database.getReference("kullanicilar");
        myOylarRef = database.getReference("oylar");
        myKayitRef = database.getReference("oy_kayitlari");
        progressDialog = new ProgressDialog(Performansim.this);
        progressDialog.setMessage("Kontrol Ediliyor... Lütfen bekleyiniz...");
        progressDialog.show();

        myKullanicilarRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("yetki").getValue().toString().equals("1")) { //işçi olup olmadığının kontrolünü sağlıyorum

                    isci = true;
                } else {
                    lnrPerf.setVisibility(View.GONE);//kendi performans puanı kısmını eğer admin ise ekrandan siliyoruz
                }

//Kullanıcılara ait puan hesaplamaları ve işçinin kendi puanını hesaplanması
                myKullanicilarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot data : snapshot.getChildren()) {

                            if (data.child("yetki").getValue().toString().equals("2")) { //ustabaşı yetkisinde olanları çekiyoruz

                                myKayitRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot sssnapshot) {

                                        isciler = new String[Integer.parseInt(String.valueOf(sssnapshot.getChildrenCount()))][3]; //puanlamada sadece işçiuid, cuvalmiktarı ve çuval seçimini kullanacağımız için 3 boyutlu bir dizi tanımlıyoruz.

                                        int eleman=0;

                                        for (int i = 1; i <= sssnapshot.getChildrenCount(); i++) {


                                            isci_uid = sssnapshot.child(String.valueOf(i)).child("isci").getValue().toString();


                                            cuvalMiktari = sssnapshot.child(String.valueOf(i)).child("cuval_miktari").getValue().toString();
                                            cuvalSecimi = sssnapshot.child(String.valueOf(i)).child("cuval_secimi").getValue().toString();

                                            int gelen_eleman = dizide_ara(isciler, isci_uid, cuvalMiktari, cuvalSecimi,eleman,getApplicationContext()); //3 boyutlu diziyi dizide araya gönderiyorum


                                         if (eleman != gelen_eleman) { //eleman gelen elemana eşit değilse işçi uid sini ve cuval miktarını ve switch case ile çuval seçimini alıyoruz
                                                isciler[eleman][0] = isci_uid;
                                                isciler[eleman][1] = cuvalMiktari;

                                             switch (cuvalSecimi) {
                                                 case "1":

                                                     isciler[eleman][2] = String.valueOf(25);

                                                     break;
                                                 case "2":

                                                     isciler[eleman][2] = String.valueOf(50);

                                                     break;
                                                 case "3":

                                                     isciler[eleman][2] = String.valueOf(75);

                                                     break;
                                                 case "4":

                                                     isciler[eleman][2] = String.valueOf(100);

                                                     break;
                                             }



                                             eleman = gelen_eleman; //eleman ile gelen elemanı eşitledik bundan sonra elemanı artırcak yeni elemanı kontrol edicek
                                            }
                                        }

                                      isciler = clear_dizi(isciler,eleman,getApplicationContext()); //işçileri clear dizi ile temizledik
                                        modelList = new ArrayList<>();

                                        for (int aaa=0; aaa<isciler.length;aaa++) { //işçiler kadar for dongüsü döndürüyorum



                                            int finalAaa = aaa; // aaa yı i gibi tanımladım farklı bi harf olsun diye yani kısaca i=0 dan başlıyorum 1,2,3 diye finalaaa tanımladım çünkü katsayı ile puanlama yapıcaz
                                            int finalAaa1 = aaa;
                                            int finalAaa2 = aaa;
                                            myKullanicilarRef.child(isciler[aaa][0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot assnapshot) {

                                                    if (assnapshot.child("ad").getValue() != null) {

                                                        myOylarRef.child(isciler[finalAaa][0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot sssnapshot) {
                                                                if (sssnapshot.getValue() != null) {
                                                                    //aşağıda unvanı ve toplam oyu alıyorum
                                                                    String toplamOy= "0";
                                                                    String unvani = assnapshot.child("unvani").getValue().toString();
                                                                    if (sssnapshot.child("toplam").getValue() != null) {

                                                                        toplamOy = sssnapshot.child("toplam").getValue().toString();
                                                                        if (isciler[finalAaa][0].equals(userId)) {


                                                                            if (isci) { //eğer kullanıcı isci ise

                                                                                int cuval_miktar = Integer.parseInt(isciler[finalAaa1][1]);
                                                                                int cuval_secimi = Integer.parseInt(isciler[finalAaa2][2]);
                                                                                int oy = Integer.parseInt(toplamOy); //oyları toplam oy a aktarıyorum
                                                                                //aşağıda makine adlarını tanımlayarak makinalara katsayı tanımlıyorum
                                                                                int katSa = 1;

                                                                                if (unvani.equals("Singer")) {
                                                                                    katSa = 1;
                                                                                } else if (unvani.equals("Overlok")){
                                                                                    katSa = 2;
                                                                                } else if (unvani.equals("Orta")){
                                                                                    katSa = 2;
                                                                                } else if (unvani.equals("Reçme")){
                                                                                    katSa = 3;
                                                                                }
                                                                                //toplam işçi puanının hesaplamasını aşağıdaki matematiksel işlem ile hepsini çarpıyorum ve verilen her 1 oyu 10 puana sabitliyorum ve bu çıkan puanıda toplam oya ekliyorum
                                                                                int puan = cuval_miktar * cuval_secimi * katSa;

                                                                                //1 oy 10 puan kaç puan almışsa o kadar çarpıp toplam puana ekliyorum
                                                                                oy *= 10;

                                                                                puan += oy;

                                                                                txtMyPerf.setText("Puanınız : " + puan); //puanı görüntülüyorum

                                                                            }
                                                                        }





                                                                    } else {
                                                                        txtMyPerf.setText("Puanınız : 0"); //işçi hiç oy almamışsa puanı 0 gözükcek


                                                                    }

                                                                    String kime = assnapshot.child("ad").getValue().toString();

                                                                    String kacOy = toplamOy;

                                                                    oy_adapter model = new oy_adapter();

                                                                    //             Toast.makeText(Performansim.this, ""+isciler[finalAaa1][1] + " " +  isciler[finalAaa2][2], Toast.LENGTH_SHORT).show();

                                                                    model.setmId(isciler[finalAaa][0]);
                                                                    model.setmKime(kime);
                                                                    model.setmUnvani(unvani);
                                                                    model.setmCuvalMiktar(isciler[finalAaa1][1]);
                                                                    model.setmCuvalSecimi(isciler[finalAaa2][2]);
                                                                    model.setmKacOy(kacOy);

                                                                    modelList.add(model); //modelliste yukarıda ki işçilerin bilgileri ekliyorum

                                                                    Collections.sort(modelList,new Sirala()); //sırala classında puanları denetliyor ve hangisi hangisinden küçükse onu alta alıyor ve sıralama listesi oluşuyor

                                                                    setAdapter();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                    }
                                                   }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });




                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });



                             /*    myOylarRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ssnapshot) {

                                        if (ssnapshot.getValue() != null) {

                                            modelList = new ArrayList<>();
                                            currentMount = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());


                                            myKullanicilarRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    String id = data.getKey();

                                                    if (snapshot.child("ad").getValue() != null) {

                                                        if (ssnapshot.child("toplam").getValue() != null) {
                                                            if (data.getKey().equals(userId)) {

                                                                if (isci) {

                                                                    txtMyPerf.setText("Oy Sayınız : " + ssnapshot.child("toplam").getValue().toString());

                                                                }
                                                            }

                                                            String kime = snapshot.child("ad").getValue().toString();
                                                            String unvani = snapshot.child("unvani").getValue().toString();
                                                            String kacOy = ssnapshot.child("toplam").getValue().toString();

                                                            oy_adapter model = new oy_adapter();

                                                            model.setmId(id);
                                                            model.setmKime(kime);
                                                            model.setmUnvani(unvani);
                                                            model.setmKacOy(kacOy);

                                                            modelList.add(model);


                                                            setAdapter();
                                                        } else {
                                                            txtMyPerf.setText("Oy Sayınız : 0");
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(Performansim.this, "Beklenmedik bir hata oluştu...", Toast.LENGTH_LONG).show();
                                    }
                                });*/
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static String[][] clear_dizi(String[][] myarray, int gelen_eleman, Context context) {
        int i;


        String[][] newArray = new String[gelen_eleman][3];

        //copy original array into new array
        for (i = 0; i < gelen_eleman; i++) {
                newArray[i][0] = myarray[i][0];
                newArray[i][1] = myarray[i][1];
                newArray[i][2] = myarray[i][2];

        }

        //add element to the new array

        return newArray;
    }

    public static int dizide_ara(String[][] myarray, String isci_uid, String cuvalMiktari, String cuvalSecimi, int elemana,Context context) {

        boolean bulundu_mu = false;


        for (int i = 0; i < myarray.length; i++) {
            if (myarray[i][0] != null)
                if (myarray[i][0].equals(isci_uid)) {

                    bulundu_mu=true;

                    myarray[i][1] = String.valueOf(Integer.parseInt(myarray[i][1]) + Integer.parseInt(cuvalMiktari));


                    switch (cuvalSecimi) {
                        case "1":

                            myarray[i][2] = String.valueOf(Integer.parseInt(myarray[i][2]) + 25);

                            break;
                        case "2":

                            myarray[i][2] =  String.valueOf(Integer.parseInt(myarray[i][2]) + 50);

                            break;
                        case "3":

                            myarray[i][2] = String.valueOf(Integer.parseInt(myarray[i][2]) + 75);

                            break;
                        case "4":

                            myarray[i][2] = String.valueOf(Integer.parseInt(myarray[i][2]) + 100);

                            break;
                    }
                }
        }

        if (!bulundu_mu) { //dizide ara fonksiyonu bulunduysa performans ekranında işçi 2 defa eklenmicek ama bulunmadıysa ilk defa eklicek aşağıya

                elemana++;

        }

        return elemana;
    }

    private void setAdapter() {

        //RecyclerView.Adapter mAdapter = new MyAdapter(modelList,context);
        //recyclerView.setAdapter(mAdapter);
        oy_list_adapter adapter = new oy_list_adapter(getApplicationContext(), modelList);
        listView.setAdapter(adapter);

        progressDialog.dismiss();
    }
}