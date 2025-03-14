package dev.dergoogler.mmrl.compat.stub;

import android.os.ParcelFileDescriptor;

interface IFileManager {
    boolean deleteOnExit(String path);
    boolean writeText(String path, String data);
    boolean writeBytes(String path, in byte[] data);
    String readText(String path);
    byte[] readBytes(String path);
    String readAsBase64(String path);
    String[] list(String path);
    long stat(String path);
    long size(String path);
    long sizeRecursive(String path);
    boolean delete(String path);
    boolean exists(String path);
    boolean isDirectory(String path);
    boolean isFile(String path);
    boolean mkdir(String path);
    boolean mkdirs(String path);
    boolean createNewFile(String path);
    boolean renameTo(String target, String dest);
    boolean copyTo(String target, String dest, boolean overwrite);
    boolean canExecute(String path);
    boolean canWrite(String path);
    boolean canRead(String path);
    boolean isHidden(String path);
    boolean setPermissions(String path, int mode);
    boolean setOwner(String path, int owner, int group);
    String resolve(in String[] paths);
    String normalizeStringPosix(String path, boolean allowAboveRoot);
    ParcelFileDescriptor parcelFile(String path);
}