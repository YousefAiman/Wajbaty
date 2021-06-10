package com.developers.wajbaty.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.developers.wajbaty.R;

public class ProgressDialogFragment extends DialogFragment {

    private String title;
    private String message;

    public ProgressDialogFragment(){
    }

    public ProgressDialogFragment(String title){
        this.title = title;
    }


    public ProgressDialogFragment(String title,String message){
            this.message = message;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        if(title == null && message == null){
            view = inflater.inflate(R.layout.main_progress_dialog, container, false);
        }else if(title!=null && message == null){

            view = inflater.inflate(R.layout.main_progress_dialog_with_title, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(title);

        }else{

            view = inflater.inflate(R.layout.main_progress_dialog_with_title_and_message, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(title);

             final TextView progressMessageTv = view.findViewById(R.id.progressMessageTv);
            progressMessageTv.setText(message);

        }

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
