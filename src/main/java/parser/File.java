package parser;

import modelling.infrastructure.NgramModelKylmImpl;
import output.CsvPrinter;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class File implements Serializable, CsvPrinter.CsvLine {
    private static final String HEADERS = "file,entropy,tokensCount";
    private final String filePath;
    private final transient Iterable<String> tokens;
    private final long tokensCount;
    private Double entropy = null;

    public File(String filePath, Iterable<String> tokens) {
        this.filePath = filePath;
        this.tokens = tokens;
        this.tokensCount = StreamSupport.stream(tokens.spliterator(), false).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File line = (File) o;
        return Objects.equals(filePath, line.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
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
        return filePath + "," + entropy + "," + tokensCount;
    }

}
