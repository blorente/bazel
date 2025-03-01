// Copyright 2019 The Bazel Authors. All rights reserved.
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

package com.google.devtools.build.lib.rules.cpp;

import com.google.devtools.build.lib.collect.nestedset.Depset;
import com.google.devtools.build.lib.starlarkbuildapi.FileApi;
import com.google.devtools.build.lib.starlarkbuildapi.RunfilesApi;
import com.google.devtools.build.lib.starlarkbuildapi.StarlarkRuleContextApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.CcCompilationContextApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.CcInfoApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.CcToolchainProviderApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.CompilationInfoApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.CppConfigurationApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.FeatureConfigurationApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.PyWrapCcHelperApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.PyWrapCcInfoApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.WrapCcHelperApi;
import com.google.devtools.build.lib.starlarkbuildapi.cpp.WrapCcIncludeProviderApi;
import com.google.devtools.build.lib.starlarkbuildapi.platform.ConstraintValueInfoApi;
import net.starlark.java.eval.EvalException;
import net.starlark.java.eval.Printer;
import net.starlark.java.eval.Sequence;

/**
 * Fake stub implementations for C++-related Starlark API which are unsupported without use of
 * --experimental_google_legacy_api.
 */
public final class GoogleLegacyStubs {

  private GoogleLegacyStubs() {}

  private static class WrapCcHelper
      implements WrapCcHelperApi<
          FeatureConfigurationApi,
          ConstraintValueInfoApi,
          StarlarkRuleContextApi<ConstraintValueInfoApi>,
          CcToolchainProviderApi<
              FeatureConfigurationApi,
              ?,
              ?,
              ConstraintValueInfoApi,
              StarlarkRuleContextApi<ConstraintValueInfoApi>,
              ?,
              ? extends CppConfigurationApi<?>,
              ?>,
          CompilationInfoApi<FileApi>,
          FileApi,
          CcCompilationContextApi<FileApi>,
          WrapCcIncludeProviderApi> {

    @Override
    public FeatureConfigurationApi starlarkGetFeatureConfiguration(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext,
        CcToolchainProviderApi<
                FeatureConfigurationApi,
                ?,
                ?,
                ConstraintValueInfoApi,
                StarlarkRuleContextApi<ConstraintValueInfoApi>,
                ?,
                ? extends CppConfigurationApi<?>,
                ?>
            ccToolchain)
        throws EvalException, InterruptedException {
      return null;
    }

    @Override
    public Depset starlarkCollectTransitiveSwigIncludes(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext) {
      return null;
    }

    @Override
    public CompilationInfoApi<FileApi> starlarkCreateCompileActions(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext,
        FeatureConfigurationApi featureConfiguration,
        CcToolchainProviderApi<
                FeatureConfigurationApi,
                ?,
                ?,
                ConstraintValueInfoApi,
                StarlarkRuleContextApi<ConstraintValueInfoApi>,
                ?,
                ? extends CppConfigurationApi<?>,
                ?>
            ccToolchain,
        FileApi ccFile,
        FileApi headerFile,
        Sequence<?> depCcCompilationContexts, // <CcCompilationContextApi>
        Sequence<?> targetCopts /* <String> */)
        throws EvalException, InterruptedException {
      return null;
    }

    @Override
    public String starlarkGetMangledTargetName(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext)
        throws EvalException, InterruptedException {
      return null;
    }

    @Override
    public WrapCcIncludeProviderApi getWrapCcIncludeProvider(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext, Depset swigIncludes)
        throws EvalException, InterruptedException {
      return null;
    }

    @Override
    public void registerSwigAction(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext,
        CcToolchainProviderApi<
                FeatureConfigurationApi,
                ?,
                ?,
                ConstraintValueInfoApi,
                StarlarkRuleContextApi<ConstraintValueInfoApi>,
                ?,
                ? extends CppConfigurationApi<?>,
                ?>
            ccToolchain,
        FeatureConfigurationApi featureConfiguration,
        CcCompilationContextApi<FileApi> wrapperCcCompilationContext,
        Depset swigIncludes,
        FileApi swigSource,
        Sequence<?> subParameters, // <String>
        FileApi ccFile,
        FileApi headerFile,
        Sequence<?> outputFiles, // <FileApi>
        Object outDir,
        Object javaDir,
        Depset auxiliaryInputs,
        String swigAttributeName,
        Object zipTool)
        throws EvalException, InterruptedException {}
  }

  /**
   * Fake no-op implementation of {@link PyWrapCcHelperApi}. This implementation should be
   * unreachable without (discouraged) use of --experimental_google_legacy_api.
   */
  public static class PyWrapCcHelper extends WrapCcHelper
      implements PyWrapCcHelperApi<
          FileApi,
          ConstraintValueInfoApi,
          StarlarkRuleContextApi<ConstraintValueInfoApi>,
          CcInfoApi<FileApi>,
          FeatureConfigurationApi,
          CcToolchainProviderApi<
              FeatureConfigurationApi,
              ?,
              ?,
              ConstraintValueInfoApi,
              StarlarkRuleContextApi<ConstraintValueInfoApi>,
              ?,
              ? extends CppConfigurationApi<?>,
              ?>,
          CompilationInfoApi<FileApi>,
          CcCompilationContextApi<FileApi>,
          WrapCcIncludeProviderApi> {

    @Override
    public Sequence<String> getPyExtensionLinkopts(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext) {
      return null;
    }

    @Override
    public Depset getTransitivePythonSources(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext, FileApi pyFile) {
      return null;
    }

    @Override
    public RunfilesApi getPythonRunfiles(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext, Depset filesToBuild) {
      return null;
    }

    @Override
    public PyWrapCcInfoApi<FileApi> getPyWrapCcInfo(
        StarlarkRuleContextApi<ConstraintValueInfoApi> starlarkRuleContext,
        CcInfoApi<FileApi> ccInfo) {
      return null;
    }
  }

  /**
   * Fake no-op implementation of {@link PyWrapCcInfoApi.Provider}. This implementation should be
   * unreachable without (discouraged) use of --experimental_google_legacy_api.
   */
  public static class PyWrapCcInfoProvider implements PyWrapCcInfoApi.Provider {

    @Override
    public void repr(Printer printer) {
      printer.append("<unknown object>");
    }
  }
}
