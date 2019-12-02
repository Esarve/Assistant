package com.lazycodes.assistant.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.lazycodes.assistant.R;
import com.lazycodes.assistant.db.Command;
import com.lazycodes.assistant.db.CommandDatabase;

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
    public void onBindViewHolder(@NonNull CommandViewHolder holder, final int position) {

        holder.nameTV.setText(employeeList.get(position).getFullCommand());
        holder.phoneTV.setText(employeeList.get(position).getPinNo() + "");

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog(employeeList.get(position));

            }
        });


    }

    private void showConfirmationDialog(final Command command) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Item");
        builder.setIcon(R.drawable.ic_delete_black_24dp);
        builder.setMessage("Delete this item ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int deletedRow = CommandDatabase.getInstance(context)
                        .getCommandDao()
                        .deleteCommand(command);

                if(deletedRow > 0){
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    employeeList.remove(command);
                    notifyDataSetChanged();
                }

            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    class CommandViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, phoneTV;
        ImageView deleteBtn;

        public CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.row_command_TV);
            phoneTV = itemView.findViewById(R.id.row_pin_TV);
            deleteBtn = itemView.findViewById(R.id.row_menu_icon);


        }
    }
}
