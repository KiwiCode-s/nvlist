package nl.weeaboo.vn.impl.core;

/**
 * Invalid version.
 */
public class UnsupportedVersionException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnsupportedVersionException(String message) {
        super(message);
    }

}