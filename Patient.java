package Main;

/**
 * Patient model with status enum. Added registeredById (account_id) and
 * registeredBy (display name).
 */
public class Patient {

    public int patientId;
    public String name;
    public String sex;
    public String dateOfBirth;
    public String contactNumber;
    public String email;
    public String address;
    public String bloodType;
    public String allergies;
    public String existingConditions;
    public String emergencyContact;
    public String dateRegistered;
    // registeredById is the accounts.account_id (if available)
    public int registeredById;
    // registeredBy is a human-readable name (username or full name), used for display
    public String registeredBy;
    public String profilePicture;

    public Patient() {
    }

    public Patient(int patientId, String name, String dateOfBirth, String sex) {
        this.patientId = patientId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
    }
}
