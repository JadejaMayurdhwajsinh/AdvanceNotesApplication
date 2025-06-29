package com.example.homeactivity.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.Transliterator;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeactivity.R;
import com.example.homeactivity.entities.Note;
import com.example.homeactivity.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>
{

    private List<Note> note;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> noteSource;

    public NoteAdapter(List<Note> note,NoteListener noteListener) {
        this.note = note;
        this.noteListener = noteListener;
        noteSource = note;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(note.get(position));
        holder.layoutNote.setOnClickListener(view -> noteListener.onNoteClicked(note.get(position), position));
    }


    @Override
    public int getItemCount()
    {
        return note.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder
    {

        TextView textTitle,textSubtitle,textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        NoteViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textTitle=itemView.findViewById(R.id.textTitle);
            textSubtitle=itemView.findViewById(R.id.textSubtitle);
            textDateTime=itemView.findViewById(R.id.textDateTime);
            layoutNote=itemView.findViewById(R.id.layoutNote);
            imageNote=itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note note)
        {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty())
            {
                textSubtitle.setVisibility(View.GONE);
            }
            else
            {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable=(GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null)
            {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else
            {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImage_path() != null)
            {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImage_path()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else
            {
                imageNote.setVisibility(View.GONE);
            }
        }
    }

    public void searchNotes(final String searchKeyword)
    {
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                if (searchKeyword.trim().isEmpty())
                {
                    note=noteSource;
                }
                else
                {
                    ArrayList<Note> temp=new ArrayList<>();
                    for (Note note : noteSource)
                    {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase()))
                        {
                            temp.add(note);
                        }
                    }
                    note=temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }

    public void cancelTimer()
    {
        if (timer!= null)
        {
            timer.cancel();
        }
    }
}