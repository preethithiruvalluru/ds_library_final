category: subject
topic: SQL and databases

## What is SQL?
keywords: sql, structured query language, query language, database language, what is sql
questions: why learn sql | what is sql used for | is sql a programming language

**SQL** (Structured Query Language) is the standard language for working with data stored in **relational databases** (tables made of rows and columns). With it you can ask questions of the data (`SELECT`), add (`INSERT`), change (`UPDATE`) and remove (`DELETE`) rows, and create tables (`CREATE TABLE`). Databases such as MySQL, PostgreSQL, SQLite and SQL Server all use it.

## How do I write a basic SELECT query?
keywords: select, select query, from, where, order by, limit, distinct, query, sql query, basic query, select statement
questions: how to write a sql query | how to get data from a table | select syntax | how to filter in sql
starter: no

```sql
SELECT name, marks            -- the columns you want (use * for all)
FROM students                 -- the table
WHERE marks >= 80             -- filter rows
ORDER BY marks DESC           -- sort
LIMIT 5;                      -- only the first 5 rows
```

`SELECT DISTINCT city FROM students;` gives each city once. Useful filters: `AND`, `OR`, `IN (...)`, `BETWEEN a AND b`, `LIKE 'A%'`, `IS NULL`.

## What are the types of SQL joins?
keywords: join, joins, inner join, left join, right join, full join, outer join, left outer join, sql join, join tables, join two tables, cross join
questions: explain sql joins | difference between inner join and left join | how to join two tables in sql
starter: yes

A **JOIN** combines rows from two tables using a matching column.

- **INNER JOIN**: only rows that match in **both** tables.
- **LEFT JOIN**: **all** rows from the left table, plus matches from the right (no match gives NULL).
- **RIGHT JOIN**: all rows from the right table, plus matches from the left.
- **FULL (OUTER) JOIN**: all rows from both tables.

```sql
SELECT s.name, d.dept_name
FROM students s
INNER JOIN departments d ON s.dept_id = d.id;
```

## How do GROUP BY and aggregate functions work?
keywords: group by, groupby, aggregate, aggregate function, count, sum, avg, min, max, average, total
questions: how to count rows per group in sql | what is group by | how to find average in sql

**Aggregate functions** (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`) turn many rows into one value. **GROUP BY** makes one result per group.

```sql
SELECT dept_id, COUNT(*) AS students, AVG(marks) AS average
FROM students
GROUP BY dept_id
ORDER BY average DESC;
```

Every selected column must either be in `GROUP BY` or inside an aggregate function.

## What is the difference between WHERE and HAVING?
keywords: where, having, where vs having, filter groups, filter after group by
questions: difference between where and having | when to use having

- **WHERE** filters **rows before** grouping.
- **HAVING** filters **groups after** `GROUP BY` (it can use aggregate functions).

```sql
SELECT dept_id, AVG(marks) AS avg_marks
FROM students
WHERE marks > 35               -- rows first
GROUP BY dept_id
HAVING AVG(marks) > 60;        -- then groups
```

## What is the difference between a primary key and a foreign key?
keywords: primary key, foreign key, key, keys, unique key, candidate key, constraint, referential integrity, relationship
questions: what is a primary key | what is a foreign key | difference between primary key and foreign key

- A **primary key** uniquely identifies each row in a table. It must be **unique** and **not NULL**; a table has one.
- A **foreign key** is a column that refers to the primary key of **another table**, linking the two tables and keeping data consistent.

```sql
CREATE TABLE students (
  id INT PRIMARY KEY,
  name VARCHAR(50),
  dept_id INT,
  FOREIGN KEY (dept_id) REFERENCES departments(id)
);
```

## What is the difference between DELETE, TRUNCATE and DROP?
keywords: delete, truncate, drop, delete vs truncate, remove table, remove rows, drop table
questions: difference between delete truncate and drop | how to delete a table | how to remove all rows

- **DELETE**: removes **chosen rows** (with `WHERE`). Can be rolled back; the table stays.
- **TRUNCATE**: removes **all rows** quickly; the table structure stays.
- **DROP**: removes the **whole table** (structure and data).

```sql
DELETE FROM students WHERE id = 5;
TRUNCATE TABLE students;
DROP TABLE students;
```

## How do I INSERT, UPDATE and DELETE data in SQL?
keywords: insert, update, insert into, update set, add row, change data, modify data, dml
questions: how to add data to a table | how to update a row | how to insert values

```sql
INSERT INTO students (id, name, marks) VALUES (1, 'Asha', 92);
UPDATE students SET marks = 95 WHERE id = 1;     -- always use WHERE!
DELETE FROM students WHERE id = 1;
```

Without `WHERE`, `UPDATE` and `DELETE` affect **every** row.

## What is normalization in a database?
keywords: normalization, normalisation, 1nf, 2nf, 3nf, normal form, redundancy, database design
questions: what is normalization | why normalize a database | what are normal forms

**Normalization** is organising tables to **reduce repeated data** and avoid update mistakes. You split one big table into smaller linked tables.

- **1NF**: each cell holds a single value; no repeating groups.
- **2NF**: 1NF, and every non-key column depends on the **whole** primary key.
- **3NF**: 2NF, and non-key columns depend **only on the key** (not on other non-key columns).

## What is a database, a DBMS and an RDBMS?
keywords: database, dbms, rdbms, database management system, relational database, table, row, column, record
questions: what is a database | what is dbms | difference between dbms and rdbms

- A **database** is an organised collection of data.
- A **DBMS** is the software that creates and manages databases (MySQL, MongoDB...).
- An **RDBMS** stores data in related **tables** (rows and columns) and uses SQL: MySQL, PostgreSQL, SQLite, Oracle.

## What is the difference between SQL and NoSQL, and what is MongoDB?
keywords: nosql, mongodb, mongo, document database, sql vs nosql, collection, document, json database
questions: what is mongodb | difference between sql and nosql | what is a collection in mongodb | why use mongodb

- **SQL (relational)** databases store data in fixed **tables** and use joins.
- **NoSQL** databases store data in other shapes. **MongoDB** stores flexible **documents** (JSON-like) inside **collections**, so each document can have different fields.

| SQL | MongoDB |
|---|---|
| table | collection |
| row | document |
| column | field |

```javascript
db.students.insertOne({ name: "Asha", marks: 92 })
db.students.find({ marks: { $gt: 80 } })
db.students.updateOne({ name: "Asha" }, { $set: { marks: 95 } })
db.students.deleteOne({ name: "Asha" })
```

## What is an index in a database?
keywords: index, indexing, database index, create index, faster queries, query speed
questions: what is indexing | how to make queries faster | why use an index

An **index** is like the index of a book: the database can jump straight to matching rows instead of scanning the whole table, so searches and joins on that column are much **faster**. The price is extra storage and slightly slower inserts and updates.

```sql
CREATE INDEX idx_students_name ON students(name);
```
