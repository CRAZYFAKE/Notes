package com.lguipeng.notes.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.NotePresenter;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.utils.DialogUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import javax.inject.Inject;

import butterknife.Bind;
import io.github.mthli.knife.KnifeText;

public class NoteActivity extends BaseActivity implements NoteView {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.label_edit_text)
    MaterialEditText labelEditText;
    @Bind(R.id.content_edit_text)
    KnifeText contentEditText;
    @Bind(R.id.opr_time_line_text)
    TextView oprTimeLineTextView;
    @Bind(R.id.frame_layout)
    FrameLayout mFramLayout;
    @Bind(R.id.knife_tools)
    HorizontalScrollView mKnifeToolsView;

    private MenuItem doneMenuItem;
    private MenuItem knifeMenuItem;
    @Inject
    NotePresenter notePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
        notePresenter.onCreate(savedInstanceState);
        setFormat();
    }

    private void initializePresenter() {
        notePresenter.attachView(this);
        notePresenter.attachIntent(getIntent());
    }

    @Override
    protected void initializeDependencyInjector() {
        App app = (App) getApplication();
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build();
        mActivityComponent.inject(this);
    }

    @Override
    public boolean showKnifeTools() {
        if (mKnifeToolsView.getVisibility() == View.GONE) {
            knifeMenuItem.setIcon(R.drawable.ic_format_full);
            mKnifeToolsView.setVisibility(View.VISIBLE);
        } else {
            knifeMenuItem.setIcon(R.drawable.ic_format);
            mKnifeToolsView.setVisibility(View.GONE);
        }
        return true;
    }

    @Override
    protected void onStop() {
        notePresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        notePresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        knifeMenuItem = menu.getItem(1);
        notePresenter.onPrepareOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (notePresenter.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return notePresenter.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void setToolbarTitle(int title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void initViewOnEditMode(SNote note) {
        showKeyBoard();
        labelEditText.requestFocus();
        labelEditText.setText(note.getLabel());
        contentEditText.fromHtml(note.getContent());
        labelEditText.setSelection(note.getLabel().length());
        contentEditText.setSelection(contentEditText.getEditableText().length());
        labelEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnViewMode(SNote note) {
        hideKeyBoard();
        labelEditText.setText(note.getLabel());
        contentEditText.fromHtml(note.getContent());
        labelEditText.setOnFocusChangeListener(notePresenter);
        contentEditText.setOnFocusChangeListener(notePresenter);
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnCreateMode(SNote note) {
        labelEditText.requestFocus();
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void setOperateTimeLineTextView(String text) {
        oprTimeLineTextView.setText(text);
    }

    @Override
    public void setDoneMenuItemVisible(boolean visible) {
        if (doneMenuItem != null) {
            doneMenuItem.setVisible(visible);
        }
    }

    @Override
    public boolean isDoneMenuItemVisible() {
        return doneMenuItem != null && doneMenuItem.isVisible();
    }

    @Override
    public boolean isDoneMenuItemNull() {
        return doneMenuItem == null;
    }

    @Override
    public String getLabelText() {
        return labelEditText.getText().toString();
    }

    @Override
    public String getContentText() {
//        return contentEditText.getText().toString();
        return contentEditText.toHtml();
    }

    @Override
    public void showNotSaveNoteDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        builder.setPositiveButton(R.string.sure, notePresenter);
        builder.setNegativeButton(R.string.cancel, notePresenter);
        builder.show();
    }

    @Override
    public void hideKeyBoard() {
        hideKeyBoard(labelEditText);
    }

    @Override
    public void showKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyBoard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case NotePresenter.NOTE_REQUEST_CODE:
                int isChanged = data.getExtras().getInt(NotePresenter.IS_ATTACHMENT_CHANGED);
                if (isChanged == 1) {
                    setDoneMenuItemVisible(true);
                } else {
                    setDoneMenuItemVisible(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void startAttachmentActivityForResult(SNote note) {
        Intent intent = new Intent(NoteActivity.this, AttachmentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(NotePresenter.CURRENT_NOTE, note);
        intent.putExtras(bundle);
        startActivityForResult(intent, NotePresenter.NOTE_REQUEST_CODE);
    }

    private void setFormat(){
        setupBold();
        setupItalic();
        setupUnderline();
        setupStrikethrough();
        setupBullet();
        setupLink();
        setupClear();
    }

    private void setupBold() {
        ImageButton bold = (ImageButton) findViewById(R.id.bold);

        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.bold(!contentEditText.contains(KnifeText.FORMAT_BOLD));
            }
        });

        bold.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_bold, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupItalic() {
        ImageButton italic = (ImageButton) findViewById(R.id.italic);

        italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.italic(!contentEditText.contains(KnifeText.FORMAT_ITALIC));
            }
        });

        italic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_italic, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupUnderline() {
        ImageButton underline = (ImageButton) findViewById(R.id.underline);

        underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.underline(!contentEditText.contains(KnifeText.FORMAT_UNDERLINED));
            }
        });

        underline.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_underline, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupStrikethrough() {
        ImageButton strikethrough = (ImageButton) findViewById(R.id.strikethrough);

        strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.strikethrough(!contentEditText.contains(KnifeText.FORMAT_STRIKETHROUGH));
            }
        });

        strikethrough.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_strikethrough, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupBullet() {
        ImageButton bullet = (ImageButton) findViewById(R.id.bullet);

        bullet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.bullet(!contentEditText.contains(KnifeText.FORMAT_BULLET));
            }
        });


        bullet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_bullet, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupLink() {
        ImageButton link = (ImageButton) findViewById(R.id.link);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkDialog();
            }
        });

        link.setOnLongClickListener((v) ->{
            Toast.makeText(NoteActivity.this, R.string.toast_insert_link, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupClear() {
        ImageButton clear = (ImageButton) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.clearFormats();
            }
        });

        clear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(NoteActivity.this, R.string.toast_format_clear, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void showLinkDialog() {
        final int start = contentEditText.getSelectionStart();
        final int end = contentEditText.getSelectionEnd();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.edit);
        builder.setView(view);
        builder.setTitle(R.string.insert_link);

        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String link = editText.getText().toString().trim();
                if (TextUtils.isEmpty(link)) {
                    return;
                }
                contentEditText.link(link, start, end);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING HERE
            }
        });

        builder.create().show();
    }
}