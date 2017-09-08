package cn.leither.btsp.view

import android.view.View
import android.widget.ListView

/**
 * Created by lvqiang on 17-9-7.
 */
class MyListView(context: android.content.Context, attrs: android.util.AttributeSet) : ListView(context, attrs) {

    /**
     * 设置不滚动
     */
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2,
                View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)

    }

}