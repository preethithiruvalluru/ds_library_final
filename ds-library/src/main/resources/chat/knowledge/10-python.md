category: subject
topic: Python

<!--
  HOW TO ADD YOUR OWN ANSWER (works the same in every file in this folder)
  1. Start a new entry with  "## " and the main question.
  2. Right under it, optional lines:  keywords: a, b, c   |   questions: one way | another way   |   starter: yes
  3. Then write the answer in simple Markdown (lists, `code`, **bold**, and ``` code blocks).
  Keywords matter most: put the important words students will use (and common short forms or spelling variants).
-->

## What is Python?
keywords: python, python language, programming language, python for data science
questions: why use python for data science | is python good for data science | what is python used for
starter: yes

**Python** is a popular, easy-to-read programming language. It is the most common choice for data science because it has powerful libraries:

- **NumPy** for fast number crunching
- **Pandas** for tables of data
- **Matplotlib / Seaborn** for charts
- **scikit-learn, TensorFlow, PyTorch** for machine learning

```python
print("Hello, Data Science!")
```

## What is the difference between a list and a tuple in Python?
keywords: list, tuple, list vs tuple, mutable list, immutable tuple, square brackets, round brackets
questions: difference between list and tuple | list or tuple which one | can i change a tuple

Both store an ordered collection of items. The key difference:

- **List** `[1, 2, 3]` is **mutable**: you can add, remove or change items.
- **Tuple** `(1, 2, 3)` is **immutable**: it cannot be changed after it is created. It is slightly faster and can be used as a dictionary key.

```python
nums = [1, 2, 3]
nums[0] = 10          # works
point = (4, 5)
# point[0] = 1        # TypeError: tuples cannot be changed
```

## What is a dictionary in Python?
keywords: dictionary, dict, key value, key-value pair, curly braces, get method, items
questions: how does a dict work | how to use a dictionary | how to access dictionary values

A **dictionary** stores data as **key: value** pairs and lets you look a value up by its key very quickly.

```python
student = {"name": "Asha", "marks": 92}
print(student["name"])          # Asha
print(student.get("age", 18))   # 18  (a safe default when the key is missing)
student["marks"] = 95           # update
for key, value in student.items():
    print(key, value)
```

## What is a set in Python?
keywords: set, sets, unique values, remove duplicates, union, intersection
questions: how to remove duplicates from a list | what is a set

A **set** is an unordered collection of **unique** items. It is handy for removing duplicates and for set maths.

```python
a = {1, 2, 3, 3}        # {1, 2, 3}
b = {3, 4}
print(a | b)            # union        {1, 2, 3, 4}
print(a & b)            # intersection {3}
unique = list(set([1, 1, 2, 2, 3]))
```

## What is the difference between mutable and immutable objects?
keywords: mutable, immutable, mutability, changeable, unchangeable
questions: which python types are mutable | what does immutable mean

- **Mutable** objects can be changed after creation: `list`, `dict`, `set`.
- **Immutable** objects cannot: `int`, `float`, `str`, `tuple`, `bool`.

Changing an immutable value really creates a new object. Mutable objects can be changed "in place", which is why passing a list to a function and changing it inside affects the original.

## What is a list comprehension?
keywords: list comprehension, comprehension, one line loop, shortcut for loop
questions: how to create a list in one line | what is comprehension in python

A **list comprehension** builds a list in a single readable line.

```python
squares = [x * x for x in range(1, 6)]            # [1, 4, 9, 16, 25]
evens = [x for x in range(10) if x % 2 == 0]       # [0, 2, 4, 6, 8]
```

It is the short form of a `for` loop that appends to a list.

## How do I write a function in Python?
keywords: function, def, define function, return, parameter, argument, default argument, call a function
questions: how to create a function | how to define a function in python | what is return

Use `def`, give the function a name, list its inputs (parameters), and use `return` to give back a result.

```python
def area(length, width=1):      # width has a default value
    return length * width

print(area(5, 3))   # 15
print(area(5))      # 5
```

## What is a lambda function?
keywords: lambda, anonymous function, lambda expression, inline function
questions: what is lambda in python | how to use lambda with map filter sorted

A **lambda** is a small, nameless one-line function: `lambda inputs: expression`.

```python
double = lambda x: x * 2
print(double(4))                              # 8
people = [("Ravi", 21), ("Asha", 19)]
people.sort(key=lambda p: p[1])               # sort by age
```

## How do loops work in Python?
keywords: loop, for loop, while loop, range, enumerate, iterate, iteration, break, continue
questions: how to use for loop | how to write while loop | how to loop through a list

- **for** repeats over a sequence. **while** repeats as long as a condition is true.
- `range(n)` gives numbers 0 to n-1. `enumerate()` gives index and value together.
- `break` stops the loop, `continue` skips to the next round.

```python
for i in range(3):
    print(i)                       # 0 1 2
for index, name in enumerate(["A", "B"]):
    print(index, name)
count = 0
while count < 3:
    count += 1
```

## How do I handle errors with try and except?
keywords: exception, exception handling, try, except, finally, error handling, raise, catch error
questions: how to handle errors in python | what is try except | how to stop program from crashing

Wrap risky code in `try`, and say what to do in `except` if it fails.

```python
try:
    number = int(input("Enter a number: "))
    print(10 / number)
except ValueError:
    print("That was not a number.")
except ZeroDivisionError:
    print("You cannot divide by zero.")
finally:
    print("Done.")                 # always runs
```

## How do I read and write a file in Python?
keywords: file handling, open file, read file, write file, with open, text file, append file
questions: how to open a file in python | how to save text to a file | how to read a text file

Use `with open(...)`: it closes the file for you.

```python
with open("notes.txt", "w") as f:       # "w" write, "a" append, "r" read
    f.write("Hello\n")

with open("notes.txt") as f:
    for line in f:
        print(line.strip())
```

For tables of data, use `pandas.read_csv("file.csv")` instead.

## What is a class and an object in Python?
keywords: class, object, oop, object oriented programming, init, self, constructor, inheritance, method, attribute
questions: what is oop | explain class and object | what is self in python

**OOP** groups data and behaviour together. A **class** is a blueprint; an **object** is one thing made from it. `__init__` is the constructor and `self` refers to the current object.

```python
class Student:
    def __init__(self, name):
        self.name = name
    def greet(self):
        return "Hi, I am " + self.name

s = Student("Asha")
print(s.greet())
```

**Inheritance:** `class Topper(Student):` reuses everything from `Student`.

## What are *args and **kwargs?
keywords: args, kwargs, star args, double star, variable arguments, arbitrary arguments
questions: what is *args | what is **kwargs | how to pass many arguments

They let a function accept any number of arguments.

- `*args` collects extra **positional** arguments into a tuple.
- `**kwargs` collects extra **keyword** arguments into a dictionary.

```python
def show(*args, **kwargs):
    print(args)      # (1, 2)
    print(kwargs)    # {'a': 3}
show(1, 2, a=3)
```

## What is pip and how do I install a package?
keywords: pip, install package, pip install, library installation, install library, requirements
questions: how to install pandas | how to install a library in python | how to use pip

**pip** is Python's package installer. Run this in a terminal (not inside Python):

```
pip install pandas numpy matplotlib
pip list                    # see what is installed
pip install -r requirements.txt
```

In Jupyter or Colab, put `!` or `%` first: `!pip install seaborn`.

## What is a virtual environment?
keywords: virtual environment, venv, virtualenv, conda environment, environment, isolated environment
questions: how to create a virtual environment | why use a virtual environment

A **virtual environment** is a separate, clean copy of Python for one project, so different projects can use different library versions without clashing.

```
python -m venv myenv
myenv\Scripts\activate          (Windows)
source myenv/bin/activate       (Mac / Linux)
```

With Anaconda: `conda create -n myenv python=3.11` then `conda activate myenv`.

## How do I take input from the user in Python?
keywords: input, user input, input function, read input, keyboard input, int input, convert input
questions: how to get input in python | how to read a number from the user

`input()` always returns **text**, so convert it when you need a number.

```python
name = input("Your name: ")
age = int(input("Your age: "))
print(f"{name} is {age} years old")
```

## What is the difference between == and is in Python?
keywords: equals equals, is operator, identity, equality, double equals, == vs is
questions: difference between == and is | when to use is operator in python

- `==` checks whether two values are **equal**.
- `is` checks whether two names point to the **same object** in memory.

```python
a = [1, 2]
b = [1, 2]
print(a == b)   # True  (same values)
print(a is b)   # False (different objects)
```

Use `is` mainly for `None`: `if x is None:`.

## How do I fix ModuleNotFoundError or ImportError?
keywords: modulenotfounderror, importerror, no module named, module not found, import error, cannot import
questions: no module named pandas | why import is not working | module not found error

It means Python cannot find the library. Check these in order:

1. **Install it:** `pip install pandas` (use the exact name from the message).
2. **Right environment?** Install it in the same Python/environment that runs your code (VS Code: pick the interpreter; Jupyter: use `!pip install`).
3. **File name clash:** do not name your own file `pandas.py` or `random.py`.
4. **Restart** the notebook kernel after installing.

## What is an IndentationError and how do I fix it?
keywords: indentation, indentationerror, indent, spaces, tabs, unexpected indent, expected an indented block
questions: why indentation error comes | unexpected indent error

Python uses **indentation** (spaces at the start of a line) to mark blocks of code. Lines inside `if`, `for`, `def`, `class` must be indented the same amount.

- Use **4 spaces** per level and never mix tabs and spaces.
- "expected an indented block" means you forgot to indent the line after a colon `:`.

## How do I print formatted text with f-strings?
keywords: f string, fstring, format string, string formatting, print variable, format
questions: how to print a variable in a sentence | how to format a number to 2 decimals

Put `f` before the quotes and write variables inside `{}`.

```python
name, mark = "Asha", 91.456
print(f"{name} scored {mark:.2f}")     # Asha scored 91.46
```

## What is slicing in Python?
keywords: slicing, slice, colon, substring, reverse string, negative index, start stop step
questions: how to slice a list | how to reverse a string in python | what is reverse slicing

`sequence[start:stop:step]` takes a part of a list, tuple or string. The stop position is **not** included.

```python
x = [10, 20, 30, 40, 50]
print(x[1:4])      # [20, 30, 40]
print(x[:2])       # [10, 20]
print(x[-1])       # 50  (last item)
print(x[::-1])     # reversed
print("python"[::-1])   # nohtyp
```
