package gavinli.slidinglayoutlibrary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import gavinli.slidinglayout.SlidingLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FrameLayout mMainLayout;
    private SlidingLayout mSlidingLayout;
    private Button mSubView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout = (FrameLayout) findViewById(R.id.main_layout);

        Button showView = (Button) findViewById(R.id.button);
        showView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayout = (SlidingLayout) View.inflate(
                        MainActivity.this,
                        R.layout.sliding_layout,
                        null);
                mSlidingLayout.setOnViewStatusChangedListener(new MyViewStatusChangedAdapter());
                mMainLayout.addView(mSlidingLayout);
            }
        });
    }

    private class MyViewStatusChangedAdapter extends SlidingLayout.OnViewStatusChangedAdapter {
        @Override
        public void onViewMaximized() {
            ((ViewGroup) mSubView.getParent()).removeView(mSubView);
        }

        @Override
        public void onViewMinimized() {
            mSubView = new Button(MainActivity.this);
            mSubView.setText("SubView");
            mSubView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "------------->subView click");
                }
            });
            ((LinearLayout) mSlidingLayout.findViewById(R.id.viewHeader)).addView(mSubView);
        }
    }
}
