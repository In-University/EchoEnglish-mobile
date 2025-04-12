package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.db.DBHandler;

public class FolderFormActivity extends AppCompatActivity {
    private DBHandler dbhandler;
    private int folderIdToUpdate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_form);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button submitFolderButton = findViewById(R.id.submitFolderButton);
        EditText folderNameEditText = findViewById(R.id.folderNameEditText);
        dbhandler = new DBHandler(this);

        Intent intent = getIntent();
        if (intent.hasExtra("folderId") && intent.hasExtra("folderName")) {
            folderIdToUpdate = intent.getIntExtra("folderId", -1);
            String currentFolderName = intent.getStringExtra("folderName");
            folderNameEditText.setText(currentFolderName);
            // setTitle("Edit Folder");
            submitFolderButton.setText("Update");
        } else {
            // setTitle("Create Folder");
        }
        submitFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = folderNameEditText.getText().toString();

                if (folderName.isEmpty()) {
                    Toast.makeText(FolderFormActivity.this, "Please enter folder name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (folderIdToUpdate != -1) {
                    dbhandler.updateFolderName(folderIdToUpdate, folderName);
                    Toast.makeText(FolderFormActivity.this, "Folder has been updated", Toast.LENGTH_SHORT).show();
                } else {
                    dbhandler.addFolder(folderName);
                    Toast.makeText(FolderFormActivity.this, "Folder has been created", Toast.LENGTH_SHORT).show();
                }
                redirectToFolderList();
            }
        });
    }

    private void redirectToFolderList() {
        Intent intent = new Intent(this, FolderListActivity.class);
        startActivity(intent);
        finish();
    }
}