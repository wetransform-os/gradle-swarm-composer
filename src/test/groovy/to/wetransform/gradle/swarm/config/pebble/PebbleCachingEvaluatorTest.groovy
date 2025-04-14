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

import io.pebbletemplates.pebble.error.PebbleException
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
  void testValueSameMap() {
    def config = [
      foo: 'Test',
      blub: '{{ foo }}',
      bar: '{{ blub }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      foo: 'Test',
      blub: 'Test',
      bar: 'Test'
    ]

    assert evaluated == expected
  }

  @Test
  void testValueNestedCrossRef() {
    def config = [
      foo: 'Test',
      blub: [
        no1: '{{ foo }}',
        no2: '{{ blub.no1 }}',
        no3: [
          np1: '{{ blub.no1 }}',
          np2: '{{ blub.no3.np1 }}'
        ]
      ],
      bar: '{{ blub.no1 }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      foo: 'Test',
      blub: [
        no1: 'Test',
        no2: 'Test',
        no3: [
          np1: 'Test',
          np2: 'Test'
        ]
      ],
      bar: 'Test'
    ]

    assert evaluated == expected
  }

  @Test
  void testEvaluationOrder() {
    def config = [
      proxy: [
        use_paths: "{{ not proxy.no_proxy }}",
        no_proxy: true
      ]
    ]

    def evaluated = eval.evaluate(config)

    assert evaluated.proxy.no_proxy == true
    assert evaluated.proxy.use_paths == false

    // other evaluation order
    evaluated = eval.evaluate(config)

    assert evaluated.proxy.use_paths == false
    assert evaluated.proxy.no_proxy == true
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

  @Test
  void testMergeLiterals() {
    def config = [
      merged: '{{ {"foo": "bar"} | merge({"bar": "foo"}) | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      merged: [
        foo: 'bar',
        bar: 'foo'
      ]
    ]

    assert evaluated == expected
  }

  @Test
  void testMergeInput() {
    def config = [
      defaults: [
        foo: 'bar',
        test: 'example'
      ],
      merged: '{{ defaults | merge({"bar": "foo", "test": "concrete"}) | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      defaults: [
        foo: 'bar',
        test: 'example'
      ],
      merged: [
        foo: 'bar',
        bar: 'foo',
        test: 'concrete'
      ]
    ]

    assert evaluated == expected
  }

  @Test
  void testMergeNestedInput() {
    def config = [
      platform: [
        defaults: [
          foo: 'bar',
          test: 'example'
        ]
      ],
      merged: '{{ platform.defaults | merge({"bar": "foo", "test": "concrete"}) | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      platform: [
        defaults: [
          foo: 'bar',
          test: 'example'
        ]
      ],
      merged: [
        foo: 'bar',
        bar: 'foo',
        test: 'concrete'
      ]
    ]

    assert evaluated == expected
  }

  @Test
  void testMergeVars() {
    def config = [
      defaults: [
        foo: 'bar',
        test: 'example'
      ],
      override: [
        bar: 'foo',
        test: 'concrete'
      ],
      merged: '{{ defaults | merge(override) | expand }}'
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      defaults: [
        foo: 'bar',
        test: 'example'
      ],
      override: [
        bar: 'foo',
        test: 'concrete'
      ],
      merged: [
        foo: 'bar',
        bar: 'foo',
        test: 'concrete'
      ]
    ]

    assert evaluated == expected
  }

  @Test
  void testFail() {
    def config = [
      fail: true,
      check: '{% if fail %}{{ fail("fail should not be set to true") }}{% endif %}'
    ]

    try {
      def evaluated = eval.evaluate(config)

      evaluated.check

      throw new IllegalStateException("should not reach here")
    } catch (PebbleException e) {
      assert e.message.startsWith('fail should not be set to true')
    }
  }

  @Test
  void testFilterExpand() {
    def config = [
      root: [
        namespaces: [
          'namespace1': [
            enabled: true,
            test: 'test1'
          ],
          'namespace2': [
            enabled: false,
            test: 'test2'
          ]
        ],
        computed_config: [
          namespaces: '{{ root.namespaces | default({}) | filter(\'it.value.enabled | default(true)\') | expand }}'
        ],
        json: [
          namespaces: '{{ root.computed_config.namespaces | json }}'
        ]
      ]
    ]

    def evaluated = eval.evaluate(config)

    def expected = [
      root: [
        namespaces: [
          'namespace1': [
            enabled: true,
            test: 'test1'
          ],
          'namespace2': [
            enabled: false,
            test: 'test2'
          ]
        ],
        computed_config: [
          namespaces: [
            'namespace1': [
              enabled: true,
              test: 'test1'
            ]
          ]
        ],
        json: [
          namespaces: '{"namespace1":{"enabled":true,"test":"test1"}}'
        ]
      ]
    ]

    assert evaluated == expected
  }

}
