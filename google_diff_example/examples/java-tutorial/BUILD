
package(default_visibility = [
    "//visibility:public",
])

java_binary(
    name = "ProjectRunner",
    srcs = glob(["src/main/java/com/example/ProjectRunner.java"]),
    deps = [
        "@com_google_java_differential_privacy//main/com/google/privacy/differentialprivacy",
        "@maven//:mysql_mysql_connector_java",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "test",
    srcs = glob(["src/main/java/com/example/test.java"]),
    deps = [
        "@com_google_java_differential_privacy//main/com/google/privacy/differentialprivacy",
        "@maven//:mysql_mysql_connector_java",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "Nonprivate",
    srcs = glob(["src/main/java/com/example/Nonprivate.java"]),
    deps = [
        "@maven//:mysql_mysql_connector_java",
    ],
)

java_binary(
    name = "eval",
    srcs = glob(["src/main/java/com/example/eval.java"]),
)


