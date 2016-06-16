import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit.SourceUnitOperation;
import org.codehaus.groovy.control.SourceUnit;

//org.gradle.groovy.scripts.internal.TaskDefinitionScriptTransformer
public class TaskTransformer extends SourceUnitOperation {

    @Override
    public void call(SourceUnit source) throws CompilationFailedException {
        source.getAST().getStatementBlock().visit(new TaskDefinitionTransformer());

    }

    private class TaskDefinitionTransformer extends CodeVisitorSupport {
        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            doVisitMethodCallExpression(call);
            super.visitMethodCallExpression(call);
        }

        private void doVisitMethodCallExpression(MethodCallExpression call) {
            if (!isInstanceMethod(call, "task")) {
                return;
            }

            ArgumentListExpression args = (ArgumentListExpression) call.getArguments();

            // Matches: task <arg> or task(<arg>)

            Expression arg = args.getExpression(0);
            if (arg instanceof MethodCallExpression) {
                // Matches: task <method-call>
                maybeTransformNestedMethodCall((MethodCallExpression) arg, call);
            }
        }

        private boolean maybeTransformNestedMethodCall(MethodCallExpression nestedMethod, MethodCallExpression target) {

            // Matches: task <identifier> <arg-list> | task <string> <arg-list>
            // Map to: task("<identifier>", <arg-list>) | task(<string>, <arg-list>)

            Expression taskName = nestedMethod.getMethod();
            Expression mapArg = null;
            List<Expression> extraArgs = Collections.emptyList();

            if (nestedMethod.getArguments() instanceof TupleExpression) {
                TupleExpression nestedArgs = (TupleExpression) nestedMethod.getArguments();
                if (nestedArgs.getExpressions().size() == 2 && nestedArgs.getExpression(0) instanceof MapExpression && nestedArgs.getExpression(1) instanceof ClosureExpression) {
                    // Matches: task <identifier>(<options-map>) <closure>
                    mapArg = nestedArgs.getExpression(0);
                    extraArgs = nestedArgs.getExpressions().subList(1, nestedArgs.getExpressions().size());
                } else if (nestedArgs.getExpressions().size() == 1 && nestedArgs.getExpression(0) instanceof ClosureExpression) {
                    // Matches: task <identifier> <closure>
                    extraArgs = nestedArgs.getExpressions();
                } else if (nestedArgs.getExpressions().size() == 1 && nestedArgs.getExpression(0) instanceof NamedArgumentListExpression) {
                    // Matches: task <identifier>(<options-map>)
                    mapArg = nestedArgs.getExpression(0);
                } else if (nestedArgs.getExpressions().size() != 0) {
                    return false;
                }
            }

            target.setMethod(new ConstantExpression("task"));
            ArgumentListExpression args = (ArgumentListExpression) target.getArguments();
            args.getExpressions().clear();
            if (mapArg != null) {
                args.addExpression(mapArg);
            }
            args.addExpression(taskName);
            for (Expression extraArg : extraArgs) {
                args.addExpression(extraArg);
            }
            return true;
        }

        private boolean isInstanceMethod(MethodCallExpression call, String name) {
            return call.getArguments() instanceof ArgumentListExpression;
        }
    }

}
