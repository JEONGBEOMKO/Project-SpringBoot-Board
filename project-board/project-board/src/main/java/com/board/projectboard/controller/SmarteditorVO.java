package com.board.projectboard.controller;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SmarteditorVO {
    private MultipartFile filedata;
    private String callback;
    private String callback_func;
}
