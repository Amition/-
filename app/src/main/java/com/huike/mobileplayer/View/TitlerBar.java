package com.huike.mobileplayer.View;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huike.mobileplayer.Activity.SearchActivity;
import com.huike.mobileplayer.R;

public class TitlerBar extends LinearLayout {

    private  Context context;
    private View tv_search ,iv_game, iv_history;
    private TitlerBar.myOnClickListener myOnClickListener;

    public TitlerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_search = getChildAt(1);
        iv_game = getChildAt(2);
        iv_history = getChildAt(3);
        myOnClickListener = new myOnClickListener();
        tv_search.setOnClickListener(myOnClickListener);
        iv_game.setOnClickListener(myOnClickListener);
        iv_history.setOnClickListener(myOnClickListener);
    }

    class  myOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_title_search:
                    Toast.makeText(context, "s", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context,SearchActivity.class));
                    break;

                case R.id.rl_title_game:
                    Toast.makeText(context, "g", Toast.LENGTH_SHORT).show();

                    break;

                case R.id.iv_title_history:
                    Toast.makeText(context, "h", Toast.LENGTH_SHORT).show();

                    break;

            }
        }
    }
}
