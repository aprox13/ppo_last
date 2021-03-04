## RX

Http сервер стартует на порту, указанном в `application.conf` как ``http.port`` с `scheduler.pools` потоками

Монго подключается к базе `mongo.db-name` на порту `mongo.port` хоста `mongo.host`

Для добавление тестовых данных можно запустить сначала Main, а затем `testdata.FillDb` (этот скрипт удалит все данные из базы)
