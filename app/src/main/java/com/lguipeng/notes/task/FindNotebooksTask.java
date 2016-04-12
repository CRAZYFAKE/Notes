package com.lguipeng.notes.task;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Notebook;
import com.lguipeng.notes.utils.NotesLog;

import java.util.List;

/**
 * @author rwondratschek
 */
public class FindNotebooksTask extends BaseTask<List<Notebook>> {

    @SuppressWarnings("unchecked")
    public FindNotebooksTask() {
        super((Class) List.class);
    }

    @Override
    protected List<Notebook> checkedExecute() throws Exception {
        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        NotesLog.i(String.valueOf(noteStoreClient.listNotebooks().size()));
        System.out.print(noteStoreClient.listNotebooks().size());
        return noteStoreClient.listNotebooks();
    }
}
