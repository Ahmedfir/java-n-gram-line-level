package cli.line;

import cli.CliRequest;
import modelling.exception.TrainingFailedException;
import modelling.infrastructure.NgramModelKylmImpl;
import modelling.infrastructure.kylm.ngram.smoother.KNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.MKNSmoother;
import modelling.infrastructure.kylm.ngram.smoother.NgramSmoother;
import modelling.main.kylm.SmootherFactory;
import output.CsvPrinter;
import parser.Line;
import tokenizer.line.AbstractLineTokenizer;
import utils.Checker;
import utils.ThreadUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CliLineRequest extends CliRequest<AbstractLineTokenizer, List<Line>> {
    private final static Logger LOG = Logger.getLogger(CliLineRequest.class.getSimpleName());

    public CliLineRequest(AbstractLineTokenizer tokenizer, String repoPath, List<String> cliFileRequests, String outputCsvPath) {
        super(tokenizer, repoPath, cliFileRequests, outputCsvPath, DEFAULT_SIZE, DEFAULT_THRESHOLD, DEFAULT_SMOOTHER, DEFAULT_EXCLUDE_FILES_CONTAINING_WORDS_IN_PATH, null, false);
    }

    public List<Line> getTrainingList() throws IOException, ExecutionException, InterruptedException {
        return ThreadUtils.runMergeParallel(File2TokenizedLinesTask.fromPaths(getTrainingFiles(), repoPath, tokenizer));
    }

    public List<Line> getTargetList() throws ExecutionException, InterruptedException {
        assert targetPaths != null && !targetPaths.isEmpty();
        return ThreadUtils.runMergeParallel(File2TokenizedLinesTask.fromPaths(targetPaths, repoPath, tokenizer));
    }

    public List<Iterable<String>> getTrainingSet() throws IOException, ExecutionException, InterruptedException {
        List<Iterable<String>> res = new ArrayList<>();
        for (Line l : getTrainingList()) {
            if (l.getTokens() != null && l.getTokens().iterator().hasNext())
                res.add(l.getTokens());
        }
        return res;
    }

    public CliLineRequest train() throws IOException, ExecutionException, InterruptedException, TrainingFailedException {
        this.model = new NgramModelKylmImpl(size, smoother, threshold);
        model.train(getTrainingSet());
        return this;
    }

    public List<Line> rank() throws ExecutionException, InterruptedException, IOException {
        List<Line> targetLines = getTargetList().stream().filter(l -> l.getTokens() != null).collect(Collectors.toList());
        targetLines.forEach(line -> line.calculateEntropy(model));
        targetLines.sort(Comparator.comparing(Line::getEntropy));
        if (!Checker.isTrimNlOrEmpty(outputCsvPath)) {
            new CsvPrinter<>(outputCsvPath, targetLines).print();
        }
        return targetLines;
    }

}
