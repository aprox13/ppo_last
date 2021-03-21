package ru.ifkbhit.ppo

import ru.ifkbhit.ppo.backend.{DefaultDatabaseComponents, DefaultUserActionsComponents, DefaultUserManagerComponents, DefaultUserMarketActionsComponents}
import ru.ifkbhit.ppo.database.provider.DbConfig

class UserTestBackend(val dbConfig: DbConfig)
  extends TestExecutionContextComponents
    with DefaultDatabaseComponents
    with DefaultUserActionsComponents
    with DefaultUserMarketActionsComponents
    with DefaultUserManagerComponents
