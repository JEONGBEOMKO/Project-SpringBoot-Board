package com.board.projectboard.service;

import com.board.projectboard.domain.File;
import com.board.projectboard.dto.FileDto;
import com.board.projectboard.repository.FileRepository;
import org.springframework.transaction.annotation.Transactional;

public class FileService {
    private FileRepository fileRepository;

    public FileService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    @Transactional
    public Long saveFile(FileDto fileDto) {
        return fileRepository.save(fileDto.toEntity()).getId();
    }

    @Transactional
    public FileDto getFile(Long id) {
        File file = fileRepository.findById(id).get();

        FileDto fileDto = FileDto.builder()
                .id(id)
                .origFilename(file.getOrigFilename())
                .filename(file.getFilePath())
                .filePath(file.getFilePath())
                .build();
        return fileDto;
    }
}
