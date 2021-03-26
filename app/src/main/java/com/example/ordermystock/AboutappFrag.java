package com.example.ordermystock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutappFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutappFrag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View inflatedView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AboutappFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutappFrag newInstance(String param1, String param2) {
        AboutappFrag fragment = new AboutappFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        inflatedView = inflater.inflate(R.layout.fragment_about_app, container, false);
        TextView tv = inflatedView.findViewById(R.id.tv_aboutapp_appname);
        tv.setText("App name");
        tv = inflatedView.findViewById(R.id.tv_aboutapp_appname_value);
        tv.setText(R.string.app_name);

        tv = inflatedView.findViewById(R.id.tv_aboutapp_appversion);
        tv.setText("App version");
        tv = inflatedView.findViewById(R.id.tv_aboutapp_appversion_value);
        tv.setText(R.string.app_version);

        tv = inflatedView.findViewById(R.id.tv_aboutapp_developedby);
        tv.setText("Developed By");
        tv = inflatedView.findViewById(R.id.tv_aboutapp_developedby_value);
        tv.setText(R.string.app_developedby);

        tv = inflatedView.findViewById(R.id.tv_aboutapp_license);
        tv.setText("App license");
        tv = inflatedView.findViewById(R.id.tv_aboutapp_license_value);
        tv.setText(R.string.app_license);

        MainActivity.mMenu.findItem(R.id.replacements).setVisible(false);

        return inflatedView;
    }
}