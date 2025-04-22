package com.example.echoenglish_mobile.view.activity.flashcard;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;

public class LearnPagerAdapter extends RecyclerView.Adapter<LearnPagerAdapter.LearnCardViewHolder> {

    private static final String TAG = "LearnPagerAdapter"; // Thêm TAG để log
    private Context context;
    private List<VocabularyResponse> vocabularyList;
    private List<Boolean> isFlippedList;

    public LearnPagerAdapter(Context context, List<VocabularyResponse> vocabularyList) {
        this.context = context;
        this.vocabularyList = vocabularyList != null ? vocabularyList : new ArrayList<>();
        this.isFlippedList = new ArrayList<>(this.vocabularyList.size());  // theo dõi trạng thái lật của từng thẻ (true = mặt sau, false = mặt trước).
        for (int i = 0; i < this.vocabularyList.size(); i++) {
            isFlippedList.add(false);
        }
    }

    @NonNull
    @Override
    public LearnCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learn_card, parent, false);
        return new LearnCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LearnCardViewHolder holder, int position) {
        // Kiểm tra position hợp lệ ngay từ đầu
        if (position < 0 || position >= vocabularyList.size() || position >= isFlippedList.size()) {
            Log.e(TAG, "Invalid position in onBindViewHolder: " + position + " Vocab Size: " + vocabularyList.size() + " Flipped Size: " + isFlippedList.size());
            holder.itemView.setVisibility(View.GONE); // Ẩn item lỗi
            return;
        }
        // Đảm bảo item hiển thị nếu trước đó bị ẩn
        holder.itemView.setVisibility(View.VISIBLE);

        VocabularyResponse vocab = vocabularyList.get(position);
        holder.bind(vocab, context);

        // Reset tag trạng thái animation
        holder.cardContainer.setTag(false); // Mặc định là không đang chạy animation

        // Đặt trạng thái lật ban đầu một cách an toàn
        boolean isFlipped = isFlippedList.get(position);
        holder.setFlipped(isFlipped);

        // Flip Animation Logic

        View.OnClickListener flipClickListener = v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < isFlippedList.size()) {
                Object tag = holder.cardContainer.getTag(); // Vẫn dùng tag của container để tránh click khi đang animation
                Boolean isAnimating = (tag instanceof Boolean) ? (Boolean) tag : false;
                if (isAnimating) {
                    return;
                }
                boolean wasFlipped = isFlippedList.get(currentPosition);
                isFlippedList.set(currentPosition, !wasFlipped);
                flipCard(holder, !wasFlipped);
            }
        };

        holder.frontCardView.setOnClickListener(flipClickListener);
        holder.backCardView.setOnClickListener(flipClickListener);

//        holder.cardContainer.setOnClickListener(v -> {
//            int currentPosition = holder.getAdapterPosition(); // Lấy vị trí mới nhất
//
//            // Kiểm tra lại position một lần nữa
//            if (currentPosition == RecyclerView.NO_POSITION || currentPosition < 0 || currentPosition >= isFlippedList.size()) {
//                Log.w(TAG, "Invalid or outdated position on click: " + currentPosition);
//                return; // Thoát nếu position không hợp lệ
//            }
//
//            Object tag = holder.cardContainer.getTag();
//            Boolean isAnimating = (tag instanceof Boolean) ? (Boolean) tag : false;
//
//            if (isAnimating) {
//                Log.d(TAG, "Animation already running at position " + currentPosition + ", ignoring click.");
//                return;
//            }
//
//            // Lấy trạng thái lật hiện tại từ list (an toàn hơn lấy từ biến cục bộ)
//            boolean currentFlippedState = isFlippedList.get(currentPosition);
//            boolean nextFlippedState = !currentFlippedState; // Trạng thái muốn lật tới
//
//            Log.d(TAG, "Click detected at position: " + currentPosition + ". Current Flipped: " + currentFlippedState + ". Flipping to: " + nextFlippedState);
//
//            isFlippedList.set(currentPosition, nextFlippedState); // Cập nhật trạng thái logic
//            flipCard(holder, nextFlippedState); // Bắt đầu animation
//        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    public VocabularyResponse getItem(int position) {
        if (position >= 0 && position < vocabularyList.size()) {
            return vocabularyList.get(position);
        }
        return null;
    }

    public void updateData(List<VocabularyResponse> newVocabularies) {
        this.vocabularyList.clear();
        this.isFlippedList.clear();
        if (newVocabularies != null) {
            this.vocabularyList.addAll(newVocabularies);
            for (int i = 0; i < this.vocabularyList.size(); i++) {
                isFlippedList.add(false);
            }
        }
        // Sử dụng notifyDataSetChanged() tạm thời, nên dùng DiffUtil cho hiệu năng tốt hơn
        notifyDataSetChanged();
        Log.d(TAG, "Data updated. New size: " + vocabularyList.size());
    }


    private void flipCard(LearnCardViewHolder holder, boolean showBack) {
        // Đánh dấu đang chạy animation
        holder.cardContainer.setTag(true);
        Log.d(TAG, "flipCard called at position " + holder.getAdapterPosition() + ". ShowBack: " + showBack);


        Animator outAnimator, inAnimator;
        View frontView = holder.frontCardView;
        View backView = holder.backCardView;

        // Đảm bảo cả hai view đều có thể nhìn thấy được bởi hệ thống animation ban đầu
        // (Visibility sẽ được quản lý bởi listener sau)
        // frontView.setVisibility(View.VISIBLE);
        // backView.setVisibility(View.VISIBLE);


        if (showBack) {
            outAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_out);
            inAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in);
        } else {
            outAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
            inAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);
        }

        if (outAnimator == null || inAnimator == null) {
            Log.e(TAG, "Failed to load animators!");
            holder.cardContainer.setTag(false); // Reset tag nếu animator lỗi
            // Fallback: chỉ đổi visibility không animation
            holder.setFlipped(showBack);
            return;
        }


        outAnimator.setTarget(showBack ? frontView : backView);
        inAnimator.setTarget(showBack ? backView : frontView);

        float scale = context.getResources().getDisplayMetrics().density;
        frontView.setCameraDistance(8000 * scale);
        backView.setCameraDistance(8000 * scale);

        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            private boolean outAnimationEnded = false;
            private boolean inAnimationEnded = false; // Theo dõi cả hai animation

            // Hàm kiểm tra và thực hiện cleanup khi cả hai animation kết thúc
            private void checkAndCleanup() {
                if (outAnimationEnded && inAnimationEnded) {
                    Log.d(TAG, "Both animations ended for position " + holder.getAdapterPosition() + ". ShowBack: " + showBack);

                    // Chỉ ẩn view cũ đi SAU KHI cả hai animation hoàn tất
                    if (showBack) {
                        frontView.setVisibility(View.GONE);
                    } else {
                        backView.setVisibility(View.GONE);
                    }

                    // Reset rotation của cả hai view về 0 (rất quan trọng)
                    frontView.setRotationY(0);
                    backView.setRotationY(0);

                    // Reset alpha (đảm bảo không bị trong suốt)
                    frontView.setAlpha(1f);
                    backView.setAlpha(1f);

                    // Đánh dấu kết thúc animation
                    holder.cardContainer.setTag(false);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                // Khi animation 'in' BẮT ĐẦU, đảm bảo view MỚI là VISIBLE
                // để người dùng thấy nó quay vào
                if (animation == inAnimator) {
                    Log.d(TAG, "In-Animation started. Making target view visible.");
                    if (showBack) backView.setVisibility(View.VISIBLE); else frontView.setVisibility(View.VISIBLE);
                }
                // Không cần làm gì đặc biệt khi out-animation bắt đầu
                // vì view đó đã hiển thị sẵn rồi
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "One animation ended for position " + holder.getAdapterPosition());
                if (animation == outAnimator) {
                    outAnimationEnded = true;
                } else if (animation == inAnimator) {
                    inAnimationEnded = true;
                }
                checkAndCleanup(); // Kiểm tra xem có cần cleanup chưa
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Nếu animation bị hủy giữa chừng, reset trạng thái
                Log.w(TAG, "Animation cancelled for position " + holder.getAdapterPosition());
                outAnimationEnded = true; // Coi như đã kết thúc để cleanup
                inAnimationEnded = true;
                checkAndCleanup();
            }
        };

        // Gắn listener cho cả hai animator để theo dõi kết thúc
        outAnimator.addListener(listener);
        inAnimator.addListener(listener);

        // Bắt đầu animation
        outAnimator.start();
        inAnimator.start();
    }

    // ViewHolder Class
    static class LearnCardViewHolder extends RecyclerView.ViewHolder {
        FrameLayout cardContainer;
        View frontCardView, backCardView;
        ImageView imageView;
        TextView wordText, phoneticText, typeText, definitionText, exampleText;

        LearnCardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.learnCardContainer);
            frontCardView = itemView.findViewById(R.id.viewLearnCardFront);
            backCardView = itemView.findViewById(R.id.viewLearnCardBack);

            imageView = itemView.findViewById(R.id.imageViewLearnCard);
            wordText = itemView.findViewById(R.id.textViewLearnCardWord);
            phoneticText = itemView.findViewById(R.id.textViewLearnCardPhonetic);
            typeText = itemView.findViewById(R.id.textViewLearnCardType);

            definitionText = itemView.findViewById(R.id.textViewLearnCardDefinition);
            exampleText = itemView.findViewById(R.id.textViewLearnCardExample);
        }

        void bind(VocabularyResponse vocab, Context context) {
            // Bind data mặt trước
            wordText.setText(vocab.getWord());
            phoneticText.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
            phoneticText.setVisibility(vocab.getPhonetic() != null ? View.VISIBLE : View.GONE);
            typeText.setText(vocab.getType() != null ? ("("+vocab.getType()+")") : "");
            typeText.setVisibility(vocab.getType() != null ? View.VISIBLE : View.GONE);

            // Glide ảnh mặt trước
            if (imageView != null && vocab.getImageUrl() != null && !vocab.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(vocab.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else if (imageView != null) {
                imageView.setVisibility(View.GONE); // Ẩn nếu không có ảnh
            }

            // Bind data mặt sau
            definitionText.setText(vocab.getDefinition());
            exampleText.setText(vocab.getExample() != null ? "Ví dụ: " + vocab.getExample() : "");
            exampleText.setVisibility(vocab.getExample() != null ? View.VISIBLE : View.GONE);
        }

        void setFlipped(boolean isFlipped) {
            // Quan trọng: Reset rotation và alpha TRƯỚC KHI đổi visibility
            frontCardView.setRotationY(0);
            backCardView.setRotationY(0);
            frontCardView.setAlpha(1f);
            backCardView.setAlpha(1f);

            // Đặt visibility
            frontCardView.setVisibility(isFlipped ? View.GONE : View.VISIBLE);
            backCardView.setVisibility(isFlipped ? View.VISIBLE : View.GONE);
            Log.d("SetFlipped", "Position " + getAdapterPosition() + " set to flipped: " + isFlipped);

        }
    }
}