package com.lguipeng.notes.task;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.type.NoteRef;

/**
 * 删除笔记
 */
public class DeleteNoteTask extends BaseTask<DeleteNoteTask.Result> {

    private final NoteRef mNoteRef;

    public DeleteNoteTask(NoteRef noteRef) {
        super(Result.class);
        mNoteRef = noteRef;
    }

    @Override
    protected Result checkedExecute() throws Exception {
        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        noteStoreClient.deleteNote(mNoteRef.getGuid());

        return Result.SUCCESS;
    }

    public enum Result {
        SUCCESS
    }
}
