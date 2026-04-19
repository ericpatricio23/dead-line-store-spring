package exception;

public class VendaNotFoundException extends RuntimeException {
    public VendaNotFoundException(String message) {
        super(message);
    }
}
