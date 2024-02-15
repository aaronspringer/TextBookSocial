package org.example;

import org.mindrot.jbcrypt.BCrypt;
import java.io.Console;

public class SecurityUtils {

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    public static void resetPassword(User user){
        Console console = System.console();
        String newPassword;
        newPassword = new String(console.readPassword("Enter new password: "));

        while (!isValidPassword(newPassword)) {
            console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
            newPassword = new String(console.readPassword("Enter new password: "));
        }

        String hashedPassword = hashPassword(newPassword);

        DatabaseUtils.resetUserPassword(user.getUsername(), hashedPassword);

        System.out.println("Your password has been reset successfully.");
        user.setFirstLogin(0);

    }

    public static String createPassword(){
        Console console = System.console();
        String newPassword;
        newPassword = new String(console.readPassword("Enter password: "));

        while (!isValidPassword(newPassword)) {
            console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
            newPassword = new String(console.readPassword("Enter new password: "));
        }

        String hashedPassword = hashPassword(newPassword);
        newPassword = null;
        return hashedPassword;
    }

    public static boolean isValidPassword(String password) {
        if (password.length() < 10) {
            return false;
        }

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!&$@#*]+.*");

        return hasUppercase && hasNumber && hasSpecial;
    }

}

