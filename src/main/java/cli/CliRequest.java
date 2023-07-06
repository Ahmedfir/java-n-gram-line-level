package cli;

import cli.file.CliFileRequest;
import cli.line.CliLineRequest;
import gitutils.FilesOfInterest;
import modelling.exception.TrainingFailedException;
import modelling.infrastructure.NgramModelKylmImpl;
import modelling.infrastructure.kylm.ngram.smoother.KNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.MKNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.NgramSmoother;
import modelling.main.kylm.SmootherFactory;
import tokenizer.AbstractTokenizer;
import tokenizer.file.AbstractFileTokenizer;
import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;
import utils.Checker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class CliRequest<T extends AbstractTokenizer, Res> {
    private final static Logger LOG = Logger.getLogger(CliRequest.class.getSimpleName());
    /**
     * @see {https://orbilu.uni.lu/bitstream/10993/36135/1/icsme3.pdf}
     */
    protected static final int DEFAULT_SIZE = 4;
    protected static final int DEFAULT_THRESHOLD = 1;

    /**
     * Current implementation trains and ranks lines.
     * Because of the small number of tokens (per line), the default smoother {@link MKNSmoother} is not applicable.
     *
     * @see for more details on the default setup for File level ranking:
     * {https://github.com/Ahmedfir/tuna-FL/tree/master/experiment/src/main/java/defectstudy/Application.java}.
     */
    protected static final NgramSmoother DEFAULT_SMOOTHER = SmootherFactory.create(KNSmoother.ABV);
    // protected static final NgramSmoother DEFAULT_SMOOTHER = SmootherFactory.create(MKNSmoother.ABV);
    // TODO: 29/10/2022 document this behaviour
    protected static final List<String> DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH = Arrays.asList("test", "example");

    protected final String repoPath;
    protected final List<String> targetPaths;
    protected final String outputCsvPath;
    protected final T tokenizer;
    private boolean includeNeighbours;
    protected int size;
    protected int threshold;
    protected NgramSmoother smoother;
    protected NgramModelKylmImpl model;
    private List<String> excludeFilesContainingWordsInPath;
    private List<String> includeFilesContainingWordsInPath;

    /**
     * factory method that returns the appropriate method depending on the command-line args.
     *
     * @param args command-line args. @see {@link CliArgPrefix}
     * @return CliRequest instance depending on the command-line args.
     */
    public static CliRequest<? extends AbstractTokenizer, ?> parseArgs(String... args) {
        String outputPath = null;
        String repoPath = null;
        List<String> files = new ArrayList<>();
        List<String> excludeFilesContainingWordsInPath = null;
        List<String> includeFilesContainingWordsInPath = null;
        boolean includeNeighbours = false;
        int size = DEFAULT_SIZE;
        int threshold = DEFAULT_THRESHOLD;
        AbstractTokenizer tokenizer = null;

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
        CliRequest cliRequest;
        if (tokenizer instanceof AbstractLineTokenizer) {
            cliRequest = new CliLineRequest((AbstractLineTokenizer) tokenizer, repoPath, files, outputPath);
        } else if (tokenizer instanceof AbstractFileTokenizer) {
            cliRequest = new CliFileRequest((AbstractFileTokenizer) tokenizer, repoPath, files, outputPath);
        } else {
            throw new IllegalArgumentException(tokenizer.getClass().getSimpleName() + " is not handled.");
        }


        cliRequest.threshold = threshold;
        cliRequest.size = size;
        cliRequest.excludeFilesContainingWordsInPath = excludeFilesContainingWordsInPath;
        cliRequest.includeFilesContainingWordsInPath = includeFilesContainingWordsInPath;
        cliRequest.includeNeighbours = includeNeighbours;
        return cliRequest;
    }

    public CliRequest(T tokenizer, String repoPath, List<String> cliFileRequests, String outputCsvPath, int size, int threshold, NgramSmoother smoother, List<String> excludeFilesContainingWordsInPath, List<String> includeFilesContainingWordsInPath, boolean includeNeighbours) {
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

    public abstract Res getTrainingList() throws IOException, ExecutionException, InterruptedException;

    public abstract Res getTargetList() throws ExecutionException, InterruptedException;

    public abstract CliRequest<T, Res> train() throws IOException, ExecutionException, InterruptedException, TrainingFailedException;

    public abstract Res rank() throws ExecutionException, InterruptedException, IOException;
}
