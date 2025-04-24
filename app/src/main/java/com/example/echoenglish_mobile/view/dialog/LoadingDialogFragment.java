package com.example.echoenglish_mobile.view.dialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.echoenglish_mobile.R;

public class LoadingDialogFragment extends DialogFragment {

    public static final String TAG = "LoadingDialog";
    private static final String ARG_MESSAGE = "message";

    private String message;

    public static LoadingDialogFragment newInstance(String message) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public static LoadingDialogFragment newInstance() {
        return new LoadingDialogFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LottieAnimationView animationView = view.findViewById(R.id.lottie_animation_view);
        TextView tvMessage = view.findViewById(R.id.tv_loading_message);
        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setText("Loading...");
        }
    }


    public static void showLoading(FragmentManager fragmentManager, String tag, @Nullable String message) {
        if (fragmentManager.findFragmentByTag(tag) == null) {
            LoadingDialogFragment dialog = (message == null) ? newInstance() : newInstance(message);
            // Sử dụng commitAllowingStateLoss để tránh lỗi khi trạng thái đã được lưu (ví dụ: sau onSaveInstanceState)
            // Nhưng hãy cẩn thận vì có thể mất trạng thái dialog nếu Activity bị hủy và tạo lại hoàn toàn.
            // dialog.show(fragmentManager, tag); // Cách an toàn hơn nhưng có thể crash nếu gọi sai thời điểm
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(dialog, tag);
            ft.commitAllowingStateLoss(); // Cân nhắc rủi ro
        }
    }

    public static void showLoading(FragmentManager fragmentManager, String tag) {
        showLoading(fragmentManager, tag, null); // Gọi với message mặc định
    }

    public static void hideLoading(FragmentManager fragmentManager, String tag) {
        if (fragmentManager == null) return;
        try {
            DialogFragment dialog = (DialogFragment) fragmentManager.findFragmentByTag(tag);
            if (dialog != null && dialog.isAdded()) {
                dialog.dismissAllowingStateLoss();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error dismissing loading dialog: " + e.getMessage());
        } catch (Exception e){
            Log.e(TAG, "Generic error dismissing loading dialog: " + e.getMessage());
        }
    }
}
