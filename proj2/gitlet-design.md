# Gitlet Design Document

**Name**: Sophia Xia

## Classes and Data Structures

### Main


#### Fields

1. Field 1
2. Field 2


### Commit

This class represents a `Commit` that will be stored in a file. Because each commit ID will be unique, we use it as the name of the file that the object is serialized to.

All `Commit` objects are serialized within `OBJECT_FOLDER` which is withint `GITLET_DIR`.

#### Instance Variables
- Message - contains the message of a commit
- Branch
- Blob
- Parent - the parent commit of a commit object
- Timestamp - time at which a commit was created. Assigned by the constructor
- CommitIndex - Hashmap stores the hierarchical relationship between commits
    - `key`: commitId - branchName, `value`: parent's commitId


### Repository
This class defers all `Commit` specific logic to `Commit` class.

#### Instance Variables

- CWD - user's current working directory
- GITLET_DIR - .gitlet folder
- master - master branch of gitlet repository
- HEAD - the head pointer


## Algorithms

## Persistence
The directory structure looks like this
```dtd
CWD
├── .gitlet
│   ├── staged_add
│   ├── staged_rm
│   ├── objects
│   └── sentinel
```
It will:
1. Create the `.gitlet` folder if it doesn't already exist
2. Create the `staged_add` and `objects` folder if it doesn't already exist

The `Commit` class will handle the serialization of `Commit` objects.
1. `public static Commit fromFile()` - Given the name of the `Commit` object, it retrieves the serialized data from the `objects` folder.
2. `public void saveCommit()` - Serialize the `Commit` object to the `objects` folder.