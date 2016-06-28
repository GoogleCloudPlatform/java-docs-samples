# Cloud Platform Java Repository Tools

[![Build
Status](https://travis-ci.org/GoogleCloudPlatform/java-repo-tools.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/java-repo-tools)
[![Coverage
Status](https://codecov.io/gh/GoogleCloudPlatform/java-repo-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/GoogleCloudPlatform/java-repo-tools)

This is a collection of common tools used to maintain and test Java repositories
in the [GoogleCloudPlaftorm](https://github.com/GoogleCloudPlatform)
organization.


## Using this repository

This repository is copied into a subtree of other Java repositories, such as
[java-docs-samples](/GoogleCloudPlatform/java-docs-samples). Note, that a
subtree is just the code copied into a directory, so a regular `git clone` will
continue to work.


### Adding to a new repository

To copy `java-repo-tools` into a subtree of a new repository `my-java-samples`,
first add this repository as a remote. We then fetch all the changes from this
`java-repo-tools`.

```
git remote add java-repo-tools git@github.com:GoogleCloudPlatform/java-repo-tools.git
git fetch java-repo-tools master
```

We can then go back to the `my-java-samples` code and prepare a Pull Request to
add the `java-repo-tools` code in a subtree. Making a new branch is optional, but
recommended so that you can more easily send a pull request to start using
`java-repo-tools`.

```
git checkout -b use-java-repo-tools origin/master
```

Finally, read the `java-repo-tools` into a subtree. So that you can pull future
updates from the `java-repo-tools` repository, this command will merge histories.
This way prevents unnecessary conflicts when pulling changes in.

```
git subtree add --prefix=java-repo-tools java-repo-tools master
```

Now all the content of `java-repo-tools` will be in the `java-repo-tools/`
directory (which we specified in the `--prefix` command).

#### Using the Maven configuration

If all the projects within your `my-java-samples` share a common parent POM for
plugin configuration (like checkstyle). We can then make the
`java-repo-tools/pom.xml` parent of this.

```
<!-- Parent POM defines common plugins and properties. -->
<parent>
  <groupId>com.google.cloud</groupId>
  <artifactId>shared-configuration</artifactId>
  <version>1.0.0</version>
  <relativePath>java-repo-tools</relativePath>
</parent>
```

Once this is added to the common parent, all modules will have the same plugin
configuration applied. If the children POMs provide the plugin information
themselves, it will override this configuration, so you should delete any
now-redundant plugin information.


#### Examples

- Adding to repository with an existing parent POM: Pull Request
  [java-docs-samples#125][java-docs-samples-125].

[java-docs-samples-125]: https://github.com/GoogleCloudPlatform/java-docs-samples/pull/125


### Detecting if you need to synchronize a subtree

If you haven't done this before, run

```
git remote add java-repo-tools git@github.com:GoogleCloudPlatform/java-repo-tools.git
```

To detect if you have changes in the directory, run

```
git fetch java-repo-tools master
git diff-tree -p HEAD:java-repo-tools/ java-repo-tools/master
```

or to diff against your local `java-repo-tools` branch:

```
git diff-tree -p HEAD:java-repo-tools/ java-repo-tools --
```

(The trailing `--` is to say that we want to compare against the branch, not the
directory.)


### Pulling changes from Java Repository Tools to a subtree

To update the `java-repo-tools` directory, if you haven't done this before, run

```
git remote add java-repo-tools git@github.com:GoogleCloudPlatform/java-repo-tools.git
```

To pull the latest changes from this `java-repo-tools` repository, run:

```
git checkout master
# Making a new branch is optional, but recommended to send a pull request for
# update.
git checkout -b update-java-repo-tools
git pull -s subtree java-repo-tools master
```

Then you can make any needed changes to make the rest of the repository
compatible with the updated `java-repo-tools` code, commit, push, and send a
Pull Request as you would in the normal flow.


### Pushing changes from a subtree upstream to Java Repository Tools

What if you make changes in your repository and now want to push them upstream?

Assuming you just commited changes in the `java-repo-tools/` directory of your
`my-main-branch`, to split the `java-repo-tools` changes into their own branch.
The first time using the `subtree` command, we may need to use the `--rejoin`
argument.

```
git subtree split --prefix=java-repo-tools -b ${USER}-push-java-repo-tools
git checkout ${USER}-push-java-repo-tools
git push java-repo-tools ${USER}-push-java-repo-tools
```

Then, you can send a pull request to the `java-repo-tools` repository.


## References

- [GitHub's subtree merge reference](https://help.github.com/articles/about-git-subtree-merges/)

## Contributing changes

-  See [CONTRIBUTING.md](CONTRIBUTING.md)


## Licensing

- Apache 2.0 - See [LICENSE](LICENSE)

