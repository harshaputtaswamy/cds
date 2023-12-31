.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.


The **C**\ ontroller **B**\ lueprint **A**\ rchive is the overall service design, fully model-driven, intent based
**package** needed for SELF SERVICE provisioning and configuration management automation.

The CBA is **.zip** file, comprised of the following folder structure, the files may vary:

.. code-block language is required for ReadTheDocs to render code-blocks. Python set as default.

.. code-block::

    ├── Definitions
    │   ├── blueprint.json                          Overall TOSCA service template (workflow + node_template)
    │   ├── artifact_types.json                     (generated by enrichment)
    │   ├── data_types.json                         (generated by enrichment)
    │   ├── policy_types.json                       (generated by enrichment)
    │   ├── node_types.json                         (generated by enrichment)
    │   ├── relationship_types.json                 (generated by enrichment)
    │   ├── resources_definition_types.json         (generated by enrichment, based on Data Dictionaries)
    │   └── *-mapping.json                          One per Template
    │
    ├── Environments                                Contains *.properties files as required by the service
    │
    ├── Plans                                       Contains Directed Graph
    │
    ├── Tests                                       Contains uat.yaml file for testing cba actions within a cba package
    │
    ├── Scripts                                     Contains scripts
    │   ├── python                                  Python scripts
    │   └── kotlin                                  Kotlin scripts
    │
    ├── TOSCA-Metadata
    │   └── TOSCA.meta                              Meta-data of overall package
    │
    └── Templates                                   Contains combination of mapping and template

To process a CBA for any service we need to enrich it first. This will gather all the node- type, data-type,
artifact-type, data-dictionary definitions provided in the blueprint.json.
