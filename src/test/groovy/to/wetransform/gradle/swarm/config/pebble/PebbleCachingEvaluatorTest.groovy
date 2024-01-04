/*
 * Copyright 2021 wetransform GmbH
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

package to.wetransform.gradle.swarm.config.pebble

import org.junit.Ignore
import org.junit.Test

/**
 * Test for PebbleCachingEvaluator.
 *
 * @author Simon Templer
 */
class PebbleCachingEvaluatorTest extends ConfigEvaluatorTest<PebbleCachingEvaluator> {

  @Override
  protected PebbleCachingEvaluator createEvaluator() {
    return new PebbleCachingEvaluator()
  }

  @Test
  void testValueNested() {
    def config = [
      object: [name: 'Object1'],
      value: 'Hallo {{ object.name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      object: [name: 'Object1'],
      value: 'Hallo Object1'
      ]

    assert evaluated == expected
  }

  @Test(expected = IllegalStateException)
  void testValueLoop() {
    def config = [
      foo: '{{ bar }}',
      bar: '{{ foo }}'
      ]

    def evaluated = eval.evaluate(config)

    evaluated.bar
  }

  @Test
  void testValueNested2() {
    def config = [
      object: [name: 'Object{{ object.num }}', num: 1],
      value: 'Hallo {{ object.name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      object: [name: 'Object1', num: 1],
      value: 'Hallo Object1'
      ]

    assert evaluated == expected
  }

  @Test
  void testValueNested2Order() {
    def config = [
      value: 'Hallo {{ object.name }}',
      object: [name: 'Object{{ object.num }}', num: 1]
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      object: [name: 'Object1', num: 1],
      value: 'Hallo Object1'
      ]

    assert evaluated == expected
  }

  @Test
  void testDependenciesValueNested3() {
    def config = [
      object: [
        properties: [
          name: 'Object{{ object.properties.num }}',
          num: 1
        ]
      ],
      value: 'Hallo {{ object.properties.name }}'
      ]

    def evaluated = eval.evaluate(config)

    // check value directly to rule out any effect in evaluation order
    assert evaluated.value == 'Hallo Object1'

    def expected = [
      object: [
        properties: [
          name: 'Object1',
          num: 1
        ]
      ],
      value: 'Hallo Object1'
      ]

    assert evaluated == expected
  }

  @Test
  void testDependenciesValueMapAccessor() {
    def config = [
      object: [
        properties: [
          name: 'Object{{ object.properties.num }}',
          num: 1
        ]
      ],
      value: 'Hallo {{ object[\'properties\'].name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      object: [
        properties: [
          name: 'Object1',
          num: 1
        ]
      ],
      value: 'Hallo Object1'
      ]

    assert evaluated == expected
  }

  @Test
  void testValueDependent() {
    def config = [
      variable: 'bar',
      object: [
        foo: [name: 'Foo'],
        bar: [name: 'Bar']
        ],
      value: 'Hallo {{ object[variable].name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      variable: 'bar',
      object: [
        foo: [name: 'Foo'],
        bar: [name: 'Bar']
        ],
      value: 'Hallo Bar'
      ]

    assert evaluated == expected
  }

  @Test
  void testValueDependent2() {
    def config = [
      variable: [sub: 'bar'],
      object: [
        foo: [name: 'Foo'],
        bar: [name: 'Bar']
        ],
      value: 'Hallo {{ object[variable.sub].name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      variable: [sub: 'bar'],
      object: [
        foo: [name: 'Foo'],
        bar: [name: 'Bar']
        ],
      value: 'Hallo Bar'
      ]

    assert evaluated == expected
  }

  @Test
  void testDependenciesValueComplex() {
    def config = [
      latest_config_version: 10,
      condition: true, // as string?
      config_breaking_version: '{{ latest_config_version }}',
      value: '{{ (toInt(config_breaking_version) < 4) and (not condition) }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      latest_config_version: 10,
      condition: true, // as string?
      config_breaking_version: '10',
      value: false
      ]

    assert evaluated == expected
  }

  @Test
  void testExpandList() {
    def config = [
      name: 'Jim',
      list: ['Hello {{ name }}', 'Bye {{ name }}'],
      copy: '{{ list | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'Jim',
      list: ['Hello Jim', 'Bye Jim'],
      copy: ['Hello Jim', 'Bye Jim']
    ]

    assert evaluated == expected
  }

  @Test
  void testExpandNumber() {
    def config = [
      number: 12,
      copy: '{{ number | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      number: 12,
      copy: 12
    ]

    assert evaluated == expected
  }

  @Test
  void testNotExpandNumber() {
    def config = [
      number: 12,
      copy: '{{ number }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      number: 12,
      copy: '12'
    ]

    assert evaluated == expected
  }

  @Test
  void testExpandMap() {
    def config = [
      name: 'Jim',
      map: [
        name: '{{ name }}',
        hello: 'Hello {{ name }}',
        there: 'Tom',
        helloThere: 'Hello {{ _.there }}'
      ],
      copy: '{{ map | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'Jim',
      map: [
        name: 'Jim',
        hello: 'Hello Jim',
        there: 'Tom',
        helloThere: 'Hello Tom'
      ],
      copy: [
        name: 'Jim',
        hello: 'Hello Jim',
        there: 'Tom',
        helloThere: 'Hello Tom'
      ]
    ]

    assert evaluated == expected
  }

  @Test
  void testExpandMix() {
    def config = [
      name: 'Jim',
      map: [
        hello: 'Hello {{ name }}',
        list: ['{{ map.hello }}', 'Bye {{ name }}'],
        more: [
          foo: 'bar',
          story: '{{ name }} went to a bar.'
        ]
      ],
      copy: '{{ map | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'Jim',
      map: [
        hello: 'Hello Jim',
        list: ['Hello Jim', 'Bye Jim'],
        more: [
          foo: 'bar',
          story: 'Jim went to a bar.'
        ]
      ],
      copy: [
        hello: 'Hello Jim',
        list: ['Hello Jim', 'Bye Jim'],
        more: [
          foo: 'bar',
          story: 'Jim went to a bar.'
        ]
      ]
    ]

    assert evaluated == expected
  }

}
