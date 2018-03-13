package com.xl.test;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hushendian on 2017/5/12.
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_FAIL = 2;
    public static final int TYPE_PAUSE = 3;
    public static final int TYPE_CANCELED = 4;

    private DownloadListener listener;
    private boolean isCanceled = false;
    private boolean isPause = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        return downLoad(strings[0]);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }


    @Override
    protected void onPostExecute(Integer status) {

        switch (status) {
            case TYPE_SUCCESS:
                listener.onSuccess();
                ;
                break;
            case TYPE_FAIL:
                listener.onFail();
                break;

            case TYPE_PAUSE:
                listener.onPause();
                break;

            case TYPE_CANCELED:
                listener.onCanceled();
                break;
        }
    }

    public void pauseDownload() {
        isPause = true;
    }

    public void cancelDownload() {
        isCanceled = true;
    }

    private int downLoad(String url) {
        long downloadLength = 0;

        RandomAccessFile saveFiel = null;
        File file = null;
        InputStream is = null;
        String fileName = url.substring(url.lastIndexOf("/"));
        String director = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS).getPath();

        file = new File(director + fileName);
        if (file.exists()) {
            downloadLength = file.length();
        }
        long contentLength = getContentLength(url);

        if (contentLength == 0) {
            return TYPE_FAIL;
        } else if (contentLength == downloadLength) {
            return TYPE_SUCCESS;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("RANGE", "bytes=" + downloadLength +
                "-").url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            if (response != null) {
                is = response.body().byteStream();
                saveFiel = new RandomAccessFile(file, "rw");
                saveFiel.seek(downloadLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPause) {
                        return TYPE_PAUSE;
                    } else {
                        total += len;
                        saveFiel.write(b, 0, len);
                        int progress = (int) ((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (saveFiel != null) {
                    saveFiel.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        return TYPE_FAIL;
    }


    public long getContentLength(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long currentLength = response.body().contentLength();
                response.close();
                return currentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
