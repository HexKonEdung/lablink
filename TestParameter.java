// TestParameter.java
package Main;

public class TestParameter {

    // keep legacy public field for compatibility with some UI code
    public int parameterId;
    public int testId;
    public String parameterName;
    public String resultValue;
    public String normalRange;
    public String units;
    public String interpretation;
    public String remarks;

    // new optional field used by template code
    public String criticalValues;

    // compatibility getters/setters used by TemplateDAO
    public int getId() {
        return parameterId;
    }

    public void setId(int id) {
        this.parameterId = id;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String name) {
        this.parameterName = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String u) {
        this.units = u;
    }

    // TemplateDAO expects "reference range"
    public String getReferenceRange() {
        return normalRange;
    }

    public void setReferenceRange(String r) {
        this.normalRange = r;
    }

    public String getCriticalValues() {
        return criticalValues;
    }

    public void setCriticalValues(String cv) {
        this.criticalValues = cv;
    }
}
