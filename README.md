# Family Tree

A program for viewing and editing family members and a family tree.

# Purpose

- To record data about family members in one place including notes which can detail key parts of their life
- To help with remembering birthdays by providing a view sorted by closest birthday first, showing: name, number of days until birthday, birth day, and age on birthday
- To display a family tree dynamically made from the people

# Versions

There are three versions which each have their own README files:

1. Old Python (`src/old/python`): This is a very old version that stored the data in a database. This hasn't been touched since June 2020.
1. Old Java (`src/old/java`): **This is the current version** despite the below version being planned. It stores data in a CSV file and the main web version is a custom web server, although files that can be served by a generic web server were started.
1. New Java (`src/main`): This has barely been started but was aimed at simplifying the web code by using the Spark web framework to serve templated HTML files.

# Images

![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/Home.png "Homepage")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/People.png "People Table")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/Person.png "Person Page")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/PersonEdit.png "Edit Person")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/SearchableLink.png "Searchable dropdown for parent finding")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/Birthdays.png "Birthdays table")
![Alt text](https://github.com/jamesw1892/FamilyTree/raw/master/images/BirthdaysLiving.png "Living filter")

# Things that could be included in Notes

- Birthplace
- Important relationships eg marriages including dates
- Interests, likes, dislikes
- Anything to demonstrate personality
- Career and any notable qualifications like degree
- Nicknames and changes in last name if married (as last name recorded is maiden)
