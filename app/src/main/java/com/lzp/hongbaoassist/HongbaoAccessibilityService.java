package com.lzp.hongbaoassist;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SKJP on 2017/1/12.
 */

public class HongbaoAccessibilityService extends AccessibilityService {
    @Override
    protected void onServiceConnected() {
        Log.e("Test", "ServiceConnected");
    }

    private ArrayList<Hongbao> caches = new ArrayList<Hongbao>();
    private Object object = new Object();
    private AccessibilityNodeInfo root;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        root = event.getSource();

        if (root == null) {
            return;
        }
//        printNodeInfo(root);
        List<AccessibilityNodeInfo> hongbaos = root.findAccessibilityNodeInfosByText("领取红包");
        List<AccessibilityNodeInfo> kais = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bi3");
        List<AccessibilityNodeInfo> results = root.findAccessibilityNodeInfosByText("红包详情");
        List<AccessibilityNodeInfo> slowlys = root.findAccessibilityNodeInfosByText("手慢了，红包派完了");
        Log.e("Test", "hongbao size=" + hongbaos.size());
        Log.e("Test", "kais size=" + kais.size());
        Log.e("Test", "results size=" + results.size());
        Log.e("Test", "slowlys size=" + slowlys.size());
        boolean rst = !getHongbao(hongbaos) && !caiHongbao(kais) && !slowly(slowlys) && !result(results);
    }

    /**
     * 收到红包
     */
    private boolean getHongbao(List<AccessibilityNodeInfo> hongbaos) {
        boolean rest = false;
        if (hongbaos != null && !hongbaos.isEmpty()) {//收到红包
            rest = true;
            Log.e("Test", "收到红包 " + printCache());
            int size = hongbaos.size();
            for (int i = 0; i < size; i++) {
                AccessibilityNodeInfo hongbao = hongbaos.get(i);
                Hongbao hb = findAvaliableHongbao(hongbao);
                if (hb != null && !checkHongbao(hb) && hongbao.isVisibleToUser()) {
                    Rect rect = new Rect();
                    try {
                        hongbao.getParent().getParent().getBoundsInScreen(rect);
                        Log.e("Test", "点击收到红包 " + rect.toShortString());
                        hongbao.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        caches.add(hb);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return rest;
    }

    private boolean checkHongbao(Hongbao item) {
        for (Hongbao hb : caches) {
            if (hb.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private String printCache() {
        StringBuffer sb = new StringBuffer();
        for (Hongbao hb : caches) {
            sb.append("[" + hb.toString() + "]");
        }
        return sb.toString();
    }

    /**
     * 拆红包
     */
    private boolean caiHongbao(List<AccessibilityNodeInfo> kais) {
        if (kais != null && !kais.isEmpty()) {//拆红包
            Log.e("Test", "拆红包");
            AccessibilityNodeInfo kai = kais.get(0);
            kai.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        return false;
    }

    /**
     * 手慢了
     */
    private boolean slowly(List<AccessibilityNodeInfo> slowlys) {
        if (slowlys != null && !slowlys.isEmpty()) {
            Log.e("Test", "手慢了");
            List<AccessibilityNodeInfo> backs = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bfu");
            if (backs != null && !backs.isEmpty()) {
                backs.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            return true;
        }
        return false;
    }

    /**
     * 红包详情
     */
    private boolean result(List<AccessibilityNodeInfo> results) {
        if (results != null && !results.isEmpty()) {
            Log.e("Test", "红包详情");
            List<AccessibilityNodeInfo> backs = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gv");
            if (backs != null && !backs.isEmpty()) {
                backs.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            return true;
        }
        return false;
    }

    private Hongbao findAvaliableHongbao(AccessibilityNodeInfo hongbao) {
        Hongbao rest = new Hongbao();
        if (root != null) {
            List<AccessibilityNodeInfo> listViews = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a22");
            if (listViews != null && !listViews.isEmpty()) {
                AccessibilityNodeInfo listview = listViews.get(0);

                AccessibilityNodeInfo hongbaoItem = null;
                try {
                    hongbaoItem = hongbao.getParent().getParent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //找到红包在listview中的位置
                int pos = -1;
                if (listview != null && hongbaoItem != null) {
                    int count = listview.getChildCount();
                    for (int i = 0; i < count; i++) {
                        AccessibilityNodeInfo child = listview.getChild(i);
                        if (child != null) {
                            if (child.toString().equals(hongbaoItem.toString())) {
                                pos = i;
                                break;
                            }
                        }
                    }
                }

                String pre = "", next = "";
                if (pos != -1) {//在listview中找到了红包
                    if (pos == 0) {
                        pre = "";
                    } else {
                        AccessibilityNodeInfo preNode = listview.getChild(pos - 1);
                        pre = getNodeContent(preNode);
                    }
                    if (pos + 1 >= listview.getChildCount()) {
                        next = "";
                    } else {
                        AccessibilityNodeInfo nextNode = listview.getChild(pos + 1);
                        next = getNodeContent(nextNode);
                    }
                }
                rest.pre = pre;
                rest.next = next;
                rest.cur = hongbaoItem;
            }
        }
        return rest;
    }


    private String getNodeContent(AccessibilityNodeInfo nodeInfo) {
        String str1 = "";
        String str2 = "";
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> ids = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/id");//头像

            if (ids != null && !ids.isEmpty()) {
                CharSequence sequence = ids.get(0).getContentDescription();
                if (sequence != null) {
                    str1 = sequence.toString();
                }
            }

            List<AccessibilityNodeInfo> msgs = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a5t");//红包的标语
            if (msgs != null && !msgs.isEmpty()) {
                CharSequence sequence = msgs.get(0).getText();
                if (sequence != null) {
                    str2 = sequence.toString();
                }
            } else {
                List<AccessibilityNodeInfo> contents = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/if");//纯文本消息内容
                if (contents != null && !contents.isEmpty()) {
                    CharSequence sequence = contents.get(0).getText();
                    if (sequence != null) {
                        str2 = sequence.toString();
                    }
                }
            }
        }
        return str1 + str2;
    }

    private void printNodeInfo(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        Log.e("Test", rect.toShortString());
        CharSequence sequence = nodeInfo.getText();
        if (sequence != null) {
            Log.e("Test", sequence.toString());
        }
        int childCount = nodeInfo.getChildCount();
        for (int i = 0; i < childCount; i++) {
            printNodeInfo(nodeInfo.getChild(i));
        }
    }

    @Override
    public void onInterrupt() {
        Log.e("Test", "Interrupt");
    }

    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
