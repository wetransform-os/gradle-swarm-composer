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

/**
 * Visitor that checks if there is any dynamic content in the visited nodes.
 *
 * @author Simon Templer
 */
public class DynamicCheckVisitor implements NodeVisitor {

  private boolean dynamic = false;

  public boolean isDynamic() {
    return dynamic;
  }

  @Override
  public void visit(Node node) {
    // ignore
    //XXX for which nodes is this called?
  }

  @Override
  public void visit(ArgumentsNode node) {
    dynamic = true;
  }

  @Override
  public void visit(AutoEscapeNode node) {
    dynamic = true;
  }

  @Override
  public void visit(BlockNode node) {
    dynamic = true;
  }

  @Override
  public void visit(BodyNode node) {
    node.getChildren().forEach((n) -> n.accept(this));
  }

  @Override
  public void visit(ExtendsNode node) {
    dynamic = true;
  }

  @Override
  public void visit(FlushNode node) {
    dynamic = true;
  }

  @Override
  public void visit(ForNode node) {
    dynamic = true;
  }

  @Override
  public void visit(IfNode node) {
    dynamic = true;
  }

  @Override
  public void visit(ImportNode node) {
    dynamic = true;
  }

  @Override
  public void visit(IncludeNode node) {
    dynamic = true;
  }

  @Override
  public void visit(MacroNode node) {
    dynamic = true;
  }

  @Override
  public void visit(NamedArgumentNode node) {
    dynamic = true;
  }

  @Override
  public void visit(ParallelNode node) {
    dynamic = true;
  }

  @Override
  public void visit(PositionalArgumentNode node) {
    dynamic = true;
  }

  @Override
  public void visit(PrintNode node) {
    dynamic = true;
  }

  @Override
  public void visit(RootNode node) {
    node.getBody().accept(this);
  }

  @Override
  public void visit(SetNode node) {
    dynamic = true;
  }

  @Override
  public void visit(TextNode node) {
    // not dynamic
  }

}
