A Study of Lucene (Mobile Version)
==================================

[![Build Status](https://travis-ci.org/lukhnos/lucenestudy.svg)](https://travis-ci.org/lukhnos/lucenestudy)

This project is a modified version of [
lucenestudy](https://github.com/lukhnos/lucenestudy/tree/mobile), it serves as an example of how you can use
Lucene to index documents, search with sorting and paging, highlight search
results, and provide search suggestions (also known as "autocomplete").

The sample application in this project works like a movie review search
engine. A sample dataset is provided for testing the app.

**Mobile version notes**: This is a "mobile" version of lucenestudy that
comes with the prebuilt JARs of
[Android Lucene](https://github.com/shashavali-d/android-lucene).
The source code is built as jar and the library can be utilized in Android project. 

Sample Dataset
--------------

Subset of the
[Large Movie Review Dataset v1.0](http://ai.stanford.edu/~amaas/data/sentiment/)
prepared by Andrew L. Maas et al. as part of their 2011 paper "Learning Word
Vectors for Sentiment Analysis"
([PDF](http://ai.stanford.edu/~ang/papers/acl11-WordVectorsSentimentAnalysis.pdf),
[BibTeX entry](http://ai.stanford.edu/~ang/papers/acl11-WordVectorsSentimentAnalysis.pdf)) is being used in this project.
The original dataset is used for sentiment analysis research, and is selected
from IMDb.

The subset contains 1,000 entries about 500 films, each two reviews for each
film, one "positive" and one "negative" according to the Large Movie Review
Dataset.

The original dataset contains two subsets, one "training" and one "testing".
This is the standard division in machine learning and natural language
processing. In each subset, each review takes one file, and there are separate
listings of the source URL.

The sample subset here is from the "testing" dataset. I've converted the files
into a JSON file in `sample/acl-imdb-subset.json`. The original dataset does
not have the titles and the release years for the films reviewed. I've also
collected them in the JSON file.

For more information about the original data set, please visit the Andrew
Maas's [website](http://ai.stanford.edu/~amaas/data/sentiment/), on which you
can find the download link to the dataset. A detailed README can also be found
in the tarball.


How to Build and Run the App
----------------------------

To build the sample app, you'll need JDK 1.8 or above installed. The project
uses Gradle, and I've included a Gradle wrapper that can bootstrap itself.

To build the app, simple run:

    ./gradlew build

This will compile the app, make the JAR, and also run the tests.

The built JAR is located at `./build/libs/lucenestudy.jar`. Once you have
the JAR, you can build the indices. Suppose you want to put the built
indices under /tmp/testidx:

    java -jar build/libs/lucenestudy.jar index sample/acl-imdb-subset.json /tmp/testidx

Then, to search for reviews containing the keyword "robot":

    java -jar build/libs/lucenestudy.jar search /tmp/testidx robot

And to see the search suggestion in action:

    java -jar build/libs/lucenestudy.jar suggest /tmp/testidx love


Boolean Searches
----------------

The sample app uses Lucene's query parser to process the query string. You can
perform boolean searches with quote strings. For example, this searches for
titles and reviews that contain the term "apocalypse" but not "now":

    java -jar build/libs/lucenestudy.jar search /tmp/testidx "apocalypse -now"


Extending the App
-----------------

This is nowhere near a comprehensive survey of Lucene. It is a big library
that covers a wide range of use cases. Pull requests that extend the app are
therefore more than welcome.
