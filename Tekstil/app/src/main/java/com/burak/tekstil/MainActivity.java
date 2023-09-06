package com.burak.tekstil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //Firebase kullanıcı işlemleri için mAuth adında bir değişken tanımlıyoruz...
    private FirebaseAuth mAuth;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Değişkene FirebaseAuth nesnesini atıyoruz...
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //3 saniye bekleyip giriş ekranına veya ana ekrana yönlendirilecek.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //FirebaseAuth.getInstance().signOut();
                // Check if user is signed in (non-null) and update UI accordingly.
                FirebaseUser currentUser = mAuth.getCurrentUser();

                //Kullanıcı daha önce giriş yapmış mı yoksa yapmamış mı kontrolü yapılıyor...
                if (currentUser != null) {
                    //(GİRİŞ YAPMIŞ) Ana Ekrana yönlendirilecek....

                    startActivity(new Intent(getApplicationContext(), AnaEkran.class));
                } else {

                    //(GİRİŞ YAPMAMIŞ) Giriş sayfasına yönlendirilecek...
                    startActivity(new Intent(getApplicationContext(), GirisEkrani.class));
                }

                finish();
            }
        }, 3000);
    }
}