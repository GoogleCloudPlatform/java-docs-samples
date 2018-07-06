# Google3

This is the root of the Google3 source tree.  Code in here is built by the
Google3 build system.  Submissions are subject to review and approval by the
OWNERS of each package.

Building and testing using Blaze:  (http://blaze/)

    % blaze build java/com/google/common/util:util
    % blaze test  javatests/com/google/common/util:all

Searching the codebase: http://cs/

## Further resources:

Google3 documentation index

> http://www/eng/doc/google3.html

Writing Google3 BUILD files

> http://www/eng/howto/google3/write_build_file.html

Google3 BUILD Encyclopedia of Functions

> http://go/build-encyclopedia

A user's guide to Blaze

> http://go/blaze-user-manual

If you have trouble finding, editing, submitting, building, or testing code in
this tree, check go/build-help or write to build-help@google.com.

---

## Directory Names

This section gives guidelines for choosing a good location for a new project's
code under `//depot/google3`, `//depot/google3/java/com/google`,
`//depot/googleclient`, and other repositories. While some existing directories
violate these rules, we follow them for new directories.

### Rationale

We have two goals with top-level directory names. First, we want to ensure the
names are **easy to understand by someone new to the codebase** . The reason is
it makes it easier for someone who sees that directory to understand it. As a
hypothetical example, if I'm reading a file and I see

    #include "google3/trippy/util.h"

I'll have no idea what that `#include` is about. But if it's

    #include "google3/captcha/trippy/util.h"

I'll have a much better idea of what this file is and what it's doing in the
source code I'm looking at. The same argument applies when files are used in
other contexts (e.g. namespaces, design docs, even just browsing google3).

The second goal is to **keep similar code together**, to allow for high-level
'gatekeepers' (OWNERS) to keep an eye on different projects in the same space.
For instance, it would be nice if we had people whose role it was to vet
(briefly!) all things related to frontends. That's easier to do if all frontend
code is in a single directory tree.

### Guidelines

Ideally, a new project is placed under an existing top-level directory, so look
around first. Don't forget to look both for "top-level" directories both in
`//depot/google3` and in `//depot/google3/java/com/google`; if
`//depot/google3/{foo}` already exists, it's easier to get
`//depot/google3/java/com/google/{foo}` created, and vice versa.

If no existing directory is appropriate we'll ask that you add a top-level
directory with a generic name under which you could put your project's
directory. This new top-level directory could serve as an appropriate container
not only for your new project but also for an existing project that's currently
at the top level, that we could move under this new directory some day.

Please don't use code names and acronyms for directory names. This makes it
harder for people unfamiliar with a project to find your code. **If we expect
that a noogler wouldn't immediately understand what the directory was for (more
or less), we'll ask you to consider a different name.** The naming conventions
may be relaxed if you're creating a subdirectory in a clearly named top-level
directory, such as "learning" or "tv".

Hypothetical bad directory names:

*   trippy (code name)
*   srb (cryptic acronym)
*   admuncher (should be a subdirectory of ads or of muncher)
*   apollo (what is this? a new moon project?)

Example good directory names:

*   net/rpc
*   nlp/sentencesplit
*   bigtable (a code name, but a descriptive one)
*   power/solar (ah, a solar energy project! with a place for a future
    power/wind.)

Note that not all top-level directories follow these guidelines. Over time, it
would be great if some of these top-level directories were migrated under more
descriptive umbrella directories, but that won't happen overnight. New code
should start out in a well-named place from the beginning.

### Approval process

If you're adding a directory `{foo}` to the top-level, prepare a changelist that
adds

*   `//depot/google3/{foo}/OWNERS` (with at least two owners)
*   `//depot/google3/{foo}/README.md`

If you're adding to the Java tree, prepare a changelist that includes:

*   `//depot/google3/java/com/google/{foo}/OWNERS` (at least two owners)
*   `//depot/google3/java/com/google/{foo}/README.md`
*   `//depot/google3/javatests/com/google/{foo}/OWNERS`

The `javatests/.../OWNERS` file should include *exactly* one line:

    file://depot/google3/java/com/google/{foo}/OWNERS

i.e., a reference to the project's Java OWNERS file.

See the go/g3doc for how to create `README.md`.

Send the changelist to a randomly-selected subset of the list of "people who can
approve top-level google3 checkins" in the google3/OWNERS file.

### Directory Naming FAQ

1.  **How do I get permission to create a new directory?**<br> Ask someone in
     the `OWNERS` file for the parent directory to do `g4 approve` on the
     change that creates the first file in that directory.
2.  **I'm naming the directory the same as my project.  What's wrong with
     that?**<br> Even if your project's name isn't a code-name, it's still
     likely to be a name that's more meaningful to you than to others outside
     your project.  Think about what a noogler would think when they see your
     proposed directory name.  Would they know exactly what it is?  If not,
     try to come up with a more descriptive name.  What is your project
     *doing*?
3.  **I'm naming the directory after my team, so all my team's work can go
     together.  Shouldn't I give it my team's name?**<br> Not necessarily.
     Again, the goal is to have nooglers know what's in the directory.  If
     your team name doesn't obviously reflect what the team does, pick a
     different name that does.
4.  **I have a project in `google3`, and want to add a piece to
     `googleclient`.  Should I use a different directory name?**<br> Please
     use a parallel directory name.  eg, `google3/ads/tv` has some code in
     `googleclient/ads/tv`.
