package com.lazycodes.assistant.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_command")
public class Command {
    @PrimaryKey(autoGenerate = true)
    private long cmdID;
    private String fullCommand;
    @NonNull
    private int pinNo;


    public Command(String fullCommand, int pinNo) {
        this.fullCommand = fullCommand;
        this.pinNo = pinNo;
    }

    @Ignore
    public Command(long cmdID, String fullCommand, int pinNo) {
        this.cmdID = cmdID;
        this.fullCommand = fullCommand;
        this.pinNo = pinNo;
    }

    public long getCmdID() {
        return cmdID;
    }

    public void setCmdID(long cmdID) {
        this.cmdID = cmdID;
    }

    public String getFullCommand() {
        return fullCommand;
    }

    public void setFullCommand(String fullCommand) {
        this.fullCommand = fullCommand;
    }

    public int getPinNo() {
        return pinNo;
    }

    public void setPinNo(int pinNo) {
        this.pinNo = pinNo;
    }
}
