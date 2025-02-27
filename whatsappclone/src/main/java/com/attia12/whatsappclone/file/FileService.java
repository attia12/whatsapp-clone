package com.attia12.whatsappclone.file;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;

@Service
@Slf4j
@RequiredArgsConstructor

public class FileService {
    @Value("${application.file.uploads.media-output-path}")
    private String fileUploadPath;
    public String saveFile(@NonNull MultipartFile sourceFile,
                           @NonNull String userId) {
        final String fileUploadSubPath = "users" + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile sourceFile, @NonNull String fileUploadSubPath) {
        final String finalUploadPath=fileUploadPath+separator+fileUploadSubPath;
        File targetFolder=new File(finalUploadPath);
        if(!targetFolder.exists()){
            boolean folderCreated=targetFolder.mkdirs();
            if(!folderCreated){
                log.warn("Unable to create folder {}",targetFolder);
                return null;
            }
        }
        final String fileExtention=getFileExtention(sourceFile.getOriginalFilename());
        String targetFilePath=finalUploadPath+separator+System.currentTimeMillis()+fileExtention;
        Path targetPath= Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            return targetFilePath;

        } catch (IOException e) {
            log.error("Unable to save file",e);

        }
        return null;

    }

    private String getFileExtention(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
