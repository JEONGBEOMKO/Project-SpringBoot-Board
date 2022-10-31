package com.board.projectboard.controller;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SmarteditorVO {
    private MultipartFile FileData;
    private String callback;
    private String callback_func;


    public MultipartFile getFiledata() {
        return FileData;
    }

    public void setFiledata(MultipartFile filedata){
        MultipartFile Filedata = filedata;
    }
    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback){
        this.callback = callback;
    }

    public  String getCallback_func(){
        return callback_func;
    }
    public String getcallback_func(){
        return callback_func;
    }
    public void setCallback_func(String callback_func){
        this.callback_func = callback_func;
    }

}
