package org.hopestarter.wallet.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.util.concurrent.AtomicDouble;

import org.hopestarter.wallet_test.R;

import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WalletFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WalletFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private TextView mDonatedWorldwide;
    private TextView mRefugeeCount;

    public WalletFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment WalletFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WalletFragment newInstance() {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        mDonatedWorldwide = (TextView)rootView.findViewById(R.id.world_donations);
        startDonationsUpdater();
        mRefugeeCount = (TextView)rootView.findViewById(R.id.refugees);
        mRefugeeCount.setText(getString(R.string.donated_so_far_worldwide, 833));
        return rootView;
    }

    private void startDonationsUpdater() {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.set(2016, 0, 1, 0, 0, 0);
        final long sD = calendar.getTimeInMillis();
        final double r = 0.025; // $/min

        final NumberFormat currencyFormater = NumberFormat.getCurrencyInstance(Locale.US);

        Runnable update = new Runnable() {
            @Override
            public void run() {
                double lastValue = getLastDonationValue(sD, System.currentTimeMillis(), r);
                mDonatedWorldwide.setText(currencyFormater.format(lastValue));
                mDonatedWorldwide.postDelayed(this, 1000);
            }
        };

        mDonatedWorldwide.post(update);

    }

    private double getLastDonationValue(long start, long now, double r) {
        long d = now - start;
        double min = Long.valueOf(d).doubleValue()/(1000 * 60);
        return min * r;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
