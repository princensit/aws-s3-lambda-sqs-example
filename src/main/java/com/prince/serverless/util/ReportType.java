package com.prince.serverless.util;

import lombok.Getter;

import com.prince.serverless.exception.InvalidFileException;

/**
 * Input file types that are supported
 *
 * @author Prince Raj
 */
public enum ReportType {

    INACTIVE("Inactive"), ACTIVE("Active");

    @Getter
    private final String value;

    ReportType(String value) {
        this.value = value;
    }

    public static ReportType getReportType(String fileName) {
        final ReportType reportType;
        if (fileName.startsWith(INACTIVE.getValue())) {
            reportType = INACTIVE;
        } else if (fileName.startsWith(ACTIVE.getValue())) {
            reportType = ACTIVE;
        } else {
            throw new InvalidFileException("File: " + fileName + " not supported");
        }

        return reportType;
    }
}
