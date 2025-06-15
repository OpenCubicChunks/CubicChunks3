# Cubic Chunks 3

## Not yet usable or functional, don't try.

Rewrite of the previous rewrite, targeting NeoForge/MC 1.21.5. 

This Minecraft mod extends Minecraft height and depth to be nearly infinite (at least a million blocks).

For the most up-to-date information about this mod and its related mods, as well as the newest downloads, please join us on the [**Cubic Chunks Discord**](https://discord.gg/kMfWg9m).

### Cubic Chunks (CC) - Links:

Github - [Cubic Chunks - 1.21.5 and above](https://github.com/OpenCubicChunks/CubicChunks3)  
Github - [Cubic Chunks - 1.12.2 and lower](https://github.com/OpenCubicChunks/CubicChunks)

### Cloning the repository

Note: you need git installed to do the following:
```
git clone --recursive
```
You need a git submodule for the project to compile.
If you don't yet have the submodule but already cloned the repository:
```
git submodule update --init --recursive
```

To get latest version of the submodule:
```
git submodule update --recursive --remote
```

### .git-blame-ignore-revs
Configure commits to be ignored for git blame:

```shell
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

### Contributing

#### PR Guidelines
- All mixin methods and fields _**must**_ have a `cc_` prefix, except for those in test mixins. // todo automate this check in gh workflows.
- If a class is significantly modified with mixins, it _**must**_ have documentation explaining:
	- The original vanilla behaviour, can mention important fields/methods.
		- If the vanilla class has sufficient javadoc (through parchment), this can be skipped. Our javadoc should link to the parchment javadoc.
	- The goal of all mixins targeting the class.
- Any merged mixins _**must**_ have tests associated with them. 
	- If a mixin is "untestable" its test class should have a comment explaining *why* it's untestable. 
		- Optionally a to-do (project task? issue?) suggesting integration tests when possible.
	- All non-trivial mixins _**must**_ have a comment explaining their purpose.
	-  //todo Investigate code coverage for mixin tests ([jacoco?](https://docs.gradle.org/current/userguide/jacoco_plugin.html) [other link maybe it's bad](https://igorski.co/generating-junit-test-coverage-using-gradle-and-jacoco/)) .
- Any non-mixin class _**must**_ have tests associated with it.
	- The tests should reasonably cover all expected usage of the class (its external api).
	- Any method(s) that can be reasonably unit tested _**must**_ be.
- Anything annotated with `@SuppressWarnings` _**must**_ have either a TODO comment, or a comment explaining why the warning is suppressed.
- _**Must**_ pass spotless check and checkstyle. Spotless formats automatically if you run `./gradlew spotlessApply`; checkstyle violations must be fixed manually.
- _**Must**_ build.
- All tests _**must**_ pass (no regressions).
(TODO more contributing docs)
