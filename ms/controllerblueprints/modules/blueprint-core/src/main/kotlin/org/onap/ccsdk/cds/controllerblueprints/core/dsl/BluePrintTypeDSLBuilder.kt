/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.*


open class EntityTypeBuilder(private val id: String,
                             private val version: String,
                             private val description: String? = "") {
    lateinit var derivedFrom: String
    var metadata: MutableMap<String, String>? = null
    var properties: MutableMap<String, PropertyDefinition>? = null
    var attributes: MutableMap<String, AttributeDefinition>? = null

    fun derivedFrom(derivedFrom: String) {
        this.derivedFrom = derivedFrom
    }

    fun metadata(key: String, value: String) {
        if (metadata == null)
            metadata = hashMapOf()
        metadata!![key] = value
    }

    fun attribute(id: String, type: String? = "string", required: Boolean? = false, description: String? = "") {
        if (attributes == null)
            attributes = hashMapOf()
        val attribute = AttributeDefinitionBuilder(id, type, required, description).build()
        attributes!![id] = attribute
    }

    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "") {
        if (properties == null)
            properties = hashMapOf()
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties!![id] = property
    }

    fun buildEntityType(entity: EntityType) {
        entity.id = id
        entity.description = description
        entity.version = version
        entity.derivedFrom = derivedFrom
        entity.metadata = metadata
        entity.properties = properties
        entity.attributes = attributes
    }
}

class NodeTypeBuilder(private val id: String, private val version: String,
                      private val description: String? = "") : EntityTypeBuilder(id, version, description) {
    private var nodeType = NodeType()
    private var capabilities: MutableMap<String, CapabilityDefinition>? = null
    private var requirements: MutableMap<String, RequirementDefinition>? = null
    private var interfaces: MutableMap<String, InterfaceDefinition>? = null
    private var artifacts: MutableMap<String, ArtifactDefinition>? = null

    fun capability(id: String, block: CapabilityDefinitionBuilder.() -> Unit) {
        if (capabilities == null)
            capabilities = hashMapOf()
        capabilities!![id] = CapabilityDefinitionBuilder(id).apply(block).build()
    }

    fun requirement(id: String, block: RequirementDefinitionBuilder.() -> Unit) {
        if (requirements == null)
            requirements = hashMapOf()
        requirements!![id] = RequirementDefinitionBuilder(id).apply(block).build()
    }

    fun artifact(id: String, type: String, file: String) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).build()
    }

    private fun nodeInterface(id: String, block: InterfaceDefinitionBuilder.() -> Unit) {
        if (interfaces == null)
            interfaces = hashMapOf()
        interfaces!![id] = InterfaceDefinitionBuilder(id).apply(block).build()
    }

    fun build(): NodeType {
        buildEntityType(nodeType)
        nodeType.capabilities = capabilities
        nodeType.requirements = requirements
        nodeType.interfaces = interfaces
        nodeType.artifacts = artifacts
        return nodeType
    }
}

class ArtifactTypeBuilder(private val id: String, private val version: String,
                          private val description: String? = "") : EntityTypeBuilder(id, version, description) {
    private var artifactType = ArtifactType()
    //TODO()
    fun build(): ArtifactType {
        buildEntityType(artifactType)
        return artifactType
    }
}

class RequirementTypeBuilder(private val id: String, private val version: String,
                             private val description: String? = "") : EntityTypeBuilder(id, version, description) {
    private var requirementType = RequirementType()
    // TODO()
    fun build(): RequirementType {
        buildEntityType(requirementType)
        return requirementType
    }
}

class RelationshipTypeBuilder(private val id: String, private val version: String,
                              private val description: String? = "") : EntityTypeBuilder(id, version, description) {
    private var relationshipType = RelationshipType()
    // TODO()
    fun build(): RelationshipType {
        buildEntityType(relationshipType)
        return relationshipType
    }
}

class DataTypeBuilder(private val id: String, private val version: String,
                      private val description: String? = "") : EntityTypeBuilder(id, version, description) {
    private var dataType = DataType()
    // TODO()
    fun build(): DataType {
        buildEntityType(dataType)
        return dataType
    }
}

class CapabilityDefinitionBuilder(private val id: String) {

    private var capabilityDefinition = CapabilityDefinition()
    private val properties: MutableMap<String, PropertyDefinition> = hashMapOf()
    // TODO()
    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "") {
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties.put(id, property)
    }

    fun build(): CapabilityDefinition {
        capabilityDefinition.id = id
        capabilityDefinition.properties = properties
        return capabilityDefinition
    }
}

class RequirementDefinitionBuilder(private val id: String) {
    private var requirementDefinition = RequirementDefinition()
    // TODO()
    fun build(): RequirementDefinition {
        requirementDefinition.id = id

        return requirementDefinition
    }
}

class InterfaceDefinitionBuilder(private val id: String) {

    private var interfaceDefinition: InterfaceDefinition = InterfaceDefinition()
    private var operations: MutableMap<String, OperationDefinition>? = null

    fun operation(id: String, description: String? = "", block: OperationDefinitionBuilder.() -> Unit) {
        if (operations == null)
            operations = hashMapOf()
        operations!![id] = OperationDefinitionBuilder(id, description).apply(block).build()
    }

    fun build(): InterfaceDefinition {
        interfaceDefinition.id = id
        interfaceDefinition.operations = operations
        return interfaceDefinition
    }
}

class OperationDefinitionBuilder(private val id: String,
                                 private val description: String? = "") {
    private var operationDefinition: OperationDefinition = OperationDefinition()

    fun inputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        operationDefinition.inputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun outputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        operationDefinition.outputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun build(): OperationDefinition {
        operationDefinition.id = id
        operationDefinition.description = description
        return operationDefinition
    }
}

class AttributesDefinitionBuilder {
    private val attributes: MutableMap<String, AttributeDefinition> = hashMapOf()

    fun property(id: String, attribute: AttributeDefinition) {
        attributes.put(id, attribute)
    }

    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "") {
        val attribute = AttributeDefinitionBuilder(id, type, required, description).build()
        attributes.put(id, attribute)
    }

    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "",
                 block: AttributeDefinitionBuilder.() -> Unit) {
        val attribute = AttributeDefinitionBuilder(id, type, required, description).apply(block).build()
        attributes.put(id, attribute)
    }

    fun build(): MutableMap<String, AttributeDefinition> {
        return attributes
    }
}

class AttributeDefinitionBuilder(private val id: String,
                                 private val type: String? = "string",
                                 private val required: Boolean? = false,
                                 private val description: String? = "") {

    private var attributeDefinition: AttributeDefinition = AttributeDefinition()

    fun entrySchema(entrySchemaType: String) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }

    // TODO("Constrains")

    fun defaultValue(defaultValue: JsonNode) {
        attributeDefinition.defaultValue = defaultValue
    }

    fun build(): AttributeDefinition {
        attributeDefinition.id = id
        attributeDefinition.type = type!!
        attributeDefinition.required = required
        attributeDefinition.description = description
        return attributeDefinition
    }
}

class PropertiesDefinitionBuilder {
    private val properties: MutableMap<String, PropertyDefinition> = hashMapOf()

    fun property(id: String, property: PropertyDefinition) {
        properties.put(id, property)
    }

    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "") {
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties.put(id, property)
    }

    fun property(id: String, type: String? = "string", required: Boolean? = false, description: String? = "",
                 block: PropertyDefinitionBuilder.() -> Unit) {
        val property = PropertyDefinitionBuilder(id, type, required, description).apply(block).build()
        properties.put(id, property)
    }

    fun build(): MutableMap<String, PropertyDefinition> {
        return properties
    }
}

class PropertyDefinitionBuilder(private val id: String,
                                private val type: String? = "string",
                                private val required: Boolean? = false,
                                private val description: String? = "") {

    private var propertyDefinition: PropertyDefinition = PropertyDefinition()

    fun entrySchema(entrySchemaType: String) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }
    // TODO("Constrains")

    fun defaultValue(defaultValue: JsonNode) {
        propertyDefinition.defaultValue = defaultValue
    }

    fun build(): PropertyDefinition {
        propertyDefinition.id = id
        propertyDefinition.type = type!!
        propertyDefinition.required = required
        propertyDefinition.description = description
        return propertyDefinition
    }
}

class EntrySchemaBuilder(private val type: String) {
    private var entrySchema: EntrySchema = EntrySchema()

    fun build(): EntrySchema {
        entrySchema.type = type
        return entrySchema
    }
}