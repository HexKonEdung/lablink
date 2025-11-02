// Session.java
package Main;
public class Session {

    private static Account currentUser = null;

    public static void setCurrentUser(Account a) {
        currentUser = a;
    }

    public static Account getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}
