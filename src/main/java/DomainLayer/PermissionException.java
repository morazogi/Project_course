/* src/main/java/DomainLayer/PermissionException.java */
package DomainLayer;

/** Thrown when the caller lacks a valid token for the requested action. */
public class PermissionException extends RuntimeException {
    public PermissionException(String msg) { super(msg); }
}
