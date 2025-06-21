package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;

public interface InstanceBindable {

    PyObject bindToInstance(Interpreter interpreter, PyObject instance);
}
