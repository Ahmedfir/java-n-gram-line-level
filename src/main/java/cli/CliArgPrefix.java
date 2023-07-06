package cli;

// @see {}
enum CliArgPrefix {
    TOKENIZER("-tokenizer="),
    THRESHOLD("-threshold="),
    SIZE("-n="),
    FILE_INCLUDE_REQUEST("-in="),
    EXCLUDE_FILES_WITH_WORD_IN_PATH("-ex_w_in_path="),
    INCLUDE_FILES_WITH_WORD_IN_PATH("-inc_w_in_path="),
    INCLUDE_FILE_NEIGHBOURS_WITH_WORD_IN_PATH("-inc_neighbours_w_in_path"),
    REPO("-repo="),
    OUTPUT_FILE("-out=");

    final String argPrefix;

    CliArgPrefix(String argPrefix) {
        this.argPrefix = argPrefix;
    }

    static CliArgPrefix startsWithPrefix(String arg) {
        for (CliArgPrefix cap : CliArgPrefix.values()) {
            if (arg.startsWith(cap.argPrefix)) {
                return cap;
            }
        }
        throw new IllegalArgumentException(arg);
    }

}
