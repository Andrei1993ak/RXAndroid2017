package com.andrei.rxtestapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private Subscriber<View> mButtonClickSubscriber;
    private Handler mHandler;
    private TextView mHelloRxTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(getMainLooper());
        mHelloRxTextView = (TextView) findViewById(R.id.hello_rx_textView);
        final MyOnclickListener helloRxClickListener = new MyOnclickListener();
        mHelloRxTextView.setOnClickListener(helloRxClickListener);

        Observable<View> clickEventObservable = Observable.create(new Observable.OnSubscribe<View>() {
            @Override
            public void call(final Subscriber<? super View> subscriber) {
                helloRxClickListener.setSubscriber(subscriber);
            }
        });

        mButtonClickSubscriber = new Subscriber<View>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted() called");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError() called with: e = [" + e + "]");
            }

            @Override
            public void onNext(View pView) {
                Log.d(TAG, "onNext() called with: pView = [" + pView + "]");
            }
        };

        clickEventObservable.subscribe(mButtonClickSubscriber);

        Observable.interval(1000L, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(final Long pLong) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mHelloRxTextView.performClick();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        mButtonClickSubscriber.unsubscribe();

        super.onDestroy();
    }

    private static class MyOnclickListener implements View.OnClickListener {

        private Subscriber<? super View> mSubscriber;

        void setSubscriber(Subscriber<? super View> pSubscriber) {
            mSubscriber = pSubscriber;
        }

        @Override
        public void onClick(View v) {
            if (mSubscriber == null || mSubscriber.isUnsubscribed()) {

                return;
            }

            mSubscriber.onNext(v);
        }
    }
}
