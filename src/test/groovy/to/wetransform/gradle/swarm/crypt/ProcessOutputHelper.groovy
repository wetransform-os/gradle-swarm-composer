/*
 * Copyright 2026 wetransform GmbH
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
package to.wetransform.gradle.swarm.crypt

import java.nio.charset.StandardCharsets

/**
 * Helper process for testing the process command runner independent of the platform.
 *
 * Modes:
 * <ul>
 * <li><code>output &lt;size&gt; &lt;exitCode&gt;</code> - write size times 'o' to stdout and 'e' to stderr, exit with code</li>
 * <li><code>stdin</code> - read stdin until EOF, then print 'done'</li>
 * <li><code>utf8</code> - write the UTF-8 bytes of 'ü' without a line ending</li>
 * </ul>
 *
 * @author Simon Templer
 */
class ProcessOutputHelper {

  /**
   * Build the command to run this helper in a new JVM.
   */
  static List<String> command(String... args) {
    String java = ProcessHandle.current().info().command()
      .orElse(new File(System.getProperty('java.home'), 'bin/java').absolutePath)
    [
      java,
      '-cp',
      System.getProperty('java.class.path'),
      ProcessOutputHelper.name
    ] + args.toList()
  }

  static void main(String[] args) {
    switch (args[0]) {
      case 'output':
        int size = args[1] as int
        System.out.write(('o' * size).getBytes(StandardCharsets.US_ASCII))
        System.err.write(('e' * size).getBytes(StandardCharsets.US_ASCII))
        System.out.flush()
        System.err.flush()
        System.exit(args[2] as int)
        break
      case 'stdin':
        System.in.bytes
        System.out.println('done')
        break
      case 'utf8':
        System.out.write('ü'.getBytes(StandardCharsets.UTF_8))
        System.out.flush()
        break
      default:
        throw new IllegalArgumentException("Unknown mode ${args[0]}")
    }
  }
}
