/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.common.testing.suite.dex;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.isEmpty;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import com.google.android.apps.common.testing.proto.TestInfo;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationPb;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationValuePb;
import com.google.android.apps.common.testing.suite.dex.DexClassData.MethodData;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CodingErrorAction;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Iterates thru a dexdump xml file.
 *
 * These files are very large (~10-100mb) to ensure efficent
 * parsing attempt to handle each DexClassData result immediately
 * on retrieval or shortly afterwards so its eligible for GC. Do
 * not parse the documents multiple times (certainly) and do not
 * try to keep all the DexClassDatas in memory.
 *
 */
class DexDumpIterator extends AbstractIterator<DexClassData> {

  private static final String SYSTEM_ANNOTATION_PACKAGE = "dalvik.annotation";
  private static final String DEFAULT_ANNOTATION_CLASS = "dalvik.annotation.AnnotationDefault";

  private static final ImmutableMap<String, TestInfo.Type> XML_TO_PB_TYPE =
      new ImmutableMap.Builder<String, TestInfo.Type>()
        .put("BOOL", TestInfo.Type.BOOL)
        .put("ENUM", TestInfo.Type.ENUM)
        .put("METHOD", TestInfo.Type.METHOD)
        .put("FIELD", TestInfo.Type.FIELD)
        .put("TYPE", TestInfo.Type.CLASS)
        .put("STRING", TestInfo.Type.STRING)
        .put("DOUBLE", TestInfo.Type.DOUBLE)
        .put("FLOAT", TestInfo.Type.FLOAT)
        .put("LONG", TestInfo.Type.LONG)
        .put("INT", TestInfo.Type.INTEGER)
        .put("CHAR", TestInfo.Type.CHAR)
        .put("SHORT", TestInfo.Type.SHORT)
        .put("BYTE", TestInfo.Type.BYTE)
        .put("NULL", TestInfo.Type.NULL)
        .build();
  private final XMLStreamReader xmlStreamReader;
  private Location currentLocation;
  private String currentPackage;

  DexDumpIterator(InputStream xmlIn) {
    this(liberalDataReader(xmlIn));
  }

  private DexDumpIterator(Reader inReader) {
    super();
    checkNotNull(inReader);
    try {
      xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(inReader);
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }

  private boolean streamHasNext() {
    try {
      return xmlStreamReader.hasNext();
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }

  private int streamNext() {
    try {
      return xmlStreamReader.next();
    } catch (XMLStreamException xse) {
      throw new RuntimeException(xse);
    }
  }

  private String getAttributeValue(String attributeName) {
    String result = xmlStreamReader.getAttributeValue(null, attributeName);
    return result;
  }

  @Override
  protected DexClassData computeNext() {
    DexClassData.Builder currentClass = null;
    MethodData.MethodBuilder currentMethod = null;
    Deque<AnnotationContext> annotationStack = Lists.newLinkedList();
    boolean inConstructor = false;

    while (streamHasNext()) {
      String name = null;
      switch (streamNext()) {
        case END_DOCUMENT:
          return endOfData();
        case START_ELEMENT:
          currentLocation = xmlStreamReader.getLocation();
          name = xmlStreamReader.getLocalName();
          if ("package".equals(name)) {
            currentPackage = handleNewPackageNode();
          } else if ("class".equals(name)) {
            currentClass = handleNewClassNode(currentClass);
          } else if ("method".equals(name)) {
            currentMethod = handleNewMethodNode(currentMethod);
          } else if ("parameter".equals(name)) {
            currentMethod = handleParameterNode(currentMethod, inConstructor);
          } else if ("constructor".equals(name)) {
            checkState(!inConstructor, "Already in a constructor node. %s",
                currentLocation);
            inConstructor = true;
          } else if ("annotation".equals(name)) {
            AnnotationContext context = new AnnotationContext();
            annotationStack.push(context);
            context.currentAnnotation = handleNewAnnotationNode();
          } else if ("anno_field".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentAnnotationValue = handleNewAnnotationFieldNode(
                context.currentAnnotationValue);
            annotationStack.push(context);
          } else if ("anno_field_value".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentAnnotationValue = handleNewAnnotationFieldValueNode(
                context.currentAnnotationValue,
                context.currentArrayStatus);
            annotationStack.push(context);
          } else if ("anno_field_array".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentArrayStatus = handleNewAnnotationFieldArrayNode(
                context.currentAnnotationValue,
                context.currentArrayStatus);
            annotationStack.push(context);
          } else if ("array_element".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentArrayStatus = handleNewAnnotationFieldArrayElementNode(
                context.currentAnnotationValue,
                context.currentArrayStatus);
            annotationStack.push(context);
          }
          break;
        case END_ELEMENT:
          currentLocation = xmlStreamReader.getLocation();
          name = xmlStreamReader.getLocalName();
          if ("package".equals(name)) {
            currentPackage = handleEndPackageNode();
          } else if ("class".equals(name)) {
            return handleEndClassNode(currentClass, currentMethod, annotationStack);
          } else if ("method".equals(name)) {
            currentMethod = handleEndMethodNode(currentClass, currentMethod);
          } else if ("constructor".equals(name)) {
            checkState(inConstructor, "Not in constructor. %s", currentLocation);
            inConstructor = false;
          } else if ("annotation".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            checkState(null == context.currentAnnotationValue, "Never closed value node %s",
                currentLocation);
            checkState(null == context.currentArrayStatus, "Never closed array node %s",
                currentLocation);

            if (annotationStack.isEmpty()) {
              handleEndAnnotationNode(
                  context.currentAnnotation,
                  currentClass,
                  currentMethod,
                  inConstructor);
            } else {
              AnnotationContext parentContext = annotationStack.pop();
              AnnotationPb nestedAnnotation =
                  handleEndNestedAnnotation(
                      context.currentAnnotation,
                      parentContext.currentAnnotation,
                      parentContext.currentAnnotationValue);
              parentContext.nestedAnnotations.add(nestedAnnotation);
              annotationStack.push(parentContext);
            }
          } else if ("anno_field".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentAnnotationValue = handleEndAnnotationFieldNode(
                context.currentAnnotation,
                context.currentAnnotationValue,
                context.nestedAnnotations);
            context.nestedAnnotations.clear();
            annotationStack.push(context);
          } else if ("anno_field_value".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            checkState(null != context.currentAnnotationValue, "anno value not set. %s",
                currentLocation);
            annotationStack.push(context);
          } else if ("anno_field_array".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentArrayStatus = handleEndAnnotationFieldArrayNode(
                context.currentAnnotationValue,
                context.currentArrayStatus);
            annotationStack.push(context);
          } else if ("array_element".equals(name)) {
            checkState(!annotationStack.isEmpty(), "Not in annotation! %s",
                currentLocation);
            AnnotationContext context = annotationStack.pop();
            context.currentArrayStatus = handleEndAnnotationFieldArrayElementNode(
                context.currentAnnotationValue,
                context.currentArrayStatus);
            annotationStack.push(context);
          }
          break;
        default: { /* don't care */ }
      }
      currentLocation = null;
    }
    throw new IllegalStateException("Should never happen. Document end not emitted?");
  }

  private static class AnnotationContext {
    private AnnotationPb.Builder currentAnnotation = null;
    private AnnotationValuePb.Builder currentAnnotationValue = null;
    private ArrayStatus currentArrayStatus = null;
    private List<AnnotationPb> nestedAnnotations = Lists.newArrayList();
  }

  private static class ArrayStatus {
    private Integer currentIndex = null;
    private final BitSet setElements;
    private final int len;
    public ArrayStatus(int arrayLength) {
      checkArgument(arrayLength > -1);
      len = arrayLength;
      if (arrayLength == 0) {
        setElements = null;
      } else {
        setElements = new BitSet(arrayLength);
      }
    }

    public void setCurrentIndex(Integer currentIndex) {
      if (null == currentIndex) {
        this.currentIndex = null;
        return;
      } else {
        checkArgument(currentIndex < len, "Index over bounds! index: %s len: %s", currentIndex,
            len);
        checkState(currentIndex > -1, "Index under bounds! %s", currentIndex);
        checkState(!setElements.get(currentIndex), "Element already processed! %s ", currentIndex);
        setElements.set(currentIndex);
        this.currentIndex = currentIndex;
      }
    }

    public Integer getCurrentIndex() {
      return currentIndex;
    }

    public boolean allElementsSet() {
      if (null == setElements) {
        return true;
      }

      return setElements.cardinality() == len;
    }
  }

  private AnnotationPb handleEndNestedAnnotation(
      AnnotationPb.Builder endingAnnotation,
      AnnotationPb.Builder parentAnnotation,
      AnnotationValuePb.Builder parentValue) {
    checkState("annotation".equals(xmlStreamReader.getLocalName()), "not an " +
        "annotation node %s", currentLocation);
    checkState(null != endingAnnotation, "Not building an annotation? %s",
        currentLocation);
    checkState(null != parentAnnotation, "No parent annotation? %s",
        currentLocation);
    checkState(null != parentValue, "No parent value? %s",
        currentLocation);
    return endingAnnotation.build();
  }

  private AnnotationPb.Builder handleEndAnnotationNode(AnnotationPb.Builder currentAnnotation,
      DexClassData.Builder currentClass, MethodData.MethodBuilder currentMethod,
      boolean inConstructor) {
    checkState("annotation".equals(xmlStreamReader.getLocalName()), "not an " +
        "annotation node %s", currentLocation);
    checkState(null != currentAnnotation, "Not building an annotation? %s",
        currentLocation);
    if (currentAnnotation.getClassName().startsWith(SYSTEM_ANNOTATION_PACKAGE) &&
        !DEFAULT_ANNOTATION_CLASS.equals(currentAnnotation.getClassName())) {
      // discard system annotations - unless they're holding default annotation info.
      return null;
    }
    if (null != currentMethod) {
      currentMethod.addAnnotation(currentAnnotation.build());
    } else if (null != currentClass) {
      if (!inConstructor) {
        currentClass.addAnnotation(currentAnnotation.build());
      } // else ignore the constructor annos, dont need them for now.
    } else {
      throw new IllegalStateException(String.format("Annotation occurs outside of class/method"
          + " context. %s", currentLocation));
    }
    return null;
  }

  private AnnotationValuePb.Builder handleEndAnnotationFieldNode(
      AnnotationPb.Builder currentAnnotation, AnnotationValuePb.Builder currentAnnotationValue,
      Iterable<AnnotationPb> nestedAnnotations) {
    checkState("anno_field".equals(xmlStreamReader.getLocalName()), "not an " +
        "anno_field node");

    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    checkState(null != currentAnnotation, "Not building annotation! %s",
        currentLocation);
    if (!isEmpty(nestedAnnotations)) {
      currentAnnotationValue.addAllFieldAnnotationValue(nestedAnnotations);
      currentAnnotationValue.setFieldType(TestInfo.Type.ANNOTATION);
    }

    int fieldCount = 0;
    if (currentAnnotationValue.getFieldType().equals(TestInfo.Type.ANNOTATION)) {
      fieldCount = currentAnnotationValue.getFieldAnnotationValueCount();
    } else {
      fieldCount = currentAnnotationValue.getFieldValueCount();
    }
    if (!currentAnnotationValue.getIsArray()) {
      checkState(fieldCount == 1, "Never processed field value!"
          + " %s", currentLocation);
    }

    // else we could have had a 0 length array, so thats ok.
    if (fieldCount > 0) {
      checkState(currentAnnotationValue.hasFieldType(), "No field type in anno value! %s",
          currentLocation);
    }

    currentAnnotation.addAnnotationValue(currentAnnotationValue.build());
    return null;
  }

  private ArrayStatus handleEndAnnotationFieldArrayNode(
      AnnotationValuePb.Builder currentAnnotationValue, ArrayStatus currentArrayStatus) {
    checkState("anno_field_array".equals(xmlStreamReader.getLocalName()), "not an " +
        "anno_field_array node. %s", currentLocation);
    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    checkState(null != currentArrayStatus, "Have not processed anno_field_array yet. %s",
        currentLocation);
    checkState(null == currentArrayStatus.getCurrentIndex(), "current index still set. %s",
        currentLocation);
    checkState(currentArrayStatus.allElementsSet(), "some elements not set by end of node! %s",
        currentLocation);
    return null;
  }

  private ArrayStatus handleEndAnnotationFieldArrayElementNode(
      AnnotationValuePb.Builder currentAnnotationValue, ArrayStatus currentArrayStatus) {
    checkState("array_element".equals(xmlStreamReader.getLocalName()), "not an " +
        "array_element node %s", currentLocation);
    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    checkState(null != currentArrayStatus, "Have not processed anno_field_array yet %s",
        currentLocation);
    checkState(null != currentArrayStatus.getCurrentIndex(), "current index not set %s",
        currentLocation);
    currentArrayStatus.setCurrentIndex(null);
    return currentArrayStatus;
  }

  private ArrayStatus handleNewAnnotationFieldArrayElementNode(
      AnnotationValuePb.Builder currentAnnotationValue, ArrayStatus currentArrayStatus) {
    checkState("array_element".equals(xmlStreamReader.getLocalName()), "not an " +
        "array_element node. %s", currentLocation);
    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    checkState(null != currentArrayStatus, "Have not processed anno_field_array yet. %s",
        currentLocation);
    checkState(null == currentArrayStatus.getCurrentIndex(), "already set a current index! %s",
        currentLocation);

    String currentIndex = getAttributeValue("index");
    checkState(null != currentIndex, "Index attribute not found on array_element node! %s",
        currentLocation);

    currentArrayStatus.setCurrentIndex(Integer.parseInt(currentIndex));
    return currentArrayStatus;
  }

  private ArrayStatus handleNewAnnotationFieldArrayNode(
      AnnotationValuePb.Builder currentAnnotationValue, ArrayStatus currentArrayStatus) {
    checkState("anno_field_array".equals(xmlStreamReader.getLocalName()), "not an " +
        "anno_field_array node. %s", currentLocation);
    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    checkState(null == currentArrayStatus, "Already processing a anno_field_array %s",
        currentLocation);
    String arrayLength = getAttributeValue("length");

    checkState(null != arrayLength, "No length attribute on anno_field_array node. %s",
        currentLocation);

    currentAnnotationValue.setIsArray(true);
    int length = Integer.parseInt(arrayLength);

    for (int i = 0; i < length; i++) {
      currentAnnotationValue.addFieldValue("");
    }

    return new ArrayStatus(length);
  }

  private AnnotationValuePb.Builder handleNewAnnotationFieldValueNode(
      AnnotationValuePb.Builder currentAnnotationValue, ArrayStatus currentArrayStatus) {
    checkState("anno_field_value".equals(xmlStreamReader.getLocalName()), "not an " +
        "anno_field_value node %s", currentLocation);
    checkState(null != currentAnnotationValue, "Not building annotation value! %s",
        currentLocation);
    if (currentAnnotationValue.getIsArray()) {
      checkState(null != currentArrayStatus, "processing an array value without array info."
          + " %s", currentLocation);
      checkState(null != currentArrayStatus.getCurrentIndex(), "processing array value without "
          + " index info. %s", currentLocation);
    } else {
      checkState(null == currentArrayStatus, "processing a non array value with array status %s",
        currentLocation);
    }


    String fieldType = getAttributeValue("type");
    String fieldValue = getAttributeValue("value");

    checkState(null != fieldType, "No type attribute present! %s", currentLocation);
    checkState(null != fieldValue, "No value attribute present! %s", currentLocation);
    TestInfo.Type pbType = XML_TO_PB_TYPE.get(fieldType);
    checkState(null != pbType, "cannot map xml to pb type. %s %s", fieldType,
        currentLocation);

    if (currentAnnotationValue.getIsArray()) {
      if (currentAnnotationValue.hasFieldType()) {
        checkState(pbType.equals(currentAnnotationValue.getFieldType()), "array with elems of ",
            "different types! %s", currentLocation);
      }
      currentAnnotationValue.setFieldValue(currentArrayStatus.getCurrentIndex(), fieldValue);
    } else {
      currentAnnotationValue.addFieldValue(fieldValue);
    }

    return currentAnnotationValue.setFieldType(pbType);
  }

  private AnnotationValuePb.Builder handleNewAnnotationFieldNode(
      AnnotationValuePb.Builder currentAnnotationValue) {
    checkState(null == currentAnnotationValue, "Already building annotation value. cannot nest!"
        + " %s", currentLocation);
    checkState("anno_field".equals(xmlStreamReader.getLocalName()), "not an anno_field node"
        + " %s", currentLocation);
    String fieldName = getAttributeValue("name");
    checkState(fieldName != null, "no field name in anno_field node %s",
        currentLocation);

    return AnnotationValuePb.newBuilder()
        .setFieldName(fieldName)
        .setIsArray(false); // might change later if we encounter anno_field_array node.
  }

  private AnnotationPb.Builder handleNewAnnotationNode() {
    checkState("annotation".equals(xmlStreamReader.getLocalName()), "not an annotation node, %s",
        currentLocation);
    String className = getAttributeValue("type");
    checkState(className != null, "no type attribute in annotation node. %s",
        currentLocation);

    return AnnotationPb.newBuilder()
        .setClassName(className);
  }

  private MethodData.MethodBuilder handleParameterNode(MethodData.MethodBuilder currentMethod,
      boolean inConstructor) {
    checkState("parameter".equals(xmlStreamReader.getLocalName()), "Not a param node? %s",
        currentLocation);
    checkState(inConstructor || currentMethod != null, "Not in a method node? %s",
        currentLocation);
    if (!inConstructor) {
      currentMethod.setHasArguments(true);
    }
    return currentMethod;
  }

  private MethodData.MethodBuilder handleEndMethodNode(DexClassData.Builder currentClass,
      MethodData.MethodBuilder currentMethod) {
    checkState("method".equals(xmlStreamReader.getLocalName()), "Not a method node? %s",
        currentLocation);
    checkState(null != currentMethod, "Not building a method node currently! %s",
        currentLocation);
    checkState(null != currentClass, "Not building a class currently! %s",
        currentLocation);
    currentClass.addMethod(currentMethod.build());
    return null;
  }

  private MethodData.MethodBuilder handleNewMethodNode(MethodData.MethodBuilder currentMethod) {
    checkState("method".equals(xmlStreamReader.getLocalName()), "Not a method node? %s",
        currentLocation);
    checkState(null == currentMethod, "Already building a method - method nodes do not nest. %s",
        currentLocation);
    String methodName = getAttributeValue("name");
    String isAbstract = getAttributeValue("abstract");
    String returnType = getAttributeValue("return");
    String visibility = getAttributeValue("visibility");
    checkState(methodName != null, "No method name found in stream! %s",
        currentLocation);
    checkState(visibility != null, "No visibility found in stream! %s",
        currentLocation);
    checkState(isAbstract != null, "No abstract attribute found in stream %s",
        currentLocation);
    checkState(returnType != null, "No returns attribute found in stream %s",
        currentLocation);

    return MethodData.builder()
      .setMethodName(methodName)
      .setVisibility(visibility)
      .setIsAbstract(Boolean.valueOf(isAbstract))
      .setHasArguments(false)  // we might update this later in the xml stream.
      .setHasReturnType("void".equals(returnType) ? Boolean.FALSE : Boolean.TRUE);

  }

  private DexClassData handleEndClassNode(DexClassData.Builder currentClass,
      MethodData.MethodBuilder currentMethod, Deque<AnnotationContext> annoStack) {
    checkState("class".equals(xmlStreamReader.getLocalName()), "Not a class node? %s",
        currentLocation);
    checkState(currentClass != null, "Closing a class node without a class? %s",
        currentLocation);
    checkState(currentMethod == null, "Closing a class node with a method being built? %s",
        currentLocation);
    checkState(annoStack.isEmpty(), "Closing a class with annotations on the stack? %s",
        currentLocation);
    return currentClass.build();
  }

  private DexClassData.Builder handleNewClassNode(DexClassData.Builder currentClass) {
    checkState("class".equals(xmlStreamReader.getLocalName()), "Not a class node? %s",
        currentLocation);
    checkState(null == currentClass, "Already building a class - class nodes do not nest! %s",
        currentLocation);
    checkState(null != currentPackage, "class node outside of a package node? %s",
        currentLocation);
    String className = getAttributeValue("name");
    String visibility = getAttributeValue("visibility");
    String isAbstract = getAttributeValue("abstract");
    String extendsClass = getAttributeValue("extends");

    checkState(className != null, "No class name found in stream!  %s",
        currentLocation);
    checkState(visibility != null, "No visibility found in stream! %s",
        currentLocation);
    checkState(isAbstract != null, "No abstract attribute found in stream %s",
        currentLocation);
    checkState(extendsClass != null, "No extends attribute found in stream %s",
        currentLocation);

    return DexClassData.builder()
        .setPackageName(currentPackage)
        .setClassName(className.replace(".", "$"))
        .setIsAbstract(Boolean.valueOf(isAbstract))
        .setVisibility(visibility)
        .setExtendsClass(extendsClass);
  }

  private String handleEndPackageNode() {
    checkState("package".equals(xmlStreamReader.getLocalName()), "Not a package node? %s",
        currentLocation);
    checkState(null != currentPackage, "a package node end without a start? %s",
        currentLocation);
    return null;
  }

  private String handleNewPackageNode() {
    checkState("package".equals(xmlStreamReader.getLocalName()), "Not a package node? %s",
        currentLocation);
    checkState(null == currentPackage, "nested package nodes? this seems wrong! %s",
        currentLocation);
    String name = getAttributeValue("name");
    checkState(name != null, "No name attribute in package node: %s", currentLocation);
    return name;
  }

  /**
   * Kotlin Compiler emits a KotlinMetadata annotation that has binary data in a string field.
   *
   * <p>Since its mistyped the dexdump stream is not valid utf8 (if it was in a bytes[] field, it
   * would have been properly escaped). This disgusting piece of work drops invalid bytes that
   * aren't UTF-8 at all. Some of the binary data is valid UTF-8 but with control chars - replace
   * that with !'s.
   *
   * <p>In the long term Nitrogen will replace all dex parsing so this code should be deletable in a
   * short period of time.
   */
  private static Reader liberalDataReader(InputStream xmlIn) {
    checkNotNull(xmlIn);
    return new FilterReader(
        new InputStreamReader(
            xmlIn, UTF_8.newDecoder().onMalformedInput(CodingErrorAction.IGNORE))) {
      @Override
      public int read() throws IOException {
        // not actually used...
        int result = super.read();
        if (replaceIt((char) result)) {
          return 33; // !
        }
        return result;
      }

      @Override
      public int read(char[] buffer, int offset, int count) throws IOException {
        int read = super.read(buffer, offset, count);
        if (read > 0) {
          int dataUntil = read + offset;
          for (int i = offset; i < dataUntil; i++) {
            if (replaceIt(buffer[i])) {
              buffer[i] = 33; // !
            }
          }
        }
        return read;
      }
    };
  }

  private static final char[] LEGAL_CONTROL_CHARS = new char[] {'\n', '\t', '\r'};

  // Is a char an unprintable control char?
  private static boolean replaceIt(char c) {
    if (Character.isISOControl(c)) {
      for (char allowed : LEGAL_CONTROL_CHARS) {
        if (c == allowed) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
