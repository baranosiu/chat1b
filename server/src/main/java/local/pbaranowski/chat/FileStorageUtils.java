package local.pbaranowski.chat;

class FileStorageUtils {

    private static final String FILE_LIST_FORMAT_STRING = "%4s %-16s : %s";

    public static String fileRecordToString(String key, String sender, String fileName) {
        return String.format(FILE_LIST_FORMAT_STRING, key, sender, fileName);
    }

    private FileStorageUtils() {
    }
}
