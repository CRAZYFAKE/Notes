package com.lguipeng.notes.model;

import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

/**
 * Created by Yao.YiXiang on 2016/5/17.
 */
@Table(name = "attachment")
public class Attachment implements Serializable {

    private int id;
    private int noteId;
    private String guid;
    private String fileName;
    private String mimeType;
    private String path;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[guid:" + guid + ",");
        sb.append("name:" + fileName + ",");
        sb.append("type:" + mimeType + ",");
        sb.append("path:" + path + "]");
        return sb.toString();
    }
}