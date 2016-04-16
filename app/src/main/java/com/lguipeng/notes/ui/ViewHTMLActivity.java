package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteClientFactory;
import com.evernote.client.android.asyncclient.EvernoteHtmlHelper;
import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;
import com.lguipeng.notes.R;
import com.lguipeng.notes.utils.ToolbarUtils;
import com.squareup.okhttp.Response;

import net.vrallev.android.task.TaskResult;

import java.io.IOException;

import butterknife.Bind;

/**
 * 浏览在线笔记，以HTML形式
 */
public class ViewHTMLActivity extends BaseActivity {

    public static final String KEY_NOTE = "KEY_NOTE";
    public static final String KEY_HTML = "KEY_HTML";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private NoteRef mNoteRef;
    private String mHtml;
    private EvernoteHtmlHelper mEvernoteHtmlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoteRef = (NoteRef) getIntent().getExtras().getParcelable(KEY_NOTE);
        mHtml = getIntent().getExtras().getString(KEY_HTML);

        toolbar.setTitle(mNoteRef.getTitle());

        final WebView webView = (WebView) findViewById(R.id.web_view);

        if (savedInstanceState == null) {
            String data = "<html><head></head><body>" + mHtml + "</body></html>";

            webView.setWebViewClient(new WebViewClient() {

                @SuppressWarnings("deprecation")
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    try {
                        Response response = getEvernoteHtmlHelper().fetchEvernoteUrl(url);
                        WebResourceResponse webResourceResponse = toWebResource(response);
                        if (webResourceResponse != null) {
                            return webResourceResponse;
                        }
                    } catch (Exception e) {
                    }

                    return super.shouldInterceptRequest(view, url);
                }
            });

            webView.loadDataWithBaseURL("", data, "text/html", "UTF-8", null);
        }
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_view_html;
    }

    @Override
    public void initToolbar() {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    @TaskResult(id = "html")
    private void getNoteHtml(String html) {
        mHtml = html;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener((view) -> finish());
        }
    }

    protected WebResourceResponse toWebResource(Response response) throws IOException {
        if (response == null || !response.isSuccessful()) {
            return null;
        }
        String mimeType = response.header("Content-Type");
        String charset = response.header("charset");
        return new WebResourceResponse(mimeType, charset, response.body().byteStream());
    }

    protected EvernoteHtmlHelper getEvernoteHtmlHelper() throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        if (mEvernoteHtmlHelper == null) {
            EvernoteClientFactory clientFactory = EvernoteSession.getInstance().getEvernoteClientFactory();

            if (mNoteRef.isLinked()) {
                mEvernoteHtmlHelper = clientFactory.getLinkedHtmlHelper(mNoteRef.loadLinkedNotebook());
            } else {
                mEvernoteHtmlHelper = clientFactory.getHtmlHelperDefault();
            }
        }

        return mEvernoteHtmlHelper;
    }
}
