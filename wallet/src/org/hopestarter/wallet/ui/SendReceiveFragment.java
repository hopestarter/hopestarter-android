package org.hopestarter.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hopestarter.wallet.R;

/**
 * Created by Adrian on 03/02/2016.
 */
public class SendReceiveFragment extends Fragment {

    public SendReceiveFragment() {}

    public static SendReceiveFragment newInstance() {
        return new SendReceiveFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sendreceive, parent, false);
    }
}
