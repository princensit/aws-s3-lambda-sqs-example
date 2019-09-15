package com.prince.serverless.model;

import lombok.Data;

/**
 * File details object
 *
 * @author Prince Raj
 */
@Data
public class FileDetails {

    private final String fileName;

    private final String lastFolderName;

    private final String filePath;
}
