package com.lazycodes.assistant.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lazycodes.assistant.R;
import com.lazycodes.assistant.db.Command;
import com.lazycodes.assistant.db.CommandDatabase;

import java.util.ArrayList;
import java.util.List;

public class AllCommandListActivity extends AppCompatActivity {

    private List<Command> mCommandList = new ArrayList<>();
    private Context context;
    private CommandAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_command_list);



        context = AllCommandListActivity.this;
        mRecyclerView = findViewById(R.id.all_commandRV);
        mTextView = findViewById(R.id.emptyTV);


        mCommandList = CommandDatabase.getInstance(context)
                .getCommandDao()
                .getAllCommand();


        if (mCommandList.size() == 0) {
            mTextView.setVisibility(View.VISIBLE);
        }

        adapter = new CommandAdapter(context, mCommandList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }

}
