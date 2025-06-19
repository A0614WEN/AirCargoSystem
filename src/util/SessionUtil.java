package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SessionUtil {

    private static final String SESSION_FILE = "session.txt";

    public static void createSession(String username) throws IOException {
        Files.write(Paths.get(SESSION_FILE), username.getBytes());
    }

    public static void clearSession() throws IOException {
        Files.deleteIfExists(Paths.get(SESSION_FILE));
    }

    public static boolean isLoggedIn() {
        return new File(SESSION_FILE).exists();
    }

    public static String getLoggedInUser() throws IOException {
        if (isLoggedIn()) {
            return new String(Files.readAllBytes(Paths.get(SESSION_FILE)));
        }
        return null;
    }
}
