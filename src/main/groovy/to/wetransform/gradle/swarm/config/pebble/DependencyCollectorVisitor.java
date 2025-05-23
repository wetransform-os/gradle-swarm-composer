/*
 * Copyright 2017 wetransform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package to.wetransform.gradle.swarm.config.pebble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.node.ArgumentsNode;
import io.pebbletemplates.pebble.node.AutoEscapeNode;
import io.pebbletemplates.pebble.node.BlockNode;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.node.ExtendsNode;
import io.pebbletemplates.pebble.node.FlushNode;
import io.pebbletemplates.pebble.node.ForNode;
import io.pebbletemplates.pebble.node.IfNode;
import io.pebbletemplates.pebble.node.ImportNode;
import io.pebbletemplates.pebble.node.IncludeNode;
import io.pebbletemplates.pebble.node.MacroNode;
import io.pebbletemplates.pebble.node.NamedArgumentNode;
import io.pebbletemplates.pebble.node.Node;
import io.pebbletemplates.pebble.node.ParallelNode;
import io.pebbletemplates.pebble.node.PositionalArgumentNode;
import io.pebbletemplates.pebble.node.PrintNode;
import io.pebbletemplates.pebble.node.RootNode;
import io.pebbletemplates.pebble.node.SetNode;
import io.pebbletemplates.pebble.node.TextNode;
import io.pebbletemplates.pebble.node.expression.BinaryExpression;
import io.pebbletemplates.pebble.node.expression.ContextVariableExpression;
import io.pebbletemplates.pebble.node.expression.Expression;
import io.pebbletemplates.pebble.node.expression.FunctionOrMacroInvocationExpression;
import io.pebbletemplates.pebble.node.expression.GetAttributeExpression;
import io.pebbletemplates.pebble.node.expression.LiteralStringExpression;
import io.pebbletemplates.pebble.node.expression.UnaryExpression;

/**
 * Visitor that checks if there is any dynamic content in the visited nodes.
 *
 * @author Simon Templer
 */
public class DependencyCollectorVisitor implements NodeVisitor {

  private Set<List<String>> dependencies = new HashSet<>();

  public Set<List<String>> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  protected void analyzeExpression(Expression<?> expression) {
    List<String> dep = doAnalyzeExpression(expression, true);
    if (dep != null) {
      dependencies.add(Collections.unmodifiableList(dep));
    }
  }

  protected List<String> doAnalyzeExpression(Expression<?> expression, boolean root) {
    while (expression instanceof UnaryExpression) {
      expression = ((UnaryExpression) expression).getChildExpression();
    }

    if (expression instanceof FunctionOrMacroInvocationExpression) {
      visit(((FunctionOrMacroInvocationExpression) expression).getArguments());
      return null;
    } else if (expression instanceof BinaryExpression<?>) {
      BinaryExpression<?> expr = (BinaryExpression<?>) expression;

      // analyze each
      analyzeExpression(expr.getLeftExpression());
      analyzeExpression(expr.getRightExpression());

      return null;
    } else if (expression instanceof ContextVariableExpression) {
      ContextVariableExpression expr = (ContextVariableExpression) expression;

      if (root) {
        return Collections.singletonList(expr.getName());
      } else {
        // this is always the beginning of a reference
        analyzeExpression(expr);

        return null;
      }
    } else if (expression instanceof GetAttributeExpression) {
      GetAttributeExpression expr = (GetAttributeExpression) expression;

      List<String> head = doAnalyzeExpression(expr.getNode(), root);
      if (head != null) {
        List<String> tail = doAnalyzeExpression(expr.getAttributeNameExpression(), false);
        if (tail != null) {
          head = new ArrayList<>(head);
          head.addAll(tail);
        }
        return head;
      }
    } else if (expression instanceof LiteralStringExpression && !root) {
      LiteralStringExpression expr = (LiteralStringExpression) expression;

      return Collections.singletonList(expr.getValue());
    }

    return null;
  }

  @Override
  public void visit(Node node) {
    // ignore
    // XXX for which nodes is this called?
  }

  @Override
  public void visit(ArgumentsNode node) {
    List<NamedArgumentNode> namedArgs = node.getNamedArgs();
    if (namedArgs != null) {
      namedArgs.forEach((n) -> n.accept(this));
    }

    List<PositionalArgumentNode> posArgs = node.getPositionalArgs();
    if (posArgs != null) {
      posArgs.forEach((n) -> n.accept(this));
    }
  }

  @Override
  public void visit(AutoEscapeNode node) {
    node.getBody().accept(this);
  }

  @Override
  public void visit(BlockNode node) {
    node.getBody().accept(this);
  }

  @Override
  public void visit(BodyNode node) {
    node.getChildren().forEach((n) -> n.accept(this));
  }

  @Override
  public void visit(ExtendsNode node) {
    analyzeExpression(node.getParentExpression());
  }

  @Override
  public void visit(FlushNode node) {
    // ignore
  }

  @Override
  public void visit(ForNode node) {
    Expression<?> iterable = node.getIterable();
    analyzeExpression(iterable);

    BodyNode forBody = node.getBody();
    if (forBody != null) {
      forBody.accept(this);
    }

    BodyNode elseBody = node.getElseBody();
    if (elseBody != null) {
      elseBody.accept(this);
    }
  }

  @Override
  public void visit(IfNode node) {
    node.getConditionsWithBodies().forEach((pair) -> {
      Expression<?> condition = pair.getLeft();
      analyzeExpression(condition);
      BodyNode then = pair.getRight();
      then.accept(this);
    });

    BodyNode elseBody = node.getElseBody();
    if (elseBody != null) {
      elseBody.accept(this);
    }
  }

  @Override
  public void visit(ImportNode node) {
    // ignore
  }

  @Override
  public void visit(IncludeNode node) {
    // ignore - not supported in config?
  }

  @Override
  public void visit(MacroNode node) {
    // ignore - not supported in config?
  }

  @Override
  public void visit(NamedArgumentNode node) {
    analyzeExpression(node.getValueExpression());
  }

  @Override
  public void visit(ParallelNode node) {
    // ignore - not supported in config?
  }

  @Override
  public void visit(PositionalArgumentNode node) {
    analyzeExpression(node.getValueExpression());
  }

  @Override
  public void visit(PrintNode node) {
    analyzeExpression(node.getExpression());
  }

  @Override
  public void visit(RootNode node) {
    node.getBody().accept(this);
  }

  @Override
  public void visit(SetNode node) {
    analyzeExpression(node.getValue());
  }

  @Override
  public void visit(TextNode node) {
    // not dynamic
  }

}
