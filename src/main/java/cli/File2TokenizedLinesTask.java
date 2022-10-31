package cli;


import parser.Line;
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


class File2TokenizedLinesTask implements Callable<List<Line>> {

    private static final Logger LOG = LogFactory.logger(CliRequest.class);

    private final String path;
    private final AbstractLineTokenizer tokenizer;
    private final String repoPath;

    File2TokenizedLinesTask(String path, String repoPath, AbstractLineTokenizer tokenizer) {
        this.path = path;
        this.repoPath = repoPath;
        this.tokenizer = tokenizer;
    }

    static List<File2TokenizedLinesTask> fromPaths(List<String> strings, String repoPath, AbstractLineTokenizer tokenizer) {
        List<File2TokenizedLinesTask> res = new ArrayList<>();
        for (String string : strings) {
            res.add(new File2TokenizedLinesTask(string, repoPath, tokenizer));
        }
        return res;
    }

    @Override
    public List<Line> call() throws Exception {
        List<Line> res = new ArrayList<>();
        Path p = Paths.get(path);
        long lineCount;
        try (Stream<String> stream = Files.lines(p, StandardCharsets.UTF_8)) {
            lineCount = stream.count();
        }
        if (lineCount < 1) {
            LOG.warning("Empty file = " + path);
            return res;
        }
        List<Iterable<String>> strLines = (List<Iterable<String>>) tokenizer.tokenize(new FileReader(p.toFile()));
        if (tokenizer instanceof JavaLemmeLineTokenizer) {
            assert lineCount == strLines.size();
        } else {
            assert lineCount <= strLines.size() : "tokenizer ignored lines: received " + strLines.size() + " instead of " + lineCount;
            if (strLines.size() > lineCount) { // some tokenizers consider the empty lines in the end.
                for (int i = (int) lineCount - 1; i < strLines.size() - 1; i++) { //  some tokenizers shift sep. in the last line.
                    assert !strLines.get(i).iterator().hasNext() || "\n".equals(strLines.get(i).iterator().next()) : "tokenizer returned extra line at " + i + " that is not a break : " + strLines.size() + " instead of " + lineCount;
                }
            }
        }
        assert !Checker.isTrimNlOrEmpty(strLines);

        for (int i = 0; i < strLines.size(); i++) {
            Iterable<String> lineTokens = strLines.get(i);
            if (lineTokens != null && lineTokens.iterator().hasNext() && lineTokens.iterator().next() != null) {
                res.add(new Line(path.replace(repoPath, ""), i + 1, lineTokens));
            }
        }

        return res;
    }

}
