/*
 * Copyright 2019-2020 the original authors
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

package com.github.hauner.openapi.core.writer.java

import io.openapiprocessor.core.converter.ApiOptions
import io.openapiprocessor.core.model.Api
import io.openapiprocessor.core.model.DataTypes
import io.openapiprocessor.core.model.Interface
import io.openapiprocessor.core.model.datatypes.MappedDataType
import io.openapiprocessor.core.model.datatypes.ObjectDataType
import io.openapiprocessor.core.model.datatypes.StringDataType
import io.openapiprocessor.core.model.datatypes.StringEnumDataType
import com.github.hauner.openapi.core.test.Sl4jMockRule
import io.openapiprocessor.core.writer.java.StringEnumWriter
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Ignore
import spock.lang.Specification

class ApiWriterSpec extends Specification {

    @Rule TemporaryFolder target

    def log = Mock Logger
    @Rule Sl4jMockRule rule = new Sl4jMockRule(ApiWriter, log)

    List<String> apiPkgPath = ['com', 'github', 'hauner', 'openapi', 'api']
    List<String> apiModelPath = ['com', 'github', 'hauner', 'openapi', 'model']

    void "creates package structure in target folder"() {
        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        when:
        new ApiWriter (opts, Stub (InterfaceWriter), null, null)
            .write (new Api())

        then:
        def api = new File([opts.targetDir, 'com', 'github', 'hauner', 'openapi', 'api'].join(File.separator))
        def model = new File([opts.targetDir, 'com', 'github', 'hauner', 'openapi', 'model'].join(File.separator))
        api.exists ()
        api.isDirectory ()
        model.exists ()
        model.isDirectory ()
    }

    void "does not log error when the target folder structure already exists" () {
        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        when:
        target.newFolder ('java', 'src', 'com', 'github', 'hauner', 'openapi', 'api')
        target.newFolder ('java', 'src', 'com', 'github', 'hauner', 'openapi', 'model')
        new ApiWriter (opts, Stub (InterfaceWriter), null, null).write (new Api())

        then:
        0 * log.error (*_)
    }

    void "generates interface sources in api target folder"() {
        def interfaceWriter = Stub (InterfaceWriter) {
            write (_ as Writer, _ as Interface) >> {
                Writer writer = it.get(0)
                writer.write ('Foo interface!\n')
            } >> {
                Writer writer = it.get(0)
                writer.write ('Bar interface!\n')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def api = new Api(interfaces: [
            new Interface('Foo', "${opts.packageName}.api", []),
            new Interface('Bar', "${opts.packageName}.api", [])
        ])

        when:
        new ApiWriter (opts, interfaceWriter, null, null, false)
            .write (api)

        then:
        def fooSource = new File(getApiPath (opts.targetDir, 'FooApi.java'))
        fooSource.text == """\
Foo interface!
"""
        def barSource = new File(getApiPath (opts.targetDir, 'BarApi.java'))
        barSource.text == """\
Bar interface!
"""
    }

    void "generates interface with valid java class name"() {
        def interfaceWriter = Stub (InterfaceWriter) {
            write (_ as Writer, _ as Interface) >> {
                Writer writer = it.get(0)
                writer.write ('interface!\n')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def api = new Api(interfaces: [
            new Interface('foo-bar', "${opts.packageName}.api", [])
        ])

        when:
        new ApiWriter (opts, interfaceWriter, null, null, false)
            .write (api)

        then:
        new File(getApiPath (opts.targetDir, 'FooBarApi.java')).exists ()
    }

    void "generates model sources in model target folder"() {
        def dataTypeWriter = Stub (DataTypeWriter) {
            write (_ as Writer, _ as ObjectDataType) >> {
                Writer writer = it.get(0)
                writer.write ('Foo class!\n')
            } >> {
                Writer writer = it.get(0)
                writer.write ('Bar class!\n')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def dt = new DataTypes()
        dt.add (new ObjectDataType('Foo', "${opts.packageName}.model", [:],
            null, false))
        dt.add (new ObjectDataType('Bar', "${opts.packageName}.model", [:],
            null, false))
        def api = new Api([], dt)

        when:
        new ApiWriter (opts, Stub(InterfaceWriter), dataTypeWriter, Stub(StringEnumWriter), false)
            .write (api)

        then:
        def fooSource = new File(getModelPath (opts.targetDir, 'Foo.java'))
        fooSource.text == """\
Foo class!
"""
        def barSource = new File(getModelPath (opts.targetDir, 'Bar.java'))
        barSource.text == """\
Bar class!
"""
    }

    void "generates model for object data types only" () {
        def dataTypeWriter = Mock (DataTypeWriter) {
            write (_ as Writer, _ as ObjectDataType) >> {
                Writer writer = it.get(0)
                writer.write ('Foo class!\n')
            } >> {
                Writer writer = it.get(0)
                writer.write ('Bar class!\n')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def dt = new DataTypes()
        dt.add (new ObjectDataType('Foo', "${opts.packageName}.model", [:],
            null, false))
        dt.add (new ObjectDataType('Bar', "${opts.packageName}.model", [:],
            null, false))
        dt.add (new MappedDataType('Type', 'mapped', [],
            null, false))
        dt.add ('simple', new StringDataType())
        def api = new Api([], dt)

        when:
        new ApiWriter (opts, Stub(InterfaceWriter), dataTypeWriter, Stub(StringEnumWriter), false)
            .write (api)

        then:
        0 * dataTypeWriter.write (_, dt.find ('simple'))
        0 * dataTypeWriter.write (_, dt.find ('Type'))
    }

    void "re-formats interface sources"() {
        def interfaceWriter = Stub (InterfaceWriter) {
            write (_ as Writer, _ as Interface) >> {
                Writer writer = it.get(0)
                writer.write ('  interface   Foo   {    }\n')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def api = new Api(interfaces: [
            new Interface('Foo', "${opts.packageName}.api", [])
        ])

        when:
        new ApiWriter (opts, interfaceWriter, null, null)
            .write (api)

        then:
        def fooSource = new File(getApiPath (opts.targetDir, 'FooApi.java'))
        fooSource.text == """\
interface Foo {
}
"""
    }

    void "re-formats model sources"() {
        def dataTypeWriter = Stub (DataTypeWriter) {
            write (_ as Writer, _ as ObjectDataType) >> {
                Writer writer = it.get(0)
                writer.write ('      class Foo {  }')
            }
        }

        def opts = new ApiOptions(
            packageName: 'com.github.hauner.openapi',
            targetDir: [target.root.toString (), 'java', 'src'].join (File.separator)
        )

        def dt = new DataTypes()
        dt.add (new ObjectDataType('Foo', "${opts.packageName}.model", [:],
            null, false))
        def api = new Api([], dt)

        when:
        new ApiWriter (opts, Stub(InterfaceWriter), dataTypeWriter, Stub(StringEnumWriter))
            .write (api)

        then:
        def fooSource = new File(getModelPath (opts.targetDir, 'Foo.java'))
        fooSource.text == """\
class Foo {
}
"""
    }

    String getApiPath(String targetFolder, String clazzName) {
        ([targetFolder] + apiPkgPath + [clazzName]).join(File.separator)
    }

    String getModelPath(String targetFolder, String clazzName) {
        ([targetFolder] + apiModelPath + [clazzName]).join(File.separator)
    }

}
