# -*- coding: utf-8 -*-

#  Copyright © 2018-2019 AT&T Intellectual Property.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: BluePrintCommon.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='BluePrintCommon.proto',
  package='org.onap.ccsdk.cds.controllerblueprints.common.api',
  syntax='proto3',
  serialized_options=_b('P\001'),
  serialized_pb=_b('\n\x15\x42luePrintCommon.proto\x12\x32org.onap.ccsdk.cds.controllerblueprints.common.api\"\xa8\x01\n\x0c\x43ommonHeader\x12\x11\n\ttimestamp\x18\x01 \x01(\t\x12\x14\n\x0coriginatorId\x18\x17 \x01(\t\x12\x11\n\trequestId\x18\x03 \x01(\t\x12\x14\n\x0csubRequestId\x18\x04 \x01(\t\x12\x46\n\x04\x66lag\x18\x05 \x01(\x0b\x32\x38.org.onap.ccsdk.cds.controllerblueprints.common.api.Flag\"$\n\x04\x46lag\x12\x0f\n\x07isForce\x18\x01 \x01(\x08\x12\x0b\n\x03ttl\x18\x02 \x01(\x05\"f\n\x11\x41\x63tionIdentifiers\x12\x15\n\rblueprintName\x18\x01 \x01(\t\x12\x18\n\x10\x62lueprintVersion\x18\x02 \x01(\t\x12\x12\n\nactionName\x18\x03 \x01(\t\x12\x0c\n\x04mode\x18\x04 \x01(\t\"\xa2\x01\n\x06Status\x12\x0c\n\x04\x63ode\x18\x01 \x01(\x05\x12\x14\n\x0c\x65rrorMessage\x18\x02 \x01(\t\x12\x0f\n\x07message\x18\x03 \x01(\t\x12P\n\teventType\x18\x04 \x01(\x0e\x32=.org.onap.ccsdk.cds.controllerblueprints.common.api.EventType\x12\x11\n\ttimestamp\x18\x05 \x01(\t*\xa3\x01\n\tEventType\x12\x1b\n\x17\x45VENT_COMPONENT_FAILURE\x10\x00\x12\x1e\n\x1a\x45VENT_COMPONENT_PROCESSING\x10\x01\x12 \n\x1c\x45VENT_COMPONENT_NOTIFICATION\x10\x02\x12\x1c\n\x18\x45VENT_COMPONENT_EXECUTED\x10\x03\x12\x19\n\x15\x45VENT_COMPONENT_TRACE\x10\x04\x42\x02P\x01\x62\x06proto3')
)

_EVENTTYPE = _descriptor.EnumDescriptor(
  name='EventType',
  full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.EventType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='EVENT_COMPONENT_FAILURE', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='EVENT_COMPONENT_PROCESSING', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='EVENT_COMPONENT_NOTIFICATION', index=2, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='EVENT_COMPONENT_EXECUTED', index=3, number=3,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='EVENT_COMPONENT_TRACE', index=4, number=4,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=556,
  serialized_end=719,
)
_sym_db.RegisterEnumDescriptor(_EVENTTYPE)

EventType = enum_type_wrapper.EnumTypeWrapper(_EVENTTYPE)
EVENT_COMPONENT_FAILURE = 0
EVENT_COMPONENT_PROCESSING = 1
EVENT_COMPONENT_NOTIFICATION = 2
EVENT_COMPONENT_EXECUTED = 3
EVENT_COMPONENT_TRACE = 4



_COMMONHEADER = _descriptor.Descriptor(
  name='CommonHeader',
  full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='timestamp', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader.timestamp', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='originatorId', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader.originatorId', index=1,
      number=23, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='requestId', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader.requestId', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='subRequestId', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader.subRequestId', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='flag', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader.flag', index=4,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=78,
  serialized_end=246,
)


_FLAG = _descriptor.Descriptor(
  name='Flag',
  full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Flag',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='isForce', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Flag.isForce', index=0,
      number=1, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='ttl', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Flag.ttl', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=248,
  serialized_end=284,
)


_ACTIONIDENTIFIERS = _descriptor.Descriptor(
  name='ActionIdentifiers',
  full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='blueprintName', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers.blueprintName', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='blueprintVersion', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers.blueprintVersion', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionName', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers.actionName', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='mode', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers.mode', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=286,
  serialized_end=388,
)


_STATUS = _descriptor.Descriptor(
  name='Status',
  full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='code', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status.code', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='errorMessage', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status.errorMessage', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='message', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status.message', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='eventType', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status.eventType', index=3,
      number=4, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='timestamp', full_name='org.onap.ccsdk.cds.controllerblueprints.common.api.Status.timestamp', index=4,
      number=5, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=391,
  serialized_end=553,
)

_COMMONHEADER.fields_by_name['flag'].message_type = _FLAG
_STATUS.fields_by_name['eventType'].enum_type = _EVENTTYPE
DESCRIPTOR.message_types_by_name['CommonHeader'] = _COMMONHEADER
DESCRIPTOR.message_types_by_name['Flag'] = _FLAG
DESCRIPTOR.message_types_by_name['ActionIdentifiers'] = _ACTIONIDENTIFIERS
DESCRIPTOR.message_types_by_name['Status'] = _STATUS
DESCRIPTOR.enum_types_by_name['EventType'] = _EVENTTYPE
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

CommonHeader = _reflection.GeneratedProtocolMessageType('CommonHeader', (_message.Message,), {
  'DESCRIPTOR' : _COMMONHEADER,
  '__module__' : 'BluePrintCommon_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader)
  })
_sym_db.RegisterMessage(CommonHeader)

Flag = _reflection.GeneratedProtocolMessageType('Flag', (_message.Message,), {
  'DESCRIPTOR' : _FLAG,
  '__module__' : 'BluePrintCommon_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.common.api.Flag)
  })
_sym_db.RegisterMessage(Flag)

ActionIdentifiers = _reflection.GeneratedProtocolMessageType('ActionIdentifiers', (_message.Message,), {
  'DESCRIPTOR' : _ACTIONIDENTIFIERS,
  '__module__' : 'BluePrintCommon_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers)
  })
_sym_db.RegisterMessage(ActionIdentifiers)

Status = _reflection.GeneratedProtocolMessageType('Status', (_message.Message,), {
  'DESCRIPTOR' : _STATUS,
  '__module__' : 'BluePrintCommon_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.common.api.Status)
  })
_sym_db.RegisterMessage(Status)


DESCRIPTOR._options = None
# @@protoc_insertion_point(module_scope)
