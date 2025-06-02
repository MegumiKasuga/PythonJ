package edu.carole.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IOProvider {

    InputStream createInputStream(String identifier, String mode) throws IOException;

    OutputStream createOutputStream(String identifier, String mode, boolean append) throws IOException;

    boolean exists(String identifier);

    String getProviderName();

    boolean supportsMode(String mode);
}