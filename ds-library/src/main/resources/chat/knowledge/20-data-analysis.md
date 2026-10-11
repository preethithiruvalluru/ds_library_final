category: subject
topic: NumPy, Pandas and charts

## What is NumPy?
keywords: numpy, numerical python, ndarray, array library, numpy array
questions: why use numpy | what is numpy used for | how to install numpy

**NumPy** is the core Python library for fast number crunching. Its main object is the **array** (`ndarray`): a grid of numbers of one type that can be added, multiplied and summarised in one go, without writing loops.

```python
import numpy as np
a = np.array([1, 2, 3])
print(a * 2)          # [2 4 6]
print(a.mean())       # 2.0
```

## What is the difference between a NumPy array and a Python list?
keywords: numpy array vs list, array vs list, ndarray vs list, why numpy is faster, vectorization, vectorised
questions: why is numpy faster than list | difference between array and list in numpy

- A **NumPy array** holds items of **one type** in a compact block of memory, so maths on it is very fast, and operations apply to every element at once (**vectorisation**): `a * 2`.
- A **Python list** can mix types and is flexible, but `[1, 2] * 2` repeats the list instead of doing maths.

Use arrays for numbers and lists for general collections.

## How do I use shape, reshape and axis in NumPy?
keywords: shape, reshape, ndim, axis, dimension, rows and columns, axis 0, axis 1, flatten
questions: what is axis in numpy | how to reshape an array | what is shape

- `a.shape` gives the size of each dimension, for example `(3, 4)` = 3 rows, 4 columns.
- `a.reshape(2, 6)` rearranges the same data into a new shape.
- **axis=0** works **down the rows** (one result per column). **axis=1** works **across the columns** (one result per row).

```python
a = np.arange(6).reshape(2, 3)     # [[0 1 2], [3 4 5]]
print(a.sum(axis=0))               # [3 5 7]  (column sums)
print(a.sum(axis=1))               # [ 3 12]  (row sums)
```

## What is broadcasting in NumPy?
keywords: broadcasting, broadcast, different shapes, shape mismatch
questions: how numpy adds arrays of different shapes | what does broadcasting mean

**Broadcasting** lets NumPy do maths between arrays of different shapes by stretching the smaller one automatically (the shapes must be compatible: equal, or one of them is 1).

```python
a = np.array([[1, 2, 3], [4, 5, 6]])   # shape (2, 3)
print(a + np.array([10, 20, 30]))      # adds to every row
print(a * 2)                            # a single number is broadcast too
```

## What is Pandas?
keywords: pandas, pandas library, data analysis library, dataframe library, tabular data
questions: why use pandas | what is pandas used for | how to install pandas
starter: yes

**Pandas** is the Python library for working with **tables of data** (like Excel or SQL tables). It gives you:

- **DataFrame**: a table with rows and named columns
- **Series**: a single column
- Tools to read files, clean data, filter, group, join and summarise

```python
import pandas as pd
df = pd.read_csv("marks.csv")
print(df.head())
```

## What is a DataFrame and what is a Series?
keywords: dataframe, series, pandas dataframe, pandas series, difference between dataframe and series, create dataframe
questions: what is a dataframe | how to create a dataframe | dataframe vs series
starter: yes

- A **DataFrame** is a 2-D table: rows plus named columns, where each column can have its own type.
- A **Series** is one column (1-D) with an index.

```python
df = pd.DataFrame({"name": ["Asha", "Ravi"], "marks": [92, 85]})
print(df["marks"])            # a Series
print(df.shape)               # (2, 2)
```

## How do I read a CSV or Excel file with Pandas?
keywords: read csv, read_csv, import csv, load csv, open csv, read excel, read_excel, load dataset, load data
questions: how to load a dataset in pandas | how to read a csv file | how to import data into python

```python
df = pd.read_csv("data.csv")                    # CSV
df = pd.read_excel("data.xlsx")                 # Excel (needs: pip install openpyxl)
df = pd.read_csv("data.csv", sep=";", encoding="utf-8")
```

If the file is not found, give the full path or put the file in the same folder as your notebook. In Colab, upload it first (left sidebar, Files).

## How do I look at my data quickly (head, info, describe)?
keywords: head, tail, info, describe, shape, columns, dtypes, summary, explore data, first rows, data overview
questions: how to see first 5 rows | how to get a summary of a dataframe | how to check data types

```python
df.head()          # first 5 rows (df.tail() = last 5)
df.shape           # (rows, columns)
df.info()          # columns, types, non-null counts
df.describe()      # count, mean, std, min, quartiles, max
df.columns         # column names
df.dtypes          # type of each column
```

## How do I handle missing values in Pandas?
keywords: missing values, null values, nan, isnull, dropna, fillna, missing data, empty cells, impute, imputation
questions: how to remove null values | how to fill missing values | how to check for nan

```python
df.isnull().sum()                          # missing values per column
df = df.dropna()                           # remove rows with any missing value
df["age"] = df["age"].fillna(df["age"].mean())   # fill with the mean
df["city"] = df["city"].fillna("Unknown")
```

Choose carefully: dropping loses data; filling (imputing) with mean/median/mode keeps rows.

## What is the difference between loc and iloc?
keywords: loc, iloc, select rows, select columns, label based, position based, indexing, df.loc, df.iloc
questions: difference between loc and iloc | how to select rows in pandas | how to select a column

- **`loc`** selects by **labels** (index names and column names), and includes the end label.
- **`iloc`** selects by **integer position** (0, 1, 2...), and excludes the end position.

```python
df.loc[0, "name"]            # row labelled 0, column "name"
df.loc[:, ["name", "marks"]] # all rows, two columns
df.iloc[0:3, 0:2]            # first 3 rows, first 2 columns
```

## How do I filter rows in a DataFrame?
keywords: filter, filtering, boolean indexing, condition, query, select rows where, conditional selection
questions: how to filter a dataframe | how to select rows with a condition | how to use multiple conditions

Put a condition inside square brackets. Combine conditions with `&` (and), `|` (or), and wrap each in brackets.

```python
df[df["marks"] > 80]
df[(df["marks"] > 80) & (df["city"] == "Tirupati")]
df[df["city"].isin(["Chennai", "Hyderabad"])]
df.query("marks > 80 and age < 21")
```

## How do I use groupby in Pandas?
keywords: groupby, group by, aggregate, aggregation, agg, split apply combine, summarise by group
questions: how to group data in pandas | how to find average per category | how to count by group

`groupby` splits the data into groups, applies a calculation to each, and combines the result.

```python
df.groupby("department")["marks"].mean()
df.groupby("department").agg({"marks": ["mean", "max"], "name": "count"})
df["city"].value_counts()           # quick count per value
```

## How do I merge, join or concat DataFrames?
keywords: merge, join, concat, concatenate, combine dataframes, append, pandas merge, pandas join, inner join pandas
questions: how to join two dataframes | how to combine two tables in pandas | difference between merge and concat

- **`merge`**: join two tables on a common column (like SQL JOIN).
- **`concat`**: stack tables on top of each other (`axis=0`) or side by side (`axis=1`).

```python
result = pd.merge(students, marks, on="roll_no", how="left")   # how: inner, left, right, outer
stacked = pd.concat([df1, df2], ignore_index=True)
```

## How do I apply a function to a column?
keywords: apply, map, applymap, transform column, custom function, new column, add column, create column
questions: how to add a new column | how to use apply in pandas | how to change values in a column

```python
df["grade"] = df["marks"].apply(lambda m: "A" if m >= 90 else "B")
df["percent"] = df["marks"] / 100 * 100          # vectorised maths is faster than apply
df["city"] = df["city"].str.upper()              # string methods
df["gender"] = df["gender"].map({"M": "Male", "F": "Female"})
```

## How do I sort values and remove duplicates?
keywords: sort, sort_values, sorting, ascending, descending, duplicates, drop_duplicates, remove duplicate rows, rename column
questions: how to sort a dataframe | how to remove duplicate rows | how to rename a column

```python
df.sort_values("marks", ascending=False)
df.sort_values(["department", "marks"])
df = df.drop_duplicates()
df = df.rename(columns={"mark": "marks"})
df = df.drop(columns=["unused"])
```

## How do I change data types and work with dates in Pandas?
keywords: astype, convert type, data type, to_datetime, datetime, date column, change dtype, string to number, to_numeric
questions: how to convert a column to integer | how to convert string to date | how to change data type

```python
df["age"] = df["age"].astype(int)
df["price"] = pd.to_numeric(df["price"], errors="coerce")   # bad values become NaN
df["date"] = pd.to_datetime(df["date"])
df["year"] = df["date"].dt.year
```

## How do I make a basic chart with Matplotlib?
keywords: matplotlib, plot, plt, line plot, bar chart, histogram, pyplot, draw graph, plotting, subplot, figure
questions: how to plot a graph in python | how to draw a bar chart | how to make a histogram | how to install matplotlib

```python
import matplotlib.pyplot as plt

plt.plot([1, 2, 3], [2, 4, 9])              # line chart
plt.bar(["A", "B", "C"], [5, 8, 3])         # bar chart
plt.hist(df["marks"], bins=10)              # histogram
plt.title("Marks")
plt.xlabel("Student")
plt.ylabel("Marks")
plt.show()
```

Use `fig, axes = plt.subplots(1, 2)` to put two charts side by side.

## What is the difference between Matplotlib, Seaborn and Plotly?
keywords: seaborn, plotly, matplotlib vs seaborn, visualization library, plotting library, interactive chart, difference
questions: which library is best for visualization | seaborn vs matplotlib | what is seaborn | what is plotly

- **Matplotlib**: the base plotting library. Very flexible, takes more code.
- **Seaborn**: built on Matplotlib. Nicer default style and one-line statistical charts (`sns.histplot`, `sns.boxplot`, `sns.heatmap`).
- **Plotly**: **interactive** charts (hover, zoom) that work well in web pages and dashboards.

```python
import seaborn as sns
sns.histplot(df["marks"], kde=True)
sns.heatmap(df.corr(numeric_only=True), annot=True)
```

## Which chart should I use?
keywords: which chart, types of charts, types of plots, scatter plot, box plot, pie chart, choose chart, chart type, heatmap
questions: when to use a bar chart | when to use a scatter plot | what is a box plot | which graph to use

- **Line chart**: change over time.
- **Bar chart**: compare categories.
- **Histogram**: how one numeric value is spread out.
- **Scatter plot**: relationship between two numeric values.
- **Box plot**: median, spread and outliers (good to compare groups).
- **Heatmap**: a table of numbers shown as colours, e.g. a correlation matrix.
- **Pie chart**: parts of a whole, only for a few categories.

## What is EDA (exploratory data analysis)?
keywords: eda, exploratory data analysis, explore data, data exploration, understand data
questions: how to do eda | steps of exploratory data analysis

**EDA** means exploring a dataset *before* modelling, to understand it. Typical steps:

1. Look at it: `head()`, `info()`, `describe()`, `shape`.
2. Check **missing values** and **duplicates**.
3. Check each column's **distribution** (histograms, box plots) and **outliers**.
4. Look at **relationships** (scatter plots, correlation heatmap, `groupby`).
5. Write down what you learned and which cleaning is needed.

## What is data cleaning?
keywords: data cleaning, cleaning, data preprocessing, preprocessing, data wrangling, clean data, messy data, data preparation
questions: how to clean data | what is data preprocessing | why is data cleaning important

**Data cleaning** (preprocessing) means fixing a dataset so it is ready for analysis. Common jobs:

- Handle **missing values** (fill or drop)
- Remove **duplicates**
- Fix **data types** and dates
- Fix inconsistent text ("Male", "male", "M")
- Deal with **outliers**
- **Scale** numbers and **encode** categories (for machine learning)

Data scientists often spend most of their time on this step, because models are only as good as their data.
