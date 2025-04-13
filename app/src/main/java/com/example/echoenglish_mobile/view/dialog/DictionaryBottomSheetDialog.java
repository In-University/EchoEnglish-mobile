package com.example.echoenglish_mobile.view.dialog;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Meaning;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DictionaryBottomSheetDialog extends BottomSheetDialogFragment {

    public static final String TAG = "DictionaryBottomSheet";
    private static final String ARG_SELECTED_WORD = "selected_word";

    // --- Views ---
    private TextView tvWord;
    private TextView tvPronunciation;
    private ImageButton btnUkPronunciationAudio;
    private ImageButton btnUsPronunciationAudio;
    private LinearLayout meaningsContainer;

    // --- Data ---
    private String selectedWord;
    private ApiService apiService;
    private Call<Word> currentApiCall;
    private ExoPlayer exoPlayer;

    public static DictionaryBottomSheetDialog newInstance(String selectedWord) {
        DictionaryBottomSheetDialog fragment = new DictionaryBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_WORD, selectedWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedWord = getArguments().getString(ARG_SELECTED_WORD);
        }
        apiService = ApiClient.getApiService();
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    Log.d(TAG, "ExoPlayer playback ended.");
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "ExoPlayer Error: " + error.getMessage(), error);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Lỗi khi phát âm thanh: " + error.getErrorCodeName(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_dictionary, container, false);

        tvWord = view.findViewById(R.id.tvWord);
        tvPronunciation = view.findViewById(R.id.tv_pronunciation);
        btnUkPronunciationAudio = view.findViewById(R.id.btnUkPronunciationAudio);
        btnUsPronunciationAudio = view.findViewById(R.id.btnUsPronunciationAudio);
        meaningsContainer = view.findViewById(R.id.meaningsContainer);

        tvPronunciation.setText("Loading...");
        tvPronunciation.setVisibility(View.INVISIBLE);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (selectedWord != null && !selectedWord.isEmpty()) {
            tvWord.setText(selectedWord);
            fetchWordDetails(selectedWord);
        } else {
            Log.e(TAG, "Selected word is null or empty.");
            Toast.makeText(getContext(), "Không có từ nào được chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWordDetails(String word) {
        showLoading(true);
        meaningsContainer.removeAllViews();
        meaningsContainer.setVisibility(View.GONE);
        tvPronunciation.setVisibility(View.INVISIBLE);
        disableAudioButtonsOnError();

        if (currentApiCall != null) {
            currentApiCall.cancel();
        }

        currentApiCall = apiService.getWordDetails(word);
        currentApiCall.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                showLoading(false);
                meaningsContainer.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    handleApiError(response.code());
                }
                currentApiCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Log.d(TAG, "API Call was cancelled.");
                } else {
                    showLoading(false);
                    meaningsContainer.setVisibility(View.VISIBLE);
                    Log.e(TAG, "API Failure: " + t.getMessage(), t);
                    meaningsContainer.removeAllViews();
                    TextView errorText = new TextView(getContext());
                    errorText.setText("Network or unknown error");
                    Log.e(TAG, "Network or unknown error", t);
                    meaningsContainer.addView(errorText);
                    disableAudioButtonsOnError();
                }
                currentApiCall = null;
            }
        });
    }

    private void updateUI(Word wordDetail) {
        tvWord.setText(wordDetail.getWord());

        String ukPhonetic = formatPhonetic(wordDetail.getUkPronunciation());
        String usPhonetic = formatPhonetic(wordDetail.getUsPronunciation());
        String combinedPhonetics = "";

        boolean hasUk = !ukPhonetic.isEmpty();
        boolean hasUs = !usPhonetic.isEmpty();

        if (hasUk && hasUs) {
            combinedPhonetics = "UK " + ukPhonetic + "  US " + usPhonetic;
        } else if (hasUk) {
            combinedPhonetics = "UK " + ukPhonetic;
        } else if (hasUs) {
            combinedPhonetics = "US " + usPhonetic;
        } else {
            combinedPhonetics = "IPA not found";
        }

        tvPronunciation.setText(combinedPhonetics);
        tvPronunciation.setVisibility(View.VISIBLE);

        setupPronunciationButton(btnUkPronunciationAudio, wordDetail.getUkAudio());
        setupPronunciationButton(btnUsPronunciationAudio, wordDetail.getUsAudio());

        meaningsContainer.removeAllViews();
        List<Meaning> meanings = wordDetail.getMeanings();
        if (meanings != null && !meanings.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            int count = 0;
            for (Meaning meaning : meanings) {
                if (count >= 5) break;

                View meaningView = inflater.inflate(R.layout.item_meaning, meaningsContainer, false);
                TextView tvPartOfSpeech = meaningView.findViewById(R.id.tvPartOfSpeech);
                TextView tvBulletPoint = meaningView.findViewById(R.id.tvBulletPoint);
                TextView tvLevel = meaningView.findViewById(R.id.tvLevel);
                TextView tvDefinition = meaningView.findViewById(R.id.tvDefinition);

                // Dùng Objects.requireNonNullElse để tránh NullPointerException nếu getPartOfSpeech trả về null
                tvPartOfSpeech.setText(Objects.requireNonNullElse(meaning.getPartOfSpeech(), "").toUpperCase());
                tvDefinition.setText(formatDefinition(count + 1, meaning.getDefinition()));

                String level = meaning.getLevel();
                if (level != null && !level.trim().isEmpty()) {
                    tvLevel.setText(level);
                    tvLevel.setVisibility(View.VISIBLE);
                    tvBulletPoint.setVisibility(View.VISIBLE);
                } else {
                    tvLevel.setVisibility(View.GONE);
                    tvBulletPoint.setVisibility(View.GONE);
                }
                meaningsContainer.addView(meaningView);
                count++;
            }
        } else {
            TextView noMeaningText = new TextView(getContext());
            noMeaningText.setText("Not found any meaning...");
            meaningsContainer.addView(noMeaningText);
        }
    }

    // --- Setup nút phát âm (Giữ nguyên logic này) ---
    private void setupPronunciationButton(ImageButton button, final String audioUrl) {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            button.setEnabled(true);
            button.setAlpha(1.0f); // Đảm bảo button rõ ràng
            button.setOnClickListener(v -> playAudio(audioUrl));
            Log.e(TAG, "setupPronunciationButton::::: ");
        } else {
            button.setEnabled(false);
            button.setOnClickListener(null);
            button.setAlpha(0.5f);
        }
    }

    private void playAudio(String url) {
        if (exoPlayer == null || getContext() == null || url == null || url.isEmpty()) {
            Log.w(TAG, "ExoPlayer not initialized or URL is invalid.");
            return;
        }

        try {
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            String userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36";
            dataSourceFactory.setUserAgent(userAgent);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);

            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.play();

            Log.d(TAG, "ExoPlayer prepare called for: " + url + " with User-Agent: " + userAgent);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up ExoPlayer: " + e.getMessage(), e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Cannot play audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatPhonetic(String phonetic) {
        if (phonetic == null || phonetic.isEmpty()) {
            return "";
        }
        phonetic = phonetic.trim();
        if (!phonetic.startsWith("/")) {
            phonetic = "/" + phonetic;
        }
        if (!phonetic.endsWith("/")) {
            phonetic = phonetic + "/";
        }
        return phonetic;
    }

    private String formatDefinition(int index, String definition) {
        if (definition == null) return index + ". ";
        return index + ". " + definition.trim();
    }

    private void handleApiError(int errorCode) {
        meaningsContainer.removeAllViews();
        TextView errorText = new TextView(getContext());
        if (errorCode == 404) {
            errorText.setText("Not found '" + selectedWord + "' in dictionary.");
        } else {
            errorText.setText("Error when fetching data (Code: " + errorCode + ").");
        }
        meaningsContainer.addView(errorText);

        tvPronunciation.setText("IPA not found");
        tvPronunciation.setVisibility(View.VISIBLE);
        disableAudioButtonsOnError();
    }

    private void disableAudioButtonsOnError() {
        btnUkPronunciationAudio.setEnabled(false);
        btnUkPronunciationAudio.setAlpha(0.5f);
        btnUsPronunciationAudio.setEnabled(false);
        btnUsPronunciationAudio.setAlpha(0.5f);
    }

    private void showLoading(boolean isLoading) {
        float alpha = isLoading ? 0.5f : 1.0f;
        btnUkPronunciationAudio.setAlpha(alpha);
        btnUsPronunciationAudio.setAlpha(alpha);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentApiCall != null) {
            currentApiCall.cancel();
            currentApiCall = null;
        }
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.stop();
            }
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}