package com.example.echoenglish_mobile.view.activity.dashboard;

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
import android.widget.PopupMenu; // Import PopupMenu

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ForbiddenHandler;
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.view.activity.analyze_result.AnalyzeResultActivity;
import com.example.echoenglish_mobile.view.activity.auth.MainActivity;
import com.example.echoenglish_mobile.view.activity.dictionary.DictionaryWordDetailActivity;
import com.example.echoenglish_mobile.view.activity.dictionary.SearchFragment;
import com.example.echoenglish_mobile.view.activity.document_hub.MainDocumentHubActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.MainFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.SpacedRepetitionActivity;
import com.example.echoenglish_mobile.view.activity.grammar.GrammarActivity;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.UploadSpeechActivity;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizActivity;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateTextActivity;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.view.activity.writing_feedback.UploadNewWritingActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements SearchFragment.SearchListener, View.OnClickListener {

    private static final String TAG = "DashboardActivity";

    private FrameLayout suggestionsOverlayContainer;

    private ImageView ivProfile; // Already exists
    private ViewPager2 viewPagerBanners;

    // Cho tự động cuộn
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private CardView flashcardsCard, translateCard,  grammarCard, quizCard;
    private CardView speechAnalyzeCard, documentHubCard, writingCard, reportCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- Tìm container cho danh sách gợi ý nổi ---
        suggestionsOverlayContainer = findViewById(R.id.suggestions_overlay_container);

        // --- Thêm OnClickListener để xử lý click vào khoảng trống overlay (và khắc phục cảnh báo Lint) ---
        suggestionsOverlayContainer.setOnClickListener(v -> {
            Log.d(TAG, "Overlay container clicked (handled by OnClickListener)");
            // Tìm Fragment SearchFragment
            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
            // Kiểm tra nếu Fragment tồn tại và hiển thị
            if (searchFragment != null && searchFragment.isVisible()) {
                // Lấy View đang focus (thường là EditText trong Fragment)
                View focusedView = getCurrentFocus();
                if (focusedView instanceof EditText) {
                    // Xóa focus và ẩn bàn phím
                    focusedView.clearFocus();
                    hideKeyboard(focusedView);
                }
                // Báo cho Fragment ẩn danh sách gợi ý
                searchFragment.hideSuggestionsList();
            }
        });

        // --- Đảm bảo container này chặn các sự kiện chạm xuống dưới khi hiển thị (nên giữ) ---
        // Sử dụng @SuppressLint để bỏ qua cảnh báo Lint cho listener này
        @SuppressLint("ClickableViewAccessibility")
        View.OnTouchListener touchListener = (v, event) -> {
            if (suggestionsOverlayContainer.getVisibility() == View.VISIBLE) {
                // Trả về true để tiêu thụ sự kiện chạm và ngăn nó truyền xuống View bên dưới.
                // OnClickListener sẽ xử lý sự kiện click (down + up).
                return true; // Chặn chạm
            }
            return false; // Cho phép chạm truyền xuống khi ẩn
        };
        suggestionsOverlayContainer.setOnTouchListener(touchListener);


        // --- Thêm SearchFragment vào container cố định ---
        // Chỉ thêm khi Activity được tạo lần đầu (savedInstanceState == null)
        if (savedInstanceState == null) {
            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
            // Kiểm tra nếu chưa có Fragment trong container
            if (searchFragment == null) {
                searchFragment = new SearchFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.search_fragment_container, searchFragment);
                transaction.commit();
            }
        }

        initializeViews();
        // --- Lấy thông tin người dùng và cập nhật View SAU khi initializeViews ---
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
        // Find the ImageView for the profile picture
        ivProfile = findViewById(R.id.ivProfile); // Make sure ivProfile is found here
        viewPagerBanners = findViewById(R.id.viewPagerBanners);
    }

    private void setClickListeners() {
        if (flashcardsCard != null) flashcardsCard.setOnClickListener(this);
        if (grammarCard != null) grammarCard.setOnClickListener(this);
        if (quizCard != null) quizCard.setOnClickListener(this);
        if (speechAnalyzeCard != null) speechAnalyzeCard.setOnClickListener(this);
        if (documentHubCard != null) documentHubCard.setOnClickListener(this);
        if (writingCard != null) writingCard.setOnClickListener(this);
        if (reportCard != null) reportCard.setOnClickListener(this);
        if (translateCard != null) translateCard.setOnClickListener(this);

        // Add click listener for the profile image
        if (ivProfile != null) ivProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;

        if (id == R.id.flashcardsCard) {
            intent = new Intent(DashboardActivity.this, SpacedRepetitionActivity.class);
        } else if (id == R.id.grammarCard) {
            intent = new Intent(DashboardActivity.this, GrammarActivity.class);
        } else if (id == R.id.quizCard) {
            intent = new Intent(DashboardActivity.this, MainQuizActivity.class);
        } else if (id == R.id.speechAnalyzeCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(DashboardActivity.this, UploadSpeechActivity.class);
        } else if (id == R.id.documentHubCard) {
            intent = new Intent(DashboardActivity.this, MainDocumentHubActivity.class);
        } else if (id == R.id.writingCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(DashboardActivity.this, UploadNewWritingActivity.class);
        } else if (id == R.id.reportCard) {
            if (!isUserLoggedIn()) {
                ForbiddenHandler.handleForbidden();
                return;
            }
            intent = new Intent(DashboardActivity.this, AnalyzeResultActivity.class);
        } else if (id == R.id.translateCard) {
            intent = new Intent(DashboardActivity.this, TranslateTextActivity.class);
        } else if (id == R.id.ivProfile) { // Handle click on profile avatar
            showProfilePopupMenu(v); // Call the method to show the popup
            return; // Consume the click event
        }


        if (intent != null) {
            startActivity(intent);
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
            // Xóa dòng ẩn TabLayout
            // if (tabLayoutBannerIndicator != null) tabLayoutBannerIndicator.setVisibility(View.GONE);
            return;
        }

        BannerAdapter bannerAdapter = new BannerAdapter(this, bannerUrls);
        if (viewPagerBanners != null) {
            viewPagerBanners.setAdapter(bannerAdapter);

            // Thiết lập hiệu ứng chuyển trang (vẫn giữ lại nếu muốn)
            viewPagerBanners.setPageTransformer(new ZoomOutPageTransformer());

            // Xóa toàn bộ phần code liên kết TabLayoutMediator
            /*
            if (tabLayoutBannerIndicator != null) {
                new TabLayoutMediator(tabLayoutBannerIndicator, viewPagerBanners,
                        (tab, position) -> {
                            // Không cần đặt text cho tab, chỉ cần indicator dot
                        }
                ).attach();
            }
            */

            // Cài đặt tự động cuộn (vẫn giữ lại nếu muốn)
            sliderRunnable = () -> {
                if (viewPagerBanners.getCurrentItem() == bannerUrls.size() - 1) {
                    viewPagerBanners.setCurrentItem(0);
                } else {
                    viewPagerBanners.setCurrentItem(viewPagerBanners.getCurrentItem() + 1);
                }
            };

            // Thêm Listener để dừng auto-scroll khi người dùng vuốt (vẫn giữ lại nếu muốn)
            viewPagerBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        stopAutoScroll();
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        // Có thể thêm delay nhỏ ở đây nếu cần, ví dụ 500ms
                        sliderHandler.postDelayed(sliderRunnable, 3000); // Bắt đầu lại sau 3 giây delay
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    // Có thể không cần làm gì ở đây nếu không có indicator
                    // Super class implementation is empty, so safe to leave blank or remove override if not needed
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // Super class implementation is empty, so safe to leave blank or remove override if not needed
                }
            });

            // Bắt đầu auto-scroll ban đầu (vẫn giữ lại nếu muốn)
            startAutoScroll();
        }
    }

    // Hàm bắt đầu tự động cuộn
    private void startAutoScroll() {
        stopAutoScroll();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    // Hàm dừng tự động cuộn
    private void stopAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng tự động cuộn khi Activity không còn hiển thị
        stopAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bắt đầu lại tự động cuộn khi Activity hiển thị lại
        // Chỉ bắt đầu nếu có banner
        if (viewPagerBanners != null && viewPagerBanners.getAdapter() != null && viewPagerBanners.getAdapter().getItemCount() > 0) {
            startAutoScroll();
        }
    }


    // --- Hàm mới để lấy thông tin người dùng và hiển thị (bao gồm ảnh) ---
    private void loadAndDisplayUserInfo() {
        // Lấy thông tin người dùng từ SharedPrefManager
        User user = SharedPrefManager.getInstance(this).getUserInfo();

        // Kiểm tra nếu có thông tin người dùng
        if (user != null) {
            // --- Cập nhật ImageView ảnh đại diện ---
            String avatarUrl = user.getAvatar();
            if (!TextUtils.isEmpty(avatarUrl)) {
                // Sử dụng Glide để tải ảnh từ URL và ÁP DỤNG circleCropTransform
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.image_profile) // Placeholder image while loading
                        .error(R.drawable.image_profile) // Image to show if loading fails
                        // .apply(RequestOptions.circleCropTransform()) // Optional: Apply circle crop transformation if not using CircleImageView
                        .into(ivProfile);
            } else {
                // Set default image if no avatar URL or URL is empty
                ivProfile.setImageResource(R.drawable.image_profile);
            }
        } else {
            // Set default image if user is not logged in or info is not available
            ivProfile.setImageResource(R.drawable.image_profile);
        }
    }

    // --- New method to show the profile popup menu ---
    private void showProfilePopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        // Inflate the menu from the XML file
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        // Set a listener to handle menu item clicks
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_settings) {
                // Handle Settings click
                Toast.makeText(DashboardActivity.this, "Chức năng cài đặt chưa khả dụng", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Settings Activity if you have one
                return true; // Indicate that the click was handled
            } else if (id == R.id.menu_logout) {
                // Handle Logout click
                performLogout(); // Call your existing logout method
                return true; // Indicate that the click was handled
            } else if (id == R.id.menu_exit) {
                // Handle Exit click
                finish(); // Close the current activity
                // If you want to close the entire application, you might need additional flags
                // or navigate back to the root (e.g., HomeActivity if it's the root)
                // but 'finish()' is the standard way to exit the current screen.
                return true; // Indicate that the click was handled
            }
            return false; // Return false if the item ID was not handled
        });

        // Show the popup menu
        popup.show();
    }


    // --- Implement phương thức từ SearchFragment.SearchListener ---

    // Fragment gọi hàm này khi có danh sách gợi ý cần hiển thị
    @Override
    public void showSuggestionsOverlay(RecyclerView recyclerViewSuggestions, int x, int y, int width) {
        if (suggestionsOverlayContainer == null || recyclerViewSuggestions == null) return;

        // Xóa tất cả các View con hiện tại
        suggestionsOverlayContainer.removeAllViews();

        // Thiết lập LayoutParams cho RecyclerView
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                width, // Chiều rộng bằng chiều rộng của search bar
                ViewGroup.LayoutParams.WRAP_CONTENT // Chiều cao wrap_content
        );
        // Đặt vị trí (margin top và margin left)
        params.leftMargin = x;
        params.topMargin = y;
        // Có thể thêm Gravity nếu cần căn chỉnh khác
        // params.gravity = Gravity.TOP | Gravity.START;


        // Đảm bảo RecyclerView được thêm vào overlay
        if (recyclerViewSuggestions.getParent() != null) {
            ((ViewGroup) recyclerViewSuggestions.getParent()).removeView(recyclerViewSuggestions);
        }
        suggestionsOverlayContainer.addView(recyclerViewSuggestions, params);
        Log.d(TAG, "Suggestions RecyclerView added to overlay at x=" + x + ", y=" + y + ", width=" + width);


        // Hiển thị container overlay nếu chưa hiển thị
        if (suggestionsOverlayContainer.getVisibility() != View.VISIBLE) {
            suggestionsOverlayContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Suggestions overlay container set to VISIBLE");
        }
    }

    // Fragment gọi hàm này khi danh sách cần ẩn
    @Override
    public void hideSuggestionsOverlay() {
        if (suggestionsOverlayContainer != null && suggestionsOverlayContainer.getVisibility() == View.VISIBLE) {
            // Xóa tất cả View con
            suggestionsOverlayContainer.removeAllViews();
            // Ẩn container overlay
            suggestionsOverlayContainer.setVisibility(View.GONE);
            Log.d(TAG, "Suggestions overlay hidden");
        }
    }

    // Fragment gọi hàm này khi có chi tiết từ
    @Override
    public void onWordDetailRequested(Word wordData) {
        Log.d(TAG, "Received word detail request from SearchFragment: " + wordData.getWord());
        navigateToDetail(wordData);
    }

    // --- Hàm để chuyển sang màn hình chi tiết từ ---
    private void navigateToDetail(Word wordData) {
        Intent intent = new Intent(this, DictionaryWordDetailActivity.class);
        intent.putExtra("word_data", wordData);
        startActivity(intent);
    }

    // --- Hàm xử lý logout (Already exists) ---
    private void performLogout() {
        SharedPrefManager.getInstance(this).clear();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // --- Implement logic ẩn bàn phím/list khi chạm ngoài ---
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView instanceof EditText) {
                View overlayContainer = findViewById(R.id.suggestions_overlay_container);

                if (overlayContainer != null && overlayContainer.getVisibility() == View.VISIBLE) {
                    Rect overlayRect = new Rect();
                    overlayContainer.getGlobalVisibleRect(overlayRect);

                    // Nếu điểm chạm nằm ngoài Rect của overlay container
                    if (!overlayRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        focusedView.clearFocus();
                        hideKeyboard(focusedView);
                        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
                        if (searchFragment != null && searchFragment.isVisible()) {
                            searchFragment.hideSuggestionsList();
                        }
                    } else {
                        // Nếu điểm chạm nằm trong overlay container
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

                        // Nếu chạm không nằm trên bất kỳ View con nào (chạm vào khoảng trống của overlay)
                        if (!touchedChild) {
                            // This block is reached when touching the overlay but not a child (like the RecyclerView)
                            // We might still want to dismiss the keyboard/suggestions here
                            focusedView.clearFocus();
                            hideKeyboard(focusedView);
                            SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment_container);
                            if (searchFragment != null && searchFragment.isVisible()) {
                                searchFragment.hideSuggestionsList();
                            }
                        }
                        // If touchedChild is true, the touch is on the RecyclerView item, let it handle the event naturally.
                    }
                } else {
                    // Nếu overlay không hiển thị, xử lý chạm ngoài EditText bất kỳ
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

    // Hàm tiện ích để ẩn bàn phím
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}