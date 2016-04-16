package org.hopestarter.wallet.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.utils.Fiat;
import org.bitcoinj.utils.MonetaryFormat;
import org.hopestarter.wallet.Configuration;
import org.hopestarter.wallet.ExchangeRatesProvider;
import org.hopestarter.wallet.ExchangeRatesProvider.ExchangeRate;
import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet_test.R;

import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private static final int WALLET_BALANCE_LOADER = 0;
    private static final int EXCHANGE_RATE_LOADER = 1;

    private static final String TAG = WalletFragment.class.getName();

    private OnFragmentInteractionListener mListener;
    private TextView mDonatedWorldwideView;
    private TextView mBalanceView;
    private TextView mRefugeeCountView;
    private WalletApplication mApplication;
    private Wallet mWallet;
    private Coin mBalance;
    private LoaderManager mLoaderManager;
    private Configuration mConfig;
    private ExchangeRate mExchangeRate;
    private TextView mLocalCurrencyBalance;

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance() {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (WalletApplication)getActivity().getApplication();
        mWallet = mApplication.getWallet();
        mConfig = mApplication.getConfiguration();
        mLoaderManager = getLoaderManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoaderManager.initLoader(WALLET_BALANCE_LOADER, null, balanceLoaderCallbacks);
        mLoaderManager.initLoader(EXCHANGE_RATE_LOADER, null, rateLoaderCallbacks);
    }

    @Override
    public void onPause() {
        mLoaderManager.destroyLoader(WALLET_BALANCE_LOADER);
        mLoaderManager.destroyLoader(EXCHANGE_RATE_LOADER);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        mDonatedWorldwideView = (TextView)rootView.findViewById(R.id.world_donations);
        startDonationsUpdater();
        mRefugeeCountView = (TextView)rootView.findViewById(R.id.refugees);
        mRefugeeCountView.setText(getString(R.string.donated_so_far_worldwide, 833));
        mBalanceView = (TextView)rootView.findViewById(R.id.mbtc_donations);
        mLocalCurrencyBalance = (TextView)rootView.findViewById(R.id.currency_donations);
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
                mDonatedWorldwideView.setText(currencyFormater.format(lastValue));
                mDonatedWorldwideView.postDelayed(this, 1000);
            }
        };

        mDonatedWorldwideView.post(update);

    }

    private double getLastDonationValue(long start, long now, double r) {
        long d = now - start;
        double min = Long.valueOf(d).doubleValue()/(1000 * 60);
        return min * r;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateViews() {
        if (mBalance == null) {
            return;
        }

        MonetaryFormat format = MonetaryFormat.MBTC;
        mBalanceView.setText(format.postfixCode().format(mBalance));

        if (mExchangeRate != null) {
            Fiat localValue = mExchangeRate.rate.coinToFiat(mBalance);
            MonetaryFormat localFormat = MonetaryFormat.FIAT.noCode();
            mLocalCurrencyBalance.setText(localFormat.format(localValue) + " " + localValue.currencyCode);
        }
    }


    private final LoaderCallbacks<Coin> balanceLoaderCallbacks = new LoaderCallbacks<Coin>() {
        @Override
        public Loader<Coin> onCreateLoader(int id, Bundle args) {
            return new WalletBalanceLoader(WalletFragment.this.getActivity(), mWallet);
        }

        @Override
        public void onLoadFinished(Loader<Coin> loader, Coin balance) {
            mBalance = balance;

            updateViews();
        }

        @Override
        public void onLoaderReset(Loader<Coin> loader) {

        }
    };


    private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>()
    {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
        {
            return new ExchangeRateLoader(getActivity(), mConfig);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
        {
            if (data != null && data.getCount() > 0)
            {
                data.moveToFirst();
                mExchangeRate = ExchangeRatesProvider.getExchangeRate(data);
                updateViews();
            }
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader)
        {
        }
    };

}
