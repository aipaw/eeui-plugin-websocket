package app.eeui.websocket;


import com.alibaba.fastjson.JSONObject;
import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.WsStatus;
import com.rabtman.wsmanager.listener.WsStatusListener;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.eeui.framework.extend.base.WXModuleBase;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

public class eeuiWebsocketModule extends WXModuleBase {

    private String TAG = "eeuiWebsocketModule";

    private WsManager socket;

    private boolean isConnect = false;

    @JSMethod
    public void connect(String url, final JSCallback callback) {
        if (socket != null) {
            socket.stopConnect();
            socket = null;
        }
        socket = new WsManager.Builder(getContext())
                .client(new OkHttpClient().newBuilder().pingInterval(15, TimeUnit.SECONDS).retryOnConnectionFailure(true).build())
                .wsUrl(url)
                .needReconnect(false)
                .build();
        socket.setWsStatusListener(new WsStatusListener(){
            @Override
            public void onOpen(Response response) {
                super.onOpen(response);
                isConnect = true;
                Map<String, Object> data = new HashMap<>();
                data.put("status", "open");
                data.put("msg", "");
                if (callback != null) {
                    callback.invokeAndKeepAlive(data);
                }
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
                Map<String, Object> data = new HashMap<>();
                data.put("status", "message");
                data.put("msg", text);
                if (callback != null) {
                    callback.invokeAndKeepAlive(data);
                }
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
                Map<String, Object> data = new HashMap<>();
                data.put("status", "message");
                data.put("msg", bytes);
                if (callback != null) {
                    callback.invokeAndKeepAlive(data);
                }
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
                isConnect = false;
                JSONObject resMsg = new JSONObject();
                resMsg.put("code", code);
                resMsg.put("reason", reason);
                //
                Map<String, Object> data = new HashMap<>();
                data.put("status", "closed");
                data.put("msg", resMsg);
                if (callback != null) {
                    callback.invoke(data);
                }
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                super.onFailure(t, response);
                isConnect = false;
                Map<String, Object> data = new HashMap<>();
                data.put("status", "failure");
                data.put("msg", t.getMessage());
                if (callback != null) {
                    callback.invoke(data);
                }
            }
        });
        try {
            socket.startConnect();
        } catch (Exception e) {
            isConnect = false;
            Map<String, Object> data = new HashMap<>();
            data.put("status", "error");
            data.put("msg", e.getMessage());
            if (callback != null) {
                callback.invoke(data);
            }
        }
    }

    @JSMethod
    public void send(String msg) {
        if (socket == null) {
            return;
        }
        try {
            socket.sendMessage(msg);
        } catch (Exception ignored) {

        }
    }

    @JSMethod
    public void stop() {
        if (socket == null) {
            return;
        }
        try {
            socket.stopConnect();
            socket = null;
        } catch (Exception ignored) {

        }
    }

    @JSMethod(uiThread = false)
    public int state() {
        if (socket == null) {
            return 0;
        }
        if (socket.getCurrentStatus() == WsStatus.CONNECTED && isConnect) {
            return 1;
        }
        return 0;
    }
}
