package Main;

import java.util.List;

/**
 * FINAL: POJO for Test Templates. ADDED: Constructor for initialization.
 */
public class TestTemplate {

    private int id;
    private String name;
    private String category;
    private String description;
    private List<TestParameter> parameters; // Parameters associated with this template

    // --- ADD THIS CONSTRUCTOR ---
    public TestTemplate(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }
    // --- END ADDED CONSTRUCTOR ---

    // Default constructor (required if you use frameworks or libraries that need it)
    public TestTemplate() {
    }

    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TestParameter> parameters) {
        this.parameters = parameters;
    }
}
