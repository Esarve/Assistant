package com.lazycodes.assistant.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.lazycodes.assistant.R;
import com.lazycodes.assistant.db.Command;

import java.util.List;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {

    private Context context;
    private List<Command> employeeList;

    public CommandAdapter(Context context, List<Command> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.command_row, parent, false);

        return new CommandViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {

        holder.nameTV.setText(employeeList.get(position).getFullCommand());
        holder.phoneTV.setText(employeeList.get(position).getPinNo() + "");

    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    class CommandViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, phoneTV;

        public CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.row_command_TV);
            phoneTV = itemView.findViewById(R.id.row_pin_TV);

        }
    }
}
