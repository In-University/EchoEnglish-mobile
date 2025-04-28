package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.content.Context;
import android.view.LayoutInflater; // Không cần nữa nếu chỉ tạo RecyclerView
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeViewPagerAdapter extends RecyclerView.Adapter<AnalyzeViewPagerAdapter.PageViewHolder> {

    private static final int SPEAKING_PAGE = 0;
    private static final int WRITING_PAGE = 1;
    private final Context context;

    public AnalyzeViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? SPEAKING_PAGE : WRITING_PAGE;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- Tạo RecyclerView bằng code ---
        RecyclerView recyclerView = new RecyclerView(context);
        // Quan trọng: Đặt LayoutParams để RecyclerView chiếm toàn bộ không gian của trang ViewPager
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        // (Tùy chọn) Thêm padding nếu muốn, giống như trong file XML đã xóa
        int padding = (int) (context.getResources().getDisplayMetrics().density * 8); // 8dp
        recyclerView.setPadding(0, padding, 0, padding);
        recyclerView.setClipToPadding(false);

        // (Tùy chọn nhưng nên làm) Đặt ID để tránh xung đột tiềm ẩn
        recyclerView.setId(View.generateViewId());

        return new PageViewHolder(recyclerView); // Trả về ViewHolder chứa RecyclerView vừa tạo
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        // Lấy RecyclerView từ ViewHolder
        RecyclerView currentPageRecyclerView = holder.recyclerView;

        // Thiết lập LayoutManager (luôn cần thiết cho RecyclerView)
        currentPageRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        currentPageRecyclerView.setHasFixedSize(true); // Tối ưu hóa

        // Thiết lập Adapter tương ứng cho RecyclerView bên trong
        if (position == SPEAKING_PAGE) {
            List<SpeakingResult> speakingResults = createFakeSpeakingData();
            SpeakingResultAdapter speakingAdapter = new SpeakingResultAdapter(context, speakingResults);
            currentPageRecyclerView.setAdapter(speakingAdapter);
        } else { // WRITING_PAGE
            List<WritingResult> writingResults = createFakeWritingData();
            WritingResultAdapter writingAdapter = new WritingResultAdapter(context, writingResults);
            currentPageRecyclerView.setAdapter(writingAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Vẫn là 2 trang
    }

    // --- ViewHolder cho mỗi trang ViewPager ---
    static class PageViewHolder extends RecyclerView.ViewHolder {
        // Giờ đây ViewHolder giữ tham chiếu trực tiếp đến RecyclerView của trang
        RecyclerView recyclerView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            // itemView chính là RecyclerView được tạo trong onCreateViewHolder
            this.recyclerView = (RecyclerView) itemView;
        }
    }

    // --- Các phương thức tạo dữ liệu giả (giữ nguyên) ---
    private List<SpeakingResult> createFakeSpeakingData() {
        List<SpeakingResult> list = new ArrayList<>();
        list.add(new SpeakingResult("How to introduce yourself", "24/04/2025 · 15:30", "Giới thiệu", 92, 95, 88, 90));
        list.add(new SpeakingResult("Ordering food at a restaurant", "23/04/2025 · 11:05", "Đời sống", 85, 88, 80, 86));
        list.add(new SpeakingResult("Talking about your hobbies", "22/04/2025 · 09:15", "Sở thích", 90, 91, 92, 89));
        // Thêm dữ liệu khác nếu cần
        return list;
    }

    private List<WritingResult> createFakeWritingData() {
        List<WritingResult> list = new ArrayList<>();
        list.add(new WritingResult("Advantages and disadvantages of social media", "25/04/2025 · 10:15", "Essay", 250));
        list.add(new WritingResult("My favorite holiday destination", "24/04/2025 · 16:00", "Description", 180));
        list.add(new WritingResult("The importance of learning English", "23/04/2025 · 08:30", "Argumentative", 310));
        // Thêm dữ liệu khác nếu cần
        return list;
    }
}