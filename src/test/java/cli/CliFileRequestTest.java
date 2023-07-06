package cli;

import modelling.exception.TrainingFailedException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import parser.File;
import utils.Checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cli.CliArgTokenizer.FileLevelJavaParser;


public class CliFileRequestTest {

    private static final Path TMP_DIR = Paths.get("src/test/resources/tmp").toAbsolutePath();

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(TMP_DIR);
    }

    @Test
    public void parseArgs_JP_file_tokenizer() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        String repoPathStr = "src/test/resources/files";
        Path outputFile = TMP_DIR.resolve("parseArgs_JP_file_tokenizer_test.csv").toAbsolutePath();
        Path expectedFile = Paths.get("src/test/resources/expected/parseArgs_JP_file_tokenizer_test.csv").toAbsolutePath();

        CliRequest req = CliRequest.parseArgs(
                "-tokenizer=F_JP" ,
                "-repo=" + Paths.get(repoPathStr).toAbsolutePath(),
                "-n=4",
                "-out=" + outputFile,
                "-ex_w_in_path=example",
                "-in=src/test/resources/files/ArgumentImpl.java",
                "-in=src/test/resources/files/Role.java",
                "-in=src/test/resources/files/empty.java");
        Assert.assertEquals(4, req.getTrainingFiles().size());
        CliRequest m = req.train();
        Object r = m.rank();
        Assert.assertTrue(r instanceof List);
        List<File> rl = (List<File>) r;
        Assert.assertFalse(Checker.isTrimNlOrEmpty(rl));
        Set<String> expectedLines, outputLines;
        try (Stream<String> lines = Files.lines(expectedFile)) {
            expectedLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        try (Stream<String> lines = Files.lines(outputFile)) {
            outputLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        Assert.assertTrue(expectedLines.containsAll(outputLines));
        Assert.assertTrue(outputLines.containsAll(expectedLines));
        Assertions.assertThat(expectedFile).hasSameTextualContentAs(outputFile);
    }


    @Test
    public void parseArgs_UTF_file_tokenizer() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        String repoPathStr = "src/test/resources/files";
        Path outputFile = TMP_DIR.resolve("parseArgs_UTF_file_tokenizer_test.csv").toAbsolutePath();
        Path expectedFile = Paths.get("src/test/resources/expected/parseArgs_UTF_file_tokenizer_test.csv").toAbsolutePath();

        CliRequest req = CliRequest.parseArgs(
                "-tokenizer=F_UTF8" ,
                "-repo=" + Paths.get(repoPathStr).toAbsolutePath(),
                "-n=4",
                "-out=" + outputFile,
                "-ex_w_in_path=example",
                "-in=src/test/resources/files/ArgumentImpl.java",
                "-in=src/test/resources/files/Role.java",
                "-in=src/test/resources/files/empty.java");
        Assert.assertEquals(4, req.getTrainingFiles().size());
        CliRequest m = req.train();
        Object r = m.rank();
        Assert.assertTrue(r instanceof List);
        List<File> rl = (List<File>) r;
        Assert.assertFalse(Checker.isTrimNlOrEmpty(rl));
        Set<String> expectedLines, outputLines;
        try (Stream<String> lines = Files.lines(expectedFile)) {
            expectedLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        try (Stream<String> lines = Files.lines(outputFile)) {
            outputLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        Assert.assertTrue(expectedLines.containsAll(outputLines));
        Assert.assertTrue(outputLines.containsAll(expectedLines));
        Assertions.assertThat(expectedFile).hasSameTextualContentAs(outputFile);
    }


    @After
    public void tearDown() throws IOException {
        Files.walk(TMP_DIR)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }
}