package com.xl.test;

/**
 * Created by hushendian on 2017/5/12.
 */

public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFail();

    void  onPause();

    void onCanceled();


}
