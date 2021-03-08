create type event_t as enum (
    'CreateUser',
    'CreatePass',
    'RenewPass',
    'UserEntered',
    'UserExit'
    );


create table if not exists events
(
    event_id     serial primary key not null,
    aggregate_id bigint             not null,
    data         json               not null,
    event_type   event_t            not null,
    event_time   timestamp          not null default now()
);