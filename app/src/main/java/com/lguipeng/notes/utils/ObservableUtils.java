package com.lguipeng.notes.utils;

import android.content.Context;

import com.evernote.edam.type.User;
import com.lguipeng.notes.model.SNote;

import net.tsz.afinal.FinalDb;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class ObservableUtils {

    @Inject
    public ObservableUtils() {
    }

    /**
     * 根据type查找数据库的笔记
     *
     * @param finalDb
     * @param type
     * @return
     */
    public Observable<List<SNote>> getLocalNotesByType(FinalDb finalDb, int type) {
        return create(new GetLocalNotesByTypeFun(finalDb, type));
    }

    /**
     * 获取用户
     *
     * @param everNoteUtils
     * @return
     */
    public Observable<User> getEverNoteUser(EverNoteUtils everNoteUtils) {
        return create(new GetEverNoteUserFun(everNoteUtils));
    }

    /**
     * 备份笔记
     *
     * @param context
     * @param finalDb
     * @param fileUtils
     * @return
     */
    public Observable<Boolean> backNotes(Context context, FinalDb finalDb, FileUtils fileUtils) {
        return create(new BackupNotesFun(context, finalDb, fileUtils));
    }

    /**
     * 笔记同步到印象笔记
     *
     * @param everNoteUtils
     * @param type
     * @return
     */
    public Observable<EverNoteUtils.SyncResult> sync(EverNoteUtils everNoteUtils, EverNoteUtils.SyncType type) {
        return create(new SyncFun(everNoteUtils, type));
    }

    /**
     * 更新笔记
     *
     * @param everNoteUtils
     * @param note
     * @return
     */
    public Observable<Boolean> pushNote(EverNoteUtils everNoteUtils, SNote note) {
        return create(new PushNoteFun(everNoteUtils, note));
    }

    private <T> Observable<T> create(Fun<T> fun) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    T t = fun.call();
                    subscriber.onNext(t);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private class SyncFun implements Fun<EverNoteUtils.SyncResult> {

        private EverNoteUtils mEverNoteUtils;
        private EverNoteUtils.SyncType type;

        public SyncFun(EverNoteUtils mEverNoteUtils, EverNoteUtils.SyncType type) {
            this.mEverNoteUtils = mEverNoteUtils;
            this.type = type;
        }

        @Override
        public EverNoteUtils.SyncResult call() throws Exception {
            return mEverNoteUtils.sync(type);
        }
    }

    private class PushNoteFun implements Fun<Boolean> {
        private EverNoteUtils mEverNoteUtils;
        private SNote sNote;

        public PushNoteFun(EverNoteUtils mEverNoteUtils, SNote sNote) {
            this.mEverNoteUtils = mEverNoteUtils;
            this.sNote = sNote;
        }

        @Override
        public Boolean call() throws Exception {
            return mEverNoteUtils.pushNote(sNote);
        }
    }

    private class BackupNotesFun implements Fun<Boolean> {
        private Context mContext;
        private FinalDb mFinalDb;
        private FileUtils mFileUtils;

        public BackupNotesFun(Context mContext, FinalDb mFinalDb, FileUtils mFileUtils) {
            this.mContext = mContext;
            this.mFinalDb = mFinalDb;
            this.mFileUtils = mFileUtils;
        }

        @Override
        public Boolean call() throws Exception {
            List<SNote> notes = mFinalDb.findAllByWhere(SNote.class, " type = 0");
            return mFileUtils.backupSNotes(mContext, notes);
        }
    }

    private class GetEverNoteUserFun implements Fun<User> {
        private EverNoteUtils mEverNoteUtils;

        public GetEverNoteUserFun(EverNoteUtils mEverNoteUtils) {
            this.mEverNoteUtils = mEverNoteUtils;
        }

        @Override
        public User call() throws Exception {
            return mEverNoteUtils.getUser();
        }
    }

    private class GetLocalNotesByTypeFun implements Fun<List<SNote>> {

        private FinalDb mFinalDb;
        private int type;

        public GetLocalNotesByTypeFun(FinalDb mFinalDb, int type) {
            this.mFinalDb = mFinalDb;
            this.type = type;
        }

        /**
         * 根据条件查找所有数据
         */
        @Override
        public List<SNote> call() throws Exception {
            return mFinalDb.findAllByWhere(SNote.class, "type = " + type
                    , "lastOprTime", true);
        }
    }

    /**
     * Created by lgp on 2015/9/11.
     */
    public interface Fun<T> {
        T call() throws Exception;
    }
}