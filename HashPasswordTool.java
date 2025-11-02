// HashPasswordTool.java
// Simple CLI utility to print a PBKDF2 password hash using PasswordUtil.
// Usage:
//   javac -cp ".;libs/*" HashPasswordTool.java
//   java -cp ".;libs/*" HashPasswordTool <password>
package Main;
public class HashPasswordTool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java HashPasswordTool <plaintextPassword>");
            return;
        }
        String p = args[0];
        try {
            String hash = PasswordUtil.hashPassword(p);
            System.out.println("PBKDF2 hash for password '" + p + "':");
            System.out.println(hash);
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
