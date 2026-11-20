public class RentalException extends RuntimeException {
    private final int status;

    public RentalException(String message, int status) {
        super(message);
        this.status = status;
    }

    public RentalException(String message) {   
        this(message, 400);
    }

    public int getStatus() { return status; }
}
