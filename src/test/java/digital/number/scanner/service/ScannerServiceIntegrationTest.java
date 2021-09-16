package digital.number.scanner.service;

import java.util.List;

public class ScannerServiceIntegrationTest extends BaseScannerServiceIntegrationTest {
    @Override
    protected List<String> performScanning(String inputFilePath) throws Exception {
        return new DigitalNumberScannerService().processFile(inputFilePath);
    }
}
