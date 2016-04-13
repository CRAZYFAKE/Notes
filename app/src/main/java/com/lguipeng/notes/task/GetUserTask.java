package com.lguipeng.notes.task;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.lguipeng.notes.utils.NotesLog;

/**
 * 获取用户信息任务
 */
public class GetUserTask extends BaseTask<User> {

    public GetUserTask() {
        super(User.class);
    }

    @Override
    protected User checkedExecute() throws Exception {
        NotesLog.e(EvernoteSession.getInstance().getEvernoteClientFactory().getUserStoreClient().getUser().toString());
        return EvernoteSession.getInstance().getEvernoteClientFactory().getUserStoreClient().getUser();
    }
}