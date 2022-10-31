package parser;

import modelling.infrastructure.NgramModelKylmImpl;
import output.CsvPrinter;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class Line implements Serializable, CsvPrinter.CsvLine {
    private static final String HEADERS = "file,line,entropy,tokens_count";
    private final String filePath;
    private final int lineNumber;
    private final transient Iterable<String> tokens;
    private final long tokensCount;
    private Double entropy = null;

    public Line(String filePath, int lineNumber, Iterable<String> tokens) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.tokens = tokens;
        this.tokensCount = StreamSupport.stream(tokens.spliterator(), false).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return lineNumber == line.lineNumber && Objects.equals(filePath, line.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, lineNumber);
    }

    public Iterable<String> getTokens() {
        return tokens;
    }

    public void calculateEntropy(NgramModelKylmImpl model) {
        if (tokens != null)
            this.entropy = model.crossEntropy(tokens);
    }

    public Double getEntropy() {
        return entropy;
    }

    @Override
    public String getHeader() {
        return HEADERS;
    }

    @Override
    public String toCsv() {
        return filePath + "," + lineNumber + "," + entropy + "," + tokensCount;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
