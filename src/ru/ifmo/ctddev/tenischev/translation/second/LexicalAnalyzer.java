package ru.ifmo.ctddev.tenischev.translation.second;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by kris13 on 06.05.15.
 */
public class LexicalAnalyzer {
    private final InputStream input;
    private int curPos;
    private int curChar;
    private Token curToken;

    public LexicalAnalyzer(InputStream inputStream) throws ParseException {
        this.input = inputStream;
        curPos = 0;
        nextChar();
    }

    private boolean isBlank(int c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    private void nextChar() throws ParseException {
        curPos++;
        try {
            curChar = input.read();
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), curPos);
        }
    }

    public void nextToken() throws ParseException {
        while (isBlank(curChar))
            nextChar();
        switch (curChar) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    nextChar();
                } while (!isBlank(curChar) && curChar != -1);
                curToken = Token.NUMBER;
                break;
            case '+':
            case '-':
            case '*':
                nextChar();
                curToken = Token.OPERATIONS;
                break;
            case -1:
                curToken = Token.END;
                break;
            default:
                throw new ParseException("Illegal character " + (char) curChar, curPos);
        }
    }

    public Token curToken() {
        return curToken;
    }

    public int curPos() {
        return curPos;
    }
}
