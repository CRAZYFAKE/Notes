package com.lguipeng.notes.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.conn.mobile.FileData;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.User;
import com.lguipeng.notes.BuildConfig;
import com.lguipeng.notes.injector.ContextLifeCycle;
import com.lguipeng.notes.model.Attachment;
import com.lguipeng.notes.model.SNote;

import net.tsz.afinal.FinalDb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EverNoteUtils {

    private EvernoteSession mEvernoteSession;

    private PreferenceUtils mPreferenceUtils;

    private ThreadExecutorPool mThreadExecutorPool;

    private FinalDb mFinalDb;

    private Context mContext;

    private FileUtils mFileUtils;

    public static final String NOTE_BOOK_NAME = "简笔";

    @Inject
    public EverNoteUtils(@ContextLifeCycle("App") Context mContext, ThreadExecutorPool pool,
                         FinalDb mFinalDb, PreferenceUtils mPreferenceUtils,
                         FileUtils mFileUtils) {
        mEvernoteSession = EvernoteSession.getInstance();
        this.mPreferenceUtils = mPreferenceUtils;
        this.mThreadExecutorPool = pool;
        this.mFinalDb = mFinalDb;
        this.mContext = mContext;
        this.mFileUtils = mFileUtils;
    }

    /**
     * 获取是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return mEvernoteSession != null && mEvernoteSession.isLoggedIn();
    }

    /**
     * 跳转登录页面
     *
     * @param activity
     */
    public void auth(Activity activity) {
        if (activity == null)
            return;
        mEvernoteSession.authenticate(activity);
    }

    /**
     * 登出
     */
    public void logout() {
        mEvernoteSession.logOut();
        mPreferenceUtils.removeKey(PreferenceUtils.EVERNOTE_ACCOUNT_KEY);
    }

    /**
     * 获取用户
     *
     * @return 用戶
     * @throws Exception
     */
    public User getUser() throws Exception {
        return mEvernoteSession.getEvernoteClientFactory()
                .getUserStoreClient().getUser();

    }

    public void getUser(EvernoteCallback<User> callback) throws Exception {
        mEvernoteSession.getEvernoteClientFactory()
                .getUserStoreClient().getUserAsync(callback);
    }

    public String getUserAccount(User user) {
        if (user != null) {
            String accountInfo = user.getEmail();
            if (!TextUtils.isEmpty(accountInfo)) {
                return accountInfo;
            } else {
                accountInfo = user.getUsername();
            }
            mPreferenceUtils.saveParam(PreferenceUtils.EVERNOTE_ACCOUNT_KEY, accountInfo);
            return accountInfo;
        }
        return "";
    }

    /**
     * 获取笔记本是否存在
     *
     * @param notebookName
     * @throws Exception
     */
    public void makeSureNoteBookExist(String notebookName) throws Exception {
        NotesLog.d("");
        String guid = mPreferenceUtils
                .getStringParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY);
        if (!TextUtils.isEmpty(guid)) {
            Notebook notebook = findNotebook(guid);
            if (notebook != null && TextUtils.equals(notebook.getName(), notebookName)) {
                mPreferenceUtils.saveParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY,
                        notebook.getGuid());
            } else {
                tryCreateNoteBook(notebookName);
            }
        } else {
            tryCreateNoteBook(notebookName);
        }
        NotesLog.d("");
    }

    public boolean hasNoteBookExist(String guid, String name) throws Exception {
        boolean result = false;
        try {
            Notebook notebook = findNotebook(guid);
            if (notebook == null)
                return false;
            if (notebook.getName().equals(name)) {
                result = true;
                mPreferenceUtils.saveParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY
                        , notebook.getGuid());
            }
        } catch (EDAMNotFoundException e) {
            handleException(e);
            result = false;
        }
        return result;
    }

    /**
     * 查找笔记本，根据笔记本guid
     *
     * @param guid
     * @return
     * @throws Exception
     */
    public Notebook findNotebook(String guid) throws Exception {
        Notebook notebook;
        try {
            notebook = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient().getNotebook(guid);

        } catch (EDAMNotFoundException e) {
            handleException(e);
            notebook = null;
        }
        return notebook;
    }

    /**
     * 获取所有笔记本
     *
     * @return
     * @throws Exception
     */
    public List<Notebook> listNotebooks() throws Exception {
        List<Notebook> books = new ArrayList<>();
        try {
            books = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient().listNotebooks();
        } catch (Exception e) {
            handleException(e);
        }
        return books;
    }

    /**
     * 创建笔记本
     *
     * @param bookName 笔记本名称
     * @return
     * @throws Exception
     */
    public Notebook tryCreateNoteBook(String bookName) throws Exception {
        Notebook notebook = new Notebook();
        notebook.setName(bookName);
        try {
            Notebook result = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient().createNotebook(notebook);
            mPreferenceUtils.saveParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY
                    , result.getGuid());
            return result;
        } catch (EDAMUserException e) {
            if (e.getErrorCode() == EDAMErrorCode.DATA_CONFLICT) {
                List<Notebook> books = listNotebooks();
                for (Notebook book : books) {
                    if (TextUtils.equals(book.getName(), bookName)) {
                        mPreferenceUtils.saveParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY
                                , book.getGuid());
                        return book;
                    }
                }
            }
            handleException(e);
            return null;
        }
    }

    /**
     * 新建笔记
     *
     * @param sNote 笔记
     * @return
     * @throws Exception
     */
    public Note createNote(SNote sNote) throws Exception {
        if (sNote == null)
            return null;
        Note note = sNote.parseToNote();
        note.setActive(true);
        String guid = mPreferenceUtils.getStringParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY);
        note.setNotebookGuid(guid);
        NotesLog.d(guid);
        Note result = mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient().createNote(note);
        if (result == null)
            return null;
        sNote.setGuid(result.getGuid());
        sNote.setStatus(SNote.Status.IDLE.getValue());
        //sNote.setCreateTime(result.getCreated());
        //sNote.setLastOprTime(result.getUpdated());
        mFinalDb.update(sNote);
        return result;
    }

    /**
     * 更新笔记
     *
     * @param sNote 要更新的笔记
     * @return
     * @throws Exception
     */
    public Note pushUpdateNote(SNote sNote) throws Exception {
        List<Attachment> resources;
        Note note = new Note();
        note.setTitle(sNote.getLabel());
        resources = mFinalDb.findAllByWhere(Attachment.class, "noteId=\"" + sNote.getId() + "\"");
        String content = EvernoteUtil.NOTE_PREFIX
                + sNote.getContent().
                replace("<", "&lt;").
                replace(">", "&gt;").
                replace("\n", "<br/>");
        if (resources != null && resources.size() > 0) {
            for (Attachment attachment : resources) {
                InputStream in = null;
                try {
                    if (attachment.getPath() == null)
                        break;
                    in = new BufferedInputStream(new FileInputStream(attachment.getPath()));
                    FileData data = new FileData(EvernoteUtil.hash(in), new File(attachment.getPath()));
                    ResourceAttributes attributes = new ResourceAttributes();
                    attributes.setFileName(attachment.getFileName());
                    Resource resource = new Resource();
                    resource.setData(data);
                    resource.setMime(attachment.getMimeType());
                    resource.setAttributes(attributes);
                    note.addToResources(resource);
                    content += EvernoteUtil.createEnMediaTag(resource);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        content += EvernoteUtil.NOTE_SUFFIX;
        note.setContent(content);
        note.setGuid(sNote.getGuid());
        note.setActive(true);
        Note result = mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient().updateNote(note);
        sNote.setStatus(SNote.Status.IDLE.getValue());
        sNote.setLastOprTime(result.getUpdated());
        mFinalDb.update(sNote);
        return note;
    }

    /**
     * @param sNote
     * @throws Exception
     */

    public void pullUpdateNote(SNote sNote) throws Exception {
        Note note = mEvernoteSession.getEvernoteClientFactory().getNoteStoreClient()
                .getNote(sNote.getGuid(), true, false, false, false);
        sNote.parseFromNote(note);
        sNote.setType(SNote.NoteType.NORMAL);
        mFinalDb.update(sNote);
    }

    /**
     * 获取笔记内容并保存到本地数据库
     *
     * @param noteGuid
     * @throws Exception
     */
    public void loadEverNote(String noteGuid) throws Exception {
        if (TextUtils.isEmpty(noteGuid))
            return;
        Note note = mEvernoteSession.getEvernoteClientFactory().getNoteStoreClient()
                .getNote(noteGuid, true, false, false, false);
        SNote sNote = new SNote();
        sNote.parseFromNote(note);
        mFinalDb.saveBindId(sNote);
        if (note.getResources() == null) {
            return;
        } else {
            if (note.getResources().size() <= 0) {
                return;
            } else {
                List<Resource> resourceList = note.getResources();
                for (Resource resource : resourceList) {
                    downloadAttachment(note, resource.getGuid());
                }
            }
        }
    }

    /**
     * 下载附件
     *
     * @param note    笔记
     * @param resGuid 附件唯一识别的id
     */
    public void downloadAttachment(Note note, String resGuid) {
        String fileName = "", mimeType;
        String lable = note.getTitle();
        int noteID = mFinalDb.findAllByWhere(SNote.class, "guid=\"" +
                note.getGuid() + "\"").get(0).getId();
        String path = mFileUtils.createAttDir(lable);
        try {
            Resource resource = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient().getResource(resGuid, true, false, true, false);
            if (!resource.getAttributes().getFileName().equals("")) {
                fileName = resource.getAttributes().getFileName();
            }
            mimeType = resource.getMime();
            String resUrl = mPreferenceUtils.getApiPrefix() + "/res/" + resGuid;
            //如果没有SD卡停止下载附件
            if (path.equals(FileUtils.SD_CARD_NOT_READY)) {
                return;
            }
            Attachment attachment = new Attachment();
            attachment.setNoteId(noteID);
            attachment.setFileName(fileName);
            attachment.setMimeType(mimeType);
            attachment.setGuid(resGuid);
            attachment.setPath(path + File.separator + fileName);
            mFinalDb.save(attachment);
            downloadFile(resUrl, path, fileName);
        } catch (Exception e) {
            NotesLog.e(e.toString());
        }
    }


    /**
     * post请求下载文件，需要携带名为auth的印象笔记认证参数
     *
     * @param url  地址
     * @param path 下载的文件地址
     */
    private void downloadFile(final String url, final String path, final String fileName) {
        final String auth = PreferenceUtils.getInstance(mContext).getAuth();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("auth", auth).build();
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = response.body().byteStream();
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File downLoad = new File(file, fileName);
                    FileOutputStream fos = new FileOutputStream(downLoad);
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                }
            }
        });
    }

    /**
     * 删除笔记
     *
     * @param guid
     * @throws Exception
     */
    public void deleteNote(String guid) throws Exception {
        if (TextUtils.isEmpty(guid))
            return;
        mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient().deleteNote(guid);
    }

    /**
     * 删除本地笔记
     *
     * @param guid
     */
    public void deleteLocalNote(String guid) {
        if (TextUtils.isEmpty(guid))
            return;
        try {
            mFinalDb.deleteByWhere(SNote.class, "guid = '" + guid + "'");
        } catch (Exception e) {
            NotesLog.e("delete local note error");
            e.printStackTrace();
        }
    }

    public void expungeNote(String guid) throws Exception {
        if (TextUtils.isEmpty(guid))
            return;
        mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient().expungeNote(guid);
    }

    /**
     * 更新笔记
     *
     * @param sNote 笔记
     * @return
     * @throws Exception
     */
    public boolean pushNote(SNote sNote) throws Exception {
        if (sNote == null)
            return false;
        if (sNote.hasReadyRemove()) {
            if (!TextUtils.isEmpty(sNote.getGuid())) {
                deleteNote(sNote.getGuid());
            }
            sNote.setStatus(SNote.Status.IDLE);
            mFinalDb.update(sNote);
        } else if (sNote.hasReadyNewPush()) {
            createNote(sNote);
        } else if (sNote.hasReadyUpdatePush()) {
            pushUpdateNote(sNote);
        }
        return true;
    }

    /**
     * 更新笔记
     *
     * @throws Exception
     */
    public void pushNotes() throws Exception {
        NotesLog.d("");
        List<SNote> sNotes = mFinalDb.findAll(SNote.class);
        for (SNote sNote : sNotes) {
            pushNote(sNote);
        }
        NotesLog.d("");
    }

    /**
     * 更新笔记
     *
     * @throws Exception
     */
    public void pullNotes() throws Exception {
        NotesLog.d("");
        NoteFilter noteFilter = new NoteFilter();
        noteFilter.setOrder(NoteSortOrder.UPDATED.getValue());
        String guid = mPreferenceUtils.getStringParam(PreferenceUtils.EVERNOTE_NOTEBOOK_GUID_KEY);
        noteFilter.setNotebookGuid(guid);
        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeUpdated(true);
        spec.setIncludeCreated(true);
        NoteCollectionCounts counts = mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient().findNoteCounts(noteFilter, false);
        List<SNote> sNoteList = mFinalDb.findAllByWhere(SNote.class,
                "type != " + SNote.NoteType.TRASH.getValue());
        List<String> guids = new ArrayList<>();
        for (SNote note : sNoteList) {
            guids.add(note.getGuid());
        }

        if (counts == null || counts.getNotebookCounts() == null) {
            for (String deleteGuid : guids) {
                deleteLocalNote(deleteGuid);
            }
            return;
        }

        int maxCount = counts.getNotebookCounts().get(guid);

        NotesMetadataList list = mEvernoteSession.getEvernoteClientFactory()
                .getNoteStoreClient()
                .findNotesMetadata(noteFilter, 0, maxCount, spec);

        for (NoteMetadata data : list.getNotes()) {
            guids.remove(data.getGuid());
            List<SNote> sNotes = mFinalDb.findAllByWhere(SNote.class, "guid = '" + data.getGuid() + "'");
            //根据返回的笔记更新时间来判断是否更新本地笔记
            if (sNotes != null && sNotes.size() > 0) {
                //上传
                SNote sNote = sNotes.get(0);
                if (data.getUpdated() > sNote.getLastOprTime())
                    pullUpdateNote(sNote);
            } else {
                //下载更新
                loadEverNote(data.getGuid());
            }
        }
        if (guids.size() > 0) {
            for (String deleteGuid : guids) {
                deleteLocalNote(deleteGuid);
            }
        }
        NotesLog.d("");
    }

    /**
     * 检测登录
     *
     * @param silence
     * @return
     */
    private boolean checkLogin(boolean silence) {
        if (!isLogin()) {
            if (!silence)
                EventBus.getDefault().post(SyncResult.ERROR_NOT_LOGIN);
            return false;
        }
        return true;
    }

    /**
     * 查看是否登录
     *
     * @return
     */
    public SyncResult checkLogin() {
        if (!isLogin()) {
            return SyncResult.ERROR_NOT_LOGIN;
        }
        return SyncResult.SUCCESS;
    }

    /**
     * 同步笔记
     *
     * @param type
     * @return
     */
    public SyncResult sync(final SyncType type) {
        if (checkLogin() == SyncResult.ERROR_NOT_LOGIN) {
            return SyncResult.ERROR_NOT_LOGIN;
        }
        try {
            makeSureNoteBookExist(NOTE_BOOK_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof EDAMUserException) {
                EDAMUserException exception = (EDAMUserException) e;
                EDAMErrorCode errorCode = exception.getErrorCode();
                switch (errorCode) {
                    case RATE_LIMIT_REACHED:
                        if (!BuildConfig.DEBUG) {
                            return SyncResult.ERROR_FREQUENT_API;
                        }
                        break;
                    //need to auth again
                    case AUTH_EXPIRED:
                        //clear login message
                        logout();
                        return SyncResult.ERROR_AUTH_EXPIRED;
                    case PERMISSION_DENIED:
                        return SyncResult.ERROR_PERMISSION_DENIED;
                    //quota reached max, so fail
                    case QUOTA_REACHED:
                        return SyncResult.ERROR_QUOTA_EXCEEDED;
                    default:
                        return SyncResult.ERROR_OTHER;
                }
            }
            return SyncResult.ERROR_OTHER;
        }
        try {
            switch (type) {
                case ALL:
                    pushNotes();
                    pullNotes();
                    break;
                case PULL:
                    pullNotes();
                    break;
                case PUSH:
                    pushNotes();
                    break;
            }
            return SyncResult.SUCCESS;
        } catch (Exception e) {
            System.out.println(e);
            NotesLog.e(e.toString());
            e.printStackTrace();
            return SyncResult.ERROR_OTHER;
        }
    }

    private void handleException(Exception e) {
        if (e != null)
            e.printStackTrace();
    }

    public enum SyncResult {
        START,
        ERROR_NOT_LOGIN,
        ERROR_FREQUENT_API,
        ERROR_EXPUNGE,
        ERROR_DELETE,
        ERROR_RECOVER,
        ERROR_AUTH_EXPIRED,
        ERROR_PERMISSION_DENIED,
        ERROR_QUOTA_EXCEEDED,
        ERROR_OTHER,
        SUCCESS_SILENCE,
        SUCCESS
    }

    public enum SyncType {
        ALL,
        PULL,
        PUSH
    }
}