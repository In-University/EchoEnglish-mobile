package com.example.echoenglish_mobile.ui.dictionary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.activity.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainDictionaryActivity extends AppCompatActivity {

    private EditText editTextWord;
    private Button buttonSearch;
    private TextView textWord, textUkPronunciation, textUsPronunciation, textMeanings, textSynonyms;
    private ImageView imageWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dictionary);

        editTextWord = findViewById(R.id.editTextWord);
        buttonSearch = findViewById(R.id.buttonSearch);
        textWord = findViewById(R.id.textWord);
        textUkPronunciation = findViewById(R.id.textUkPronunciation);
        textUsPronunciation = findViewById(R.id.textUsPronunciation);
        textMeanings = findViewById(R.id.textMeanings);
        textSynonyms = findViewById(R.id.textSynonyms);
        imageWord = findViewById(R.id.imageWord);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = editTextWord.getText().toString().trim();
                if (!word.isEmpty()) {
                    searchWord(word);
                } else {
                    Toast.makeText(MainDictionaryActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchWord(String word) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Word> call = apiService.getWordDetails(word);

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, Response<Word> response) {
                if (response.isSuccessful()) {
                    Word wordData = response.body();
                    if (wordData != null) {
                        textWord.setText(wordData.getWord());
                        textUkPronunciation.setText("UK: " + wordData.getUkPronunciation());
                        textUsPronunciation.setText("US: " + wordData.getUsPronunciation());

                        // Set image
                        Picasso.get().load(wordData.getImageUrl()).into(imageWord);

                        // Set meanings
                        StringBuilder meanings = new StringBuilder();
                        for (Meaning meaning : wordData.getMeanings()) {
                            meanings.append(meaning.getPartOfSpeech())
                                    .append(": ")
                                    .append(meaning.getDefinition())
                                    .append("\nExample: ")
                                    .append(meaning.getExample())
                                    .append("\n\n");
                        }
                        textMeanings.setText(meanings.toString());

                        // Set synonyms
                        StringBuilder synonyms = new StringBuilder();
                        for (Synonym synonym : wordData.getSynonyms()) {
                            synonyms.append(synonym.getSynonym()).append("\n");
                        }
                        textSynonyms.setText("Synonyms:\n" + synonyms.toString());
                    }
                } else {
                    Toast.makeText(MainDictionaryActivity.this, "Word not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                Toast.makeText(MainDictionaryActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
