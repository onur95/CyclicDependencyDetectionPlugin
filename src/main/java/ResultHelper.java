import java.util.HashMap;

final class ResultHelper {
    private HashMap<String, String> psiClasses;
    private HashMap<String, String> psiInnerClasses;

    public ResultHelper(HashMap<String, String> psiClasses, HashMap<String, String> psiInnerClasses) {
        this.psiClasses = psiClasses;
        this.psiInnerClasses = psiInnerClasses;
    }

    public HashMap<String, String> getPsiClasses() {
        return psiClasses;
    }

    public void setPsiClasses(HashMap<String, String> psiClasses) {
        this.psiClasses = psiClasses;
    }

    public HashMap<String, String> getPsiInnerClasses() {
        return psiInnerClasses;
    }

    public void setPsiInnerClasses(HashMap<String, String> psiInnerClasses) {
        this.psiInnerClasses = psiInnerClasses;
    }
}
