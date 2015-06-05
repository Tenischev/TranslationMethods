package ru.ifmo.ctddev.tenischev.translation.fourth;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by kris13 on 03.06.15.
 */
public class ParseGenerator {
    private final String outPackage;
    private final String outPath;

    public static void main(String[] args) throws ParseException {
        if (args.length == 0 || args.length > 2)
            System.out.println("Not specified file with grammar");
        else {
            String defPackage = "out";
            if (args.length == 2)
                 defPackage = args[1];
            File grammar = new File(".", args[0]);
            if (!grammar.exists())
                System.out.println("Grammar file not exists");
            else
                new ParseGenerator(grammar, defPackage);
        }
    }

    public ParseGenerator(File grammar, String outPackage) throws ParseException {
        this.outPackage = outPackage;
        outPath = "src" + File.separator + outPackage.replaceAll("[.]", File.separator);
        try (FileReader fileReader = new FileReader(grammar)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                StringBuilder text = new StringBuilder();
                bufferedReader.lines().forEach(s -> text.append(s.contains("#") ? s.substring(0, s.indexOf("#")) : s));
                parseGrammar(text.toString().replaceAll("[\n]", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseGrammar(String grammar) throws ParseException {
        Map<String, String> tokenMap = new HashMap<>();
        Map<String, List<Pair<String, String>>> attrMap = new HashMap<>();
        Map<String, List<List<String>>> ruleMap = new HashMap<>();
        List<String> rules = new ArrayList<>();
        int l =  0;
        int bal = 0;
        for (int i = 0; i < grammar.length(); i++) {
            if (bal == 0)
                if (grammar.charAt(i) == ';') {
                    rules.add(grammar.substring(l, i));
                    l = i + 1;
                }
            if ('{' == grammar.charAt(i))
                bal++;
            else if ('}' == grammar.charAt(i))
                bal--;
        }
        for (String rule : rules) {
            if (!rule.contains(":")) {
                System.err.println("Not found delimiter ':' in rule '" + rule + "'");
                throw new ParseException("Not found delimiter", -1);
            }
            String[] pieces = rule.split(" : ");
            String left = pieces[0].replaceAll(" ", "");
            if (left.matches("[A-Z]+")) {
                if (tokenMap.containsKey(left)) {
                    System.err.println("Rule for '" + left + "' already exists");
                    throw new ParseException("Rule with same name", -1);
                }
                tokenMap.put(left, pieces[1]);
            } else if (left.matches("[a-z][a-zA-Z]*")) {
                String[] variables = pieces[1].split("[|]");
                for (String variable : variables) {
                    List<String> tokens = new ArrayList<>();
                    l =  0;
                    bal = 0;
                    for (int i = 0; i < variable.length(); i++) {
                        if (bal == 0)
                            if (variable.charAt(i) == ' ') {
                                if (i != l)
                                    tokens.add(variable.substring(l, i));
                                l = i + 1;
                            }
                        if ('{' == variable.charAt(i))
                            bal++;
                        else if ('}' == variable.charAt(i))
                            bal--;
                    }
                    if (variable.length() != l)
                        tokens.add(variable.substring(l, variable.length()));
                    if (tokens.size() == 3 && ".".equals(tokens.get(1))) {
                        List<Pair<String, String>> list = attrMap.get(left);
                        if (list == null) {
                            list = new ArrayList<>();
                            attrMap.put(left, list);
                        }
                        list.add(new Pair<>(tokens.get(0), tokens.get(2)));
                    } else {
                        List<List<String>> element = ruleMap.get(left);
                        if (element == null) {
                            element = new ArrayList<>();
                            ruleMap.put(left, element);
                        }
                        element.add(tokens);
                    }
                }
            } else {
                System.err.println("Name isn't correct in rule '" + rule + "'");
                throw new ParseException("Error in name", -1);
            }
        }
        constructParser(tokenMap, attrMap, ruleMap);
    }

    private void constructParser(Map<String, String> tokenMap, Map<String, List<Pair<String, String>>> attrMap, Map<String, List<List<String>>> ruleMap) throws ParseException {
        File folderPackage = new File(outPath);
        folderPackage.mkdirs();
        try {
            generateTokenFile(tokenMap.keySet());
            generateLexicalAnalyzer(tokenMap);
            generateTree(tokenMap, attrMap, ruleMap);
            generateParser(tokenMap, ruleMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateParser(Map<String, String> tokenMap, Map<String, List<List<String>>> ruleMap) throws IOException {
        File parser = new File(outPath, "Parser.java");
        if (parser.exists())
            parser.delete();
        parser.createNewFile();
        try (FileWriter writer = new FileWriter(parser)) {
            writer.write("package " + outPackage + ";\n");
            writer.write("import java.io.InputStream;\n");
            writer.write("import java.text.ParseException;\n");
            writer.write("import java.util.HashMap;\n");
            writer.write("import java.util.Map;\n");
            writer.write("import java.util.Set;\n");
            writer.write("import java.util.HashSet;\n");
            writer.write("public class Parser {\n");
            writer.write("    private LexicalAnalyzer lex;\n");
            for (Map.Entry<String, List<List<String>>> element : ruleMap.entrySet()) {
                writer.write(String.format("    private Tree.%s %s() throws ParseException {\n", element.getKey(), element.getKey()));
                writer.write("        switch (lex.curToken()) {\n");
                int kol = 0;
                for (List<String> rule : element.getValue()) {
                    Set<String> set;
                    if (rule.size() == 0 || (rule.size() == 1 && rule.get(0).startsWith("{"))) {
                        set = findAfter(element.getKey(), ruleMap, tokenMap);
                    } else
                        if (tokenMap.containsKey(rule.get(0))) {
                            set = new HashSet<>();
                            set.add(rule.get(0));
                        } else
                            set = getOf(rule.get(0), ruleMap, tokenMap);
                    for (String s : set)
                        writer.write(String.format("        case %s:\n", s));
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < rule.size(); i++) {
                        if (!rule.get(i).startsWith("{")) {
                            if (stringBuilder.length() > 0)
                                stringBuilder.append(", ");
                            if (tokenMap.containsKey(rule.get(i))) {
                                writer.write(String.format("            if (lex.curToken() != Token.%s)\n", rule.get(i)));
                                writer.write(String.format("                throw new ParseException(\"Expected %s token\", lex.curPos());\n", rule.get(i)));
                                writer.write(String.format("            Tree.%s %s%d = new Tree.%s(lex.curStr());\n", rule.get(i), rule.get(i), kol, rule.get(i)));
                                writer.write("            lex.nextToken();\n");
                            } else
                                writer.write(String.format("            Tree.%s %s%d = %s();\n", rule.get(i), rule.get(i), kol, rule.get(i)));
                            stringBuilder.append(String.format("%s%d", rule.get(i), kol++));
                        }
                    }
                    writer.write(String.format("            return new Tree.%s(%s);\n", element.getKey(), stringBuilder.toString()));
                }
                writer.write("        default:\n");
                writer.write("            throw new ParseException(\"Non expected token\", lex.curPos());\n");
                writer.write("        }\n");
                writer.write("    }\n");
            }
            writer.write("    public Tree parse(InputStream inputStream) throws ParseException {\n");
            writer.write("        lex = new LexicalAnalyzer(inputStream);\n");
            writer.write("        lex.nextToken();\n");
            writer.write("        Tree ans = start();\n");
            writer.write("        if (lex.curToken() != Token.END)\n");
            writer.write("            throw new ParseException(\"End expression expected at position \", lex.curPos());\n");
            writer.write("        return ans;\n");
            writer.write("    }\n");
            writer.write("}\n");
            writer.flush();
            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<String, Set<String>> memory = new HashMap<>();

    private Set<String> getOf(String key, Map<String, List<List<String>>> ruleMap, Map<String, String> tokenMap) {
        if (!memory.containsKey(key)) {
            Set<String> set = new HashSet<>();
            memory.put(key, set);
            for (List<String> rule : ruleMap.get(key)) {
                if (rule.size() == 0 || (rule.size() == 1 && rule.get(0).startsWith("{"))) {
                    set.addAll(findAfter(key, ruleMap, tokenMap));
                } else {
                    if (tokenMap.containsKey(rule.get(0)))
                        set.add(rule.get(0));
                    else
                        set.addAll(getOf(rule.get(0), ruleMap, tokenMap));
                }
            }
            return set;
        }
        return memory.get(key);
    }

    Map<String, Set<String>> memory2 = new HashMap<>();

    private Set<String> findAfter(String key, Map<String, List<List<String>>> ruleMap, Map<String, String> tokenMap) {
        if (!memory2.containsKey(key)) {
            Set<String> set = new HashSet<>();
            memory2.put(key, set);
            for (Map.Entry<String, List<List<String>>> entry : ruleMap.entrySet()) {
                for (List<String> rule2 : entry.getValue()) {
                    for (int i = 0; i < rule2.size(); i++) {
                        if (key.equals(rule2.get(i)))
                            if (i + 1 == rule2.size() || (i + 2 == rule2.size() && rule2.get(i + 1).startsWith("{"))) {
                                set.addAll(findAfter(entry.getKey(), ruleMap, tokenMap));
                                if ("start".equals(entry.getKey()))
                                    set.add("END");
                            }
                            else if (tokenMap.containsKey(rule2.get(i + 1)))
                                set.add(rule2.get(i + 1));
                            else
                                set.addAll(getOf(rule2.get(i + 1), ruleMap, tokenMap));
                    }
                }
            }
            return set;
        }
        return memory2.get(key);
    }

    private void generateTree(Map<String, String> tokenMap, Map<String, List<Pair<String, String>>> attrMap, Map<String, List<List<String>>> ruleMap) throws IOException, ParseException {
        File tree = new File(outPath, "Tree.java");
        if (tree.exists())
            tree.delete();
        tree.createNewFile();
        try (FileWriter writer = new FileWriter(tree)) {
            writer.write("package " + outPackage + ";\n");
            writer.write("import org.StructureGraphic.v1.DSChildren;\n");
            writer.write("import org.StructureGraphic.v1.DSValue;\n");
            writer.write("import java.util.ArrayList;\n");
            writer.write("import java.util.Arrays;\n");
            writer.write("import java.util.List;\n");
            writer.write("public abstract class Tree {\n");
            writer.write("    @DSValue\n");
            writer.write("    public String node;\n");
            writer.write("    @DSChildren(DSChildren.DSChildField.ITERABLE)\n");
            writer.write("    public List<Tree> children;\n");
            writer.write("    private Tree(String node, Tree... children) {\n");
            writer.write("        this.node = node;\n");
            writer.write("        if (children == null)\n");
            writer.write("            this.children = new ArrayList<>();\n");
            writer.write("        else\n");
            writer.write("            this.children = Arrays.asList(children);\n");
            writer.write("    }\n");
            for (Map.Entry<String, List<List<String>>> entry : ruleMap.entrySet()) {
                writer.write("    public static class " + entry.getKey() + " extends Tree {\n");
                if (attrMap.get(entry.getKey()) != null)
                    for (Pair<String,String> attr : attrMap.get(entry.getKey()))
                        writer.write(String.format("        public %s %s;\n", attr.second, attr.first));
                String attr = "";
                StringBuilder method = new StringBuilder();
                StringBuilder superCall = new StringBuilder();
                for (List<String> rule : entry.getValue()) {
                    int k = 1;
                    StringBuilder attrSb = new StringBuilder();
                    for (String s : rule) {
                        if (s.startsWith("{"))
                            attrSb.append(s);
                        else {
                            if (method.length() > 0)
                                method.append(", ");
                            method.append("Tree.").append(s).append(" ").append(s).append(k);
                            superCall.append(", ").append(s).append(k++);
                        }
                    }
                    if (attrSb.length() > 0) {
                        int ind;
                        while ((ind = attrSb.indexOf("_")) != -1) {
                            String temp = attrSb.substring(ind + 1);
                            try {
                                int qwe = 0;
                                while (temp.charAt(qwe) >= '0' && temp.charAt(qwe) <= '9' && qwe < temp.length())
                                    qwe++;
                                int i = Integer.parseInt(temp.substring(0, qwe)) - 1;
                                if (temp.charAt(qwe) != '.')
                                    attrSb.insert(qwe + ind + 1, ".str");
                                attrSb.replace(ind, ind + 1, rule.get(i));
                            } catch (NumberFormatException e) {
                                System.err.println("Incorrect attribute number in rule " + entry.getKey());
                                throw new ParseException("Incorrect attribute number", -1);
                            }
                        }
                        attr = attrSb.toString();
                    }
                    writer.write(String.format("        public %s(%s) {\n", entry.getKey(), method.toString()));
                    writer.write(String.format("            super(\"%s\"%s);\n", entry.getKey(), superCall.toString()));
                    writer.write(String.format("            %s\n", attr));
                    writer.write("        }\n");
                    method.setLength(0);
                    superCall.setLength(0);
                    attr = "";
                }
                writer.write("    }\n");
            }
            for (Map.Entry<String, String> entry : tokenMap.entrySet())
                if (!"skip".equalsIgnoreCase(entry.getKey())) {
                    writer.write("    public static class " + entry.getKey() + " extends Tree {\n");
                    writer.write("        public String str;\n");
                    writer.write(String.format("        public %s(String s) {\n", entry.getKey()));
                    writer.write(String.format("            super(\"%s\");\n", entry.getKey()));
                    writer.write("            str = s;\n");
                    writer.write("        }\n");
                    writer.write("    }\n");
                }
            writer.write("}\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateLexicalAnalyzer(Map<String, String> tokenMap) throws IOException {
        File lexical = new File(outPath, "LexicalAnalyzer.java");
        if (lexical.exists())
            lexical.delete();
        lexical.createNewFile();
        try (FileWriter writer = new FileWriter(lexical)) {
            writer.write("package " + outPackage + ";\n");
            writer.write("import java.io.IOException;\n");
            writer.write("import java.io.InputStream;\n");
            writer.write("import java.text.ParseException;\n");
            writer.write("import java.util.regex.Pattern;\n");
            writer.write("public class LexicalAnalyzer {\n");
            writer.write("    private final InputStream input;\n");
            writer.write("    private int curPos;\n");
            writer.write("    private int curChar;\n");
            writer.write("    private String curStr;\n");
            writer.write("    private Token curToken;\n");
            writer.write("    public LexicalAnalyzer(InputStream inputStream) throws ParseException {\n");
            writer.write("        this.input = inputStream;\n");
            writer.write("        curPos = 0;\n");
            writer.write("        nextChar();\n");
            writer.write("    }\n");
            writer.write("    public Token curToken() {\n");
            writer.write("        return curToken;\n");
            writer.write("    }\n");
            writer.write("    public int curPos() {\n");
            writer.write("        return curPos;\n");
            writer.write("    }\n");
            writer.write("    public String curStr() {\n");
            writer.write("        return curStr;\n");
            writer.write("    }\n");
            writer.write("    private void nextChar() throws ParseException {\n");
            writer.write("        curPos++;\n");
            writer.write("        try {\n");
            writer.write("            curChar = input.read();\n");
            writer.write("        } catch (IOException e) {\n");
            writer.write("            throw new ParseException(e.getMessage(), curPos);\n");
            writer.write("        }\n");
            writer.write("    }\n");
            writer.write("    private boolean isBlank(int c) {\n");
            writer.write("        return Pattern.matches(\"" + tokenMap.get("SKIP") + "\", String.valueOf((char)c));\n");
            writer.write("    }\n");
            writer.write("    public void nextToken() throws ParseException {\n");
            writer.write("        while (isBlank(curChar))\n");
            writer.write("            nextChar();\n");
            writer.write("        if (curChar == -1)\n");
            writer.write("            curToken = Token.END;\n");
            writer.write("        else {\n");
            writer.write("            StringBuilder sb = new StringBuilder().append((char)curChar);\n");
            writer.write("            int countToken = " + (tokenMap.keySet().size() - 1) + ";\n");
            writer.write("            String[] tokens = new String[countToken];\n");
            int i = 0;
            for (Map.Entry<String, String> val : tokenMap.entrySet())
                if (!"skip".equalsIgnoreCase(val.getKey())) {
                    writer.write(String.format("            tokens[%d] = \"%s\";\n", i, val.getValue()));
                    i++;
                }
            writer.write("            int match = 0;\n");
            writer.write("            int last = -1;\n");
            writer.write("            String str = sb.toString();\n");
            writer.write("            for (int i = 0; i < countToken; i++)\n");
            writer.write("                if (Pattern.matches(tokens[i], str)) {\n");
            writer.write("                    match++;\n");
            writer.write("                    last = i;\n");
            writer.write("                }\n");
            writer.write("            nextChar();\n");
            writer.write("            while(match > 0 && !isBlank(curChar) && curChar != -1) {\n");
            writer.write("                sb.append((char)curChar);\n");
            writer.write("                str = sb.toString();\n");
            writer.write("                match = 0;\n");
            writer.write("                for (int i = 0; i < countToken; i++)\n");
            writer.write("                    if (Pattern.matches(tokens[i], str)) {\n");
            writer.write("                        match++;\n");
            writer.write("                        last = i;\n");
            writer.write("                    }\n");
            writer.write("                nextChar();\n");
            writer.write("            }\n");
            writer.write("            if (last == -1)\n");
            writer.write("                throw new ParseException(\"Illegal character \" + (char) curChar, curPos);\n");
            writer.write("            curToken = Token.values()[last];\n");
            writer.write("            curStr = str;\n");
            writer.write("        }\n");
            writer.write("    }\n");
            writer.write("}\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTokenFile(Set<String> strings) throws IOException {
        File token = new File(outPath,  "Token.java");
        if (token.exists())
            token.delete();
        token.createNewFile();
        try (FileWriter writer = new FileWriter(token)) {
            writer.write("package " + outPackage + ";\n");
            writer.write("public enum Token {\n");
            for (String s : strings)
                if (!"skip".equalsIgnoreCase(s))
                    writer.write(s + ",\n");
            writer.write("END;\n}");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Pair<Q, E> {
        Q first;
        E second;

        public Pair(Q f, E s) {
            first = f;
            second = s;
        }
    }
}

