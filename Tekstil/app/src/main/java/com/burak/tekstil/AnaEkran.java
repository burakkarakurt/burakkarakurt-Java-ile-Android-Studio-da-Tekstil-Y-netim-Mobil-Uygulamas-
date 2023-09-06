package com.burak.tekstil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnaEkran extends AppCompatActivity {

    private List<is_adapter> modelList;
    private ListView listView;
    String[] newIscilerUID;
    TextView txtAnaEkranAlert;
    String userId,currentMount;

    LinearLayout lnrAnaTitle;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myKayitRef, myKullanicilarRef, myOylarRef;
    ProgressDialog progressDialog;

    int sayIsci,sayIsciFB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ana_ekran);

        currentMount = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()); //şuanki tarihi alıyorum çünkü veritabanında hangi ay da oy kullanıldığını görmek ve kontrol etmek için

        txtAnaEkranAlert = findViewById(R.id.txtAnaEkranAlert);

        lnrAnaTitle = findViewById(R.id.lnrAnaTitle);
        listView = findViewById(R.id.lstIsKayitlari);
        modelList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();

        userId = mUser.getUid();

        database = FirebaseDatabase.getInstance();
        myKullanicilarRef = database.getReference("kullanicilar"); //kullanıcılar diye bir referans oluşturuyorum hani bu firebase de kullanıcılar başlıklı olanlar
        myOylarRef = database.getReference("oylar"); //oylar diye bir referans oluşturuyorum hani bu firebase de oylar başlıklı olan bölüm
        // Veritabanından erişim sağlamak istediğimiz bölümü referansımıza atıyoruz...
        myKayitRef = database.getReference("kayitlar");
        progressDialog = new ProgressDialog(AnaEkran.this);
        progressDialog.setMessage("Kontrol Ediliyor... Lütfen bekleyiniz...");
        progressDialog.show();
        myKayitRef.child(userId).addValueEventListener(new ValueEventListener() { //kayıtlar referansının altındaki her kayıt için olan özel userid yi yani kimliğin altındaki verileri dinle eğer hiçbişey yoksa 'hiçbir kayıt bulunamadı' yazsını göster
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelList = new ArrayList<>();

                if (snapshot.getChildrenCount() == 0) {
                    txtAnaEkranAlert.setVisibility(View.VISIBLE); // burada hani hiç iş kaydı yokken 'hiçbir kayıt bulunamadı' yazısını visibilitesi 0 a eşitse yani boşsa o yazı gözükcek görünür olcak ama değilse kalkıcak ordan 105. satırda o yazıyı kaldırma satırı
                    lnrAnaTitle.setVisibility(View.GONE);
                }

                for (DataSnapshot data : snapshot.getChildren()) {
                    String id = data.getKey(); //id yi getkey olarak belli ediyorum
                    String yeri = "";
                    String c_time = "";
                    if (data.child("is_kodu").getValue() != null) { //iş kaydı varsa ekrandaki 'hicbir kayıt bulunamadı' yazısını kaldırıyorum.
                        txtAnaEkranAlert.setVisibility(View.GONE);
                        yeri = data.child("is_kodu").getValue().toString(); // iş kaydı varsa yeri adlı değişkene ekle

                    }

                    if (data.child("is_c_time").getValue() != null) { //getvalue ile şimdiki yani current time ı alıp c_time değişkenine atıyorum.
                        c_time = data.child("is_c_time").getValue().toString();
                    }


                    is_adapter model = new is_adapter(); //is_adapter da oluşturmuş olduğum iş modeli sınıfını burda çağırıyorum

                    model.setmId(id); //modeldeki id kısmına id yi atıyorum yere yeri atıyorum time a time ı atıyorum.
                    model.setmIsKodu(yeri);
                    model.setmCTime(c_time); //111. satırdaki veritabanından gelen time ı atıyorum

                    modelList.add(model); //modellisti modele ekliyorum.
                }

                setAdapter(); //listwieve gömüyorum
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnaEkran.this, "Beklenmedik bir hata oluştu...", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //burada menu_tb.xml dosyasında oluşturduğumuz menü bölümünü açmak için oluşturdum.
        // Inflate the menu; this adds items to the action bar if it is present.
        progressDialog.show();
        myKullanicilarRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // 136. satırda oluşturduğum menüyü aşağıda switch case ile veritabanında yetki kısmını kontrol ettirerek admin menülerinin açılması için 2 yani menu_tb.xml i açıyorum işçi için menu_isci.xml yani 1 yi açıyorum

                if (snapshot.getValue() != null) {
                    if (snapshot.child("yetki") != null) {

                        switch (snapshot.child("yetki").getValue(String.class)) {
                            case "2":

                                getMenuInflater().inflate(R.menu.menu_tb, menu);
                                break;
                            case "1":

                                getMenuInflater().inflate(R.menu.menu_isci, menu);
                                break;
                        }

                        progressDialog.dismiss();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //141. satırda switch case ile yaptığım işçi yada admin kontrolünden sonra menülerin işçi veya admin olmasına göre yerlerini değiştiriyorum.
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(getApplicationContext(), IsKaydet.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), ProfilGuncelle.class));
                return true;
            case R.id.action_isci_perf:
                startActivity(new Intent(getApplicationContext(), Performansim.class));
                return true;
            case R.id.action_isci_oy_ver:

                //Oy Kullanma

                myKullanicilarRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() { //kullanıcılar referansımdan userid mi çekiyorum
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.getValue() != null) {
                            if (snapshot.child("ad").getValue() != null) { //eğer profil doluysa adı varsa aşağıdaki işlemlere devam ediyor yoksa direkt else kısmına atlıyor else de de bilgilerinizi güncelleyin şeklinde uyarı veriyor
                                myOylarRef.child(userId).child(currentMount).addListenerForSingleValueEvent(new ValueEventListener() { //burda veritabanında işçi bulunduğu ay içerisinde oy vermişmi onu kontrol ediyoruz vermişse tekrar oy veremiyor vermemişse yani boşsa veritabanında o kısım oy verebilecek
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.getValue() == null) {


                                            myKullanicilarRef.orderByChild("yetki").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() { //veritabanında kullanıcılarda yetkisi 1 olanı yani işçileri seçiyorum
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        //2 tane sayiscifb şeklinde dizi tanımlıyorum çünkü profilleri güncellememiş olanlarda geliyor
                                                        sayIsciFB = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                                                        sayIsciFB--; //diziyi bir azaltıyorum çünkü oy kullanırken ben olmayacağım
                                                        String[] isciler = new String[sayIsciFB];//iki tane string dizisi tanımladım
                                                        String[] isciUID = new String[sayIsciFB];

                                                        sayIsci = 0;

                                                        for (DataSnapshot data : snapshot.getChildren()) {

                                                            if (!data.getKey().equals(userId)) { //yetkisi 1 olanları yukardaki satırda for döngüsü ile döndürüyorum eğer bana eşitse oylamaya dahil etmiyorum

                                                                myKullanicilarRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                        if (snapshot.getValue() != null) {
                                                                            if (snapshot.child("ad").getValue() != null) { //adı vs boş değilse alıyoruz adaşıdaki diziye yerleştiriyorum


                                                                                isciler[sayIsci] = snapshot.child("ad").getValue(String.class);
                                                                                isciUID[sayIsci] = data.getKey();
                                                                                sayIsci++;


                                                                            } else {
                                                                                sayIsciFB--; //eğer adı boşsa sayiscifb dizisini azaltıyorum
                                                                            }

                                                                            if (sayIsci == sayIsciFB) { //sayisci ile sayiscifb nin eşit olduğu yere geliyorum

                                                                                String[] newIsciler = new String[sayIsci]; //yukarıda dizinin eşitlendiği diziyi en son alıp yeni bir newisci dizisine aktarıyorum
                                                                                newIscilerUID = new String[sayIsci]; //bir tane daha sayisci diye dizi oluşturuyorum

                                                                              //clear dizi fonksiyonu ile daha önce kullandığım dizileri döngüyle temizledim
                                                                                newIsciler = clear_dizi(isciler,getApplicationContext());
                                                                                newIscilerUID = clear_dizi(isciUID,getApplicationContext());

                                                                                newIsciler = add_element(newIsciler,"Seçiniz",newIsciler.length); //oy kullanırken işçilerin en başına seçiniz koymak için add element oluşturuyorum
                                                                                newIscilerUID = add_element(newIscilerUID,"Seçiniz",newIscilerUID.length);
                                                                                //oy verme işleminde spinnerı ve alertdialogu aşağıdaki kod bloğu ile ekliyorum yani xml kullanmadan ekliyorum
                                                                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AnaEkran.this);
                                                                                dialogBuilder.setTitle("Oy Vermek İstediğiniz İşçiyi Seçin : ");
                                                                                LayoutInflater inflater = AnaEkran.this.getLayoutInflater();
                                                                                final View myView = inflater.inflate(R.layout.spn_oy_item, null);
                                                                                dialogBuilder.setView(myView);
                                                                                Spinner checkInProviders = (Spinner) myView.findViewById(R.id.providers);
                                                                                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(AnaEkran.this,
                                                                                        android.R.layout.simple_spinner_item, newIsciler);

                                                                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                                checkInProviders.setAdapter(dataAdapter);

                                                                                dialogBuilder.setPositiveButton("OY VER", new DialogInterface.OnClickListener() { //oy ver butonu tanımlıyorum
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                                        if (checkInProviders.getSelectedItemPosition() != 0) {

                                                                                            myOylarRef.child(newIscilerUID[checkInProviders.getSelectedItemPosition()]).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                @Override
                                                                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                                    if (dataSnapshot.child("toplam").getValue() != null) {

                                                                                                    int count = Integer.parseInt(dataSnapshot.child("toplam").getValue().toString());

                                                                                                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                                                                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


                                                                                                    myOylarRef.child(newIscilerUID[checkInProviders.getSelectedItemPosition()]).child("toplam").setValue(count +1); //oy verilen işçinin toplam oy sayısını 1 artırıyorum
                                                                                                    myOylarRef.child(userId).child(currentMount).child("kime").setValue(newIscilerUID[checkInProviders.getSelectedItemPosition()]);
                                                                                                    myOylarRef.child(userId).child(currentMount).child("c_time").setValue(currentDate + " " + currentTime);

                                                                                                    } else {
                                                                                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                                                                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


                                                                                                        myOylarRef.child(newIscilerUID[checkInProviders.getSelectedItemPosition()]).child("toplam").setValue(1);
                                                                                                        myOylarRef.child(userId).child(currentMount).child("kime").setValue(newIscilerUID[checkInProviders.getSelectedItemPosition()]);
                                                                                                        myOylarRef.child(userId).child(currentMount).child("c_time").setValue(currentDate + " " + currentTime);

                                                                                                    }
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                                                    Toast.makeText(AnaEkran.this, "Beklenmedik bir hata oluştu...", Toast.LENGTH_LONG).show();
                                                                                                }
                                                                                            });



                                                                                        } else {
                                                                                            Toast.makeText(AnaEkran.this, "Lütfen bir işci seçiniz.", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                                dialogBuilder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        dialog.cancel();
                                                                                    }
                                                                                });
// Set up the input

                                                                                dialogBuilder.show();


                                                                            }

                                                                        } else {
                                                                            Toast.makeText(AnaEkran.this, "Uygun İşçi Bulunamadı.", Toast.LENGTH_SHORT).show();

                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });


                                                            }

                                                        }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        } else {
                                            Toast.makeText(AnaEkran.this, "Zaten oy kullanmışsınız!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                Toast.makeText(AnaEkran.this, "Önce profil bilgilerinizi güncellemeniz gerekiyor.", Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(AnaEkran.this, "Beklenmedik bir hata oluştu.", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                return true;
            case R.id.action_my_perf:
                startActivity(new Intent(getApplicationContext(), Performansim.class));
                return true;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return true;
            case R.id.action_feedback:
                startActivity(new Intent(getApplicationContext(), FeedBack.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAdapter() { //SET ADAPTER İS_LİST_ADAPTER A GİDİYOR EKLEDİĞİMİZ İS ADAPTER MODELİNİ GÖNDERİYOR LİST ADAPTER A SONRA LİSTWİEVİN İÇİNE DOLDURUYOR.

        //RecyclerView.Adapter mAdapter = new MyAdapter(modelList,context);
        //recyclerView.setAdapter(mAdapter);
        is_list_adapter adapter = new is_list_adapter(getApplicationContext(), modelList);
        listView.setAdapter(adapter);

        progressDialog.dismiss();
    }
///clear dizi fonksiyonum ile değeri olanları yani içi boş olmayan işçi bilgilerini newarray e atıyorum ve newarrayi döndürüyorum
    public static String[] clear_dizi(String myarray[], Context context) {
        int i;

        int sayy = 0;

        for (i = 0; i < myarray.length; i++) {
            if (myarray[i] != "" && myarray[i] != null) {
               sayy++;
            }
        }
        String newArray[] = new String[sayy];

        //copy original array into new array
        for (i = 0; i < sayy; i++) {
            if (myarray[i] != "" && myarray[i] != null) {

                newArray[i] = myarray[i];
            }
        }

        //add element to the new array

        return newArray;
    }
//add element dizisi ile oy verme kısmında seçilme yerinin yani spinnerın en başına seçiniz koyuyorum
    public static String[] add_element(String myarray[], String ele, int lebb) {
        int i;

        String newArray[] = new String[lebb + 1];

        newArray[0] = ele;
        //copy original array into new array
        for (i = 1; i < lebb + 1; i++)
            newArray[i] = myarray[i - 1];

        //add element to the new array

        return newArray;
    }
}