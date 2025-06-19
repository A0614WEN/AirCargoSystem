package util;

import model.AdministratorUser;

import java.io.*;


public class UserUtil {

    private static final String FILE_PATH = "AdministratorUser.txt";

    public static void saveUser(AdministratorUser user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(user.toString());
            writer.newLine();
        }
    }

    public static AdministratorUser findUser(String username, String password) throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username) && parts[1].equals(password)) {
                    return new AdministratorUser(parts[0], parts[1], parts[2]);
                }
            }
        }
        return null;
    }
}
