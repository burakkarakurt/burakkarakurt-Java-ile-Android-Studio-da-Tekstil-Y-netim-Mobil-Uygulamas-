package com.burak.tekstil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class IsKaydet extends AppCompatActivity {

    Button btnIsKaydet;
    EditText etIsKodu, etIsCuvalMiktar, etIsSaat;
    Spinner spnIsci, spnCuvalSecimi, spnIsTransferBirimi, spnYukleyen;
    CheckBox oylama;
    TextView txtKayitTitle, txtToplamIsMiktari;

    String strIsKodu, strIsCuvalMiktar, strIsSaat, userId, isId, strIsci,strYukleyen;
    String[] isciler, isciUID, cuvallar, transfer_birimleri, newIscilerUID;
    DatePickerDialog datePickerDialog;

    Calendar calendar;

    int year, month, dayOfMonth, strCuvalSecimi, strIsTransferBirimi, sayIsci, sayIsciFB;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myKayitRef, myKullanicilarRef, myOyKayitlariRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_is_kaydet);

        isId = ""; //bu isId stringini oluşturma sebebim güncelleme mi yapcaz yoksa sıfırdan mı kayıt oluşturcaz onu kontrol edicek.

        btnIsKaydet = findViewById(R.id.btnIsKaydet);


        mAuth = FirebaseAuth.getInstance();
//spinnerları tanımlıyoruz
        spnIsci = findViewById(R.id.spnIsci);
        spnYukleyen = findViewById(R.id.spnYukleyen);
        spnCuvalSecimi = findViewById(R.id.spnCuvalSecimi);
        spnIsTransferBirimi = findViewById(R.id.spnIsTransferBirimi);
        etIsKodu = findViewById(R.id.etIsKodu);
        etIsCuvalMiktar = findViewById(R.id.etIsCuvalMiktar);
        etIsSaat = findViewById(R.id.etIsSaat);

//oy için checkbox açıyoruz
        oylama = findViewById(R.id.checkBox);
//değişkenleri tanımlıyoruz
        txtToplamIsMiktari = findViewById(R.id.txtToplamIsMiktari);
        txtKayitTitle = findViewById(R.id.txtKayitTitle);
//çuval miktarı seçimi için değişkeni tanımlıyoruz
        etIsCuvalMiktar.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { //s metnin içeriğini, start değişikliğin başladığı indeksi, count metnin uzunluğunu belirten tamsayı değeri, after da metnin yeni uzunluğunu girmemizi sağlayan kod bileşenleridir.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) { //eğer metnin içeriği doluysa yani 0 dan farklıysa aşağıya devam ediyoruz

                    if (strCuvalSecimi > 0) { //eğer çuval seçimi 0 dan büyükse
                        int kg = Integer.parseInt(cuvallar[strCuvalSecimi].trim().split(" ")[0]); //seçilen kg değerini trim ile ilk ve sondaki boşluklarını kaldırıp split ile diziye dönüştürüp [0] ile bölünmüş dizinin ilk öğesini seçer
                        int miktar = Integer.parseInt(s.toString()); //miktar kısmını sayı ile integer olarak değişkene atıyoruz
                        int sonuc = kg * miktar; //ekranda sonuc olarak girilen kaç kg çuval ve kaç adet miktar çuvalsa toplam iş miktarını çarparak hesaplamasını sağlıyoruz ve sonuc değişkenine atıyoruz
                        txtToplamIsMiktari.setText("Toplam İş Miktarı : " + sonuc); //yukarda yaptığımız işlemi toplam iş miktarını görüntülemesi için sonuctan aldığı veriyi gösteriyoruz
                        txtToplamIsMiktari.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(IsKaydet.this, "Toplam Miktar İçin Lütfen Çuval Seçin.", Toast.LENGTH_LONG).show();
                    }
                } else {

                    txtToplamIsMiktari.setVisibility(View.GONE);
                }
            }
        });
        // Veritabanı değişkenimize nesnesini atıyoruz....
        database = FirebaseDatabase.getInstance();

        mUser = mAuth.getCurrentUser();

        userId = mUser.getUid();
        // Veritabanından erişim sağlamak istediğimiz bölümü referansımıza atıyoruz...
        myKayitRef = database.getReference("kayitlar");

        myKullanicilarRef = database.getReference("kullanicilar");

        myOyKayitlariRef = database.getReference("oy_kayitlari");

        Bundle extras = getIntent().getExtras(); //is_list_adapter daki putextradan aldığımız extraları yani güncellenecek bilgileri bundle extras ile getiriyoruz
        if (extras != null) { //eğer boş değilse yani bi tane id gelmişse iş kayıt sayfasına ozaman güncelleme yapacağız

            txtKayitTitle.setText("Kaydı Güncelleyin");

            isId = extras.getString("id"); //veritabanından güncellenecek olan veri hangi id ise onu alıyoruz

            myKayitRef.child(userId).child(isId).addListenerForSingleValueEvent(new ValueEventListener() { //veritabanında kayitrefin altında user id ve iş id ile veriyi alıyorum
                @Override
                public void onDataChange(@NonNull DataSnapshot ssKayitlar) {
// cuvallar ve transfer_birimlerini arrays.xml den alıyorum yani dizileri
                    cuvallar = getResources().getStringArray(R.array.cuvallar);
                    transfer_birimleri = getResources().getStringArray(R.array.transfer_birimleri);


                    myKullanicilarRef.orderByChild("yetki").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            sayIsciFB = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));

                            isciler = new String[sayIsciFB];
                            isciUID = new String[sayIsciFB];

                            sayIsci = 0;

                            for (DataSnapshot data : snapshot.getChildren()) {


                                if (!data.getKey().equals(userId)) {

                                    myKullanicilarRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            if (snapshot.getValue() != null) {
                                                if (snapshot.child("ad").getValue() != null) {

                                                    isciler[sayIsci] = snapshot.child("ad").getValue(String.class);
                                                    isciUID[sayIsci] = data.getKey();
                                                    sayIsci++;


                                                } else {
                                                    sayIsciFB--;
                                                }

                                                if (sayIsci == sayIsciFB) {

                                                    String[] newIsciler = new String[sayIsci];
                                                    newIscilerUID = new String[sayIsci];


                                                    newIsciler = clear_dizi(isciler,getApplicationContext());
                                                    newIscilerUID = clear_dizi(isciUID,getApplicationContext());

                                                    newIsciler = add_element(newIsciler,"Seçiniz",newIsciler.length);
                                                    newIscilerUID = add_element(newIscilerUID,"Seçiniz",newIscilerUID.length);

                                                    ArrayAdapter iscilerArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, newIsciler);
                                                    iscilerArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    //Setting the ArrayAdapter data on the Spinner
                                                    spnIsci.setAdapter(iscilerArr);



                                                    for (int aa=0;aa<newIscilerUID.length;aa++) {
                                                        if (ssKayitlar.child("isci").getValue().toString().equals(newIscilerUID[aa])) {
                                                            spnIsci.setSelection(aa);
                                                        }
                                                    }


                                                    spnIsci.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                                                strIsci = newIscilerUID[i];

                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                                        }
                                                    });

                                                    spnYukleyen.setAdapter(iscilerArr);

                                                    for (int aa=0;aa<newIscilerUID.length;aa++) {
                                                        if (ssKayitlar.child("yukleyen").getValue().toString().equals(newIscilerUID[aa])) {
                                                            spnYukleyen.setSelection(aa);
                                                        }
                                                    }

                                                    spnYukleyen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                                            strYukleyen = newIscilerUID[i];

                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                                        }
                                                    });

                                                    ArrayAdapter cuvallarArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, cuvallar);
                                                    cuvallarArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    //Setting the ArrayAdapter data on the Spinner
                                                    spnCuvalSecimi.setAdapter(cuvallarArr);

                                                    spnCuvalSecimi.setSelection(Integer.parseInt(ssKayitlar.child("cuval_secimi").getValue().toString()));

                                                    spnCuvalSecimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                            strCuvalSecimi = i;
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                                        }
                                                    });

                                                    ArrayAdapter transferBirimiArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, transfer_birimleri);
                                                    transferBirimiArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    //Setting the ArrayAdapter data on the Spinner
                                                    spnIsTransferBirimi.setAdapter(transferBirimiArr);
                                                    spnIsTransferBirimi.setSelection(Integer.parseInt(ssKayitlar.child("transfer_birimi").getValue().toString()));
                                                    spnIsTransferBirimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                                            if (i != 0) {
                                                                strIsTransferBirimi = i;
                                                            }
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                                        }
                                                    });

                                                    etIsKodu.setText(ssKayitlar.child("is_kodu").getValue().toString());
                                                    etIsCuvalMiktar.setText(ssKayitlar.child("cuval_miktari").getValue().toString());
                                                    etIsSaat.setText(ssKayitlar.child("is_saat").getValue().toString());

                                                }
                                            } else {
                                                Toast.makeText(IsKaydet.this, "Uygun İşçi Bulunamadı.", Toast.LENGTH_SHORT).show();

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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            btnIsKaydet.setText("Kaydı Güncelle");
        } else {

            cuvallar = getResources().getStringArray(R.array.cuvallar);
            transfer_birimleri = getResources().getStringArray(R.array.transfer_birimleri);

            myKullanicilarRef.orderByChild("yetki").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    sayIsciFB = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));

                    isciler = new String[sayIsciFB];
                    isciUID = new String[sayIsciFB];

                    sayIsci = 0;

                    for (DataSnapshot data : snapshot.getChildren()) {


                        if (!data.getKey().equals(userId)) {

                            myKullanicilarRef.child(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.getValue() != null) {
                                        if (snapshot.child("ad").getValue() != null) {


                                            isciler[sayIsci] = snapshot.child("ad").getValue(String.class);
                                            isciUID[sayIsci] = data.getKey();
                                            sayIsci++;


                                        } else {
                                            sayIsciFB--;
                                        }

                                        if (sayIsci == sayIsciFB) {

                                            String[] newIsciler = new String[sayIsci];
                                            newIscilerUID = new String[sayIsci];


                                            newIsciler = clear_dizi(isciler,getApplicationContext());
                                            newIscilerUID = clear_dizi(isciUID,getApplicationContext());

                                            newIsciler = add_element(newIsciler,"Seçiniz",newIsciler.length);
                                            newIscilerUID = add_element(newIscilerUID,"Seçiniz",newIscilerUID.length);



                                            ArrayAdapter iscilerArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, newIsciler);
                                            iscilerArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            //Setting the ArrayAdapter data on the Spinner
                                            spnIsci.setAdapter(iscilerArr);

                                            spnIsci.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {



                                                        strIsci = newIscilerUID[i];

                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });

                                            spnYukleyen.setAdapter(iscilerArr);

                                            spnYukleyen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                                    strYukleyen = newIscilerUID[i];

                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });

                                            ArrayAdapter cuvallarArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, cuvallar);
                                            cuvallarArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            //Setting the ArrayAdapter data on the Spinner
                                            spnCuvalSecimi.setAdapter(cuvallarArr);

                                            spnCuvalSecimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    strCuvalSecimi = i;
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });

                                            ArrayAdapter transferBirimiArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, transfer_birimleri);
                                            transferBirimiArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            //Setting the ArrayAdapter data on the Spinner
                                            spnIsTransferBirimi.setAdapter(transferBirimiArr);

                                            spnIsTransferBirimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                                    if (i != 0) {
                                                        strIsTransferBirimi = i;
                                                    }
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {

                                                }
                                            });

                                        }
                                    } else {
                                        Toast.makeText(IsKaydet.this, "Uygun İşçi Bulunamadı.", Toast.LENGTH_SHORT).show();

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

            ArrayAdapter cuvallarArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, cuvallar);
            cuvallarArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Setting the ArrayAdapter data on the Spinner
            spnCuvalSecimi.setAdapter(cuvallarArr);

            spnCuvalSecimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    strCuvalSecimi = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            ArrayAdapter transferBirimiArr = new ArrayAdapter(IsKaydet.this, android.R.layout.simple_spinner_item, transfer_birimleri);
            transferBirimiArr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Setting the ArrayAdapter data on the Spinner
            spnIsTransferBirimi.setAdapter(transferBirimiArr);

            spnIsTransferBirimi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    if (i != 0) {
                        strIsTransferBirimi = i;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        etIsSaat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Şimdiki zaman bilgilerini alıyoruz. güncel saat, güncel dakika.
                final Calendar takvim = Calendar.getInstance();
                int saat = takvim.get(Calendar.HOUR_OF_DAY);
                int dakika = takvim.get(Calendar.MINUTE);

                TimePickerDialog tpd = new TimePickerDialog(IsKaydet.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // hourOfDay ve minute değerleri seçilen saat değerleridir.
                                // Edittextte bu değerleri gösteriyoruz.
                                etIsSaat.setText(hourOfDay + ":" + minute);
                            }
                        }, saat, dakika, true);
                // timepicker açıldığında set edilecek değerleri buraya yazıyoruz.
                // şimdiki zamanı göstermesi için yukarda tanımladğımız değişkenleri kullanıyoruz.
                // true değeri 24 saatlik format için.

                // dialog penceresinin button bilgilerini ayarlıyoruz ve ekranda gösteriyoruz.
                tpd.setButton(TimePickerDialog.BUTTON_POSITIVE, "Seç", tpd);
                tpd.setButton(TimePickerDialog.BUTTON_NEGATIVE, "İptal", tpd);
                tpd.show();
            }
        });

        //İş kaydetme işlemleri
        btnIsKaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etIsKodu.getText().toString().trim().length() > 0 && !strIsci.equals("Seçiniz") && strCuvalSecimi > 0
                        && strIsTransferBirimi > 0 && etIsCuvalMiktar.getText().toString().trim().length() > 0 &&
                        etIsSaat.getText().toString().trim().length() > 0) {


                    strIsKodu = etIsKodu.getText().toString();
                    strIsCuvalMiktar = etIsCuvalMiktar.getText().toString();
                    strIsSaat = etIsSaat.getText().toString();

                    if (isId != "") {
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        myKayitRef.child(userId).child(isId).child("is_kodu").setValue(strIsKodu);
                        myKayitRef.child(userId).child(isId).child("isci").setValue(strIsci);
                        myKayitRef.child(userId).child(isId).child("yukleyen").setValue(strYukleyen);
                        myKayitRef.child(userId).child(isId).child("cuval_secimi").setValue(strCuvalSecimi);
                        myKayitRef.child(userId).child(isId).child("transfer_birimi").setValue(strIsTransferBirimi);
                        myKayitRef.child(userId).child(isId).child("cuval_miktari").setValue(strIsCuvalMiktar);
                        myKayitRef.child(userId).child(isId).child("is_saat").setValue(strIsSaat);
                        myKayitRef.child(userId).child(isId).child("is_c_time").setValue(currentDate + " " + currentTime);

                        if (oylama.isChecked()) { //eğer oylamaya dahil et butonu seçilirse veritabanında oy_kayıtları referansına normal kayıtlara ekstra olarak işçiyi çuval seçimini ve çuval miktarını ekliyoruz ve oylamaya dahil oluyor yani puana
                            myOyKayitlariRef.child(userId).child(isId).child("isci").setValue(strIsci);
                            myOyKayitlariRef.child(userId).child(isId).child("cuval_secimi").setValue(strCuvalSecimi);
                            myOyKayitlariRef.child(userId).child(isId).child("cuval_miktari").setValue(strIsCuvalMiktar);
                        }

                        Toast.makeText(IsKaydet.this, "Kayıt başarıyla güncellendi.", Toast.LENGTH_SHORT).show();


                    } else {
                        myKayitRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long count = dataSnapshot.getChildrenCount();

                                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("is_kodu").setValue(strIsKodu);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("isci").setValue(strIsci);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("yukleyen").setValue(strYukleyen);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("cuval_secimi").setValue(strCuvalSecimi);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("transfer_birimi").setValue(strIsTransferBirimi);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("cuval_miktari").setValue(strIsCuvalMiktar);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("is_saat").setValue(strIsSaat);
                                myKayitRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("is_c_time").setValue(currentDate + " " + currentTime);

                                if (oylama.isChecked()) {
                                    myOyKayitlariRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("isci").setValue(strIsci);
                                    myOyKayitlariRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("cuval_secimi").setValue(strCuvalSecimi);
                                    myOyKayitlariRef.child(userId).child(String.valueOf(Integer.parseInt(String.valueOf(count)) + 1)).child("cuval_miktari").setValue(strIsCuvalMiktar);
                                }

                                Toast.makeText(IsKaydet.this, "Kayıt başarılı bir şekilde oluşturuldu.", Toast.LENGTH_SHORT).show();

                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(IsKaydet.this, "Beklenmedik bir hata oluştu...", Toast.LENGTH_LONG).show();
                            }
                        });

                    }


                } else {
                    if (etIsKodu.getText().toString().trim().length() == 0) {
                        Toast.makeText(IsKaydet.this, "İş Kodu boş olamaz...", Toast.LENGTH_LONG).show();
                    } else if (strIsci.equals("Seçiniz")) {
                        Toast.makeText(IsKaydet.this, "İşci boş olamaz...", Toast.LENGTH_LONG).show();
                    }  else if (strYukleyen.equals("Seçiniz")) {
                        Toast.makeText(IsKaydet.this, "Yükleyen boş olamaz...", Toast.LENGTH_LONG).show();
                    }else if (strCuvalSecimi == 0) {
                        Toast.makeText(IsKaydet.this, "Çuval Seçimi boş olamaz...", Toast.LENGTH_LONG).show();
                    } else if (strIsTransferBirimi == 0) {
                        Toast.makeText(IsKaydet.this, "Transfer Birimi boş olamaz...", Toast.LENGTH_LONG).show();
                    } else if (etIsCuvalMiktar.getText().toString().trim().length() == 0) {
                        Toast.makeText(IsKaydet.this, "Çuval Miktarı boş olamaz...", Toast.LENGTH_LONG).show();
                    } else if (etIsSaat.getText().toString().trim().length() == 0) {
                        Toast.makeText(IsKaydet.this, "Saat boş olamaz...", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }
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