// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.docgen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.devtools.build.docgen.annot.DocCategory;
import com.google.devtools.build.docgen.annot.GlobalMethods;
import com.google.devtools.build.docgen.annot.StarlarkConstructor;
import com.google.devtools.build.docgen.starlark.StarlarkBuiltinDoc;
import com.google.devtools.build.docgen.starlark.StarlarkConstructorMethodDoc;
import com.google.devtools.build.docgen.starlark.StarlarkDocExpander;
import com.google.devtools.build.docgen.starlark.StarlarkJavaMethodDoc;
import com.google.devtools.build.lib.util.Classpath;
import com.google.devtools.build.lib.util.Classpath.ClassPathException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import net.starlark.java.annot.StarlarkAnnotations;
import net.starlark.java.annot.StarlarkBuiltin;
import net.starlark.java.annot.StarlarkMethod;
import net.starlark.java.eval.Starlark;
import net.starlark.java.eval.StarlarkSemantics;
import net.starlark.java.eval.StarlarkValue;

/** A helper class that collects Starlark module documentation. */
final class StarlarkDocumentationCollector {
  @StarlarkBuiltin(
      name = "globals",
      category = DocCategory.TOP_LEVEL_MODULE,
      doc = "Objects, functions and modules registered in the global environment.")
  private static final class TopLevelModule implements StarlarkValue {}

  private StarlarkDocumentationCollector() {}

  /** Returns the StarlarkBuiltin annotation for the top-level Starlark module. */
  public static StarlarkBuiltin getTopLevelModule() {
    return TopLevelModule.class.getAnnotation(StarlarkBuiltin.class);
  }

  private static ImmutableMap<String, StarlarkBuiltinDoc> all;

  /** Applies {@link #collectModules} to all Bazel and Starlark classes. */
  static synchronized ImmutableMap<String, StarlarkBuiltinDoc> getAllModules(
      StarlarkDocExpander expander) throws ClassPathException {
    if (all == null) {
      all =
          collectModules(
              Iterables.concat(
                  /*Bazel*/ Classpath.findClasses("com/google/devtools/build"),
                  /*Starlark*/ Classpath.findClasses("net/starlark/java")),
              expander);
    }
    return all;
  }

  /**
   * Collects the documentation for all Starlark modules comprised of the given classes and returns
   * a map from the name of each Starlark module to its documentation.
   */
  static ImmutableMap<String, StarlarkBuiltinDoc> collectModules(
      Iterable<Class<?>> classes, StarlarkDocExpander expander) {
    Map<String, StarlarkBuiltinDoc> modules = new TreeMap<>();
    // The top level module first.
    // (This is a special case of {@link StarlarkBuiltinDoc} as it has no object name).
    StarlarkBuiltin topLevelModule = getTopLevelModule();
    modules.put(
        topLevelModule.name(),
        new StarlarkBuiltinDoc(
            topLevelModule,
            /*title=*/ "Globals",
            TopLevelModule.class,
            expander,
            /*isTopLevel=*/ true));

    // Creating module documentation is done in three passes.
    // 1. Add all classes/interfaces annotated with @StarlarkBuiltin with documented = true.
    for (Class<?> candidateClass : classes) {
      if (candidateClass.isAnnotationPresent(StarlarkBuiltin.class)) {
        collectStarlarkModule(candidateClass, modules, expander);
      }
    }

    // 2. Add all object methods and global functions.
    //
    //    Also, explicitly process the Starlark interpreter's MethodLibrary
    //    class, which defines None, len, range, etc.
    //    TODO(adonovan): do this without peeking into the implementation,
    //    e.g. by looking at Starlark.UNIVERSE, something like this:
    //
    //    for (Map<String, Object> e : Starlark.UNIVERSE.entrySet()) {
    //      if (e.getValue() instanceof BuiltinFunction) {
    //        BuiltinFunction fn = (BuiltinFunction) e.getValue();
    //        topLevelModuleDoc.addMethod(
    //          new StarlarkJavaMethodDoc("", fn.getJavaMethod(), fn.getAnnotation(), expander));
    //      }
    //    }
    //
    //    Note that BuiltinFunction doesn't actually have getJavaMethod.
    //
    for (Class<?> candidateClass : classes) {
      if (candidateClass.isAnnotationPresent(StarlarkBuiltin.class)) {
        collectModuleMethods(candidateClass, modules, expander);
      }
      if (candidateClass.isAnnotationPresent(GlobalMethods.class)
          || candidateClass.getName().equals("net.starlark.java.eval.MethodLibrary")) {
        collectDocumentedMethods(candidateClass, modules, expander);
      }
    }

    // 3. Add all constructors.
    for (Class<?> candidateClass : classes) {
      if (candidateClass.isAnnotationPresent(StarlarkBuiltin.class)
          || candidateClass.isAnnotationPresent(GlobalMethods.class)) {
        collectConstructorMethods(candidateClass, modules, expander);
      }
    }

    return ImmutableMap.copyOf(modules);
  }

  /**
   * Returns the {@link StarlarkBuiltinDoc} entry representing the collection of top level
   * functions. (This is a special case of {@link StarlarkBuiltinDoc} as it has no object name).
   */
  private static StarlarkBuiltinDoc getTopLevelModuleDoc(Map<String, StarlarkBuiltinDoc> modules) {
    return modules.get(getTopLevelModule().name());
  }

  /**
   * Adds a single {@link StarlarkBuiltinDoc} entry to {@code modules} representing the given {@code
   * moduleClass}, if it is a documented module.
   */
  private static void collectStarlarkModule(
      Class<?> moduleClass, Map<String, StarlarkBuiltinDoc> modules, StarlarkDocExpander expander) {
    if (moduleClass.equals(TopLevelModule.class)) {
      // The top level module doc is a special case and is handled separately.
      return;
    }

    StarlarkBuiltin moduleAnnotation =
        Preconditions.checkNotNull(moduleClass.getAnnotation(StarlarkBuiltin.class));

    if (moduleAnnotation.documented()) {
      StarlarkBuiltinDoc previousModuleDoc = modules.get(moduleAnnotation.name());
      if (previousModuleDoc == null) {
        modules.put(
            moduleAnnotation.name(),
            new StarlarkBuiltinDoc(
                moduleAnnotation, moduleAnnotation.name(), moduleClass, expander));
      } else {
        // Handle a strange corner-case: If moduleClass has a subclass which is also
        // annotated with {@link StarlarkBuiltin} with the same name, and also has the same
        // module-level docstring, then the subclass takes precedence.
        // (This is useful if one module is a "common" stable module, and its subclass is
        // an experimental module that also supports all stable methods.)
        validateCompatibleModules(previousModuleDoc.getClassObject(), moduleClass);

        if (previousModuleDoc.getClassObject().isAssignableFrom(moduleClass)) {
          // The new module is a subclass of the old module, so use the subclass.
          modules.put(
              moduleAnnotation.name(),
              new StarlarkBuiltinDoc(
                  moduleAnnotation, /*title=*/ moduleAnnotation.name(), moduleClass, expander));
        }
      }
    }
  }

  /**
   * Validate that it is acceptable that the given module classes with the same module name
   * co-exist.
   */
  private static void validateCompatibleModules(Class<?> one, Class<?> two) {
    StarlarkBuiltin moduleOne = one.getAnnotation(StarlarkBuiltin.class);
    StarlarkBuiltin moduleTwo = two.getAnnotation(StarlarkBuiltin.class);
    if (one.isAssignableFrom(two) || two.isAssignableFrom(one)) {
      if (!moduleOne.doc().equals(moduleTwo.doc())) {
        throw new IllegalStateException(
            String.format(
                "%s and %s are related modules but have mismatching documentation for '%s'",
                one, two, moduleOne.name()));
      }
    } else {
      throw new IllegalStateException(
          String.format(
              "%s and %s are unrelated modules with documentation for '%s'",
              one, two, moduleOne.name()));
    }
  }

  private static void collectModuleMethods(
      Class<?> moduleClass, Map<String, StarlarkBuiltinDoc> modules, StarlarkDocExpander expander) {
    StarlarkBuiltin moduleAnnotation =
        Preconditions.checkNotNull(moduleClass.getAnnotation(StarlarkBuiltin.class));

    if (moduleAnnotation.documented()) {
      StarlarkBuiltinDoc moduleDoc =
          Preconditions.checkNotNull(modules.get(moduleAnnotation.name()));

      if (moduleClass == moduleDoc.getClassObject()) {
        for (Map.Entry<Method, StarlarkMethod> entry :
            Starlark.getMethodAnnotations(moduleClass).entrySet()) {
          // Collect methods that aren't directly constructors (i.e. have the @StarlarkConstructor
          // annotation).
          // Struct fields that return a type that has @StarlarkConstructor are a bit special:
          // they're visited here because they're seen as an attribute of the module, but act more
          // like a reference to the type they construct
          if (!entry.getKey().isAnnotationPresent(StarlarkConstructor.class)) {
            Method javaMethod = entry.getKey();
            StarlarkMethod starlarkMethod = entry.getValue();
            // Handle struct fields that return a Starlark constructor so that
            // documentation can link to the constructed type.
            if (starlarkMethod.structField()) {
              Method constructor = getSelfCallConstructorMethod(javaMethod.getReturnType());
              if (constructor != null) {
                javaMethod = constructor;
              }
            }
            moduleDoc.addMethod(
                new StarlarkJavaMethodDoc(
                    moduleDoc.getName(), javaMethod, starlarkMethod, expander));
          }
        }
      }
    }
  }

  @Nullable
  private static Method getSelfCallConstructorMethod(Class<?> objectClass) {
    Method selfCallMethod = Starlark.getSelfCallMethod(StarlarkSemantics.DEFAULT, objectClass);
    if (selfCallMethod != null && selfCallMethod.isAnnotationPresent(StarlarkConstructor.class)) {
      return selfCallMethod;
    }
    return null;
  }

  /**
   * Adds {@link StarlarkJavaMethodDoc} entries to the top level module, one for
   * each @StarlarkMethod method defined in the given @GlobalMethods class {@code moduleClass}.
   */
  private static void collectDocumentedMethods(
      Class<?> moduleClass, Map<String, StarlarkBuiltinDoc> modules, StarlarkDocExpander expander) {
    StarlarkBuiltinDoc topLevelModuleDoc = getTopLevelModuleDoc(modules);

    for (Map.Entry<Method, StarlarkMethod> entry :
        Starlark.getMethodAnnotations(moduleClass).entrySet()) {
      // Only add non-constructor global library methods. Constructors are added later.
      if (!entry.getKey().isAnnotationPresent(StarlarkConstructor.class)) {
        topLevelModuleDoc.addMethod(
            new StarlarkJavaMethodDoc("", entry.getKey(), entry.getValue(), expander));
      }
    }
  }

  private static void collectConstructor(
      Map<String, StarlarkBuiltinDoc> modules, Method method, StarlarkDocExpander expander) {
    Preconditions.checkNotNull(method.getAnnotation(StarlarkConstructor.class));

    StarlarkBuiltin builtinType = StarlarkAnnotations.getStarlarkBuiltin(method.getReturnType());
    if (builtinType == null || !builtinType.documented()) {
      // The class of the constructed object type has no documentation, so no place to add
      // constructor information.
      return;
    }
    StarlarkMethod methodAnnot =
        Preconditions.checkNotNull(method.getAnnotation(StarlarkMethod.class));
    StarlarkBuiltinDoc doc = modules.get(builtinType.name());
    doc.setConstructor(
        new StarlarkConstructorMethodDoc(builtinType.name(), method, methodAnnot, expander));
  }

  /**
   * Collect two types of constructor methods:
   *
   * <p>1. The single method with selfCall=true and @StarlarkConstructor (if present)
   *
   * <p>2. Any methods annotated with @StarlarkConstructor
   *
   * <p>Structfield methods that return an object which itself has selfCall=true
   * and @StarlarkConstructor are *not* collected here (collectModuleMethods does that). (For
   * example, supposed Foo has a structfield method named 'Bar', which refers to the Bar type. In
   * Foo's doc, we describe Foo.Bar as an attribute of type Bar and link to the canonical Bar type
   * documentation)
   */
  private static void collectConstructorMethods(
      Class<?> moduleClass, Map<String, StarlarkBuiltinDoc> modules, StarlarkDocExpander expander) {
    Method selfCallConstructor = getSelfCallConstructorMethod(moduleClass);
    if (selfCallConstructor != null) {
      collectConstructor(modules, selfCallConstructor, expander);
    }

    for (Method method : Starlark.getMethodAnnotations(moduleClass).keySet()) {
      if (method.isAnnotationPresent(StarlarkConstructor.class)) {
        collectConstructor(modules, method, expander);
      }
    }
  }
}
