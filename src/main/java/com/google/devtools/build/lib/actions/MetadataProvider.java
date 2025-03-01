// Copyright 2017 The Bazel Authors. All rights reserved.
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
package com.google.devtools.build.lib.actions;

import com.google.devtools.build.lib.actions.Artifact.DerivedArtifact;
import com.google.devtools.build.lib.concurrent.ThreadSafety.ThreadSafe;
import com.google.devtools.build.lib.vfs.FileSystem;
import java.io.IOException;
import javax.annotation.Nullable;

/** Provides {@link ActionInput} metadata. */
@ThreadSafe
public interface MetadataProvider {
  /**
   * Returns a {@link FileArtifactValue} for the given {@link ActionInput}.
   *
   * <p>The returned {@link FileArtifactValue} instance corresponds to the final target of a symlink
   * and therefore must not have a type of {@link FileStateType#SYMLINK}.
   *
   * <p>If {@link #mayGetGeneratingActionsFromSkyframe} is {@code true} and the {@linkplain
   * DerivedArtifact#getGeneratingActionKey generating action} is not immediately available, this
   * method returns {@code null} to signify that a skyframe restart is necessary to obtain the
   * metadata for the requested {@link Artifact.DerivedArtifact}.
   *
   * @param input the input to retrieve the digest for
   * @return the artifact's digest or null if digest cannot be obtained (due to artifact
   *     non-existence, lookup errors, or any other reason)
   * @throws IOException if the action input cannot be digested
   */
  @Nullable
  FileArtifactValue getInputMetadata(ActionInput input) throws IOException;

  /** Looks up an input from its exec path. */
  @Nullable
  ActionInput getInput(String execPath);

  /**
   * Returns a {@link FileSystem} which, if not-null, should be used instead of the one associated
   * with {@linkplain Artifact#getPath() the path provided for input artifacts}.
   *
   * <p>For {@linkplain ActionInput ActionInputs} which are {@linkplain Artifact Artifacts}, we can
   * perform direct operations on the {@linkplain Artifact#getPath path}. Doing so, may require
   * {@link FileSystem} redirection. This method defines whether that is the case and which {@link
   * FileSystem} to use for that.
   */
  @Nullable
  default FileSystem getFileSystemForInputResolution() {
    return null;
  }

  /**
   * Indicates whether calls to {@link #getInputMetadata} with a {@link Artifact.DerivedArtifact}
   * may require a skyframe lookup.
   */
  default boolean mayGetGeneratingActionsFromSkyframe() {
    return false;
  }
}
