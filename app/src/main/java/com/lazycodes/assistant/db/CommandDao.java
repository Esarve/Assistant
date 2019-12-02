package com.lazycodes.assistant.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CommandDao {

    @Insert
    long insertNewCommand(Command command);

    @Update
    int updateCommand(Command command);

    @Delete
    int deleteCommand(Command command);

    @Query("select * from tbl_command")
    List<Command> getAllCommand();


    @Query("select * from tbl_command where fullCommand like:CurrantCommand")
    Command getTriggerCommand(String CurrantCommand);

    @Query("Delete from tbl_command")
    List<Command> delteAllSavedCommands();



}
