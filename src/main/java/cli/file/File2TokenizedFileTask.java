package cli.file;


import cli.CliRequest;
import parser.File;
import parser.Line;
import tokenizer.file.AbstractFileTokenizer;
import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;
import utils.Checker;
import utils.LogFactory;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Stream;


class File2TokenizedFileTask implements Callable<File> {

    private static final Logger LOG = LogFactory.logger(CliRequest.class);

    private final String path;
    private final AbstractFileTokenizer tokenizer;
    private final String repoPath;

    File2TokenizedFileTask(String path, String repoPath, AbstractFileTokenizer tokenizer) {
        this.path = path;
        this.repoPath = repoPath;
        this.tokenizer = tokenizer;
    }

    static List<File2TokenizedFileTask> fromPaths(List<String> strings, String repoPath, AbstractFileTokenizer tokenizer) {
        List<File2TokenizedFileTask> res = new ArrayList<>();
        for (String string : strings) {
            res.add(new File2TokenizedFileTask(string, repoPath, tokenizer));
        }
        return res;
    }

    @Override
    public File call() throws Exception {
        Path p = Paths.get(path);
        long lineCount;
        try (Stream<String> stream = Files.lines(p, StandardCharsets.UTF_8)) {
            lineCount = stream.count();
        }
        if (lineCount < 1) {
            LOG.warning("Empty file = " + path);
            return null;
        }
        Iterable<String> tokens = tokenizer.tokenize(new FileReader(p.toFile()));

        assert !Checker.isTrimNlOrEmpty(tokens);

        return new File(path.replace(repoPath, ""), tokens);
    }

}
