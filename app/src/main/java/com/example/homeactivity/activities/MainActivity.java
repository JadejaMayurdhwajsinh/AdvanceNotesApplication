package com.example.homeactivity.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.homeactivity.R;
import com.example.homeactivity.adapters.NoteAdapter;
import com.example.homeactivity.database.NotesDatabase;
import com.example.homeactivity.entities.Note;
import com.example.homeactivity.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener
{

    public static final int REQUEST_CODE_ADD_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTE=3;

    public static final int REQUEST_CODE_SELECT_IMAGE=4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION=5;
    private AlertDialog dialogAddURL;
    private RecyclerView noteRecyclerView;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private int noteClickedPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        ImageView imageAddNoteMain=findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(getApplicationContext(),
                        CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);


            }
        });

        noteRecyclerView=findViewById(R.id.notesRecyclerView);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

        noteList=new ArrayList<>();
        noteAdapter=new NoteAdapter(noteList,this);
        noteRecyclerView.setAdapter(noteAdapter);
        getnotes(REQUEST_CODE_SHOW_NOTE , false);

        EditText inputSearch = findViewById(R.id.inputsearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0)
                {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });

        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(getApplicationContext(),
                        CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }
                else
                {
                    selectImage();
                }
            }
        });

        findViewById(R.id.imageAddWebLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddURLDialog();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor=getContentResolver().query(contentUri,null,null,null,null);
        if (cursor == null)
        {
            filePath=contentUri.getPath();
        }
        else
        {
            cursor.moveToFirst();
            int index=cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
       noteClickedPosition=position;
       Intent intent=new Intent(getApplicationContext(),CreateNoteActivity.class);
       intent.putExtra("isViewOrUpdate" ,true);
       intent.putExtra("note",note);
       startActivityForResult(intent , REQUEST_CODE_UPDATE_NOTE);
    }

    private void getnotes(final int requestCode,final boolean isNoteDeleted)
    {

            class GetNotesTask extends AsyncTask<Void, Void, List<Note>>
            {

                @Override
                protected List<Note> doInBackground(Void... voids) {
                    return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
                }

                @Override
                protected void onPostExecute(List<Note> notes) {
                    super.onPostExecute(notes);
                    if (requestCode== REQUEST_CODE_SHOW_NOTE)
                    {
                        noteList.addAll(notes);
                        noteAdapter.notifyDataSetChanged();
                    }
                    else if (requestCode== REQUEST_CODE_ADD_NOTE)
                    {
                        noteList.add(0,notes.get(0));
                        noteAdapter.notifyItemInserted(0);
                        noteRecyclerView.smoothScrollToPosition(0);
                    }
                    else if (requestCode== REQUEST_CODE_UPDATE_NOTE)
                    {
                        noteList.remove(noteClickedPosition);
                        if(isNoteDeleted)
                        {
                            noteAdapter.notifyItemRemoved(noteClickedPosition);
                        }
                        else
                        {
                            noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                            noteAdapter.notifyItemChanged(noteClickedPosition);
                        }
                    }

                }
            }
            new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getnotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                boolean isNoteDeleted = data.getBooleanExtra("isNoteDeleted", false);
                getnotes(REQUEST_CODE_UPDATE_NOTE, isNoteDeleted);
            }
        }
        else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showAddURLDialog()
        {
        if (dialogAddURL == null)
        {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        View view= LayoutInflater.from(this).inflate(R.layout.layout_add_url,(ViewGroup) findViewById(R.id.layoutAddurlContainer));
        builder.setView(view);

        dialogAddURL=builder.create();
        if (dialogAddURL.getWindow()!= null)
        {
        dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputURL = view.findViewById(R.id.inputURL);
        inputURL.requestFocus();

        view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (inputURL.getText().toString().trim().isEmpty())
            {
                Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
            }
            else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches())
            {
                Toast.makeText(MainActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
            }
            else
            {
                dialogAddURL.dismiss();
                Intent intent= new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("isFromQuickActions",true);
                intent.putExtra("quickActionType","image");
                intent.putExtra("URL",inputURL.getText().toString());
                startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
             }
         }
        });

        view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v)
            {
                dialogAddURL.dismiss();
          }
        });
        }
        dialogAddURL.show();
        }
}
