package com.prince.serverless.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.input.ReversedLinesFileReader;

/**
 * Provides utilities related to files or directories
 *
 * @author Prince Raj
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    private static final String CSV_EXT = ".csv";

    private static final String ZIP_EXT = ".zip";

    private static final String GZ_EXT = ".gz";

    private FileUtils() {}

    public static boolean isEmpty(String filePath) {
        File file = new File(filePath);
        long length = file.length();
        return length == 0;
    }

    public static boolean checkExistenceAndCreateDirectory(String filePath) {
        File file = new File(filePath);
        if (filePath.contains(".")) {
            // This appears to be a file and not a path, so use the parent directory
            file = file.getParentFile();
        }
        return file.exists() || file.mkdirs();
    }

    public static boolean createNewFile(String filePath) throws IOException {
        File file = new File(filePath);
        boolean exists = false;
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean status = parentFile.mkdirs();
            }
            exists = file.createNewFile();
        }

        return exists;
    }

    public static boolean delete(List<String> filePaths) {
        boolean status = true;
        for (String filePath : filePaths) {
            status = status && new File(filePath).delete();
        }

        return status;
    }

    public static long linesCount(String filePath) throws IOException {
        int linesCount = 0;

        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader br = getBufferedReader(file)) {
                while (br.readLine() != null) {
                    linesCount++;
                }
            }
        } else {
            linesCount = -1;
        }

        return linesCount;
    }

    public static String getLastLine(String filePath) throws IOException {
        String lastLine;
        try (ReversedLinesFileReader fileReader =
                new ReversedLinesFileReader(new File(filePath), StandardCharsets.UTF_8)) {
            lastLine = fileReader.readLine();
        }

        return lastLine;
    }

    /**
     * Get zip input stream
     *
     * @param inputStream zip input stream
     * @return zip input stream
     * @throws IOException IO exception
     */
    public static InputStream getZipInputStream(InputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // read first entry
        zipInputStream.getNextEntry();
        return zipInputStream;
    }

    /**
     * Get a BufferedInputStream for the specified filePath. The input file must be UTF-8 encoded.
     *
     * @param filePath file path
     * @return input stream
     * @throws IOException IO exception
     */
    public static BufferedInputStream getBufferedInputStream(String filePath) throws IOException {
        File inputFile = new File(filePath);
        return new BufferedInputStream(getInputStream(inputFile));
    }

    /**
     * Get a BufferedReader for the specified File. The input file must be UTF-8 encoded.
     *
     * @param filePath path of the input file.
     * @return reader
     * @throws IOException IO exception
     */
    public static BufferedReader getBufferedReader(String filePath) throws IOException {
        File file = new File(filePath);
        return getBufferedReader(file);
    }

    /**
     * Get a BufferedReader for the specified dirName/fileName. The input file must be UTF-8
     * encoded.
     *
     * @param dirName directory of the input file.
     * @param fileName name of the input file.
     * @return reader
     * @throws IOException IO exception
     */
    public static BufferedReader getBufferedReader(String dirName, String fileName)
            throws IOException {
        File file = new File(dirName, fileName);
        return getBufferedReader(file);
    }

    /**
     * Get a BufferedReader for the specified File. The input file must be UTF-8 encoded.
     *
     * @param file the input file
     * @return reader
     * @throws IOException IO exception
     */
    public static BufferedReader getBufferedReader(File file) throws IOException {
        InputStream inputStream = getInputStream(file);
        return getBufferedReader(inputStream);
    }

    /**
     * Get a BufferedReader for the specified input stream and file path. The input file must be
     * UTF-8 encoded.
     *
     * @param inputStream the input stram
     * @param filePath the file path
     * @return reader
     * @throws IOException IO exception
     */
    public static BufferedReader getBufferedReader(InputStream inputStream, String filePath)
            throws IOException {
        inputStream = getInputStream(inputStream, filePath);

        InputStreamReader inputStreamReader =
                new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Gets a BufferedReader for the given inputStream. The input stream must be UTF-8 encoded.
     *
     * @param inputStream file path
     * @return reader
     */
    public static BufferedReader getBufferedReader(InputStream inputStream) {
        InputStreamReader inputStreamReader =
                new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

    public static BufferedOutputStream getBufferedOutputStream(File file, boolean append)
            throws IOException {
        OutputStream outputStream = getOutputStream(file, append);
        return new BufferedOutputStream(outputStream);
    }

    /**
     * Get a BufferedWriter. The output file is UTF-8 encoded.
     *
     * @param filePath path of the output file.
     * @return writer
     * @throws IOException IO exception
     */
    public static BufferedWriter getBufferedWriter(String filePath, boolean append)
            throws IOException {
        File file = new File(filePath);
        return getBufferedWriter(file, append);
    }

    /**
     * Get a BufferedWriter. The output file is UTF-8 encoded.
     *
     * @param dirName directory of the output file.
     * @param fileName name of the output file.
     * @return writer
     * @throws IOException IO exception
     */
    public static BufferedWriter getBufferedWriter(String dirName, String fileName, boolean append)
            throws IOException {
        File file = new File(dirName, fileName);
        return getBufferedWriter(file, append);
    }

    /**
     * Get a BufferedWriter. The output file is UTF-8 encoded.
     *
     * @param file the output file
     * @return writer
     * @throws IOException IO exception
     */
    public static BufferedWriter getBufferedWriter(File file, boolean append) throws IOException {
        OutputStream outputStream = getOutputStream(file, append);
        return getBufferedWriter(outputStream);
    }

    /**
     * Gets a BufferedWriter for the given outputStream. The input stream must be UTF-8 encoded.
     *
     * @param outputStream output stream
     * @return writer
     */
    public static BufferedWriter getBufferedWriter(OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        return new BufferedWriter(outputStreamWriter);
    }

    /**
     * Get a PrintWriter with autoFlush set to false for the specified File. The output file is
     * UTF-8 encoded.
     *
     * @param file the output file
     * @return writer
     * @throws IOException IO exception
     */
    public static PrintWriter getPrintWriter(File file, boolean append) throws IOException {
        BufferedWriter bufferedWriter = getBufferedWriter(file, append);
        return new PrintWriter(bufferedWriter, false);
    }

    private static InputStream getInputStream(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        String fileName = file.getName();
        return getInputStream(inputStream, fileName);
    }

    private static InputStream getInputStream(InputStream inputStream, String filePath)
            throws IOException {
        if (filePath.endsWith(GZ_EXT)) {
            inputStream = new GZIPInputStream(inputStream, BUFFER_SIZE);
        } else if (filePath.endsWith(ZIP_EXT)) {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            zipInputStream.getNextEntry();
            inputStream = zipInputStream;
        }

        return inputStream;
    }

    private static OutputStream getOutputStream(File file, boolean append) throws IOException {
        OutputStream outputStream = new FileOutputStream(file, append);
        String fileName = file.getName();
        if (fileName.endsWith(GZ_EXT)) {
            outputStream = new GZIPOutputStream(outputStream, BUFFER_SIZE);
        } else if (fileName.endsWith(ZIP_EXT)) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            fileName = fileName.replace(ZIP_EXT, CSV_EXT);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);

            outputStream = zipOutputStream;
        }

        return outputStream;
    }
}
