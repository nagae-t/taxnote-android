package com.example.taxnoteandroid.Library;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by b0ne on 2017/03/17.
 */

public class AsyncOkHttpClient {

    private static final OkHttpClient OKHTTP_CLIENT_SINGLETON = new OkHttpClient.Builder().build();

    private static final OkHttpClient.Builder OKHTTP_CLIENT_BUILDER = new OkHttpClient.Builder();

    private static AsyncOkHttpClient singleton;

    private AsyncOkHttpClient() {
    }

    public static AsyncOkHttpClient newInstance() {

        if (singleton == null) {
            singleton = new AsyncOkHttpClient();
        }
        return singleton;
    }


    /**
     * @param request
     * @param callback
     */
    public static void request(Request request, Callback callback) {
        execute(request, callback);
    }

    /**
     * @param url
     * @param callback
     */
    public static void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        execute(request, callback);
    }

    /**
     *
     * @param url
     * @param requestBody
     * @param callback
     */
    public static void get(String url, RequestBody requestBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .method("GET", requestBody)
                .build();
        execute(request, callback);
    }

    /**
     * @param url
     * @param requestBody
     * @param callback
     */
    public static void post(String url, RequestBody requestBody, Callback callback) {
        // System info
        String uaInfo = "okhttp3 (Android " + Build.VERSION.RELEASE + "; "
                + Build.MODEL + "; Build/" + Build.ID + ")";

        Request request = new Request.Builder()
                .addHeader("User-Agent", uaInfo)
                .url(url)
                .post(requestBody)
                .build();
        execute(request, callback);
    }

    /**
     *
     * @param method
     * @param url
     * @param requestBody
     * @param callback
     */
    public static void execute(String method, String url,
                               RequestBody requestBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .method(method, requestBody)
                .build();
        execute(request, callback);
    }

    public static void execute(Headers headers, String method, String url,
                              RequestBody requestBody, Callback callback) {
        Request.Builder builder = new Request.Builder();
        if (headers != null) builder.headers(headers);

        Request request = builder
                .url(url)
                .method(method, requestBody)
                .build();
        execute(request, callback);
    }

    public static void execute(String method, String url,
                               RequestBody requestBody, ResponseCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .method(method, requestBody)
                .build();
        execute(request, callback);
    }

    public static void execute(Headers headers, String method, String url,
                               RequestBody requestBody, ResponseCallback callback) {
        Request.Builder builder = new Request.Builder();
        if (headers != null) builder.headers(headers);

        Request request = builder
                .url(url)
                .method(method, requestBody)
                .build();
        execute(request, callback);
    }

    private static void execute(final Request request, final Callback callback) {
        OKHTTP_CLIENT_SINGLETON.newCall(request).enqueue(new okhttp3.Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(Call call, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(null, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String content = response.body().string();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(response, content);
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(response, null);
                        }
                    });
                }
            }

        });
    }

    private static void execute(final Request request, final ResponseCallback callback) {
        OkHttpClient client = OKHTTP_CLIENT_BUILDER
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response origResp = chain.proceed(chain.request());
                        return origResp.newBuilder()
                                .body(new ProgressResponseBody(origResp.body(), callback))
                                .build();
                    }
                }).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(final Call call, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(null, e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String content = response.body().string();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(response, content);
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(response, null);
                        }
                    });
                }
            }

        });
    }

    public interface Callback {
        void onFailure(Response response, Throwable throwable);
        void onSuccess(Response response, String content);
    }

    public interface ResponseCallback {
        void onFailure(Response response, Throwable throwable);
        void onUpdate(long bytesRead, long contentLength, boolean done);
        void onSuccess(Response response, String content);
    }

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ResponseCallback progressCallback;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ResponseCallback callback) {
            this.responseBody = responseBody;
            this.progressCallback = callback;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressCallback.onUpdate(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

}
