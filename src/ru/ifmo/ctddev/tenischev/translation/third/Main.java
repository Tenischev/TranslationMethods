import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/**
 * Created by kris13 on 08.05.15.
 */
public class Main {
    static final String fileName = "TestPascal";

    public static void main(String[] args) {
        try {
            File file = new File(fileName + ".fpc");
            ANTLRInputStream stream = new ANTLRInputStream(new FileInputStream(file));
            PascalLexer lexer = new PascalLexer(stream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PascalParser parser = new PascalParser(tokens);
            ParseTree tree = parser.program();

            ParseTreeWalker walker = new ParseTreeWalker();
            ToCPlusPlus cPlusPlus = new ToCPlusPlus();
            walker.walk(cPlusPlus, tree);

            File completeC = new File(fileName + ".cpp");
            completeC.delete();
            completeC.createNewFile();
            FileWriter fileWriter = new FileWriter(completeC);
            fileWriter.write(cPlusPlus.toString());
            fileWriter.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
