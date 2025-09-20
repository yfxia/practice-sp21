# Gitlet Design Document

**Name**: Sophia Xia

## Classes and Data Structures

### Main Class
Main class contains a giant switch statement to distinguish between different gitlet commands.
Not a good design choice, can be improved later.

### Commit Class
Commit class consists of all attributes a commit object user created.
Very critical design choices on the instance variables.


### Repository Class
Repository class encapsulates all functionalities that user can operate on the created commits.
A few methods such as gitlet merge command contains 8 different logical cases to be thought through.


## Algorithms
Using graph traversal method to find latest common ancestor.

## Persistence
The directory structure looks like this:
```dtd
CWD
├── .gitlet/
│   ├── staged_add/                   -----> area staged for addition
│   ├── staged_rm/                    -----> area staged for removal
│   ├── objects/                      
│   |-------├──blobs/                 -----> where file blob ids are stored
│   |-------├──commits/               -----> where commit hash ids are stored
│   ├── refs/heads/[branch_name]/     -----> commit Ids are the head of the branch
│   └── HEAD                          -----> commit Ids are the head of repo

```

Reasoning
- Easier to create separate folders for staged add and staged removal operations.
- Faster to search to create separate folders for file blob ids and commit ids, even though both are serialized by sha1 function.
- refs/heads/[branch name] structure is following real git's design.
- objects/ folder are storing first 2 digits of hash ids for faster index search.