package com.example.echoenglish_mobile.view.activity.document_hub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.browser.BrowserActivity;
import com.example.echoenglish_mobile.view.activity.video_youtube.VideoYoutubeActivity;

public class MainDocumentHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_hub);

        LinearLayout browseWebButton = findViewById(R.id.browseWebButton);
        LinearLayout youtubeImportButton = findViewById(R.id.youtubeImportButton);

        browseWebButton.setOnClickListener(v -> navigateToBrowserActivity());

        youtubeImportButton.setOnClickListener(v -> showYoutubeImportPopup());
    }

    private void navigateToBrowserActivity() {
        Intent intent = new Intent(this, BrowserActivity.class);
        startActivity(intent);
    }

    private void showYoutubeImportPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import YouTube Link");
        builder.setMessage("Enter the YouTube video URL:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(48, 16, 48, 16);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Import", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                Toast.makeText(this, "Importing: " + url, Toast.LENGTH_SHORT).show();
                String videoId = getIDFromYoutubeLink(url);
                Intent intent = new Intent(this, VideoYoutubeActivity.class);
                intent.putExtra("VideoId", videoId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private String getIDFromYoutubeLink(String url) {
        String pattern = "(?<=v=|youtu\\.be/)[^&#]+";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
