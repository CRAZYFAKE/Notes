package com.lguipeng.notes.task;

import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.type.Note;

/**
 * 获取笔记本内容
 */
public class GetNoteContentTask extends BaseTask<Note> {

    private final NoteRef mNoteRef;

    public GetNoteContentTask(NoteRef noteRef) {
        super(Note.class);
        mNoteRef = noteRef;
    }

    @Override
    protected Note checkedExecute() throws Exception {
        return mNoteRef.loadNote(true, false, false, false);
    }
}
