package com.lguipeng.notes.task;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Notebook;

/**
 * 创建新笔记本
 */
public class CreateNewNotebookTask extends BaseTask<Notebook> {

    private final String mName;

    public CreateNewNotebookTask(String name) {
        super(Notebook.class);
        mName = name;
    }

    @Override
    protected Notebook checkedExecute() throws Exception {
        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        Notebook notebook = new Notebook();
        notebook.setName(mName);
        return noteStoreClient.createNotebook(notebook);
    }
}
