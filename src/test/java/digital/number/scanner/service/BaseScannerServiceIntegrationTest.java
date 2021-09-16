package digital.number.scanner.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Test suite for basic end-to-end functional validations.
 * Please feel free to add more test classes for the purpose of unit/integration testing of your own implementation.
 * <p>
 * Add your test cases and their expect outputs under src/test/resources testInput and testOutput directories respectively.
 * The input and output will be paired with each other if they have matching file name.
 * <p>
 * @apiNote Implement {@link #performScanning} to return list of output given an input file path.
 * Please note that this does not necessarily define the API contract of your scanner service implementation.
 */
@RunWith(Parameterized.class)
public abstract class BaseScannerServiceIntegrationTest {

    private static final String INPUT_DIR = "testInput";
    private static final String OUTPUT_DIR = "testOutput";
    private static final String ADDNL_INPUT_DIR = "additionalTestInput";

    @Parameterized.Parameters(name = "{0}")
    public static List<String> inputFileNames() throws IOException {
        Path inputDirPath = Paths.get("src", "test", "resources", INPUT_DIR);
        return Files.list(inputDirPath)
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(Collectors.toList());
    }

    @Parameterized.Parameter
    public String inputFileName;

    protected abstract List<String> performScanning(final String inputFilePath) throws Exception;

    @Test
    public final void testScanner() throws Exception {
        String inputFilePath = Paths.get("src", "test", "resources", INPUT_DIR, inputFileName).toAbsolutePath().toString();
        List<String> expectedOutput = Files.readAllLines(Paths.get("src", "test", "resources", OUTPUT_DIR, inputFileName));
        assertThat(performScanning(inputFilePath)).containsExactlyElementsOf(expectedOutput);
    }

    @Test
    public final void testScannerWithInvalidLineCount() {
        String fileWithInvalidLineCount =
                Paths.get("src", "test", "resources", ADDNL_INPUT_DIR, "invalidLineCount").toAbsolutePath().toString();
        try {
            performScanning(fileWithInvalidLineCount);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Digital number spans 1 line(s). Expecting 3!");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public final void testScannerWithInvalidCharCountInOneLine() {
        String fileWithInvalidCharCountInOneLine =
                Paths.get("src", "test", "resources", ADDNL_INPUT_DIR, "invalidCharCountInOneLine").toAbsolutePath().toString();
        try {
            performScanning(fileWithInvalidCharCountInOneLine);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Digital number contains 20 character(s) in one line. Expecting 27!");
        } catch (Exception e) {
            fail();
        }
    }
}
