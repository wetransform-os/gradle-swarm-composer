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

import com.mitchellbosecke.pebble.extension.NodeVisitor;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.node.AutoEscapeNode;
import com.mitchellbosecke.pebble.node.BlockNode;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.ExtendsNode;
import com.mitchellbosecke.pebble.node.FlushNode;
import com.mitchellbosecke.pebble.node.ForNode;
import com.mitchellbosecke.pebble.node.IfNode;
import com.mitchellbosecke.pebble.node.ImportNode;
import com.mitchellbosecke.pebble.node.IncludeNode;
import com.mitchellbosecke.pebble.node.MacroNode;
import com.mitchellbosecke.pebble.node.NamedArgumentNode;
import com.mitchellbosecke.pebble.node.Node;
import com.mitchellbosecke.pebble.node.ParallelNode;
import com.mitchellbosecke.pebble.node.PositionalArgumentNode;
import com.mitchellbosecke.pebble.node.PrintNode;
import com.mitchellbosecke.pebble.node.RootNode;
import com.mitchellbosecke.pebble.node.SetNode;
import com.mitchellbosecke.pebble.node.TextNode;

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
