package com.example.echoenglish_mobile.ui.flashcard.about;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.base.BaseActivity;


public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Dùng theme là NoActionBar nên k xài được cái này
//        setTitle("About");

        TextView about = findViewById(R.id.textViewDescription);
        String htmlDescription = getString(R.string.app_description);
        about.setText(Html.fromHtml(htmlDescription));
    }
}