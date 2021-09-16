package digital.number.scanner.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * DigitalNumberScannerService - Scans a file containing numbers coded in a digital format
 * using only symbols | _ | and interprets and outputs the actual number
 */
public class DigitalNumberScannerService
{
    private static final Map<String, String> symbolToDigitMap = new HashMap<>();
    private static final String QUESTION_MARK = "?";
    private static final String ILLEGAL_CHAR = "ILL";
    private static final char SPACE = ' ';
    private static final char UNDER_SCORE = '_';
    private static final char PIPE = '|';

    static {
        symbolToDigitMap.put(" _ | ||_|", "0");
        symbolToDigitMap.put("     |  |", "1");
        symbolToDigitMap.put(" _  _||_ ", "2");
        symbolToDigitMap.put(" _  _| _|", "3");
        symbolToDigitMap.put("   |_|  |", "4");
        symbolToDigitMap.put(" _ |_  _|", "5");
        symbolToDigitMap.put(" _ |_ |_|", "6");
        symbolToDigitMap.put(" _   |  |", "7");
        symbolToDigitMap.put(" _ |_||_|", "8");
        symbolToDigitMap.put(" _ |_| _|", "9");
    }

    /**
     * Processes a single input file containing the digital number
     * @param inputFilePath - Full path of the input file
     * @return List containing the converted numbers in string format
     * @throws IOException - throws IO exception encountered during processing
     */
    public List<String> processFile(String inputFilePath) throws IOException {
        List<String> outputNumbers = new ArrayList<>();
        //Opens the file with explicit READ option to avoid locking the file for reading
        try(InputStream is = Files.newInputStream(Paths.get(inputFilePath), StandardOpenOption.READ)) {
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            String inputLine;
            List<String> numberLines = new ArrayList<>();
            while((inputLine = br.readLine()) != null) {
                if(inputLine.trim().isEmpty()) { //Encountered a blank line signifying end of one number
                    if(numberLines.size() > 0) {
                        outputNumbers.add(processSingleDigitalNumber(numberLines));
                        numberLines.clear();
                    }
                }
                else {
                    numberLines.add(inputLine);
                }
            }
            if(numberLines.size() > 0) {
                outputNumbers.add(processSingleDigitalNumber(numberLines));
                numberLines.clear();
            }
        }
        return outputNumbers;
    }

    /**
     * Processes one digital number coded in three lines of input
     * @param numberLines - Lines containing the coded digital number
     * @return Decoded number in String format
     */
    private String processSingleDigitalNumber(List<String> numberLines) {
        StringBuilder numberString = new StringBuilder();
        List<String[]> symbolArrayList = readSymbolsAcrossThreeLines(numberLines);
        String[] digitCode = InterpretAndConsolidateSymbols(symbolArrayList);
        String[] matchedDigits = translateSymbolsToDigits(digitCode);
        Arrays.stream(matchedDigits).forEach(numberString::append);
        return numberString.toString();
    }

    /**
     * Given a 3 line digital number as input, breaks each 27 char line into
     * 3 char wide substrings. Validates each line for illegal characters
     * and replaces any found with question mark
     * @param numberLines - Lines containing one encoded digital number
     * @return List containing lines split into array of 3 char wide substrings
     */
    private List<String[]> readSymbolsAcrossThreeLines(List<String> numberLines) {
        Objects.requireNonNull(numberLines);
        //This method gets called only if there are lines to be processed
        //as these are validated prior to calling this method
        if(numberLines.size() != 3) {//Encountered number that does not span exactly 3 lines
            StringBuilder exMessage;
            exMessage = new StringBuilder("Digital number spans " + numberLines.size()
                    + " line(s). Expecting 3! \n");
            for (String numberLine : numberLines) {
                exMessage.append(numberLine);
            }
            throw new IllegalArgumentException(exMessage.toString());
        }

        List<String[]> symbolCharMatrix = new ArrayList<>();
        for (String numberLine : numberLines) {
            if(numberLine.length() != 27) {
                String exMessage;
                exMessage = "Digital number contains " + numberLine.length()
                        + " character(s) in one line. Expecting 27! \n";
                exMessage += numberLine;
                throw new IllegalArgumentException(exMessage);
            }
            numberLine = validateSymbols(numberLine);
            String [] subLines = numberLine.split("(?<=\\G.{3})");
            symbolCharMatrix.add(subLines);
        }

        return symbolCharMatrix;
    }

    /**
     * Given list of lines corresponding to one encoded digital number, each containing
     * array of 3 char wide split substrings, scans across the 3 line list to read the
     * 3 x 3 char matrix and concatenates into one string per number
     * @param symbolArrayList - List of lines containing 3 char wide split substring array
     * @return String array containing one concatenated string per number
     */
    private String[] InterpretAndConsolidateSymbols(List<String[]> symbolArrayList) {
        String [] fullDigitCode = new String[9];
        //Validations on the number of lines and the number of characters per line
        //are done prior to calling this method
        String[] firstLine = symbolArrayList.get(0);
        String[] secondLine = symbolArrayList.get(1);
        String[] thirdLine = symbolArrayList.get(2);

        //Each line has already been validated to be 27 characters and has been split into
        //9 substrings 3 character wide
        for(int i=0; i<9; i++) {
            fullDigitCode[i] = firstLine[i] + secondLine[i] + thirdLine[i];
        }

        return fullDigitCode;
    }

    /**
     * Given an array of strings containing 3 x 3 char digital number code
     * concatenated into single string, converts into equivalent decoded
     * number by looking up in pre-defined map. If any question marks are
     * present or the code symbol in input string is not found in map,
     * convert these into "ILL" in the output string
     * @param digitCode - Input string array containing single concatenated
     *                  code per digital number digit
     * @return String containing converted / decoded number
     */
    private String[] translateSymbolsToDigits(String[] digitCode) {
        String[] digits = new String[9];
        int index = 0;
        for(String symbol : digitCode) {
            String digit;
            if(symbol.contains(QUESTION_MARK)) {
                digit = ILLEGAL_CHAR;
            } else {
                digit = symbolToDigitMap.get(symbol);
                if (digit == null) {
                    digit = ILLEGAL_CHAR;
                }
            }
            digits[index++] = digit;
        }
        return digits;
    }

    /**
     * Validates characters in the input digital number code string against
     * pre-defined set of allowed characters and converts any non-conforming
     * entries to question marks
     * @param symbolString - Input string containing the digital number codes
     * @return Validated string modified for any non-confirming characters
     */
    private String validateSymbols(String symbolString) {
        StringBuilder strBuilder = new StringBuilder();
        for(char charSymbol : symbolString.toCharArray()) {
            if(charSymbol == SPACE || charSymbol == UNDER_SCORE || charSymbol == PIPE) {
                strBuilder.append(charSymbol);
            }
            else {
                strBuilder.append(QUESTION_MARK);
            }
        }
        return strBuilder.toString();
    }
}