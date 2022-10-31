package cli;

import gitutils.FilesOfInterest;
import modelling.exception.TrainingFailedException;
import modelling.infrastructure.NgramModelKylmImpl;
import modelling.infrastructure.kylm.ngram.smoother.KNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.MKNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.NgramSmoother;
import modelling.main.kylm.SmootherFactory;
import output.CsvPrinter;
import parser.Line;
import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;
import utils.Checker;
import utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CliRequest {
    private final static Logger LOG = Logger.getLogger(CliRequest.class.getSimpleName());
    /**
     * @see {https://orbilu.uni.lu/bitstream/10993/36135/1/icsme3.pdf}
     */
    private static final int DEFAULT_SIZE = 4;
    private static final int DEFAULT_THRESHOLD = 1;

    /**
     * Current implementation trains and ranks lines.
     * Because of the small number of tokens (per line), the default smoother {@link MKNSmoother} is not applicable.
     *
     * @see for more details on the default setup for File level ranking: {@link defectstudy/Application.java}.
     */
    private static final NgramSmoother DEFAULT_SMOOTHER = SmootherFactory.create(KNSmoother.ABV);
    // TODO: 29/10/2022 document this behaviour
    private static final List<String> DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH = Arrays.asList("test", "example");

    private final String repoPath;
    private final List<String> targetPaths;
    private final String outputCsvPath;
    private final AbstractLineTokenizer tokenizer;
    private boolean includeNeighbours;
    private int size;
    private int threshold;
    private NgramSmoother smoother;
    private NgramModelKylmImpl model;
    private List<String> excludeFilesContainingWordsInPath;
    private List<String> includeFilesContainingWordsInPath;


    public static CliRequest parseArgs(String... args) {
        String outputPath = null;
        String repoPath = null;
        List<String> files = new ArrayList<>();
        List<String> excludeFilesContainingWordsInPath = null;
        List<String> includeFilesContainingWordsInPath = null;
        boolean includeNeighbours = false;
        int size = DEFAULT_SIZE;
        int threshold = DEFAULT_THRESHOLD;
        AbstractLineTokenizer tokenizer = null;

        for (String arg : args) {

            CliArgPrefix cliArgPrefix = CliArgPrefix.startsWithPrefix(arg);
            String argBody = arg.replace(cliArgPrefix.argPrefix, "");

            switch (cliArgPrefix) {
                case FILE_INCLUDE_REQUEST:
                    files.add(argBody);
                    break;
                case SIZE:
                    size = Integer.valueOf(argBody);
                    break;
                case OUTPUT_FILE:
                    outputPath = argBody;
                    break;
                case THRESHOLD:
                    threshold = Integer.parseInt(argBody);
                    break;
                case REPO:
                    repoPath = argBody;
                    break;
                case EXCLUDE_FILES_WITH_WORD_IN_PATH:
                    if (excludeFilesContainingWordsInPath == null) {
                        excludeFilesContainingWordsInPath = new ArrayList<>();
                    }
                    excludeFilesContainingWordsInPath.add(argBody);
                    break;
                case INCLUDE_FILES_WITH_WORD_IN_PATH:
                    if (includeFilesContainingWordsInPath == null) {
                        includeFilesContainingWordsInPath = new ArrayList<>();
                    }
                    includeFilesContainingWordsInPath.add(argBody);
                    break;
                case INCLUDE_FILE_NEIGHBOURS_WITH_WORD_IN_PATH:
                    includeNeighbours = true;
                    break;
                case TOKENIZER:
                    tokenizer = CliArgTokenizer.newTokenizer(argBody);
                    break;
                case LEVEL:
                default:
                    throw new IllegalArgumentException(cliArgPrefix + " choice not implemented.");
            }

        }
        assert !Checker.isTrimNlOrEmpty(repoPath);
        assert !Checker.isTrimNlOrEmpty(files);

        if (tokenizer == null) {
            tokenizer = new JavaLemmeLineTokenizer();
        }

        if (excludeFilesContainingWordsInPath == null) {
            excludeFilesContainingWordsInPath = DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH;
        } else if (excludeFilesContainingWordsInPath.size() == 0 || excludeFilesContainingWordsInPath.stream().allMatch(w -> w == null || w.length() == 0)) {
            excludeFilesContainingWordsInPath = null;
        }

        CliRequest cliRequest = new CliRequest(tokenizer, repoPath, files, outputPath);
        cliRequest.threshold = threshold;
        cliRequest.size = size;
        cliRequest.smoother = DEFAULT_SMOOTHER;
        cliRequest.excludeFilesContainingWordsInPath = excludeFilesContainingWordsInPath;
        cliRequest.includeFilesContainingWordsInPath = includeFilesContainingWordsInPath;
        cliRequest.includeNeighbours = includeNeighbours;
        return cliRequest;
    }

    public CliRequest(AbstractLineTokenizer tokenizer, String repoPath, List<String> cliFileRequests, String outputCsvPath) {
        this(tokenizer, repoPath, cliFileRequests, outputCsvPath, DEFAULT_SIZE, DEFAULT_THRESHOLD, DEFAULT_SMOOTHER, DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH, null, false);
    }

    public CliRequest(AbstractLineTokenizer tokenizer, String repoPath, List<String> cliFileRequests, String outputCsvPath, int size, int threshold, NgramSmoother smoother, List<String> excludeFilesContainingWordsInPath, List<String> includeFilesContainingWordsInPath, boolean includeNeighbours) {
        this.repoPath = repoPath;
        File repoFile = new File(repoPath);
        if (!repoFile.exists() || !repoFile.isDirectory()) {
            LOG.severe(String.format("Repo directory not found: %s ", repoPath));
            throw new IllegalArgumentException("Wrong repo path.");
        }
        this.targetPaths = new ArrayList<>();
        for (String strP : cliFileRequests) {
            File file = new File(strP);
            String strp = file.exists() ? file.getAbsolutePath() : Paths.get(repoPath).resolve(strP).toAbsolutePath().toString();
            if (!new File(strp).exists()) {
                LOG.severe(String.format("File not found: \n %s \n %s", strP, strp));
                throw new IllegalArgumentException("Wrong file path.");
            }
            this.targetPaths.add(strp);
        }
        this.tokenizer = tokenizer;
        this.outputCsvPath = outputCsvPath;
        this.size = size;
        this.threshold = threshold;
        this.smoother = smoother;
        this.excludeFilesContainingWordsInPath = excludeFilesContainingWordsInPath;
        this.includeFilesContainingWordsInPath = includeFilesContainingWordsInPath;
        this.includeNeighbours = includeNeighbours;
    }

    // @see experiments
    public List<String> getTrainingFiles() throws IOException {
        Set<String> paths;
        if (includeFilesContainingWordsInPath != null) {
            paths = new HashSet<>();
            for (String w : includeFilesContainingWordsInPath) {
                paths.addAll(FilesOfInterest.list(repoPath, "java", w));
            }
        } else {
            paths = new HashSet<>(FilesOfInterest.list(repoPath, "java"));
        }
        if (includeNeighbours) {
            Set<String> neighbours = new HashSet<>();
            for (String p : paths) {
                neighbours.addAll(FilesOfInterest.list(Paths.get(p).getParent().toAbsolutePath().toString(), "java"));
            }
            paths.addAll(neighbours);
        }
        if (excludeFilesContainingWordsInPath != null) {
            return new ArrayList<>(paths).stream().filter(path -> !(excludeFilesContainingWordsInPath.stream().anyMatch(path::contains) || targetPaths.contains(path))).collect(Collectors.toList());
        } else {
            return new ArrayList<>(paths);
        }
    }

    public List<Line> getTrainingLines() throws IOException, ExecutionException, InterruptedException {
        return ThreadUtils.runMergeParallel(File2TokenizedLinesTask.fromPaths(getTrainingFiles(), repoPath, tokenizer));
    }

    public List<Line> getTargetLines() throws ExecutionException, InterruptedException {
        assert targetPaths != null && !targetPaths.isEmpty();
        return ThreadUtils.runMergeParallel(File2TokenizedLinesTask.fromPaths(targetPaths, repoPath, tokenizer));
    }

    public List<Iterable<String>> getTrainingSet() throws IOException, ExecutionException, InterruptedException {
        List<Iterable<String>> res = new ArrayList<>();
        for (Line l : getTrainingLines()) {
            if (l.getTokens() != null && l.getTokens().iterator().hasNext())
                res.add(l.getTokens());
        }
        return res;
    }

    public CliRequest train() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        this.model = new NgramModelKylmImpl(size, smoother, threshold);
        model.train(getTrainingSet());
        return this;
    }

    public List<Line> rank() throws ExecutionException, InterruptedException, IOException {
        List<Line> targetLines = getTargetLines().stream().filter(l -> l.getTokens() != null).collect(Collectors.toList());
        targetLines.forEach(line -> line.calculateEntropy(model));
        targetLines.sort(Comparator.comparing(Line::getEntropy));
        if (!Checker.isTrimNlOrEmpty(outputCsvPath)) {
            new CsvPrinter<>(outputCsvPath, targetLines).print();
        }
        return targetLines;
    }

}
