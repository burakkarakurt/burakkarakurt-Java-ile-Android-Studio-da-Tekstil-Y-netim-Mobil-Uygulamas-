package com.burak.tekstil;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GirisEkrani extends AppCompatActivity {

    //Firebase kullanıcı işlemleri için mAuth adında bir değişken tanımlıyoruz...
    private FirebaseAuth mAuth;

    //XML dosyasında kullanılan butonlar için değişkenleri tanımlıyoruz...
    private Button btnGirisYap, btnGirisUyeOl;

    //XML dosyasında kullanılan editTextler için değişkenleri tanımlıyoruz...
    private EditText etGirisMail, etGirisPass;

    TextView txtSifremiUnuttum;

    //Metinsel ifadelerde kullanacağımız String türleri için değişkenleri tanımlıyoruz...
    private String mail, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_ekrani);

        //Değişkene FirebaseAuth nesnesini atıyoruz...
        mAuth = FirebaseAuth.getInstance();

        //Butonlar için tanımladığımız değişkenleri XML dosyasıyla ilişkilendiriyoruz...
        btnGirisYap = findViewById(R.id.btnGirisYap);
        btnGirisUyeOl = findViewById(R.id.btnGirisUyeOl);

        //EditTextler için tanımladığımız değişkenleri XML dosyasıyla ilişkilendiriyoruz...
        etGirisMail = findViewById(R.id.etGirisMail);
        etGirisPass = findViewById(R.id.etGirisPass);

        txtSifremiUnuttum = findViewById(R.id.txtSifremiUnuttum);

        txtSifremiUnuttum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//buralarda şifremi unuttum bölümüyle ilgili yani şifremi unuttum diyince e posta adresiniz yazısıyla beraber bi ekran açılması için alertdialog ekranı
                AlertDialog.Builder builder = new AlertDialog.Builder(GirisEkrani.this);
                builder.setTitle("E-Posta Adresiniz");

// boş yere yazı yazıp veri girebileceğin edittext oluşturuyo şifremi unuttum kısmındaki edittexti hani yenisini girdiğin e postayı
                final EditText input = new EditText(GirisEkrani.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("SIFIRLA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(input.getText().toString()) //oluşturduğumuz yani yazdığım e posta verisini veritabanından alıyorum burda
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) { // görev başarılıysa aşağıdaki toast metni ile kullanıcının önüne çıkarıyorum
                                            Toast.makeText(GirisEkrani.this, "Şifre sıfırlama E-Postası gönderildi. Lütfen E-Posta'nızı kontrol ediniz.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                });
                builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


            }
        });
//Giriş İşlemleri
        btnGirisYap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etGirisMail.getText().toString() != "" && etGirisMail.getText().toString().contains("@") && etGirisPass.getText().toString() != "") {

                    mail = etGirisMail.getText().toString(); //maili mail değişkenine atıyorum
                    pass = etGirisPass.getText().toString(); //şifreyi pass değişkenine atıyorum

                    mAuth.signInWithEmailAndPassword(mail, pass) //firebase kontrolünü sağlayıp giriş yapmasını sağlıyoruz.
                            .addOnCompleteListener(GirisEkrani.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Giriş başarılıysa
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startActivity(new Intent(getApplicationContext(), AnaEkran.class));
                                        finish();
                                    } else {
                                        // Giriş başarısızsa
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(GirisEkrani.this, "Kullanıcı bulunamadı...",
                                                Toast.LENGTH_LONG).show();
                                    }

                                    // ...
                                }
                            });

                } else {
                    if (etGirisMail.getText().toString() == "") {
                        Toast.makeText(GirisEkrani.this, "E-Posta boş olamaz... Lütfen E-Posta adresinizi giriniz.", Toast.LENGTH_LONG).show();
                    } else if (!etGirisMail.getText().toString().contains("@")) {
                        Toast.makeText(GirisEkrani.this, "Lütfen geçerli bir E-Posta adresi giriniz.", Toast.LENGTH_LONG).show();
                    } else if (etGirisPass.getText().toString() == "") {
                        Toast.makeText(GirisEkrani.this, "Şifre boş olamaz... Lütfen şifrenizi giriniz.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btnGirisUyeOl.setOnClickListener(new View.OnClickListener() { //üyeol içinde aynı şekil başlatıyorum yoksa öldürüyorum finish yani
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UyeOl.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}