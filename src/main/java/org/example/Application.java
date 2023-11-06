package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);


    public static void main(String[] args) {
        log.info("Starting TextBookSocial.java");
        Connection conn = DatabaseConnector.connect();
        Console console = System.console();

        if (conn != null && console != null) {
            DatabaseInitializer.initializeDatabase();

            try {
                // first run makes an admin
                createFirstAdmin(console);
                User user = null;
                while (true) {
                    // gets current user after login/signup
                    user = authenticate(console);
                    // user gotta be logged in
                    if (user != null) {
                        displayMenu(user, console);
                    } else {
                        // User chose to log out
                        console.printf("You have been logged out.\n");
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // close connection to free resources
                try {
                    conn.close();
                } catch (SQLException e) {
                    console.printf("Error closing the database connection: %s\n", e.getMessage());
                }
            }
        } else {
            assert console != null;
            console.printf("Failed to establish a database connection or console is not available.\n");
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

            // Hash the password
            String hashedPassword = SecurityUtils.hashPassword(newPassword);

            // Save the admin user in the database
            DatabaseUtils.createUser(username, email, true, hashedPassword);
            console.printf("Admin account created successfully.\n");
        }
    }


    private static void displayMenu(User user, Console console) {
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
                    DatabaseUtils.fetchPosts();
                    break;
                case "A":
                    console.printf("What will this post say?: ");
                    DatabaseUtils.createPost(user.getUsername(), console.readLine());
                    break;
                case "D":
                    deletePost(user, console);
                    break;
                case "C":
                    commentOnPost(user, console);
                    break;
                case "RC":
                    readComments(console);
                    break;
                case "DC":
                    deleteComment(user, console);
                    break;
                case "R":
                    readPostandComments(console);
                    break;
                case "M":
                    displayMenu(user, console);
                    break;
                case "L":
                    user = null;
                    return;
                case "Q":
                    user = null;
                    console.printf("Goodbye!\n");
                    break;
                default:
                    console.printf("Invalid option. Please try again.\n");
            }
        } while (!option.equals("Q"));
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
            console.printf("Welcome to TextBookSocial!\n");
            console.printf("Do you want to (L)ogin or (S)ign up?\n");
            String choice = console.readLine().toUpperCase();
            switch (choice) {
                case "L":
                    user = login(console);
                    break;
                case "S":
                    user = signUp(console);
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
                console.printf("%s, welcome to TextBookSocial!\n", user.getUsername());
            }

            if (user == null) {
                console.printf("Invalid username or password.\n");
                passwordResetHandler.recordFailedLoginAttempt();

                if (passwordResetHandler.shouldOfferReset()) {
                    passwordReset(user, console);
                }
            }
        }
        return user;
    }


    private static User signUp(Console console) {
        console.printf("Create a new account\n");

        console.printf("Enter username: ");
        String username = console.readLine();

        console.printf("Enter email: ");
        String email = console.readLine();

        // Check if the email is already associated with an account
        if (DatabaseUtils.emailExists(email)) {
            console.printf("An account with this email already exists.\n");
            passwordReset(DatabaseUtils.getUserByUsername(username), console); // Offer to reset the password
            return null; // Return null as no new account is created
        }

        char[] passwordArray = console.readPassword("Enter password: ");
        String password = new String(passwordArray);

        while (!isValidPassword(password)) {
            console.printf("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).\n");
            passwordArray = console.readPassword("Enter password: ");
            password = new String(passwordArray);
        }

        // Hash the password
        String hashedPassword = SecurityUtils.hashPassword(password);

        boolean userCreated = DatabaseUtils.createUser(username, email, false, hashedPassword);
        if (userCreated) {
            console.printf("Account created successfully.\n");
            // Automatically login the new user
            return new User(username, email, false, hashedPassword);
        } else {
            console.printf("Failed to create the account. Please try again.\n");
            return null;
        }
    }


    private static void deletePost(User user, Console console) {
        // Show only the posts of the logged-in user
        if (!user.isAdmin()) {
            DatabaseUtils.showUsersPosts(user);
        } else {
            DatabaseUtils.fetchPosts();
        }

        console.printf("Enter the ID of the post you want to delete: ");
        String postId = console.readLine();

        // No need to check if the user is the owner, as they can only see their own posts
        DatabaseUtils.deletePost(postId, user);
        console.printf("Post deleted successfully.\n");
    }

    private static void deleteComment(User user, Console console) {
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
            System.out.print("Enter your email associated with this account: ");
            String email = console.readLine();
            passwordResetHandler.initiatePasswordReset(email);
            System.out.println("Please check your email for the reset code.");

            System.out.print("Enter the reset code you received: ");
            String resetCode = console.readLine();

            if (passwordResetHandler.verifyResetCode(resetCode)) {
                System.out.print("Enter your new password: ");
                String newPassword;
                newPassword = console.readLine();

                while (!isValidPassword(newPassword)) {
                    System.out.println("Password must be at least 10 characters in length and contain at least one uppercase letter, one number, and one special character (!&$@#*).");
                    newPassword = console.readLine();
                }

                // Hash the new password
                String hashedPassword = SecurityUtils.hashPassword(newPassword);

                // Update the password in the database
                DatabaseUtils.resetUserPassword(user.getUsername(), hashedPassword);

                System.out.println("Your password has been reset successfully. Please log in again.");
            } else {
                System.out.println("Invalid or expired reset code.");
            }
        }
    }
}

