category: subject
topic: Statistics and machine learning

## What is data science?
keywords: data science, data scientist, data science lifecycle, what is data science, data science process
questions: what is data science | what does a data scientist do | steps in a data science project
starter: yes

**Data science** means using data, statistics, programming and domain knowledge to find useful answers and make predictions.

A typical project goes through these steps:

1. **Ask** a clear question or problem
2. **Collect** the data
3. **Clean** and prepare it
4. **Explore** it (EDA, charts)
5. **Model** it (statistics or machine learning)
6. **Evaluate** how good the results are
7. **Share** the findings (reports, dashboards) or **deploy** the model

## What is machine learning?
keywords: machine learning, ml, artificial intelligence, deep learning, ai vs ml, learning from data, ml vs ai
questions: what is machine learning | difference between ai ml and deep learning | what is artificial intelligence

**Machine learning (ML)** lets computers **learn patterns from data** instead of being given fixed rules, then use those patterns to predict or decide on new data (for example spam detection or house price prediction).

- **AI** is the broad goal of machines doing "smart" tasks.
- **Machine learning** is a way to achieve AI by learning from data.
- **Deep learning** is a part of ML that uses large **neural networks**.

## What is the difference between supervised and unsupervised learning?
keywords: supervised learning, unsupervised learning, reinforcement learning, labelled data, unlabeled data, classification, regression, clustering, types of machine learning
questions: types of machine learning | supervised vs unsupervised | what is classification | what is clustering

- **Supervised learning**: the data has **labels** (the right answers) and the model learns to predict them.
  - **Regression** predicts a number (price, temperature).
  - **Classification** predicts a category (spam / not spam, pass / fail).
- **Unsupervised learning**: **no labels**; the model finds structure on its own, such as **clustering** (grouping similar items) or dimensionality reduction.
- **Reinforcement learning**: an agent learns by trial and error using rewards.

## What is overfitting?
keywords: overfitting, overfit, overfitted, over fitting, too complex model, memorizes training data, high variance, generalization, generalisation, model performs badly on new data
questions: what is overfitting | how to avoid overfitting | why model works on training data but not on test data
starter: yes

**Overfitting** happens when a model learns the training data **too well**, including its noise, so it scores high on training data but **poorly on new data**.

How to reduce it:

- Get **more data**
- Use a **simpler model** (fewer features, shallower tree)
- **Regularization** (L1/L2) and **dropout** in neural networks
- **Cross-validation** and early stopping
- Remove noisy or irrelevant features

## What is underfitting?
keywords: underfitting, underfit, too simple model, high bias, model too simple
questions: what is underfitting | why is my model accuracy low on training data

**Underfitting** is the opposite of overfitting: the model is **too simple** to capture the pattern, so it does badly on **both** training and test data. Fixes: use a more powerful model, add useful features, train longer, or reduce regularization.

## What is the bias-variance tradeoff?
keywords: bias, variance, bias variance, tradeoff, trade off, bias-variance
questions: explain bias and variance | what is the bias variance tradeoff

- **Bias** is error from a model that is too simple (underfitting).
- **Variance** is error from a model that is too sensitive to the training data (overfitting).

Making a model more complex lowers bias but raises variance. The goal is the **sweet spot** with the lowest total error on new data.

## What is a train-test split and why do we use it?
keywords: train test split, train_test_split, training set, testing set, test set, validation set, split data, holdout
questions: why split data into train and test | how to split data in sklearn | what is a validation set

We keep part of the data **hidden** from the model while it learns, then use that part to check how it performs on **unseen** data. This gives an honest estimate of real-world performance.

```python
from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42)
```

A common split is 80% train and 20% test. Never train on the test set.

## What is cross-validation?
keywords: cross validation, cross-validation, k fold, kfold, k-fold, cross_val_score
questions: what is k fold cross validation | why use cross validation

**Cross-validation** gives a more reliable score than one split. In **k-fold** cross-validation the data is cut into k parts; the model trains on k-1 parts and tests on the remaining one, and this repeats k times. The final score is the average.

```python
from sklearn.model_selection import cross_val_score
scores = cross_val_score(model, X, y, cv=5)
print(scores.mean())
```

## What is linear regression?
keywords: linear regression, regression line, best fit line, simple linear regression, multiple linear regression, slope, intercept, least squares
questions: explain linear regression | how does linear regression work | how to do linear regression in python | what is regression | regression

**Linear regression** predicts a **number** by fitting a straight line (or plane) through the data: `y = m·x + c`. It finds the line that makes the squared errors smallest (**least squares**).

```python
from sklearn.linear_model import LinearRegression
model = LinearRegression().fit(X_train, y_train)
print(model.coef_, model.intercept_)
print(model.score(X_test, y_test))        # R-squared
predictions = model.predict(X_test)
```

## What is logistic regression?
keywords: logistic regression, sigmoid, binary classification, probability, logit
questions: explain logistic regression | is logistic regression classification or regression

Despite its name, **logistic regression** is a **classification** method. It uses the **sigmoid** function to turn a score into a **probability between 0 and 1**, then picks a class (for example "yes" when the probability is above 0.5).

```python
from sklearn.linear_model import LogisticRegression
clf = LogisticRegression().fit(X_train, y_train)
print(clf.predict_proba(X_test)[:3])
```

## What are decision trees and random forests?
keywords: decision tree, random forest, tree, forest, ensemble, bagging, gini, entropy, information gain, boosting, xgboost
questions: how does a decision tree work | what is random forest | what is an ensemble

- A **decision tree** asks a series of yes/no questions about the features to reach a prediction. Easy to understand, but it overfits easily.
- A **random forest** builds **many** trees on random parts of the data and averages their votes. It is more accurate and more stable (this is an **ensemble** method).

```python
from sklearn.ensemble import RandomForestClassifier
model = RandomForestClassifier(n_estimators=100).fit(X_train, y_train)
```

## What is K-means clustering?
keywords: kmeans, k means, k-means, clustering, cluster, centroid, unsupervised clustering, elbow method
questions: how does k means work | what is clustering | how to choose k in kmeans

**K-means** groups data into **k clusters** without labels:

1. Pick k starting centres (**centroids**).
2. Assign each point to its nearest centre.
3. Move each centre to the average of its points.
4. Repeat 2-3 until nothing changes.

Choose k with the **elbow method** (plot the error against k and look for the bend).

```python
from sklearn.cluster import KMeans
labels = KMeans(n_clusters=3, n_init=10).fit_predict(X)
```

## What is K-nearest neighbours (KNN)?
keywords: knn, k nearest neighbors, k nearest neighbours, nearest neighbor, neighbours, lazy learner
questions: how does knn work | explain k nearest neighbour

**KNN** classifies a new point by looking at its **k closest points** in the training data and taking a majority vote (or the average for regression). It has no real training step, but it is slow on large data and needs **scaled** features because it uses distances.

## What are accuracy, precision, recall, F1 and the confusion matrix?
keywords: confusion matrix, accuracy, precision, recall, f1, f1 score, sensitivity, specificity, true positive, false positive, evaluation metrics, classification metrics, roc, auc
questions: how to evaluate a classification model | what is precision and recall | difference between precision and recall | how to measure model performance
starter: yes

A **confusion matrix** counts correct and wrong predictions: TP (true positive), TN, FP (false positive), FN.

- **Accuracy** = (TP + TN) / all. Misleading when classes are unbalanced.
- **Precision** = TP / (TP + FP): of the items predicted positive, how many really were?
- **Recall** = TP / (TP + FN): of the real positives, how many did we find?
- **F1 score**: the balance (harmonic mean) of precision and recall.

```python
from sklearn.metrics import classification_report, confusion_matrix
print(confusion_matrix(y_test, pred))
print(classification_report(y_test, pred))
```

For regression use **MAE**, **MSE / RMSE** and **R-squared**.

## What is feature scaling (normalization and standardization)?
keywords: feature scaling, scaling, normalization, normalisation, standardization, standardisation, minmaxscaler, standardscaler, scale features
questions: why scale features | difference between normalization and standardization | how to use standardscaler

Features with big numbers (salary) can drown out small ones (age). **Scaling** puts them on a similar range.

- **Normalization (Min-Max)**: squeezes values into 0 to 1.
- **Standardization (Z-score)**: mean 0 and standard deviation 1.

```python
from sklearn.preprocessing import StandardScaler
scaler = StandardScaler().fit(X_train)       # learn from TRAIN only
X_train_s = scaler.transform(X_train)
X_test_s = scaler.transform(X_test)
```

Needed for KNN, SVM, K-means, neural networks and gradient-based models; not needed for trees.

## How do I encode categorical variables (one-hot and label encoding)?
keywords: encoding, categorical variables, one hot encoding, onehot, label encoding, get_dummies, categorical data, dummy variables, ordinal encoding
questions: how to convert text to numbers | how to handle categorical data | what is one hot encoding

Models need numbers, so text categories must be converted.

- **One-hot encoding**: one 0/1 column per category (best for categories with no order, like city).
- **Label / ordinal encoding**: numbers in order (best when order matters: low < medium < high).

```python
pd.get_dummies(df, columns=["city"], drop_first=True)
from sklearn.preprocessing import OneHotEncoder
```

## What is gradient descent?
keywords: gradient descent, learning rate, loss function, cost function, optimizer, optimisation, minimize loss, sgd
questions: explain gradient descent | what is learning rate | what is a loss function

A **loss function** measures how wrong the model's predictions are. **Gradient descent** is how many models learn: it repeatedly changes the model's parameters a small step in the direction that **reduces the loss**, like walking downhill.

The **learning rate** is the step size. Too big: it overshoots and never settles. Too small: learning is very slow.

## What is a neural network and deep learning?
keywords: neural network, neural networks, deep learning, neuron, layer, hidden layer, activation function, relu, backpropagation, epoch, batch size, cnn, rnn, ann
questions: how does a neural network work | what is deep learning | what is an epoch | what is an activation function

A **neural network** is made of layers of connected **neurons**. Each neuron takes inputs, multiplies by **weights**, adds a **bias**, and passes the result through an **activation function** (ReLU, sigmoid...). **Deep learning** uses networks with many hidden layers.

Training: forward pass, calculate the **loss**, **backpropagation** works out how to change each weight, and an optimizer updates them.

- **Epoch**: one full pass through the training data.
- **Batch size**: how many examples are used per weight update.

Popular libraries: **TensorFlow/Keras** and **PyTorch**.

## What are mean, median and mode?
keywords: mean, median, mode, average, central tendency, measures of central tendency
questions: difference between mean and median | how to calculate average | what is mode

- **Mean**: the average (sum divided by count). Pulled by extreme values.
- **Median**: the middle value when sorted. Not affected by outliers, so it is better for skewed data like salaries.
- **Mode**: the most frequent value.

```python
df["marks"].mean(), df["marks"].median(), df["marks"].mode()
```

## What are variance and standard deviation?
keywords: standard deviation, variance, std, spread, dispersion, measures of dispersion, range, iqr, quartile
questions: what is standard deviation | how to calculate variance | what does standard deviation tell us

They measure how **spread out** the data is around the mean.

- **Variance**: the average of the squared distances from the mean.
- **Standard deviation (SD)** = the square root of the variance, in the same units as the data.

A small SD means values are close to the mean; a large SD means they are scattered. `df["marks"].std()`.

## What is correlation, and is it the same as causation?
keywords: correlation, causation, correlation coefficient, pearson, corr, correlate, relationship between variables
questions: difference between correlation and causation | how to find correlation | what does correlation mean

**Correlation** (-1 to +1) measures how strongly two numbers move together. +1: rise together. -1: one rises while the other falls. 0: no linear relationship.

**Correlation is not causation**: ice-cream sales and sunburn rise together, but one does not cause the other (summer causes both).

```python
df.corr(numeric_only=True)
```

## What is the normal distribution?
keywords: normal distribution, gaussian, bell curve, 68 95 99.7, empirical rule, z score, skewness, skewed
questions: what is a bell curve | what is normal distribution | what is z score | what is skewness

The **normal distribution** is the symmetric, bell-shaped curve where mean, median and mode are equal. About **68%** of values lie within 1 standard deviation of the mean, **95%** within 2, and **99.7%** within 3.

A **z-score** = (value - mean) / SD tells you how many standard deviations a value is from the mean. A **skewed** distribution has a longer tail on one side.

## What is a p-value and hypothesis testing?
keywords: p value, p-value, hypothesis testing, hypothesis, null hypothesis, alternate hypothesis, significance, significance level, t test, chi square, anova, statistical test
questions: what is p value | what is null hypothesis | how to do hypothesis testing

**Hypothesis testing** checks whether a result could just be chance.

1. State the **null hypothesis (H0)**: there is no effect or difference.
2. Choose a **significance level**, usually 0.05.
3. Run a test (t-test, chi-square, ANOVA) to get a **p-value**.
4. If **p < 0.05**, reject H0 (the result is statistically significant). Otherwise you do not have enough evidence.

The p-value is the probability of seeing data at least this extreme **if H0 were true**. It is not the probability that H0 is true.

## What are outliers and how do I detect them?
keywords: outlier, outliers, anomaly, extreme values, iqr method, z score method, detect outliers, remove outliers
questions: how to find outliers | how to remove outliers | what is an outlier

An **outlier** is a value far away from the rest. It may be an error or a real rare event, so check before removing it.

- **IQR method**: outlier if below `Q1 - 1.5*IQR` or above `Q3 + 1.5*IQR`.
- **Z-score method**: outlier if |z| > 3.
- **Box plots** show outliers visually.

```python
q1, q3 = df["salary"].quantile([0.25, 0.75])
iqr = q3 - q1
clean = df[df["salary"].between(q1 - 1.5 * iqr, q3 + 1.5 * iqr)]
```

## How can I improve my model's accuracy?
keywords: improve accuracy, improve model, increase accuracy, better model, model tuning, hyperparameter, hyperparameter tuning, gridsearchcv, feature engineering
questions: how to increase model accuracy | how to improve machine learning model | what is hyperparameter tuning

1. **Better data**: more data, fewer errors, handle missing values and outliers.
2. **Feature engineering**: create useful features, remove useless ones, scale and encode properly.
3. **Try other algorithms** (for example a random forest instead of one tree).
4. **Tune hyperparameters** with `GridSearchCV` or `RandomizedSearchCV`.
5. Fix **overfitting or underfitting** (see those answers).
6. Use **cross-validation** to judge changes fairly, and the right metric (F1 or recall for unbalanced classes).
