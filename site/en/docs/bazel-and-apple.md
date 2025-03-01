Project: /_project.yaml
Book: /_book.yaml

# Apple Apps and Bazel

{% include "_buttons.html" %}

This page contains resources that help you use Bazel to build macOS and iOS
projects. It links to a tutorial, build rules, and other information specific to
using Bazel to build and test for those platforms.

## Working with Bazel {:#working-with-bazel}

The following resources will help you work with Bazel on macOS and iOS projects:

*  [Tutorial: Building an iOS app](/start/ios-app))
*  [Objective-C build rules](/reference/be/objective-c)
*  [General Apple rules](https://github.com/bazelbuild/rules_apple){: .external}
*  [Integration with Xcode](/install/ide)

## Migrating to Bazel {:#migrating-to-bazel}

If you currently build your macOS and iOS projects with Xcode, follow the steps
in the migration guide to start building them with Bazel:

*  [Migrating from Xcode to Bazel](/migrate/xcode)

## Apple apps and new rules {:#apple-apps-new-rules}

**Note**: Creating new rules is for advanced build and test scenarios.
You do not need it when getting started with Bazel.

The following modules, configuration fragments, and providers will help you
[extend Bazel's capabilities](/extending/concepts)
when building your macOS and iOS projects:

*  Modules:

   *  [`apple_bitcode_mode`](/rules/lib/apple_bitcode_mode)
   *  [`apple_common`](/rules/lib/apple_common)
   *  [`apple_platform`](/rules/lib/apple_platform)
   *  [`apple_platform_type`](/rules/lib/apple_platform_type)
   *  [`apple_toolchain`](/rules/lib/apple_toolchain)
   *  [`XcodeVersionConfig`](/rules/lib/XcodeVersionConfig)

*  Configuration fragments:

   *  [`apple`](/rules/lib/apple)

*  Providers:

   *  [`ObjcProvider`](/rules/lib/ObjcProvider)

## Xcode selection {:#xcode-selection}

If your build requires Xcode, Bazel will select an appropriate version based on
the `--xcode_config` and `--xcode_version` flags. The `--xcode_config` consumes
the set of available Xcode versions and sets a default version if
`--xcode_version` is not passed. This default is overridden by the
`--xcode_version` flag, as long as it is set to an Xcode version that is
represented in the `--xcode_config` target.

If you do not pass `--xcode_config`, Bazel will use the autogenerated
[`XcodeVersionConfig`](/rules/lib/XcodeVersionConfig) that represents the
Xcode versions available on your host machine. The default version is
the newest available Xcode version. This is appropriate for local execution.

If you are performing remote builds, you should set `--xcode_config` to an
[`xcode_config`](/reference/be/objective-c#xcode_config)
target whose `versions` attribute is a list of remotely available
[`xcode_version`](/reference/be/objective-c#xcode_version)
targets, and whose `default` attribute is one of these
[`xcode_versions`](/reference/be/objective-c#xcode_version).

If you are using dynamic execution, you should set `--xcode_config` to an
[`xcode_config`](/reference/be/objective-c#xcode_config)
target whose `remote_versions` attribute is an
[`available_xcodes`](/reference/be/workspace#available_xcodes)
target containing the remotely available Xcode versions, and whose
`local_versions` attribute is an
[`available_xcodes`](/reference/be/workspace#available_xcodes)
target containing the locally available Xcode versions. For `local_versions`,
you probably want to use the autogenerated
`@local_config_xcode//:host_available_xcodes`. The default Xcode version is the
newest mutually available version, if there is one, otherwise the default of the
`local_versions` target. If you prefer to use the `local_versions` default
as the default, you can pass `--experimental_prefer_mutual_default=false`.
