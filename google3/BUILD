# Do not add anything to this file.  All targets in google3 should be defined in
# a subdirectory of google3, not this file (with a small exception below).

# The WORKSPACE and BUILD files must live in google3 and are required for some
# tests.
exports_files(
    [
        "BUILD",
        "WORKSPACE",
    ],
    visibility = [
        "//devtools/blaze/exoblaze:__subpackages__",
        "//devtools/blaze/integration:__pkg__",
        "//tools/osx:__pkg__",
    ],
)

# The Python indexer for CodeSearch needs google3/__init__.py so that it can
# properly resolve 'import google3' statements.  This is because
# google3/__init__.py is an implicit dependency of py_library and py_binary
# rules, and is not supplied as an explicit dependency by Blaze.
exports_files(
    ["__init__.py"],
    visibility = ["//devtools/python/pykythe:__pkg__"],
)
