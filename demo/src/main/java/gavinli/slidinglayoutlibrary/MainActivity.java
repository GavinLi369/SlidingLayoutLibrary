package gavinli.slidinglayoutlibrary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import gavinli.slidinglayout.OnViewRemoveListener;
import gavinli.slidinglayout.SlidingLayout;

public class MainActivity extends AppCompatActivity implements OnViewRemoveListener {
    private static final String TAG = "MainActivity";
    private FrameLayout mMainLayout;
    private SlidingLayout mSlidingLayout;

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
                mSlidingLayout.setOnViewRemoveListener(MainActivity.this);
                mMainLayout.addView(mSlidingLayout);
            }
        });

//        Button subView = (Button) findViewById(R.id.subview);
//        subView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "------------->subView click");
//            }
//        });
    }

    @Override
    public void removeView() {
        mMainLayout.removeView(mSlidingLayout);
    }
}
