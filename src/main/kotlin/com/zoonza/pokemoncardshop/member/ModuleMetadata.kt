package com.zoonza.pokemoncardshop.member

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = [
        "common"
    ],
)
class ModuleMetadata
