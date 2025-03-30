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

import androidx.appcompat.widget.AppCompatTextView;

import com.example.echoenglish_mobile.data.model.PhonemeComparisonDTO;

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

    public void setPhonemeData(List<PhonemeComparisonDTO> dtoList, String errorType) {
        if (dtoList == null || dtoList.isEmpty() || dtoList.get(0).getResult() == null) {
            setText("");
            return;
        }

        String text = dtoList.get(0).getResult();
        SpannableString spannable = new SpannableString(text);

        for (PhonemeComparisonDTO dto : dtoList) {

            if (dto == null) continue;
            if (dto.getActualPhoneme() != null && dto.getActualPhoneme().equals(dto.getCorrectPhoneme())) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                        dto.getStartIndex(), dto.getEndIndex(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(new ForegroundColorSpan(Color.RED),
                        dto.getStartIndex(), dto.getEndIndex(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if ("duplicated".equalsIgnoreCase(errorType)) {
                spannable.setSpan(new StrikethroughSpan(),
                        dto.getStartIndex(), dto.getEndIndex(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if ("filter".equalsIgnoreCase(errorType)) {
                spannable.setSpan(new BackgroundColorSpan(Color.GRAY),
                        dto.getStartIndex(), dto.getEndIndex(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if ("stressed".equalsIgnoreCase(errorType)) {
                spannable.setSpan(new RelativeSizeSpan(2f),
                        dto.getStartIndex(), dto.getEndIndex(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        setText(spannable);
    }
}