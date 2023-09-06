package com.burak.tekstil;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedBack extends AppCompatActivity {
    Button btnFeedBackGonder;
    EditText etFeedBackDetay;

    String strFeedBackDetay,userId;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myFeedBackRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        mAuth = FirebaseAuth.getInstance();

        btnFeedBackGonder = findViewById(R.id.btnFeedBackGonder);

        etFeedBackDetay = findViewById(R.id.etFeedBackDetay);

        database = FirebaseDatabase.getInstance();

        // Veritabanından erişim sağlamak istediğimiz bölümü referansımıza atıyoruz...
        myFeedBackRef = database.getReference("feedbacks");

        mUser = mAuth.getCurrentUser();

        userId = mUser.getUid();

        // Geri bildirim işlemleri
        btnFeedBackGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etFeedBackDetay.getText().toString().trim().length() > 0) { //FEEDBACK METİN KUTUSUNUN İÇİ DOLU MU DEĞİLMİ KONTROL EDİYORUM.

                    strFeedBackDetay = etFeedBackDetay.getText().toString(); //EĞER İÇİ DOLUYSA GÜNCEL TARİH VE SAATLERLE BERABER FEEDBACK İ VERİTABANINA KAYDEDİYORUM.

                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    myFeedBackRef.child(userId).child(currentDate).child("mesaj").setValue(strFeedBackDetay);
                    myFeedBackRef.child(userId).child(currentDate).child("c_time").setValue(currentDate + " " + currentTime);

                    Toast.makeText(FeedBack.this, "Mesajınız başarıyla kaydedildi. Teşekkürler...", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(FeedBack.this, "Mesaj alanı boş olamaz.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}