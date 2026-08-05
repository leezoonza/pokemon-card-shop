package com.zoonza.pokemoncardshop.auth

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = [
        "common",
        "member :: api",
    ],
)
class ModuleMetadata
