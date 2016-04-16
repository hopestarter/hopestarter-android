package org.hopestarter.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.hopestarter.wallet.ui.send.SendCoinsActivity;
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
        View rootView = inflater.inflate(R.layout.fragment_sendreceive, parent, false);

        ImageButton sendButton = (ImageButton) rootView.findViewById(R.id.send_button);
        ImageButton receiveButton = (ImageButton) rootView.findViewById(R.id.request_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SendCoinsActivity.class));
            }
        });

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RequestCoinsActivity.class));
            }
        });
        return rootView;
    }
}
