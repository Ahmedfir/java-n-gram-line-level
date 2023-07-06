package cli.file;

import cli.CliRequest;
import modelling.exception.TrainingFailedException;
import modelling.infrastructure.NgramModelKylmImpl;
import output.CsvPrinter;
import parser.File;
import tokenizer.file.AbstractFileTokenizer;
import utils.Checker;
import utils.ThreadUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CliFileRequest extends CliRequest<AbstractFileTokenizer, List<File>> {
    private final static Logger LOG = Logger.getLogger(CliFileRequest.class.getSimpleName());


    public CliFileRequest(AbstractFileTokenizer tokenizer, String repoPath, List<String> cliFileRequests, String outputCsvPath) {
        super(tokenizer, repoPath, cliFileRequests, outputCsvPath, DEFAULT_SIZE, DEFAULT_THRESHOLD, DEFAULT_SMOOTHER, DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH, null, false);
    }

    public List<File> getTrainingList() throws IOException, ExecutionException, InterruptedException {
        return ThreadUtils.runParallel(File2TokenizedFileTask.fromPaths(getTrainingFiles(), repoPath, tokenizer));
    }

    public List<File> getTargetList() throws ExecutionException, InterruptedException {
        assert targetPaths != null && !targetPaths.isEmpty();
        return ThreadUtils.runParallel(File2TokenizedFileTask.fromPaths(targetPaths, repoPath, tokenizer));
    }

    // todo refactor seems very similar to the implementation of line
    public List<Iterable<String>> getTrainingSet() throws IOException, ExecutionException, InterruptedException {
        List<Iterable<String>> res = new ArrayList<>();
        for (File l : getTrainingList()) {
            if (l.getTokens() != null && l.getTokens().iterator().hasNext())
                res.add(l.getTokens());
        }
        return res;
    }

    public CliFileRequest train() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        this.model = new NgramModelKylmImpl(size, smoother, threshold);
        model.train(getTrainingSet());
        return this;
    }

    // todo refactor seems very similar to the implementation of line
    public List<File> rank() throws ExecutionException, InterruptedException, IOException {
        List<File> targetFiles = getTargetList().stream().filter(f -> f != null && f.getTokens() != null).collect(Collectors.toList());
        targetFiles.forEach(file -> file.calculateEntropy(model));
        targetFiles.sort(Comparator.comparing(File::getEntropy));
        if (!Checker.isTrimNlOrEmpty(outputCsvPath)) {
            new CsvPrinter<>(outputCsvPath, targetFiles).print();
        }
        return targetFiles;
    }

}
