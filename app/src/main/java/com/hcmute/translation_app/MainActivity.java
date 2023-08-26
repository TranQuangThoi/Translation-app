package com.hcmute.translation_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;



import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner , toSpinner;
    private TextInputEditText inputText;
    private ImageView mic ;
    private Button translateBtn;
    private TextView translated;

    String [] fromLanguages = {"From" ,"Anh","Việt","Ả Rập","Belarus"
//            ,"tiếng Bungari","Bulgarian","Bengali","Catalan","tiếng séc","xứ Wales"," Đan Mạch","Đức","Hy Lạp","quốc tế ngữ","Tây Ban Nha","Estonian","Ba Tư","Phần Lan","Pháp","Irish","Galician"
//    ,"Gujarati","Do Thái","Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Ý","tiếng Nhật","Georgian","Kannada","Hàn Quốc","Lithuanian","Latvian"
//           , "Macedonian","Marathi","Malay","Maltese","Hà Lan"
    };
    String [] toLanguages ={"To","Anh","Việt","Ả Rập","Belarus"
//            ,"tiếng Bungari","Bulgarian","Bengali","Catalan","tiếng séc","xứ Wales"," Đan Mạch","Đức","Hy Lạp","quốc tế ngữ","Tây Ban Nha","Estonian","Ba Tư","Phần Lan","Pháp","Irish","Galician"
//            ,"Gujarati","Do Thái","Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Ý","tiếng Nhật","Georgian","Kannada","Hàn Quốc","Lithuanian","Latvian"
//            , "Macedonian","Marathi","Malay","Maltese","Hà Lan"
    };



    private static final int REQUEST_PERMISSION_CODE =1;
    int languageCode , fromLanguageCode , toLanguageCode =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        anhxa();


        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });





        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.item_spinner,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);



        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                toLanguageCode = getLanguageCode(toLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.item_spinner,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);



        // xử lý nút dịch
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                translated.setText("");
                if(inputText.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this,"vui lòng nhập văn bản ", Toast.LENGTH_SHORT).show();
                }else if(fromLanguageCode == 0)
                {
                    Toast.makeText(MainActivity.this,"vui lòng chọn ngôn nữ gốc",Toast.LENGTH_SHORT).show();

                }else if(toLanguageCode == 0)
                {
                    Toast.makeText(MainActivity.this,"Vui lòng chọn ngôn ngữ muốn chuyển",Toast.LENGTH_SHORT).show();

                }else {

                    translateText(fromLanguageCode,toLanguageCode,inputText.getText().toString());

                }

            }
        });



        //Xử lý mic
//        mic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                voice.putExtras(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                voice.putExtras(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//            }
//        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                voice.putExtra(RecognizerIntent.EXTRA_PROMPT,"Nói để chuyển đổi ");

                try {

                    startActivityForResult(voice, REQUEST_PERMISSION_CODE);
                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                }

            }


        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PERMISSION_CODE)
        {
            if(resultCode == RESULT_OK && data !=null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                inputText.setText(result.get(0));
            }
        }
    }

    public void translateText(int fromLanguageCode , int toLanguageCode , String text)
    {
        translated.setText("Downloading Modal....");

        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();


        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translated.setText("Translating...");
                translator.translate(text).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translated.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Fail to translate: "+ e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download language Modal "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public int getLanguageCode(String language)
    {
        int languageCode =0;
        switch (language){
            case "Anh":
                languageCode =  FirebaseTranslateLanguage.EN;
                break;
            case "Việt":
                languageCode =  FirebaseTranslateLanguage.VI;
                break;
            case "Ả Rập":
                languageCode =  FirebaseTranslateLanguage.AR;
                break;
            case "Belarus":
                languageCode =  FirebaseTranslateLanguage.BE;
                break;

        }

        return languageCode;
    }
    private void anhxa()
    {
        fromSpinner = (Spinner) findViewById(R.id.spinner_1);

        toSpinner = (Spinner) findViewById(R.id.spinner_2);
        inputText = (TextInputEditText) findViewById(R.id.idEdittext);
        mic = (ImageView) findViewById(R.id.mic);
        translateBtn = (Button) findViewById(R.id.btnTranslate);
        translated = (TextView) findViewById(R.id.translated);



    }
}