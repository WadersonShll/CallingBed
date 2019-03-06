package com.wdkl.callingbed.util;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 类名称：DownloadUtil <br>
 * 类描述：下载文件工具类 <br>
 * 创建人：Waderson Shll<br>
 * 创建时间 ：2017-12-13  <br>
 */

public class DownloadUtil {
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;
    /**
     * 下载的APK文件绝对路径
     */
    public static final String FILE_APK_PATH = Environment.getExternalStorageDirectory() + "/CallingBed";
    /**
     * 下载的APK文件的文件名
     */
    public static final String FILE_APK_NAME = "CallingBedAPK.apk";
    /**
     * 下载的APP版本号
     */
    public static float APP_VERSION = 1.0f;

    public static DownloadUtil getInstance() {
        if (downloadUtil == null) {
            synchronized (DownloadUtil.class) {
                if (downloadUtil == null) {
                    downloadUtil = new DownloadUtil();
                }
            }
        }
        return downloadUtil;
    }

    private DownloadUtil() {//writeTimeoutMillis    connectTimeoutMillis   readTimeoutMillis
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url      下载连接
     * @param listener 下载监听
     */
    public void download(final String url, final OnDownloadListener listener) {
        LogUtil.d("download", "url==" + url);
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.d("download", "onFailure==" + e.toString());
                listener.onDownloadFailed(); // 下载失败
            }

            @Override
            public void onResponse(Call call, Response response) {
                LogUtil.d("download", "response==" + response.body().contentLength());
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(isHaveExistDir(new File(FILE_APK_PATH), new File(FILE_APK_PATH + "/" + FILE_APK_NAME)), FILE_APK_NAME);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum = sum + (long) len;
                        //int progress = (int) (sum * 1.0f / total * 100);
                        float sp = (float) sum / (float) total;
                        int progress = (int) (sp * 100);
                        LogUtil.d("download", "progress==" + progress);
                        listener.onDownloading(progress);// 下载中
                    }
                    fos.flush();
                    listener.onDownloadSuccess(); // 下载完成
                } catch (Exception e) {
                    LogUtil.d("download", "Exception==");
                    listener.onDownloadFailed();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        LogUtil.d("download", "IOException==");
                    }
                }
            }
        });
    }

    /**
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isHaveExistDir(File downloadFile, File sonFile) throws IOException {
        LogUtil.d(DownloadUtil.class, "downloadFile.mkdirs()==" + downloadFile.mkdirs());
        LogUtil.d(DownloadUtil.class, "sonFile.mkdir()==" + sonFile.mkdir());
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        deleteAPKFile(sonFile);//只要文件名相同就可以自动替换(按道理此处不需要了，但为了保险起见还是先执行删除操作)。
        return downloadFile.getAbsolutePath();
    }

    public long getAPKFileSize() {
        File downloadFile = new File(FILE_APK_PATH + "/" + FILE_APK_NAME);
        if (downloadFile.isDirectory()) {
            return downloadFile.length();
        }
        return 0;
    }

    /**
     * 删除文件APK <br></>
     */
    public boolean deleteAPKFile(File downloadFile) {
        //if (!downloadFile.isDirectory()) throw new NotHaveThisFileException();
        return downloadFile.delete();
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }

    public class NotHaveThisFileException extends RuntimeException {
        public NotHaveThisFileException() {
            super("该文件不存在！");
        }
    }
}