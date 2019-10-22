package com.lazycodes.assistant.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Command.class}, version = 1, exportSchema = false)
public abstract class CommandDatabase extends RoomDatabase {

    private static CommandDatabase db;

    public abstract CommandDao getCommandDao();

    public static CommandDatabase getInstance(Context context) {
        if (db != null) {
            return db;
        }
        db = Room.databaseBuilder(context, CommandDatabase.class, "command_db")
                .allowMainThreadQueries()
                .build();
        return db;
    }
}
