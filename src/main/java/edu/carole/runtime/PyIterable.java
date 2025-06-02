package edu.carole.runtime;

/**
 * Python迭代器接口，对应Python的__iter__和__next__方法
 */
public interface PyIterable {
    /**
     * 返回一个迭代器对象
     */
    PyObject __iter__();
}
