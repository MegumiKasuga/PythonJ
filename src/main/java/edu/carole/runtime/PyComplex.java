package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.registry.MethodBuilder;

/**
 * Python复数对象的实现
 */
public class PyComplex extends PyObjectWithMethods {
    private final double real;
    private final double imag;
    
    public PyComplex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }
    
    public double getReal() {
        return real;
    }
    
    public double getImag() {
        return imag;
    }
    
    @Override
    public String toString() {
        if (imag == 0) {
            return Double.toString(real);
        } else if (real == 0) {
            if (imag == 1) {
                return "1j";
            } else if (imag == -1) {
                return "-1j";
            }
            return imag + "j";
        } else if (imag < 0) {
            return real + "-" + Math.abs(imag) + "j";
        } else {
            return real + "+" + imag + "j";
        }
    }


    
    @Override
    public String getTypeName() {
        return "complex";
    }
    
    @Override
    public boolean isTruthy() {
        return real != 0 || imag != 0;
    }

    @Override
    protected void registerMethods() {
        // Register methods for complex numbers
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::add));
        methodRegistry.registerMethod("__sub__", MethodBuilder.oneArg(this::sub));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::mul));
        methodRegistry.registerMethod("__truediv__", MethodBuilder.oneArg(this::div));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::eq));
        methodRegistry.registerMethod("real", MethodBuilder.noArgs(this::real));
        methodRegistry.registerMethod("imag", MethodBuilder.noArgs(this::imag));
        methodRegistry.registerMethod("conjugate", MethodBuilder.noArgs(() -> new PyComplex(real, -imag)));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::ne));
        methodRegistry.registerMethod("__bool__", MethodBuilder.noArgs(() -> PyBool.valueOf(isTruthy())));
        methodRegistry.registerMethod("__abs__", MethodBuilder.noArgs(() -> new PyFloat(Math.sqrt(real * real + imag * imag))));
        methodRegistry.registerMethod("__pow__", MethodBuilder.oneArg(this::pow));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(toString())));
    }

    public PyObject add(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            return new PyComplex(
                    this.real + otherComplex.getReal(),
                    this.imag + otherComplex.getImag()
            );
        } else if (other instanceof PyInt) {
            return new PyComplex(
                    this.real + ((PyInt) other).getValue(),
                    this.imag
            );
        } else if (other instanceof PyFloat) {
            return new PyComplex(
                    this.real + ((PyFloat) other).getValue(),
                    this.imag
            );
        } else {
            throw new RuntimeException("unsupported operand type(s) for +: 'complex' and '" + other.getTypeName() + "'");
        }
    }

    public PyObject sub(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            return new PyComplex(
                this.real - otherComplex.getReal(),
                this.imag - otherComplex.getImag()
            );
        } else if (other instanceof PyInt) {
            return new PyComplex(
                this.real - ((PyInt) other).getValue(),
                this.imag
            );
        } else if (other instanceof PyFloat) {
            return new PyComplex(
                this.real - ((PyFloat) other).getValue(),
                this.imag
            );
        } else {
            throw new RuntimeException("unsupported operand type(s) for -: 'complex' and '" + other.getTypeName() + "'");
        }
    }

    public PyObject mul(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            // (a+bi)(c+di) = ac-bd + (ad+bc)i
            double newReal = this.real * otherComplex.getReal() - this.imag * otherComplex.getImag();
            double newImag = this.real * otherComplex.getImag() + this.imag * otherComplex.getReal();
            return new PyComplex(newReal, newImag);
        } else if (other instanceof PyInt) {
            double factor = ((PyInt) other).getValue();
            return new PyComplex(this.real * factor, this.imag * factor);
        } else if (other instanceof PyFloat) {
            double factor = ((PyFloat) other).getValue();
            return new PyComplex(this.real * factor, this.imag * factor);
        } else {
            throw new RuntimeException("unsupported operand type(s) for *: 'complex' and '" + other.getTypeName() + "'");
        }
    }
    
    public PyObject div(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            // (a+bi)/(c+di) = (ac+bd)/(c²+d²) + (bc-ad)/(c²+d²)i
            double c = otherComplex.getReal();
            double d = otherComplex.getImag();
            double denominator = c * c + d * d;
            if (denominator == 0) {
                throw new RuntimeException("complex division by zero");
            }
            double newReal = (this.real * c + this.imag * d) / denominator;
            double newImag = (this.imag * c - this.real * d) / denominator;
            return new PyComplex(newReal, newImag);
        } else if (other instanceof PyInt) {
            double divisor = ((PyInt) other).getValue();
            if (divisor == 0) {
                throw new RuntimeException("complex division by zero");
            }
            return new PyComplex(this.real / divisor, this.imag / divisor);
        } else if (other instanceof PyFloat) {
            double divisor = ((PyFloat) other).getValue();
            if (divisor == 0) {
                throw new RuntimeException("complex division by zero");
            }
            return new PyComplex(this.real / divisor, this.imag / divisor);
        } else {
            throw new RuntimeException("unsupported operand type(s) for /: 'complex' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject eq(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            return PyBool.valueOf(this.real == otherComplex.getReal() && this.imag == otherComplex.getImag());
        } else if (other instanceof PyInt && this.imag == 0) {
            return PyBool.valueOf(this.real == ((PyInt) other).getValue());
        } else if (other instanceof PyFloat && this.imag == 0) {
            return PyBool.valueOf(this.real == ((PyFloat) other).getValue());
        } else {
            return PyBool.FALSE;
        }
    }

    private PyObject real() {
        return new PyFloat(real);
    }

    private PyObject imag() {
        return new PyFloat(imag);
    }

    private PyObject conjugate() {
        return new PyComplex(real, -imag);
    }

    private PyObject ne(PyObject other) {
        return ((PyBool)eq(other)).reverse();
    }

    private PyObject bool() {
        return new PyBuiltinFunction("__bool__", (args, kwargs, inter) -> {
            if (args.size() != 0) {
                throw new RuntimeException("__bool__() takes no arguments (" + args.size() + " given)");
            }
            return PyBool.valueOf(real != 0 || imag != 0);
        });
    }

    private PyObject abs() {
        double magnitude = Math.sqrt(real * real + imag * imag);
        return new PyFloat(magnitude);
    }

    private PyObject pow(PyObject other) {
        if (other instanceof PyComplex otherComplex) {
            double r = Math.sqrt(this.real * this.real + this.imag * this.imag);
            double theta = Math.atan2(this.imag, this.real);

            double c = otherComplex.getReal();
            double d = otherComplex.getImag();

            double logR = Math.log(r);
            double realPart = c * logR - d * theta;
            double imagPart = c * theta + d * logR;

            double expReal = Math.exp(realPart);
            double newReal = expReal * Math.cos(imagPart);
            double newImag = expReal * Math.sin(imagPart);

            return new PyComplex(newReal, newImag);
        } else if (other instanceof PyInt) {
            long power = ((PyInt) other).getValue();
            if (power == 0) {
                return new PyComplex(1, 0);
            }
            if (power == 1) {
                return new PyComplex(this.real, this.imag);
            }

            PyComplex result = new PyComplex(1, 0);
            PyComplex base = new PyComplex(this.real, this.imag);

            boolean negative = power < 0;
            power = Math.abs(power);

            while (power > 0) {
                if (power % 2 == 1) {
                    double newReal = result.real * base.real - result.imag * base.imag;
                    double newImag = result.real * base.imag + result.imag * base.real;
                    result = new PyComplex(newReal, newImag);
                }
                double newReal = base.real * base.real - base.imag * base.imag;
                double newImag = 2 * base.real * base.imag;
                base = new PyComplex(newReal, newImag);
                power /= 2;
            }

            if (negative) {
                double denominator = result.real * result.real + result.imag * result.imag;
                return new PyComplex(result.real / denominator, -result.imag / denominator);
            }

            return result;
        } else if (other instanceof PyFloat) {
            double power = ((PyFloat) other).getValue();
            double r = Math.sqrt(this.real * this.real + this.imag * this.imag);
            double theta = Math.atan2(this.imag, this.real);

            double newR = Math.pow(r, power);
            double newTheta = theta * power;

            return new PyComplex(newR * Math.cos(newTheta), newR * Math.sin(newTheta));
        } else {
            throw new RuntimeException("unsupported operand type(s) for ** or pow(): 'complex' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject repr() {
        return new PyString(toString());
    }

    private PyObject str() {
        return new PyString(toString());
    }
}
