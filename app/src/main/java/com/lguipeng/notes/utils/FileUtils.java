package com.lguipeng.notes.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.lguipeng.notes.R;
import com.lguipeng.notes.model.SNote;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

public class FileUtils {

    public final static String SD_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String APP_DIR = SD_ROOT_DIR + File.separator + "SNotes";
    public final static String BACKUP_FILE_NAME = "notes.txt";

    @Inject
    @Singleton
    public FileUtils() {
    }

    private void makeSureAppDirCreated() {
        if (checkSdcardStatus()) {
            mkdir(APP_DIR);
        } else {
            NotesLog.e("sd card not ready");
        }
    }

    public void mkdir(String dir) {
        if (TextUtils.isEmpty(dir))
            return;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            boolean res = dirFile.mkdir();
            if (!res) {
                NotesLog.e("make dir " + dir + " error!");
            }
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath
     * @return
     */
    public boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * 创建文件
     *
     * @param filename 文件名
     * @return true if create success
     */
    public boolean createFile(String filename) {
        makeSureAppDirCreated();
        return createFile(APP_DIR, filename);
    }

    /**
     * 创建文件
     *
     * @param dir      文件dir
     * @param filename 文件名称
     * @return
     */
    public boolean createFile(String dir, String filename) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory())
            return false;
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File newFile = new File(dir + File.separator + filename);
        try {
            if (!newFile.exists())
                newFile.createNewFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param filename
     * @return true if delete success
     */
    public boolean deleteFile(String filename) {
        File deleteFile = new File(filename);
        if (deleteFile.exists()) {
            deleteFile.delete();
            return true;
        } else {
            return false;
        }
    }

    public boolean writeSNotesFile(String content) {
        return writeFile(BACKUP_FILE_NAME, content, false);
    }

    public boolean writeFile(String fileName, String content, boolean append) {
        makeSureAppDirCreated();
        return writeFile(APP_DIR, fileName, content, append);
    }

    /**
     * 写入文件
     *
     * @param dir      文件地址
     * @param fileName 文件名
     * @param content  内容
     * @param append
     * @return
     */
    public boolean writeFile(String dir, String fileName, String content, boolean append) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            String filePath = dir + File.separator + fileName;
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content + "\n");
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 获取文件大小
     *
     * @param path
     * @return
     */
    public long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 检测SD的状态
     *
     * @return
     */
    public boolean checkSdcardStatus() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 备份到本地
     *
     * @param context
     * @param notes
     * @return
     */
    public boolean backupSNotes(Context context, List<SNote> notes) {
        createFile(BACKUP_FILE_NAME);
        StringBuilder sb = new StringBuilder();
        String title = context.getString(R.string.title);
        String content = context.getString(R.string.note_content);
        for (SNote note : notes) {
            sb.append(title + ":" + note.getLabel() + "\n");
            sb.append(content + ":\n" + note.getContent() + "\n\n");
        }
        return writeSNotesFile(sb.toString());
    }

    /**
     * 根据文件名截取文件后缀名
     *
     * @param fileName 文件名
     * @return
     */
    public static String getFileSuffixName(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0) {
            return null;
        }
        /* 获取文件的后缀名 */
        String end = fileName.substring(dotIndex + 1, fileName.length()).toLowerCase();
        if (("").equals(end)) {
            return null;
        }
        return end;
    }

    /**
     * 根据文件path截取文件名
     *
     * @param pathandname
     * @return
     */
    public static String getFileName(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1, pathandname.length());
        } else {
            return null;
        }
    }

    public static String getExtension(final File file) {
        String suffix = "";
        String name = file.getName();
        final int idx = name.lastIndexOf(".");
        if (idx > 0) {
            suffix = name.substring(idx + 1);
        }
        return suffix;
    }

    /**
     * 获取文件的MimeType
     *
     * @param file
     * @return
     */
    public static String getMimeType(final File file) {
        String extension = getExtension(file);
        if (MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    extension);
        } else {
            return "text/plain";
        }
    }
}