package cn.leither.btsp.trash;

import android.app.Activity;
import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.leither.btsp.adapter.CommonAdapter;

/**
 * Created by lvqiang on 17-8-31.
 */

public class tesfor<T> extends CommonAdapter {
    public tesfor(@NotNull Context context, @NotNull List list, int layoutId, int variableId) {
        super(context, list, layoutId, variableId);
        Activity context1 = (Activity) context;
    }
}
