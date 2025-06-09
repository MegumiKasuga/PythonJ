from abc import ABC, abstractmethod

a = 10
class A(ABC):

    @abstractmethod
    def test(self):
        pass


class B(A):
    a = 10

    def test(self):
        global a
        a *= 2
        return a

test1 = A()
test2 = B()
print(a, test2.test(), test2.a, a) # Output: 10 20 100 100