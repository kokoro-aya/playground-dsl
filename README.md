# playground-dsl (WIP)

This is the old repo of Amatsukaze project.

I have applied the Kotlin DSL, together with codegen and eval scripting, to allow players to code in Kotlin code directly in the playground. Therefore no more need for usage of ANTLR and overhead of defining my own language.

Updated with thread-safe implementation of payload.

~~Deploy this program with [my old playground front-end](https://github.com/kokoro-aya/playground-front) or supply your own front-end.~~

This repository is currently in work for some adaptations to work with [a front-end written in React and Ant Design](https://github.com/kokoro-aya/shizuku-front-end), the work is expected to be completed before 04/26.

Libraries used:

- ktor-server
- ktor-serialization
- ki-shell
- kotlinpoet

Special thanks to my friends who has suggested me to dive into this way, who has adviced me with some ideas and who has helped me to debug for the whole night on scripting and interface constructors.
