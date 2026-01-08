package free.svoss.tools.jChatRAG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Hashing {


    public static byte[] getSha256(byte[] data) {
        if (data == null) return null;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.f("No SHA-256 MessageDigest");
            System.exit(1);
            return null;
        }

        md.update(data);

        return md.digest();
    }


    public static byte[] getSha256(File f) {
        long total = f.length();
        if(total<1)return null;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.f("No SHA-256 MessageDigest");
            System.exit(1);
            return null;
        }

        byte[] buffer = new byte[8192];
        //byte[] buffer= new byte[1024];
        int count;
        try (InputStream is = Files.newInputStream(f.toPath())) {



            Log.i("Hashing " + toReadableFileSize(total) + " " + f);
            while ((count = is.read(buffer)) > 0)
                md.update(buffer, 0, count);


        } catch (IOException e) {
            Log.e("Failed to read " + f + "\n" + e.getMessage() + "\n");
            return null;
        }

        return md.digest();
    }
    public static String toReadableFileSize(long bytes) {
        long kb = 1024L;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        long tb = 1024 * gb;

        if (bytes >= tb) return formatDoubleTwoDigits(bytes * 1.0 / tb) + " TB";
        if (bytes >= gb) return formatDoubleTwoDigits(bytes * 1.0 / gb) + " GB";
        if (bytes >= mb) return formatDoubleTwoDigits(bytes * 1.0 / mb) + " MB";
        if (bytes >= kb) return formatDoubleTwoDigits(bytes * 1.0 / kb) + " KB";
        return bytes + " bytes";

    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String formatDoubleTwoDigits(double v) {
        v = v * 100.0;
        int iv = (int) (0.5 + v);
        v = iv * 0.01;
        String s = "" + v;
        if (s.contains(".")) {
            s = s + "00";
            s = s.substring(0, s.indexOf(".") + 3);

            while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        }
        s = s.trim();
        if (s.isEmpty()) s = "0";
        return s;
    }
}
