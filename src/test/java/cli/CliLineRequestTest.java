package cli;

import modelling.exception.TrainingFailedException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CliLineRequestTest {

    private static final Path TMP_DIR = Paths.get("src/test/resources/tmp").toAbsolutePath();

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(TMP_DIR);
    }

    @Test
    public void parseArgs_JP_tokenizer_tests() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        String repoPathStr = "src/test/resources/test";
        Path outputFile = TMP_DIR.resolve("parseArgs_JP_tokenizer_tests_test.csv").toAbsolutePath();
        Path expectedFile = Paths.get("src/test/resources/expected/parseArgs_JP_tokenizer_tests_test.csv").toAbsolutePath();

        CliRequest req = CliRequest.parseArgs(
                "-repo=" + Paths.get(repoPathStr).toAbsolutePath(),
                "-n=4",
                "-out=" + outputFile,
                "-ex_w_in_path=",
                "-inc_w_in_path=" + "test",
                "-inc_w_in_path=" + "Test",
                "-inc_neighbours_w_in_path",
                "-in=src/test/resources/test/dummydir/SpringEarlyFeatureUsageTest.java",
                "-in=src/test/resources/test/dummydir/SpringFeatureConfiguration.java"
                );
        Assert.assertEquals(5, req.getTrainingFiles().size());
        req.train().rank();
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

    // fixme: tests run fine when each one is run separately.
    //@Test
    public void parseArgs_JP_tokenizer() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        String repoPathStr = "src/test/resources/files";
        Path outputFile = TMP_DIR.resolve("parseArgs_JP_tokenizer_test.csv").toAbsolutePath();
        Path expectedFile = Paths.get("src/test/resources/expected/parseArgs_JP_tokenizer_test.csv").toAbsolutePath();

        CliRequest req = CliRequest.parseArgs(
                "-repo=" + Paths.get(repoPathStr).toAbsolutePath(),
                "-n=4",
                "-out=" + outputFile,
                "-ex_w_in_path=example",
                "-in=src/test/resources/files/ArgumentImpl.java",
                "-in=src/test/resources/files/Role.java",
                "-in=src/test/resources/files/empty.java");
        Assert.assertEquals(4, req.getTrainingFiles().size());
        req.train().rank();
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

    // fixme: tests run fine when each one is run separately.
    //@Test
    public void parseArgs_UTF8_tokenizer() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        String tokenizerParam = "UTF8";
        String repoPathStr = "src/test/resources/files";
        Path outputFile = TMP_DIR.resolve("parseArgs_UTF8_tokenizer_test.csv").toAbsolutePath();
        Path expectedFile = Paths.get("src/test/resources/expected/parseArgs_UTF8_tokenizer_test.csv").toAbsolutePath();

        CliRequest req = CliRequest.parseArgs(
                "-tokenizer=" + tokenizerParam,
                "-repo=" + Paths.get(repoPathStr).toAbsolutePath(),
                "-n=4",
                "-out=" + outputFile,
                "-ex_w_in_path=example",
                "-in=src/test/resources/files/ArgumentImpl.java",
                "-in=src/test/resources/files/Role.java",
                "-in=src/test/resources/files/empty.java");
        Assert.assertEquals(4, req.getTrainingFiles().size());
        req.train().rank();
        Set<String> expectedLines, outputLines;
        try (Stream<String> lines = Files.lines(expectedFile)) {
            expectedLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        try (Stream<String> lines = Files.lines(outputFile)) {
            outputLines = lines.collect(Collectors.toCollection(HashSet::new));
        }
        Assert.assertTrue(expectedLines.containsAll(outputLines));
        Assert.assertTrue(outputLines.containsAll(expectedLines));
    }


    @After
    public void tearDown() throws IOException {
        Files.walk(TMP_DIR)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}