package com.lzp.hongbaoassist;

/**
 * Created by lzp on 2017/1/21.
 */

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 紅包
 */
public class Hongbao {
    public String pre;
    public AccessibilityNodeInfo cur;
    public String next;

    public Hongbao() {
        pre = "";
        next = "";
        cur = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hongbao) {
            Hongbao hongbao = (Hongbao) obj;
            boolean blCur=false;
            boolean blPre=false;
            boolean blNext=false;
            if (cur != null && hongbao.cur != null && cur.toString().equals(hongbao.cur.toString())) {//当前的node完全一样
                blCur = true;
            }
            if (pre.equals(hongbao.pre)) {
                blPre=true;
            }
            if (next.equals(hongbao.next)) {
                blNext=true;
            }
            return blCur || (blPre && blNext);
        }
        return false;
    }

    @Override
    public String toString() {
        String curStr = "";
        if (cur != null) {
            Rect rect = new Rect();
            cur.getBoundsInScreen(rect);
            curStr = rect.toShortString();
        }
        return "pre=" + pre + ",cur=" + curStr + ",next=" + next;
    }
}
