package com.burak.tekstil;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ProfilGuncelle extends AppCompatActivity {

    Button btnProfilimiGuncelle;
    EditText etProfilAd, etProfilSoyad, etProfilGSM, etProfilMail, etProfilPass;

    String strProfilAd, strProfilSoyad, strProfilGSM, strProfilMail, strProfilPass, strProfilYetki, userId;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;

    FirebaseUser mUser;

    //Firabase veritabanı bağlantısı için değişkenimizi tanımlıyoruz...
    FirebaseDatabase database;

    //Veritabanı referansını tanımlıyoruz...
    DatabaseReference myKullanicilarRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_guncelle);

        mAuth = FirebaseAuth.getInstance();

        btnProfilimiGuncelle = findViewById(R.id.btnProfilimiGuncelle);

        etProfilAd = findViewById(R.id.etProfilAd);
        etProfilSoyad = findViewById(R.id.etProfilSoyad);
        etProfilGSM = findViewById(R.id.etProfilGSM);
        etProfilMail = findViewById(R.id.etProfilMail);
        etProfilPass = findViewById(R.id.etProfilPass);

        database = FirebaseDatabase.getInstance();

        // Veritabanından erişim sağlamak istediğimiz bölümü referansımıza atıyoruz...
        myKullanicilarRef = database.getReference("kullanicilar");

        mUser = mAuth.getCurrentUser();

        userId = mUser.getUid();
        progressDialog = new ProgressDialog(ProfilGuncelle.this);
        progressDialog.setMessage("Kontrol Ediliyor... Lütfen bekleyiniz...");
        progressDialog.show();
        // Read from the database
        myKullanicilarRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.child("ad").getValue() != null) {
                        etProfilAd.setText(dataSnapshot.child("ad").getValue(String.class));
                        etProfilSoyad.setText(dataSnapshot.child("soyad").getValue(String.class));
                        etProfilGSM.setText(dataSnapshot.child("gsm").getValue(String.class));
                        etProfilMail.setText(dataSnapshot.child("mail").getValue(String.class));
                        strProfilYetki = dataSnapshot.child("yetki").getValue(String.class);
                    } else {
                        etProfilMail.setText(mUser.getEmail());
                        strProfilYetki = dataSnapshot.child("yetki").getValue(String.class);
                    }
                }

                //Profil güncelleme işlemleri
                btnProfilimiGuncelle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (etProfilAd.getText().toString().trim().length() > 0 && etProfilSoyad.getText().toString().trim().length() > 0
                                && etProfilGSM.getText().toString().trim().length() > 0 && etProfilMail.getText().toString().trim().length() > 0
                                && etProfilPass.getText().toString().trim().length() > 0) {


                            strProfilAd = etProfilAd.getText().toString();
                            strProfilSoyad = etProfilSoyad.getText().toString();
                            strProfilGSM = etProfilGSM.getText().toString();
                            strProfilMail = etProfilMail.getText().toString();
                            strProfilPass = etProfilPass.getText().toString();
                            progressDialog.show();
                            mAuth.signInWithEmailAndPassword(mUser.getEmail(), strProfilPass)
                                    .addOnCompleteListener(ProfilGuncelle.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                AuthCredential credential = EmailAuthProvider
                                                        .getCredential(strProfilMail, strProfilPass);

                                                mUser.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                //Now change your email address \\
                                                                //----------------Code for Changing Email Address----------\\
                                                                mUser.updateEmail(strProfilMail)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {

                                                                                    mAuth.signInWithEmailAndPassword(strProfilMail, strProfilPass).addOnCompleteListener(ProfilGuncelle.this, new OnCompleteListener<AuthResult>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                myKullanicilarRef.child(userId).child("ad").setValue(strProfilAd);
                                                                                                myKullanicilarRef.child(userId).child("soyad").setValue(strProfilSoyad);
                                                                                                myKullanicilarRef.child(userId).child("gsm").setValue(strProfilGSM);
                                                                                                myKullanicilarRef.child(userId).child("mail").setValue(strProfilMail);
                                                                                                myKullanicilarRef.child(userId).child("yetki").setValue(strProfilYetki);
                                                                                                etProfilPass.setText("");
                                                                                                progressDialog.dismiss();
                                                                                                Toast.makeText(ProfilGuncelle.this, "Bilgileriniz güncellendi.", Toast.LENGTH_LONG).show();

                                                                                            } else {
                                                                                                // If sign in fails, display a message to the user.
                                                                                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                                                                progressDialog.dismiss();
                                                                                                Toast.makeText(ProfilGuncelle.this, "Hatalı Şifre...",
                                                                                                        Toast.LENGTH_LONG).show();
                                                                                            }
                                                                                        }
                                                                                    });

                                                                                }
                                                                            }
                                                                        });
                                                                //----------------------------------------------------------\\
                                                            }
                                                        });
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfilGuncelle.this, "Hatalı Şifre...",
                                                        Toast.LENGTH_LONG).show();
                                            }

                                            // ...
                                        }
                                    });
                            // Get auth credentials from the user for re-authentication
                            // Current Login Credentials \\
                            // Prompt the user to re-provide their sign-in credentials


                        } else {
                            if (etProfilAd.getText().toString().trim().length() == 0) {
                                Toast.makeText(ProfilGuncelle.this, "Ad boş olamaz...", Toast.LENGTH_LONG).show();
                            } else if (etProfilSoyad.getText().toString().trim().length() == 0) {
                                Toast.makeText(ProfilGuncelle.this, "Soyad boş olamaz...", Toast.LENGTH_LONG).show();
                            } else if (etProfilGSM.getText().toString().trim().length() == 0) {
                                Toast.makeText(ProfilGuncelle.this, "GSM boş olamaz...", Toast.LENGTH_LONG).show();
                            } else if (etProfilMail.getText().toString().trim().length() == 0) {
                                Toast.makeText(ProfilGuncelle.this, "E-Posta boş olamaz...", Toast.LENGTH_LONG).show();
                            } else if (etProfilPass.getText().toString().trim().length() == 0) {
                                Toast.makeText(ProfilGuncelle.this, "Değişiklikleri uygulamak için şifrenizi girmeniz gerekmektedir.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(ProfilGuncelle.this, "Beklenmedik bir hata oluştu...", Toast.LENGTH_LONG).show();
            }
        });


    }
}