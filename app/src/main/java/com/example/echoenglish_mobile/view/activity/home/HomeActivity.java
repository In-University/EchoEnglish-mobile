package com.example.echoenglish_mobile.view.activity.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ForbiddenHandler;
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.view.activity.analyze_result.AnalyzeResultActivity;
import com.example.echoenglish_mobile.view.activity.auth.MainActivity;
import com.example.echoenglish_mobile.view.activity.chatbot.ConversationCategoriesActivity;
import com.example.echoenglish_mobile.view.activity.dictionary.DictionaryWordDetailActivity;
import com.example.echoenglish_mobile.view.activity.dictionary.SearchFragment;
import com.example.echoenglish_mobile.view.activity.document_hub.MainDocumentHubActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.SpacedRepetitionActivity;
import com.example.echoenglish_mobile.view.activity.grammar.GrammarActivity;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.UploadSpeechActivity;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizActivity;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateTextActivity;
import com.example.echoenglish_mobile.view.activity.writing_feedback.UploadNewWritingActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SearchFragment.SearchListener, View.OnClickListener {

    private static final String TAG = "DashboardActivity";
    private static final long CLICK_DEBOUNCE_DELAY_MS = 500;

    private FrameLayout suggestionsOverlayContainer;

    private ImageView ivProfile;
    private ViewPager2 viewPagerBanners;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private CardView flashcardsCard, translateCard,  grammarCard, quizCard, conversationCard;
    private CardView speechAnalyzeCard, documentHubCard, writingCard, reportCard;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        suggestionsOverlayContainer = findViewById(R.id.suggestions_overlay_container);

        suggestionsOverlayContainer.setOnClickListener(v -> {
            Log.d(TAG, "Overlay container clicked (handled by OnClickListener)");
            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
            if (searchFragment != null && searchFragment.isVisible()) {
                View focusedView = getCurrentFocus();
                if (focusedView instanceof EditText) {
                    focusedView.clearFocus();
                    hideKeyboard(focusedView);
                }
                searchFragment.hideSuggestionsList();
            }
        });

        @SuppressLint("ClickableViewAccessibility")
        View.OnTouchListener touchListener = (v, event) -> {
            if (suggestionsOverlayContainer.getVisibility() == View.VISIBLE) {
                return true;
            }
            return false;
        };
        suggestionsOverlayContainer.setOnTouchListener(touchListener);


        if (savedInstanceState == null) {
            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
            if (searchFragment == null) {
                searchFragment = new SearchFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.search_fragment_container, searchFragment);
                transaction.commit();
            }
        }

        initializeViews();
        loadAndDisplayUserInfo();
        setClickListeners();
        setupBanners();



    }

    private void initializeViews() {
        flashcardsCard = findViewById(R.id.flashcardsCard);
        translateCard = findViewById(R.id.translateCard);
        grammarCard = findViewById(R.id.grammarCard);
        quizCard = findViewById(R.id.quizCard);
        speechAnalyzeCard = findViewById(R.id.speechAnalyzeCard);
        documentHubCard = findViewById(R.id.documentHubCard);
        reportCard = findViewById(R.id.reportCard);
        writingCard = findViewById(R.id.writingCard);
        conversationCard = findViewById(R.id.conversationCard);
        ivProfile = findViewById(R.id.ivProfile);
        viewPagerBanners = findViewById(R.id.viewPagerBanners);

    }

    private void setClickListeners() {
        if (flashcardsCard != null) flashcardsCard.setOnClickListener(this);
        if (translateCard != null) translateCard.setOnClickListener(this);
        if (grammarCard != null) grammarCard.setOnClickListener(this);
        if (quizCard != null) quizCard.setOnClickListener(this);
        if (speechAnalyzeCard != null) speechAnalyzeCard.setOnClickListener(this);
        if (documentHubCard != null) documentHubCard.setOnClickListener(this);
        if (writingCard != null) writingCard.setOnClickListener(this);
        if (reportCard != null) reportCard.setOnClickListener(this);
        if (translateCard != null) translateCard.setOnClickListener(this);
        if (conversationCard != null) conversationCard.setOnClickListener(this);

        if (ivProfile != null) ivProfile.setOnClickListener(this);
    }

    private void setFeatureCardsEnabled(boolean enabled) {
        Log.d(TAG, "Setting feature cards enabled state: " + enabled);
        if (flashcardsCard != null) flashcardsCard.setEnabled(enabled);
        if (translateCard != null) translateCard.setEnabled(enabled);
        if (grammarCard != null) grammarCard.setEnabled(enabled);
        if (quizCard != null) quizCard.setEnabled(enabled);
        if (speechAnalyzeCard != null) speechAnalyzeCard.setEnabled(enabled);
        if (documentHubCard != null) documentHubCard.setEnabled(enabled);
        if (writingCard != null) writingCard.setEnabled(enabled);
        if (reportCard != null) reportCard.setEnabled(enabled);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivProfile) {
            showProfilePopupMenu(v);
            return;
        }

        if (v instanceof CardView && v.isEnabled()) {
            int clickedId = v.getId();
            if (clickedId == R.id.flashcardsCard || clickedId == R.id.translateCard ||
                    clickedId == R.id.grammarCard || clickedId == R.id.quizCard ||
                    clickedId == R.id.speechAnalyzeCard || clickedId == R.id.documentHubCard ||
                    clickedId == R.id.writingCard || clickedId == R.id.reportCard) {

                setFeatureCardsEnabled(false);

                mainHandler.postDelayed(() -> {
                    setFeatureCardsEnabled(true);
                }, CLICK_DEBOUNCE_DELAY_MS);
            }
        } else {
            return;
        }


        int id = v.getId();
        Intent intent = null;

        if (id == R.id.flashcardsCard) {
            intent = new Intent(HomeActivity.this, SpacedRepetitionActivity.class);
        } else if (id == R.id.translateCard) {
            intent = new Intent(HomeActivity.this, TranslateTextActivity.class);
        } else if (id == R.id.grammarCard) {
            intent = new Intent(HomeActivity.this, GrammarActivity.class);
        } else if (id == R.id.quizCard) {
            intent = new Intent(HomeActivity.this, MainQuizActivity.class);
        } else if (id == R.id.speechAnalyzeCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(HomeActivity.this, UploadSpeechActivity.class);
        } else if (id == R.id.documentHubCard) {
            intent = new Intent(HomeActivity.this, MainDocumentHubActivity.class);
        } else if (id == R.id.writingCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(HomeActivity.this, UploadNewWritingActivity.class);
        } else if (id == R.id.reportCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(HomeActivity.this, AnalyzeResultActivity.class);
        } else if (id == R.id.translateCard) {
            intent = new Intent(HomeActivity.this, TranslateTextActivity.class);
        } else if (id == R.id.conversationCard) {
            intent = new Intent(HomeActivity.this, ConversationCategoriesActivity.class);
        } else if (id == R.id.ivProfile) {
            showProfilePopupMenu(v);
            return;
        }


        if (intent != null) {
            startActivity(intent);
        } else {
            setFeatureCardsEnabled(true);
            mainHandler.removeCallbacks(null);
        }
    }

    private boolean isUserLoggedIn() {
        String token = SharedPrefManager.getInstance(MyApp.getAppContext()).getAuthToken();
        return token != null && !token.isEmpty();
    }

    private void setupBanners() {
        List<String> bannerUrls = new ArrayList<>();
        bannerUrls.add("https://edumart.edu.vn/public/files/upload/0000000000000000000000111111111111/images/banner(6).jpg");
        bannerUrls.add("https://bmyc.vn/wp-content/uploads/2024/06/banner-dong-hanh-cung-con-tu-hoc-tieng-anh-tai-nha-scaled.jpg");
        bannerUrls.add("https://thedragon.edu.vn/wp-content/uploads/2022/10/Banner-tieng-Anh-tong-quat-min.png");
        bannerUrls.add("https://newsky.edu.vn/wp-content/uploads/khoa-hoc-tieng-trung-tai-newsky.png");

        if (bannerUrls.isEmpty()) {
            if (viewPagerBanners != null) viewPagerBanners.setVisibility(View.GONE);
            return;
        }

        BannerAdapter bannerAdapter = new BannerAdapter(this, bannerUrls);
        if (viewPagerBanners != null) {
            viewPagerBanners.setAdapter(bannerAdapter);

            viewPagerBanners.setPageTransformer(new ZoomOutPageTransformer());

            sliderRunnable = () -> {
                if (viewPagerBanners.getCurrentItem() == bannerUrls.size() - 1) {
                    viewPagerBanners.setCurrentItem(0);
                } else {
                    viewPagerBanners.setCurrentItem(viewPagerBanners.getCurrentItem() + 1);
                }
            };

            viewPagerBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        stopAutoScroll();
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        sliderHandler.postDelayed(sliderRunnable, 3000);
                    }
                }
            });

            startAutoScroll();
        }
    }

    private void startAutoScroll() {
        stopAutoScroll();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void stopAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFeatureCardsEnabled(true);
        if (viewPagerBanners != null && viewPagerBanners.getAdapter() != null && viewPagerBanners.getAdapter().getItemCount() > 0) {
            startAutoScroll();
        }
    }

    private void loadAndDisplayUserInfo() {
        User user = SharedPrefManager.getInstance(this).getUserInfo();

        if (user != null) {
            String avatarUrl = user.getAvatar();
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.image_profile)
                        .error(R.drawable.image_profile)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.image_profile);
            }
        } else {
            ivProfile.setImageResource(R.drawable.image_profile);
        }
    }

    private void showProfilePopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_profile) {
                Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_logout) {
                performLogout();
                return true;
            } else if (id == R.id.menu_exit) {
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    public void showSuggestionsOverlay(RecyclerView recyclerViewSuggestions, int x, int y, int width) {
        if (suggestionsOverlayContainer == null || recyclerViewSuggestions == null) return;

        suggestionsOverlayContainer.removeAllViews();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = x;
        params.topMargin = y;

        if (recyclerViewSuggestions.getParent() != null) {
            ((ViewGroup) recyclerViewSuggestions.getParent()).removeView(recyclerViewSuggestions);
        }
        suggestionsOverlayContainer.addView(recyclerViewSuggestions, params);
        Log.d(TAG, "Suggestions RecyclerView added to overlay at x=" + x + ", y=" + y + ", width=" + width);


        if (suggestionsOverlayContainer.getVisibility() != View.VISIBLE) {
            suggestionsOverlayContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Suggestions overlay container set to VISIBLE");
        }
    }

    @Override
    public void hideSuggestionsOverlay() {
        if (suggestionsOverlayContainer != null && suggestionsOverlayContainer.getVisibility() == View.VISIBLE) {
            suggestionsOverlayContainer.removeAllViews();
            suggestionsOverlayContainer.setVisibility(View.GONE);
            Log.d(TAG, "Suggestions overlay hidden");
        }
    }

    @Override
    public void onWordDetailRequested(Word wordData) {
        Log.d(TAG, "Received word detail request from SearchFragment: " + wordData.getWord());
        navigateToDetail(wordData);
    }

    private void navigateToDetail(Word wordData) {
        Intent intent = new Intent(this, DictionaryWordDetailActivity.class);
        intent.putExtra("word_data", wordData);
        startActivity(intent);
    }

    private void performLogout() {
        SharedPrefManager.getInstance(this).clear();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView instanceof EditText) {
                View overlayContainer = findViewById(R.id.suggestions_overlay_container);

                if (overlayContainer != null && overlayContainer.getVisibility() == View.VISIBLE) {
                    Rect overlayRect = new Rect();
                    overlayContainer.getGlobalVisibleRect(overlayRect);
                    if (!overlayRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        focusedView.clearFocus();
                        hideKeyboard(focusedView);
                        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
                        if (searchFragment != null && searchFragment.isVisible()) {
                            searchFragment.hideSuggestionsList();
                        }
                    } else {
                        boolean touchedChild = false;
                        if (overlayContainer instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) overlayContainer;
                            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                View child = viewGroup.getChildAt(i);
                                Rect childRect = new Rect();
                                child.getGlobalVisibleRect(childRect);

                                if (childRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                                    touchedChild = true;
                                    break;
                                }
                            }
                        }

                        if (!touchedChild) {
                            focusedView.clearFocus();
                            hideKeyboard(focusedView);
                            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
                            if (searchFragment != null && searchFragment.isVisible()) {
                                searchFragment.hideSuggestionsList();
                            }
                        }
                    }
                } else {
                    Rect focusedRect = new Rect();
                    focusedView.getGlobalVisibleRect(focusedRect);
                    if (!focusedRect.contains((int)ev.getRawX(), (int)ev.getRawY())){
                        focusedView.clearFocus();
                        hideKeyboard(focusedView);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}