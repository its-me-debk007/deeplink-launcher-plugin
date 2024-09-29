package com.github.itsmedebk007.deeplinklauncherplugin.services

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros

@Service(Service.Level.APP)
@State(
    name = "DataPersistentComponent",
    storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)],
)
class DataPersistentComponent : SimplePersistentStateComponent<DataPersistentState>(DataPersistentState())

class DataPersistentState : BaseState() {
    var deeplinks by list<String>()
}