package org.hopestarter.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Adrian on 25/01/2016.
 */
public class GalleryFragment extends Fragment {
    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        TextView tv = new TextView(getActivity());
        tv.setText("GalleryFragment");
        return tv;
    }
}
