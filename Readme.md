# n-gram Naturalness-based lines ranking

This is the implementation called by the python scripts under: https://github.com/Ahmedfir/ngramlineloc/settings/access

### Typical usage: 
- input: List of java files from a project, whose lines will be ranked.
- training input: the rest of java files from that same project.
- output: ranked List of lines of the input files, by cross-entropy.

### Call:

You can download our latest released jar or recompile the sources yourself (next section).

- Launcher class:
  `
  Main.java`

- example params:

`
-repo=path/to/project/directory
-n=4
-out=csvOutputFile
-ex_w_in_path=example
-ex_w_in_path=test
-in=path/to/file/to/rank/f1.java
-in=path/to/file/to/rank/f2.java
`

- You can find an example of a request and the expected results in the tests folder:
    - System test:
      `
      src/test/java/cli/CliRequestTest.java
      `
    - expected csv results:
      `
      fl/src/test/resources/expected/parse_args_test.csv`

### Compile:

The project depends on Tuna: https://github.com/electricalwind/tuna

You will need to download that repo and install it first (it's a maven project).

### License: 
Apache 2.0

