# playground-dsl

This is the old repo of Amatsukaze project.

I have applied the Kotlin DSL, together with codegen and eval scripting, to allow players to code in Kotlin code directly in the playground. Therefore no more need for usage of ANTLR and overhead of defining my own language.

Deploy this program with [my old playground front-end](https://github.com/kokoro-aya/playground-front) or supply your own front-end.

Libraries used:

- ktor-server
- ktor-serialization
- kotlin-scripting-jvm-host
- kotlinpoet

Special thanks to my friends who has suggested me to dive into this way, who has adviced me with some ideas and who has helped me to debug for the whole night on scripting and interface constructors.
