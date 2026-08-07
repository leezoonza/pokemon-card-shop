package com.zoonza.pokemoncardshop.catalog

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = [
        "common"
    ],
)
class ModuleMetadata
