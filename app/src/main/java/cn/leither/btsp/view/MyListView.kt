package cn.leither.btsp.view

import android.view.View
import android.widget.ListView

class MyListView(context: android.content.Context, attrs: android.util.AttributeSet) : ListView(context, attrs) {

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2,
                View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)

    }
}