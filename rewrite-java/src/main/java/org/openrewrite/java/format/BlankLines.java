/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.format;

import org.openrewrite.Cursor;
import org.openrewrite.EvalContext;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaEvalVisitor;
import org.openrewrite.java.style.BlankLineStyle;
import org.openrewrite.java.style.IntelliJ;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class BlankLines extends JavaEvalVisitor {
    BlankLineStyle blankLineStyle;

    public BlankLines() {
        setCursoringOn();
    }

    @Override
    public J visitCompilationUnit(J.CompilationUnit cu, EvalContext ctx) {
        blankLineStyle = cu.getStyle(BlankLineStyle.class);
        if (blankLineStyle == null) {
            blankLineStyle = IntelliJ.defaultBlankLine();
        }

        J.CompilationUnit j = eval(cu, ctx, super::visitCompilationUnit);

        if (j.getPackageDecl() != null) {
            if (!j.getPrefix().getComments().isEmpty()) {
                j = j.withComments(ListUtils.mapLast(j.getComments(), c -> {
                    String suffix = keepMaximumLines(c.getSuffix(), blankLineStyle.getKeepMaximum().getBetweenHeaderAndPackage());
                    suffix = minimumLines(suffix, blankLineStyle.getMinimum().getBeforePackage());
                    return c.withSuffix(suffix);
                }));
            } else {
                j = minimumLines(j, blankLineStyle.getMinimum().getBeforePackage());
            }
        }

        if (j.getPackageDecl() == null) {
            if (j.getComments().isEmpty()) {
                j = j.withImports(ListUtils.mapFirst(cu.getImports(), i -> {
                    J.Import anImport = i.getElem();
                    return i.withElem(anImport.withPrefix(anImport.getPrefix().withWhitespace("")));
                }));
            } else {
                j = j.withComments(ListUtils.mapLast(j.getComments(), c ->
                        c.withSuffix(minimumLines(c.getSuffix(), blankLineStyle.getMinimum().getBeforeImports()))));
            }
        } else {
            j = j.withImports(ListUtils.mapFirst(j.getImports(), i ->
                    minimumLines(i, Math.max(
                            blankLineStyle.getMinimum().getBeforeImports(),
                            blankLineStyle.getMinimum().getAfterPackage()))));

            if (j.getImports().isEmpty()) {
                j = j.withClasses(ListUtils.mapFirst(j.getClasses(), c ->
                        minimumLines(c, blankLineStyle.getMinimum().getAfterPackage())));
            }
        }

        j = j.withClasses(ListUtils.map(j.getClasses(), (i, c) -> i == 0 ?
                minimumLines(c, blankLineStyle.getMinimum().getAfterImports()) :
                minimumLines(c, blankLineStyle.getMinimum().getAroundClass())
        ));

        return j;
    }

    @Override
    public J visitClassDecl(J.ClassDecl classDecl, EvalContext ctx) {
        J.ClassDecl j = eval(classDecl, ctx, super::visitClassDecl);

        List<JRightPadded<Statement>> statements = j.getBody().getStatements();
        j = j.withBody(j.getBody().withStatements(ListUtils.map(statements, (i, s) -> {
            s = keepMaximumLines(s, blankLineStyle.getKeepMaximum().getInDeclarations());
            if (i == 0) {
                s = minimumLines(s, blankLineStyle.getMinimum().getAfterClassHeader());
            } else if (s.getElem() instanceof J.VariableDecls) {
                if (classDecl.getKind().getElem() == J.ClassDecl.Kind.Interface) {
                    s = minimumLines(s, blankLineStyle.getMinimum().getAroundFieldInInterface());
                } else {
                    s = minimumLines(s, blankLineStyle.getMinimum().getAroundField());
                }
            } else if (s.getElem() instanceof J.MethodDecl) {
                if (classDecl.getKind().getElem() == J.ClassDecl.Kind.Interface) {
                    s = minimumLines(s, blankLineStyle.getMinimum().getAroundMethodInInterface());
                } else {
                    s = minimumLines(s, blankLineStyle.getMinimum().getAroundMethod());
                }
            } else if (s.getElem() instanceof J.Block) {
                s = minimumLines(s, blankLineStyle.getMinimum().getAroundInitializer());
            }

            if(i > 0 && statements.get(i - 1).getElem() instanceof J.Block) {
                s = minimumLines(s, blankLineStyle.getMinimum().getAroundInitializer());
            }

            return s;
        })));

        j = j.withBody(j.getBody().withEnd(minimumLines(j.getBody().getEnd(),
                blankLineStyle.getMinimum().getBeforeClassEnd())));

        return j;
    }

    @Override
    public J visitMethod(J.MethodDecl method, EvalContext ctx) {
        J.MethodDecl j = eval(method, ctx, super::visitMethod);

        if (j.getBody() != null) {
            if (j.getBody().getStatements().isEmpty()) {
                Space end = minimumLines(j.getBody().getEnd(),
                        blankLineStyle.getMinimum().getBeforeMethodBody());
                if (end.getIndent().isEmpty() && blankLineStyle.getMinimum().getBeforeMethodBody() > 0) {
                    end = end.withWhitespace(end.getWhitespace() + method.getPrefix().getIndent());
                }
                j = j.withBody(j.getBody().withEnd(end));
            } else {
                j = j.withBody(j.getBody().withStatements(ListUtils.mapFirst(j.getBody().getStatements(), s ->
                        minimumLines(s, blankLineStyle.getMinimum().getBeforeMethodBody()))));
            }
        }

        return j;
    }

    @Override
    public J visitNewClass(J.NewClass newClass, EvalContext ctx) {
        J.NewClass j = eval(newClass, ctx, super::visitNewClass);

        if (j.getBody() != null) {
            j = j.withBody(j.getBody().withStatements(ListUtils.mapFirst(j.getBody().getStatements(), s ->
                    minimumLines(s, blankLineStyle.getMinimum().getAfterAnonymousClassHeader()))));
        }

        return j;
    }

    @Override
    public J visitStatement(Statement statement, EvalContext ctx) {
        J j = eval(statement, ctx, super::visitStatement);
        Cursor parent = getCursor().getParentOrThrow();
        if (parent.getParent() != null && !(parent.getParentOrThrow().getTree() instanceof J.ClassDecl)) {
            return keepMaximumLines(j, blankLineStyle.getKeepMaximum().getInCode());
        }
        return j;
    }

    @Override
    public J visitBlock(J.Block block, EvalContext ctx) {
        J.Block j = eval(block, ctx, super::visitBlock);
        j = j.withEnd(keepMaximumLines(j.getEnd(), blankLineStyle.getKeepMaximum().getBeforeEndOfBlock()));
        return j;
    }

    private <J2 extends J> JRightPadded<J2> keepMaximumLines(JRightPadded<J2> tree, int max) {
        J2 elem = keepMaximumLines(tree.getElem(), max);
        if (elem != tree.getElem()) {
            return new JRightPadded<>(elem, tree.getAfter());
        }
        return tree;
    }

    private <J2 extends J> J2 keepMaximumLines(J2 tree, int max) {
        return tree.withPrefix(keepMaximumLines(tree.getPrefix(), max));
    }

    private Space keepMaximumLines(Space prefix, int max) {
        return prefix.withWhitespace(keepMaximumLines(prefix.getWhitespace(), max));
    }

    private String keepMaximumLines(String whitespace, int max) {
        long blankLines = whitespace.chars().filter(c -> c == '\n').count() - 1;
        if (blankLines > max) {
            int startWhitespaceAtIndex = 0;
            for (int i = 0; i < blankLines - max + 1; i++, startWhitespaceAtIndex++) {
                startWhitespaceAtIndex = whitespace.indexOf('\n', startWhitespaceAtIndex);
            }
            startWhitespaceAtIndex--;
            return whitespace.substring(startWhitespaceAtIndex);
        }
        return whitespace;
    }

    private <J2 extends J> JRightPadded<J2> minimumLines(JRightPadded<J2> tree, int min) {
        J2 elem = minimumLines(tree.getElem(), min);
        if (elem != tree.getElem()) {
            return new JRightPadded<>(elem, tree.getAfter());
        }
        return tree;
    }

    private <J2 extends J> J2 minimumLines(J2 tree, int min) {
        return tree.withPrefix(minimumLines(tree.getPrefix(), min));
    }

    private Space minimumLines(Space prefix, int min) {
        return prefix.withWhitespace(minimumLines(prefix.getWhitespace(), min));
    }

    private String minimumLines(String whitespace, int min) {
        if(min == 0) {
            return whitespace;
        }
        String minWhitespace = whitespace;
        for (int i = 0; i < min - whitespace.chars().filter(c -> c == '\n').count() + 1; i++) {
            //noinspection StringConcatenationInLoop
            minWhitespace = "\n" + minWhitespace;
        }
        return minWhitespace;
    }
}
