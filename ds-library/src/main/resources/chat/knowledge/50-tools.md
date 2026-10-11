category: subject
topic: Tools

## What is the difference between Git and GitHub?
keywords: git, github, git vs github, version control, repository, repo, source control, what is git, what is github
questions: what is git | what is github | difference between git and github | why use git

- **Git** is a **version control** tool on your computer. It records the history of your code so you can go back and work with others.
- **GitHub** is a **website** that stores Git repositories online, so you can back them up, share them and collaborate (it is also a great portfolio).

## What are the basic Git commands?
keywords: git commands, git init, git add, git commit, git status, git clone, git pull, git push, git log, git branch, basic git commands, commit
questions: list of git commands | how to use git | how to commit code

```
git init                       start a repository in this folder
git clone <url>                copy a repository from GitHub
git status                     what changed?
git add .                      stage all changes
git commit -m "message"        save a snapshot
git push                       upload commits to GitHub
git pull                       download new commits
git log --oneline              history
```

## How do I push my code to GitHub?
keywords: push code, upload to github, push to github, git push, remote, origin, create repository, publish project
questions: how to upload project to github | how to push to github | how to add my project to github

1. Create an **empty repository** on github.com and copy its address.
2. In your project folder:

```
git init
git add .
git commit -m "First commit"
git branch -M main
git remote add origin https://github.com/YOUR-NAME/YOUR-REPO.git
git push -u origin main
```

Later, just `git add .`, `git commit -m "..."` and `git push`. Never commit passwords or API keys.

## What are a branch, a merge and a pull request?
keywords: branch, merge, pull request, pr, branching, merge conflict, checkout, switch branch, fork
questions: what is a git branch | what is a pull request | how to merge branches | what is a merge conflict

- A **branch** is a separate line of work, so you can try something without breaking the main code: `git switch -c new-feature`.
- A **merge** brings a branch's changes back into another: `git merge new-feature`.
- A **pull request (PR)** is a request on GitHub to merge your branch, so others can review it first.
- A **merge conflict** happens when two branches changed the same lines; you edit the file to choose the final text, then commit.

## What is Jupyter Notebook?
keywords: jupyter, jupyter notebook, notebook, ipynb, jupyterlab, cells, kernel, run cell, start jupyter
questions: how to open jupyter notebook | how to use jupyter | what is a kernel | what is an ipynb file

**Jupyter Notebook** is an interactive document where you write code in **cells**, run them one by one, and see the output (tables, charts) right below, mixed with notes. It is the favourite tool for data science work.

```
pip install notebook
jupyter notebook
```

Run a cell with **Shift + Enter**. The **kernel** is the running Python engine; restart it if things act strangely.

## What is Google Colab?
keywords: google colab, colab, colaboratory, free gpu, cloud notebook, online notebook, run notebook online
questions: what is colab | how to use google colab | how to use gpu in colab | how to upload a file in colab

**Google Colab** is a free, online Jupyter-style notebook from Google. There is nothing to install and it can use a free **GPU** for deep learning.

- Open colab.research.google.com and sign in with a Google account.
- GPU: **Runtime, Change runtime type, GPU**.
- Files: the folder icon on the left, then upload. Files disappear when the session ends unless you save to Google Drive.
- Install a library: `!pip install seaborn`.

## What is Anaconda and conda?
keywords: anaconda, conda, anaconda navigator, miniconda, conda environment, install anaconda, spyder
questions: what is anaconda | how to install anaconda | how to create a conda environment

**Anaconda** is a Python distribution made for data science. It installs Python plus common libraries (NumPy, Pandas, Jupyter...) and **conda**, a tool to manage packages and **environments**. **Anaconda Navigator** is its point-and-click launcher.

```
conda create -n ds python=3.11
conda activate ds
conda install pandas numpy matplotlib
jupyter notebook
```

## How do I run Python code in VS Code?
keywords: vs code, vscode, visual studio code, run python in vs code, python extension, select interpreter, code editor
questions: how to run python in vscode | how to set up vs code for python

1. Install **VS Code** and its **Python** extension.
2. Open your folder (File, Open Folder) and create a file ending in `.py`.
3. Choose the interpreter: press **Ctrl+Shift+P**, then "Python: Select Interpreter" and pick your Python or conda environment.
4. Click the **Run** triangle (top right) or run `python file.py` in the terminal.

VS Code also runs Jupyter notebooks (`.ipynb`) once you install the Jupyter extension.

## What is Kaggle?
keywords: kaggle, kaggle datasets, kaggle notebooks, kaggle competition, kaggle learn, free datasets, practice data science
questions: what is kaggle | where to get datasets | how to practice data science

**Kaggle** is a website for data science: **free datasets**, free online **notebooks** (with GPU), short **courses** (Kaggle Learn) and **competitions**. It is an excellent place to practise and to build a portfolio.

## How should I start learning data science?
keywords: learn data science, roadmap, how to start, beginner, learning path, career, become data scientist, study plan, how to learn
questions: how to start data science | data science roadmap | what should i learn first | how to become a data scientist
starter: yes

A simple path:

1. **Python** basics (lists, dictionaries, functions, loops)
2. **Statistics** basics (mean, spread, correlation, probability)
3. **NumPy, Pandas** and **charts** (Matplotlib, Seaborn)
4. **SQL** for databases
5. **Machine learning** with scikit-learn (regression, classification, evaluation)
6. Optional: **deep learning** (TensorFlow or PyTorch)
7. Build small **projects** on real datasets (Kaggle) and put them on **GitHub**

Practise a little every day, and use the **Tools** and **Resources** pages of this website for tutorials.
