/*
 * Copyright 2020 the original authors
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

package io.openapiprocessor.core.parser.openapi4j

import io.openapiprocessor.core.parser.Parameter as ParserParameter
import io.openapiprocessor.core.parser.Schema as ParserSchema
import org.openapi4j.parser.model.v3.Parameter as O4jParameter

/**
 * openapi4j Parameter abstraction.
 *
 * @author Martin Hauner
 */
class Parameter(private val parameter: O4jParameter): ParserParameter {

    override fun getIn(): String = parameter.`in`

    override fun getName(): String = parameter.name

    override fun getSchema(): ParserSchema = Schema (parameter.schema)

    override fun isRequired(): Boolean = if(parameter.required != null) {
        parameter.required
    } else {
        false
    }

    override fun isDeprecated(): Boolean = parameter.deprecated ?: false

}
