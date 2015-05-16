import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * Created by kris13 on 08.05.15.
 */
public class ToCPlusPlus implements PascalListener {
    private StringBuilder out;

    public ToCPlusPlus(){
        out = new StringBuilder();
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void enterProgram(@NotNull PascalParser.ProgramContext ctx) {
        out.append("#include <iostream>\n");
        out.append("#include <cstdio>\n");
        out.append("using namespace std;\n");
    }

    @Override
    public void exitProgram(@NotNull PascalParser.ProgramContext ctx) {

    }

    @Override
    public void enterDefs(@NotNull PascalParser.DefsContext ctx) {

    }

    @Override
    public void exitDefs(@NotNull PascalParser.DefsContext ctx) {

    }

    @Override
    public void enterVarDef(@NotNull PascalParser.VarDefContext ctx) {

    }

    @Override
    public void exitVarDef(@NotNull PascalParser.VarDefContext ctx) {

    }

    @Override
    public void enterVariable(@NotNull PascalParser.VariableContext ctx) {
        if (ctx.getParent().getParent() instanceof PascalParser.DefsContext) {
            out.append(getCType(ctx.type().getText())).append(" ");
            for (int i = 0; i < ctx.VAR().size(); i++) {
                if (i > 0)
                    out.append(", ");
                out.append(ctx.VAR(i).getSymbol().getText());
            }
            out.append(";\n");
        }
    }

    private String getCType(String text) {
        switch (text) {
            case "integer": return "int";
            case "double": return "double";
            case "boolean": return "bool";
        }
        return text;
    }

    @Override
    public void exitVariable(@NotNull PascalParser.VariableContext ctx) {

    }

    @Override
    public void enterProcDef(@NotNull PascalParser.ProcDefContext ctx) {

    }

    @Override
    public void exitProcDef(@NotNull PascalParser.ProcDefContext ctx) {

    }

    @Override
    public void enterProcedure(@NotNull PascalParser.ProcedureContext ctx) {
        generateMethod("void", ctx.header().VAR().getSymbol().getText(), ctx.header().variable());
    }

    @Override
    public void exitProcedure(@NotNull PascalParser.ProcedureContext ctx) {

    }

    private void generateMethod(String retType, String name, List<PascalParser.VariableContext> variable) {
        out.append(retType).append(" ").append(name).append("(");
        for (int i = 0; i < variable.size(); i++) {
            if (i > 0)
                out.append(", ");
            for (int j = 0; j < variable.get(i).VAR().size(); j++) {
                if (j > 0)
                    out.append(", ");
                out.append(getCType(variable.get(i).type().getText()))
                        .append(" ")
                        .append(variable.get(i).VAR(j).getSymbol().getText());
            }
        }
        out.append(")\n");
    }

    @Override
    public void enterHeader(@NotNull PascalParser.HeaderContext ctx) {

    }

    @Override
    public void exitHeader(@NotNull PascalParser.HeaderContext ctx) {

    }

    @Override
    public void enterBody(@NotNull PascalParser.BodyContext ctx) {

    }

    @Override
    public void exitBody(@NotNull PascalParser.BodyContext ctx) {

    }

    @Override
    public void enterType(@NotNull PascalParser.TypeContext ctx) {

    }

    @Override
    public void exitType(@NotNull PascalParser.TypeContext ctx) {

    }

    @Override
    public void enterBlock(@NotNull PascalParser.BlockContext ctx) {
        if (ctx.getParent() instanceof PascalParser.ProgramContext) {
            out.append("int main()");
        }
        out.append("{\n");
        if (ctx.getParent() instanceof PascalParser.BodyContext) {
            PascalParser.BodyContext bodyContext = (PascalParser.BodyContext) ctx.getParent();
            if (bodyContext.varDef() != null){
                for (int i = 0; i < bodyContext.varDef().variable().size(); i++) {
                    out.append(getCType(bodyContext.varDef().variable(i).type().getText())).append(" ");
                    for (int j = 0; j < bodyContext.varDef().variable(i).VAR().size(); j++) {
                        if (j > 0)
                            out.append(", ");
                        out.append(bodyContext.varDef().variable(i).VAR(j).getSymbol().getText())
                                .append(" = ")
                                .append(getCDefForType(bodyContext.varDef().variable(i).type().getText()));
                    }
                    out.append(";\n");
                }
            }
        }
    }

    private Object getCDefForType(String text) {
        switch (text) {
            case "integer": return 0;
            case "double": return 0.0;
            case "boolean": return false;
        }
        return null;
    }

    @Override
    public void exitBlock(@NotNull PascalParser.BlockContext ctx) {
        out.append("}");
        if (ctx.getParent() instanceof PascalParser.BodyContext)
            out.append("\n");
    }

    @Override
    public void enterLine(@NotNull PascalParser.LineContext ctx) {

    }

    @Override
    public void exitLine(@NotNull PascalParser.LineContext ctx) {
        out.append(";\n");
    }

    @Override
    public void enterMethodCall(@NotNull PascalParser.MethodCallContext ctx) {
        out.append(ctx.VAR().getSymbol().getText());
    }

    @Override
    public void exitMethodCall(@NotNull PascalParser.MethodCallContext ctx) {

    }

    @Override
    public void enterAssigment(@NotNull PascalParser.AssigmentContext ctx) {
        out.append(ctx.VAR().getSymbol().getText()).append(" = ");
    }

    @Override
    public void exitAssigment(@NotNull PascalParser.AssigmentContext ctx) {

    }

    @Override
    public void enterForLoop(@NotNull PascalParser.ForLoopContext ctx) {
        out.append("for (");
    }

    @Override
    public void exitForLoop(@NotNull PascalParser.ForLoopContext ctx) {
        String var = ctx.assigment().VAR().getSymbol().getText();
        out.append(var);
        String step = "1";
        if (ctx.step() != null)
            if (ctx.step().VAR() != null)
                step = ctx.step().VAR().getSymbol().getText();
            else
                step = ctx.step().INT().getSymbol().getText();
        if (ctx.side().TO() != null)
            out.append("-=").append(step).append(";");
        else
            out.append("+=").append(step).append(";");
        // TODO See there
    }

    @Override
    public void enterRepeatUntil(@NotNull PascalParser.RepeatUntilContext ctx) {
        out.append("do\n{\n");
    }

    @Override
    public void exitRepeatUntil(@NotNull PascalParser.RepeatUntilContext ctx) {
        out.append(")");
    }

    @Override
    public void enterWhileLoop(@NotNull PascalParser.WhileLoopContext ctx) {
        out.append("while (");
    }

    @Override
    public void exitWhileLoop(@NotNull PascalParser.WhileLoopContext ctx) {

    }

    @Override
    public void enterIfStatement(@NotNull PascalParser.IfStatementContext ctx) {
        out.append("if (");
    }

    @Override
    public void exitIfStatement(@NotNull PascalParser.IfStatementContext ctx) {

    }

    @Override
    public void enterStep(@NotNull PascalParser.StepContext ctx) {
    }

    @Override
    public void exitStep(@NotNull PascalParser.StepContext ctx) {
    }

    @Override
    public void enterRead(@NotNull PascalParser.ReadContext ctx) {
        if (ctx.VAR() != null) {
            out.append("cin");
            ctx.VAR().stream().forEach(t -> out.append(" >> ").append(t.getSymbol().getText()));
        }
    }

    @Override
    public void exitRead(@NotNull PascalParser.ReadContext ctx) {

    }

    @Override
    public void enterWrite(@NotNull PascalParser.WriteContext ctx) {
        if (ctx.arguments() != null) {
            out.append("cout");
        }
    }

    @Override
    public void exitWrite(@NotNull PascalParser.WriteContext ctx) {

    }

    @Override
    public void enterWriteln(@NotNull PascalParser.WritelnContext ctx) {
        out.append("cout");
    }

    @Override
    public void exitWriteln(@NotNull PascalParser.WritelnContext ctx) {
        out.append(" << endl");
    }

    @Override
    public void enterExpressionLogic(@NotNull PascalParser.ExpressionLogicContext ctx) {
        if (ctx.getParent() instanceof PascalParser.ArgumentsContext) {
            if (ctx.getParent().getParent() instanceof PascalParser.WriteContext || ctx.getParent().getParent() instanceof PascalParser.WritelnContext) {
                out.append(" << (");
            } else {
                int ind = ((PascalParser.ArgumentsContext) ctx.getParent()).expressionLogic().indexOf(ctx);
                assert (ind != -1);
                if (ind > 0)
                    out.append(", ");
            }
        } else if (ctx.getParent() instanceof PascalParser.ExpressionValueEmbededContext || ctx.getParent() instanceof PascalParser.RepeatUntilContext) {
            out.append("(");
        }
    }

    @Override
    public void exitExpressionLogic(@NotNull PascalParser.ExpressionLogicContext ctx) {
        if (ctx.getParent() instanceof PascalParser.ExpressionValueEmbededContext || ctx.getParent() instanceof PascalParser.RepeatUntilContext) {
            out.append(")");
        } else if (ctx.getParent().getParent() instanceof PascalParser.WriteContext || ctx.getParent().getParent() instanceof PascalParser.WritelnContext) {
            out.append(")");
        }
    }

    @Override
    public void enterExpressionCompare(@NotNull PascalParser.ExpressionCompareContext ctx) {
        int ind = ctx.getParent().children.indexOf(ctx);
        assert (ind != -1);
        if (ind > 0)
            out.append(" ").append(ctx.getParent().children.get(ind - 1).getText()).append(" ");
    }

    @Override
    public void exitExpressionCompare(@NotNull PascalParser.ExpressionCompareContext ctx) {

    }

    @Override
    public void enterExpressionDownArithmetic(@NotNull PascalParser.ExpressionDownArithmeticContext ctx) {
        int ind = ctx.getParent().children.indexOf(ctx);
        assert (ind != -1);
        if (ind > 0) {
            PascalParser.OpContext tree = (PascalParser.OpContext) ctx.getParent().children.get(ind - 1);
            if (tree.equals() == null && tree.notEquals() == null)
                out.append(" ").append(tree.getText()).append(" ");
        }
    }

    @Override
    public void exitExpressionDownArithmetic(@NotNull PascalParser.ExpressionDownArithmeticContext ctx) {

    }

    @Override
    public void enterExpressionHighArithmetic(@NotNull PascalParser.ExpressionHighArithmeticContext ctx) {
        int ind = ctx.getParent().children.indexOf(ctx);
        assert (ind != -1);
        if (ind > 0)
            out.append(" ").append(ctx.getParent().children.get(ind - 1).getText()).append(" ");
    }

    @Override
    public void exitExpressionHighArithmetic(@NotNull PascalParser.ExpressionHighArithmeticContext ctx) {

    }

    @Override
    public void enterExpressionUnarity(@NotNull PascalParser.ExpressionUnarityContext ctx) {
        int ind = ctx.getParent().children.indexOf(ctx);
        assert (ind != -1);
        if (ind > 0) {
            ParseTree tree = ctx.getParent().children.get(ind - 1);
            if (!(tree instanceof PascalParser.DivWordContext || tree instanceof PascalParser.ModWordContext))
                out.append(" ").append(ctx.getParent().children.get(ind - 1).getText()).append(" ");
        }
    }

    @Override
    public void exitExpressionUnarity(@NotNull PascalParser.ExpressionUnarityContext ctx) {

    }

    @Override
    public void enterExpressionValueEmbeded(@NotNull PascalParser.ExpressionValueEmbededContext ctx) {
        if (ctx.expressionLogic() == null)
            out.append(ctx.getText());
    }

    @Override
    public void exitExpressionValueEmbeded(@NotNull PascalParser.ExpressionValueEmbededContext ctx) {

    }

    @Override
    public void enterOp(@NotNull PascalParser.OpContext ctx) {

    }

    @Override
    public void exitOp(@NotNull PascalParser.OpContext ctx) {

    }

    @Override
    public void enterSide(@NotNull PascalParser.SideContext ctx) {
        if (ctx.getParent() instanceof PascalParser.ForLoopContext) {
            PascalParser.ForLoopContext forLoopContext = (PascalParser.ForLoopContext) ctx.getParent();
            out.append(";").append(forLoopContext.assigment().VAR().getSymbol().getText());
            if (ctx.TO() != null)
                out.append("<=");
            else
                out.append(">=");
        }
    }

    @Override
    public void exitSide(@NotNull PascalParser.SideContext ctx) {

    }

    @Override
    public void enterDoWord(@NotNull PascalParser.DoWordContext ctx) {
        if (ctx.getParent() instanceof PascalParser.ForLoopContext){
            PascalParser.ForLoopContext forLoopContext = (PascalParser.ForLoopContext) ctx.getParent();
            out.append(";");
            out.append(forLoopContext.assigment().VAR().getSymbol().getText());
            String step = "1";
            if (forLoopContext.step() != null)
                if (forLoopContext.step().VAR() != null)
                    step = forLoopContext.step().VAR().getSymbol().getText();
                else
                    step = forLoopContext.step().INT().getSymbol().getText();
            if (forLoopContext.side().TO() != null)
                out.append("+=").append(step);
            else
                out.append("-=").append(step);
        }
        out.append(")\n");
        // TODO See there
    }

    @Override
    public void exitDoWord(@NotNull PascalParser.DoWordContext ctx) {
    }

    @Override
    public void enterUntilWord(@NotNull PascalParser.UntilWordContext ctx) {
        out.append("} while (!");
    }

    @Override
    public void exitUntilWord(@NotNull PascalParser.UntilWordContext ctx) {
    }

    @Override
    public void enterElseWord(@NotNull PascalParser.ElseWordContext ctx) {
        out.append("else\n");
    }

    @Override
    public void exitElseWord(@NotNull PascalParser.ElseWordContext ctx) {
    }

    @Override
    public void enterThenWord(@NotNull PascalParser.ThenWordContext ctx) {
        out.append(")\n");
    }

    @Override
    public void exitThenWord(@NotNull PascalParser.ThenWordContext ctx) {
    }

    @Override
    public void enterDivWord(@NotNull PascalParser.DivWordContext ctx) {
        out.append(" / ");
    }

    @Override
    public void exitDivWord(@NotNull PascalParser.DivWordContext ctx) {
    }

    @Override
    public void enterModWord(@NotNull PascalParser.ModWordContext ctx) {
        out.append(" % ");
    }

    @Override
    public void exitModWord(@NotNull PascalParser.ModWordContext ctx) {
    }

    @Override
    public void enterEquals(@NotNull PascalParser.EqualsContext ctx) {
        out.append(" == ");
    }

    @Override
    public void exitEquals(@NotNull PascalParser.EqualsContext ctx) {
    }

    @Override
    public void enterNotEquals(@NotNull PascalParser.NotEqualsContext ctx) {
        out.append(" != ");
    }

    @Override
    public void exitNotEquals(@NotNull PascalParser.NotEqualsContext ctx) {
    }

    @Override
    public void enterArguments(@NotNull PascalParser.ArgumentsContext ctx) {
        if (!(ctx.getParent() instanceof PascalParser.WriteContext || ctx.getParent() instanceof PascalParser.WritelnContext))
            out.append("(");
    }

    @Override
    public void exitArguments(@NotNull PascalParser.ArgumentsContext ctx) {
        if (!(ctx.getParent() instanceof PascalParser.WriteContext || ctx.getParent() instanceof PascalParser.WritelnContext))
            out.append(")");
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
    }
}
