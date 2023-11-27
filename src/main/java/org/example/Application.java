package org.example;

import java.sql.Connection;
import java.sql.SQLException;

import java.io.Console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LogDirectoryCreator.createLogDirectoryIfNotExists("logs");
        log.info("Starting TextBookSocial.java");
        Console console = System.console();

        if (console != null) {
            log.info("Decrypting database");
            DatabaseConnector.decryptDatabase();
            try (Connection conn = DatabaseConnector.connect()) {
                if (conn != null) {
                    DatabaseInitializer.initializeDatabase(conn);

                    try {
                        createFirstAdmin(console);
                        User user = null;
                        while (true) {
                            user = authenticate(console);
                            if (user != null) {
                                displayMenu(user, console);
                            } else {
                                console.printf("You have been logged out.\n");
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        try {
                            conn.close();
                        } catch (SQLException e) {
                            console.printf("Error closing the database connection: %s\n", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DatabaseConnector.encryptDatabaseFile(console, log);
            }
        } else {
            System.out.println("Console is not available.\n");
        }
    }

    private static void createFirstAdmin(Console console) {
        if (DatabaseUtils.isUsersTableEmpty()) {
            console.printf("No admin account found. Set up initial admin account.\n");

            console.printf("Enter username: ");
            String username = console.readLine();

            console.printf("Enter email: ");
            String email = console.readLine();

            String newPassword;
            newPassword = new String(console.readPassword("Enter password: "));

            while (!isValidPassword(newPassword)) {
                console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
                newPassword = new String(console.readPassword("Enter password: "));
            }

            String hashedPassword = SecurityUtils.hashPassword(newPassword);

            DatabaseUtils.createUser(username, email, true, hashedPassword);
            console.printf("Admin account created successfully.\n");
        }
    }


    private static void displayMenu(User user, Console console) throws InterruptedException {
        String option;
        do {
            console.printf("\n(P)rint TextBookSocial posts\n" +
                    "(A)dd a new post\n" +
                    "(D)elete a post\n" +
                    "(C)omment on a post\n" +
                    "(RC) Read comments of a post\n" +
                    "(DC) Delete a comment\n" +
                    "(R)ead a post and its comments\n" +
                    "(L)ogout\n" +
                    "(Q)uit\n\n" +
                    "Select an option or M for menu: ");
            option = console.readLine().toUpperCase();

            switch (option) {
                case "P":
                    TextUtils.clearDisplay();
                    DatabaseUtils.fetchPosts();
                    break;
                case "A":
                    TextUtils.clearDisplay();
                    console.printf("What will this post say?: ");
                    DatabaseUtils.createPost(user.getUsername(), console.readLine());
                    break;
                case "D":
                    TextUtils.clearDisplay();
                    deletePost(user, console);
                    break;
                case "C":
                    TextUtils.clearDisplay();
                    commentOnPost(user, console);
                    break;
                case "RC":
                    TextUtils.clearDisplay();
                    readComments(console);
                    break;
                case "DC":
                    TextUtils.clearDisplay();
                    deleteComment(user, console);
                    break;
                case "R":
                    TextUtils.clearDisplay();
                    readPostandComments(console);
                    break;
                case "M":
                    TextUtils.clearDisplay();
                    displayMenu(user, console);
                    break;
                case "L":
                    log.info("User " + user.getId() + " logged out");
                    TextUtils.clearDisplay();
                    user = null;
                    return;
                case "Q":
                    TextUtils.clearDisplay();
                    log.info("Quit TextBook");
                    user = null;
                    console.printf("Goodbye!\n");
                    DatabaseConnector.encryptDatabaseFile(console, log);
                    System.exit(0);
                    break;
                default:
                    console.printf("Invalid option. Please try again.\n");
            }
        } while (true);
    }

    private static void readComments(Console console) {
        DatabaseUtils.fetchPosts();
        System.out.print("What post would you like to see?");
        String postID = console.readLine();
        Post post = DatabaseUtils.fetchPostById(postID);
        if (post != null) {
            DatabaseUtils.fetchComments(postID);
        } else {
            System.out.println("Post not found.");
        }
    }

    private static void readPostandComments(Console console) {
        DatabaseUtils.fetchPosts();
        System.out.print("What post would you like to see?");
        String postID = console.readLine();
        Post post = DatabaseUtils.fetchPostById(postID);
        if (post != null) {
            System.out.println(post);
            DatabaseUtils.fetchComments(postID);
        } else {
            System.out.println("Post not found.");
        }
    }

    private static void commentOnPost(User user, Console console) {
        DatabaseUtils.fetchPosts();
        System.out.print("What post would you like to comment on?");
        String postID = console.readLine();
        System.out.println("What would you like to comment?");
        String comment = console.readLine();
        DatabaseUtils.createComment(postID, user.getUsername(), comment);
    }


    private static User authenticate(Console console) {
        User user = null;
        while (user == null) {
            TextUtils.clearDisplay();
            TextUtils.printLogo();
            console.printf("Do you want to (L)ogin, (S)ign up, or (Q)uit?\n");
            String choice = console.readLine().toUpperCase();
            switch (choice) {
                case "L":
                    user = login(console);
                    break;
                case "S":
                    user = signUp(console);
                    break;
                case "Q":
                    console.printf("Goodbye!\n");
                    DatabaseConnector.encryptDatabaseFile(console, log);
                    System.exit(0);
                    break;
                default:
                    console.printf("Invalid choice. Please try again.\n");
                    break;
            }
        }
        return user;
    }


    private static User login(Console console) {
        User user = null;
        PasswordReset passwordResetHandler = new PasswordReset();

        console.printf("Login to TextBookSocial\n");

        while (user == null) {
            console.printf("Enter username: ");
            String username = console.readLine();

            char[] passwordArray = console.readPassword("Enter password: ");
            String password = new String(passwordArray);

            user = DatabaseUtils.loginUser(username, password);

            if (user != null) {
                TextUtils.clearDisplay();
                console.printf("%s, welcome to TextBookSocial!\n", user.getUsername());
            }

            if (user == null) {
                console.printf("Invalid username or password.\n");
                passwordResetHandler.recordFailedLoginAttempt();

                if (passwordResetHandler.shouldOfferReset()) {
                    console.printf("You have reached the maximum number of login attempts.\n");
                    String emailOrUsername = console.readLine("Enter your email or username to reset your password: ");
                    User userForReset = DatabaseUtils.findUserByEmailOrUsername(emailOrUsername);

                    if (userForReset != null) {
                        passwordReset(userForReset, console);
                    } else {
                        console.printf("No user found with the provided email or username.\n");
                    }
                }
            }
        }
        return user;
    }

    private static User signUp(Console console) {
        console.printf("Create a new account\n");

        console.printf("Enter username: ");
        String username = console.readLine();
        if (DatabaseUtils.usernameExists(username)) {
            console.printf("An account with this username already exists.\n");
            return null;
        }

        console.printf("Enter email: ");
        String email = console.readLine();
        if (DatabaseUtils.emailExists(email)) {
            console.printf("An account with this email already exists.\n");
            return null;
        }

        String newPassword;
        newPassword = new String(console.readPassword("Enter password: "));
        while (!isValidPassword(newPassword)) {
            console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
            newPassword = new String(console.readPassword("Enter password: "));
        }

        String hashedPassword = SecurityUtils.hashPassword(newPassword);

        boolean userCreated = DatabaseUtils.createUser(username, email, false, hashedPassword);
        if (userCreated) {
            console.printf("Account created successfully.\n");
            return new User(username, email, false, hashedPassword);
        } else {
            console.printf("Failed to create the account. Please try again.\n");
            return null;
        }
    }


    private static void deletePost(User user, Console console) throws InterruptedException {
        if(DatabaseUtils.isCommentsTableEmpty()){
            console.printf("No posts exist.");
            Thread.sleep(300);
            TextUtils.clearDisplay();
            return;
        }
        if (!user.isAdmin()) {
            DatabaseUtils.showUsersPosts(user);
        } else {
            DatabaseUtils.fetchPosts();
        }

        console.printf("Enter the ID of the post you want to delete: ");
        String postId = console.readLine();

        DatabaseUtils.deletePost(postId, user);
    }

    private static void deleteComment(User user, Console console) {
        if(DatabaseUtils.isCommentsTableEmpty()){
            console.printf("No posts exist. Can't delete a comment if there is no post!");
            return;
        }
        DatabaseUtils.fetchPosts();
        console.printf("What post would you like to delete the comments from?\n");
        String post = console.readLine();
        console.printf("Comments for %s\n", post);
        DatabaseUtils.fetchComments(post);
        console.printf("What is the ID of the comment you want to delete?\n");
        String id = console.readLine();
        DatabaseUtils.deleteComment(Integer.parseInt(id), user);
        console.printf("Comment successfully deleted\n");
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


    private static void passwordReset(User user, Console console) {
        PasswordReset passwordResetHandler = new PasswordReset();
        System.out.print("Would you like to reset your password? (yes/no): ");
        String response = console.readLine();

        if ("yes".equalsIgnoreCase(response)) {
            String email = user.getEmail();
            passwordResetHandler.initiatePasswordReset(email);
            System.out.println("Please check your email for the reset code.");

            System.out.print("Enter the reset code you received: ");
            String resetCode = console.readLine();

            if (passwordResetHandler.verifyResetCode(resetCode)) {
                String newPassword;
                newPassword = new String(console.readPassword("Enter new password: "));

                while (!isValidPassword(newPassword)) {
                    console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
                    newPassword = new String(console.readPassword("Enter new password: "));
                }

                String hashedPassword = SecurityUtils.hashPassword(newPassword);

                DatabaseUtils.resetUserPassword(user.getUsername(), hashedPassword);

                System.out.println("Your password has been reset successfully. Please log in again.");
                passwordResetHandler.resetAttempts();
            } else {
                System.out.println("Invalid or expired reset code.");
            }
        }
    }
}

