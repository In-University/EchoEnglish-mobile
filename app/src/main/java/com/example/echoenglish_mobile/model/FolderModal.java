package com.example.echoenglish_mobile.model;

public class FolderModal {
    private int folderId;
    private String folderName;

    public String getFolderName(){
        return folderName;
    }

    public int getFolderId(){
        return folderId;
    }

    public FolderModal(int folder_id, String folder_name){
        this.folderId = folder_id;
        this.folderName = folder_name;
    }
}