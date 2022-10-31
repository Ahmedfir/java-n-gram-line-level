package output;


import utils.Checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CsvPrinter<T extends CsvPrinter.CsvLine> {
    private final static Logger LOG = Logger.getLogger(CsvPrinter.class.getSimpleName());
    private final String filePath;
    private final List<T> lines;

    public CsvPrinter(String filePath, List<T> lines) {
        this.filePath = filePath;
        this.lines = lines;
    }

    public void print() throws IOException {
        if (Checker.isTrimNlOrEmpty(lines)){
            LOG.severe("Csv printing FAILED: print() called with empty lines!");
            return;
        }
        String headers = lines.get(0).getHeader();
        Path outputFilePath = Paths.get(filePath);
        if (!outputFilePath.getParent().toFile().exists()) {
            Files.createDirectories(outputFilePath.getParent());
        }
        List<String> outLines = new ArrayList<>();
        outLines.add(headers);
        outLines.addAll(lines.stream().map(CsvLine::toCsv).collect(Collectors.toList()));
        Files.write(outputFilePath, outLines);
    }

    public interface CsvLine{
        String getHeader();
        String toCsv();
    }
}
