package com.example.echoenglish_mobile.ui.pronunciation_assessment;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.echoenglish_mobile.data.model.PhonemeComparison;

import java.util.List;

public class PhonemeTextView extends AppCompatTextView {

    public PhonemeTextView(Context context) {
        super(context);
    }

    public PhonemeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhonemeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPhonemeData(String targetWord, List<PhonemeComparison> dtoList) {
        if (dtoList == null || dtoList.isEmpty() || dtoList.get(0).getResult() == null) {
            setText("");
            return;
        }

        String text = targetWord;
        SpannableString spannable = new SpannableString(text);

        for (PhonemeComparison dto : dtoList) {
            int start = dto.getStartIndex();
            int end = dto.getEndIndex();
            end = Math.min(end + 1, text.length());

            if (start < 0 || end > text.length()) {
                continue;
            }

            if ("correct".equals(dto.getResult())) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(new ForegroundColorSpan(Color.RED),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        setText(spannable);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dtoList != null && targetWord != null) {
                    if (getContext() instanceof AppCompatActivity) {
                        AppCompatActivity activity = (AppCompatActivity) getContext();
                        PhoneticFeedbackFragment dialogFragment =
                                PhoneticFeedbackFragment.newInstance(dtoList, targetWord);
                        dialogFragment.show(activity.getSupportFragmentManager(), "phonetic_feedback");
                    }
                }
            }
        });
    }

    public void setPhonemeData(String targetWord, String errorType) {
        String text = targetWord;
        SpannableString spannable = new SpannableString(text);
        if ("duplicated".equalsIgnoreCase(errorType)) {
            spannable.setSpan(new StrikethroughSpan(),
                    0, targetWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ("filter".equalsIgnoreCase(errorType)) {
            spannable.setSpan(new BackgroundColorSpan(Color.GRAY),
                    0, targetWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ("high".equalsIgnoreCase(errorType)) {
            spannable.setSpan(new RelativeSizeSpan(2f),
                    0, targetWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ("low".equalsIgnoreCase(errorType)) {
            spannable.setSpan(new RelativeSizeSpan(1.3f),
                    0, targetWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(spannable);
    }
}