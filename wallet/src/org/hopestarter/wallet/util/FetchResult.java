package org.hopestarter.wallet.util;

/**
 * Used for async tasks results
 */
public class FetchResult<T> {
    private T mResult;
    private Throwable mThrowable;

    public FetchResult(T result) {
        mResult = result;
    }

    public FetchResult(Throwable throwable) {
        mThrowable = throwable;
    }

    public boolean isSuccessful() { return mThrowable == null; }
    public T getResult() { return mResult; }
    public Throwable getException() { return mThrowable; }
}