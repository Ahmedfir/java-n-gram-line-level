package cli.line;

import org.junit.Before;
import org.junit.Test;
import parser.Line;
import tokenizer.line.AbstractLineTokenizer;
import tokenizer.line.UTFLineTokenizer;
import tokenizer.line.java.JavaLemmeLineTokenizer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class File2TokenizedLinesTaskTest {
    private List<Integer> commentLines;
    private List<Integer> codeLines;
    private String javaFile = "src/test/resources/files/ControlledNumber.java";
    private int fileLinesCount = 84;
    private Set<Integer> closingBraketsLines;
    private Set<Integer> emptyCommentLines;


    @Before
    public void setUp() {
        commentLines = range(1, 16);
        commentLines.addAll(range(23, 25));
        commentLines.addAll(range(31, 33));
        commentLines.addAll(range(36, 38));
        commentLines.addAll(range(43, 56));

        codeLines = range(19, 21);
        codeLines.add(17);
        codeLines.add(26);
        codeLines.add(27);
        codeLines.add(29);
        codeLines.add(34);
        codeLines.add(39);
        codeLines.addAll(range(57, 67));
        codeLines.add(69);
        codeLines.add(70);
        codeLines.addAll(range(72, 75));
        codeLines.addAll(range(77, 81));
        codeLines.add(84);

        closingBraketsLines = new HashSet<>(range(79, 81));
        closingBraketsLines.add(84);
        closingBraketsLines.add(75);

        emptyCommentLines = new HashSet<>();
        emptyCommentLines.add(1);
        emptyCommentLines.add(8);
        emptyCommentLines.add(10);
        emptyCommentLines.add(16);
        emptyCommentLines.add(23);
        emptyCommentLines.add(25);
        emptyCommentLines.add(31);
        emptyCommentLines.add(33);
        emptyCommentLines.add(36);
        emptyCommentLines.add(38);
        emptyCommentLines.add(44);
        emptyCommentLines.add(56);
    }

    @Test
    public void test_call__java_lemm_line__comments_spaces_code() throws Exception {
        AbstractLineTokenizer tokenizer = new JavaLemmeLineTokenizer();
        File2TokenizedLinesTask file2TokenizedLinesTask = new File2TokenizedLinesTask(javaFile, "src/test/resources/files", tokenizer);
        List<Line> lines = file2TokenizedLinesTask.call();
        for (Line line : lines) {
            int ln = line.getLineNumber();
            if (commentLines.contains(ln)){
                assertEquals(ln + " in comments but line has tokens.", "\n", line.getTokens().iterator().next());
            }
        }
        int countLineBreaks = 0;
        for (Line line : lines) {
            if ("\n".equals(line.getTokens().iterator().next())) countLineBreaks++;
        }
        assertEquals(codeLines.size(), lines.size() - countLineBreaks);
    }


    @Test
    public void test_call__utf8__comments_spaces_code() throws Exception {
        AbstractLineTokenizer tokenizer = new UTFLineTokenizer();
        File2TokenizedLinesTask file2TokenizedLinesTask = new File2TokenizedLinesTask(javaFile, "src/test/resources/files", tokenizer);
        List<Line> lines = file2TokenizedLinesTask.call();
        Set<Integer> returnedLines = new HashSet<>();
        for (Line line : lines) {
            returnedLines.add(line.getLineNumber());
        }
        Set<Integer> missingCodeLines = new HashSet<>(codeLines);
        missingCodeLines.removeAll(returnedLines);
        assertEquals(closingBraketsLines,missingCodeLines);
        Set<Integer> missingCommentLines = new HashSet<>(commentLines);
        missingCommentLines.removeAll(returnedLines);
        assertEquals(emptyCommentLines,missingCommentLines);
    }


    /**
     * @see <a href="URL#https://stackoverflow.com/a/30020546/3014036">https://stackoverflow.com/a/30020546/3014036</a>
     */
    private List<Integer> range(int start, int end) {
        return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }


}