package ru.ifmo.ctddev.tenischev.translation.second;

import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by kris13 on 07.05.15.
 */
public class Parser {
    private LexicalAnalyzer lex;

    private Tree E() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER:
                Tree firstN = N();
                Tree secondEPrime = EPrime();
                if (lex.curToken() == Token.NUMBER)
                    throw new ParseException("Sign operation or end string expected at position ", lex.curPos());
                return new Tree("E", firstN, secondEPrime);
            default:
                throw new ParseException("Number expected at position ", lex.curPos());
        }
    }

    private Tree EPrime() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER:
                Tree firstF = F();
                if (lex.curToken() == Token.NUMBER)
                    throw new ParseException("Sign operation or end expression expected at position ", lex.curPos());
                return new Tree("E'", firstF);
            case OPERATIONS:
            case END:
                return new Tree("E'");
            default:
                throw new AssertionError();
        }
    }

    private Tree F() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER:
                Tree firstE = E();
                if (lex.curToken() != Token.OPERATIONS)
                    throw new ParseException("Sign operation expected at position ", lex.curPos());
                Tree secondO = O();
                Tree thirdFPrime = FPrime();
                if (lex.curToken() == Token.NUMBER)
                    throw new ParseException("Sign operation or end expression expected at position ", lex.curPos());
                return new Tree("F", firstE, secondO, thirdFPrime);
            default:
                throw new ParseException("Number expected at position ", lex.curPos());
        }
    }

    private Tree FPrime() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER:
                Tree firstF = F();
                if (lex.curToken() == Token.NUMBER)
                    throw new ParseException("Sign operation or end expression expected at position ", lex.curPos());
                return new Tree("F'", firstF);
            case OPERATIONS:
            case END:
                return new Tree("F'");
            default:
                throw new AssertionError();
        }
    }

    private Tree N() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER:
                //[0-9]+
                lex.nextToken();
                return new Tree("n");
            default:
                throw new ParseException("Number expected at position ", lex.curPos());
        }
    }

    private Tree O() throws ParseException {
        switch (lex.curToken()) {
            case OPERATIONS:
                //+,-,*
                lex.nextToken();
                return new Tree("o");
            default:
                throw new ParseException("Sign operation expected at position ", lex.curPos());
        }
    }

    Tree parse(InputStream inputStream) throws ParseException {
        lex = new LexicalAnalyzer(inputStream);
        lex.nextToken();
        Tree ans = E();
        if (lex.curToken() != Token.END)
            throw new ParseException("End expression expected at position ", lex.curPos());
        return ans;
    }
}
