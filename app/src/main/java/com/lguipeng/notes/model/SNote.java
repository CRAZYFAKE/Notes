package com.lguipeng.notes.model;

import android.text.Html;
import android.text.TextUtils;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.edam.type.Note;
import com.lguipeng.notes.utils.NotesLog;

import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

@Table(name = "notes")
public class SNote implements Serializable {
    private int id;
    private String guid;
    private int status;
    private int type;
    private String label;//标题
    private String content;//内容
    private long createTime;//创建时间
    private long lastOprTime;//最后一次更新时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getStatus() {
        return status;
    }

    public Status getStatusEnum() {
        return Status.mapValueToStatus(status);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(Status status) {
        setStatus(status.getValue());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastOprTime() {
        return lastOprTime;
    }

    public void setLastOprTime(long lastOprTime) {
        this.lastOprTime = lastOprTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(NoteType type) {
        setType(type.getValue());
    }

    public NoteType getNoteType() {
        return NoteType.mapValueToStatus(type);
    }

    public boolean hasReadyRemove() {
        return getStatusEnum() == Status.NEED_REMOVE;
    }

    private boolean hasReadyPush() {
        return getStatusEnum() == Status.NEED_PUSH;
    }

    /**
     * 新建笔记是否同步
     * @return
     */
    public boolean hasReadyNewPush() {
        if (!hasReadyPush())
            return false;
        if (TextUtils.isEmpty(getGuid())) {
            return true;
        }
        return false;
    }

    /**
     * 更新笔记是否同步
     * @return
     */
    public boolean hasReadyUpdatePush() {
        if (!hasReadyPush())
            return false;
        if (!TextUtils.isEmpty(getGuid())) {
            return true;
        }
        return false;
    }

    /**
     * 将本地笔记转化为能够同步的笔记格式
     * @return
     */
    public Note parseToNote() {
        Note note = new Note();
        note.setTitle(label);
        note.setContent(convertContentToEvernote());
        return note;
    }

    /**
     * 获取笔记的属性，创建时间，GUI等
     * @param note
     */
    public void parseFromNote(Note note) {
        setCreateTime(note.getCreated());
        setGuid(note.getGuid());
        setStatus(Status.IDLE.getValue());
        setLastOprTime(note.getUpdated());
        setLabel(note.getTitle());
        setContent(convertContentToSnote(note.getContent()));
    }

    /**
     * 将本地笔记转化为HTML格式笔记
     * @return
     */
    private String convertContentToEvernote() {
        String evernoteContent = EvernoteUtil.NOTE_PREFIX
                + getContent().
                replace("<", "&lt;").
                replace(">", "&gt;").
                replace("\n", "<br/>")
    + EvernoteUtil.NOTE_SUFFIX;
    NotesLog.d(evernoteContent);
    return evernoteContent;
}

    /**
     * 将HTML形式内容的转化为String
     * @param content HTML笔记内容
     * @return
     */
    private String convertContentToSnote(String content) {
        NotesLog.d(content);
        String snoteContent = Html.fromHtml(content).toString().trim();
        snoteContent = snoteContent.replace("\n\n", "\n");
        NotesLog.d(snoteContent);
        return snoteContent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[guid:" + guid + ",");
        sb.append("label:" + label + ",");
        sb.append("content:" + content + ",");
        sb.append("type:" + type + "]");
        return sb.toString();
    }

    /**
     * 本地笔记本的状态
     */
    public enum Status {
        NEED_PUSH(0x00),//需要同步的
        NEED_REMOVE(0x01),//需要同步删除的
        IDLE(0x02);//空闲的笔记
        private int mValue;

        Status(int value) {
            this.mValue = value;
        }

        public static Status mapValueToStatus(final int value) {
            for (Status status : Status.values()) {
                if (value == status.getValue()) {
                    return status;
                }
            }
            // If run here, return default
            return IDLE;
        }

        public static Status getDefault() {
            return IDLE;
        }

        public int getValue() {
            return mValue;
        }
    }

    /**
     * 笔记本类型，分为NORMAL->正常笔记，TRASH->在垃圾回收站的笔记
     */
    public enum NoteType {
        NORMAL(0x00),
        TRASH(0x01);
        private int mValue;

        NoteType(int value) {
            this.mValue = value;
        }

        public static NoteType mapValueToStatus(final int value) {
            for (NoteType status : NoteType.values()) {
                if (value == status.getValue()) {
                    return status;
                }
            }
            // If run here, return default
            return NORMAL;
        }

        /**
         * 默认为正常笔记
         * @return
         */
        public static NoteType getDefault() {
            return NORMAL;
        }

        /**
         * 获取笔记本类型
         * @return
         */
        public int getValue() {
            return mValue;
        }
    }
}
