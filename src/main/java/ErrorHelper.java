public class ErrorHelper {
    public static ErrorHelper errorHelperInstance = new ErrorHelper();
    private boolean hasErrors = false;

    public boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

}
