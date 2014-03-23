# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table option (
  id                        bigint not null,
  text                      varchar(255),
  blocked                   boolean,
  parent_id                 bigint,
  constraint pk_option primary key (id))
;

create table raw_utterance (
  id                        bigint not null,
  text                      TEXT,
  confidence                double,
  constraint pk_raw_utterance primary key (id))
;

create table shared_transcript (
  id                        bigint not null,
  constraint pk_shared_transcript primary key (id))
;

create table utterance (
  id                        bigint not null,
  utterance_id              bigint,
  constraint pk_utterance primary key (id))
;


create table shared_transcript_utterance (
  shared_transcript_id           bigint not null,
  utterance_id                   bigint not null,
  constraint pk_shared_transcript_utterance primary key (shared_transcript_id, utterance_id))
;

create table utterance_option (
  utterance_id                   bigint not null,
  option_id                      bigint not null,
  constraint pk_utterance_option primary key (utterance_id, option_id))
;
create sequence option_seq;

create sequence raw_utterance_seq;

create sequence shared_transcript_seq;

create sequence utterance_seq;

alter table option add constraint fk_option_parent_1 foreign key (parent_id) references utterance (id) on delete restrict on update restrict;
create index ix_option_parent_1 on option (parent_id);



alter table shared_transcript_utterance add constraint fk_shared_transcript_utteranc_01 foreign key (shared_transcript_id) references shared_transcript (id) on delete restrict on update restrict;

alter table shared_transcript_utterance add constraint fk_shared_transcript_utteranc_02 foreign key (utterance_id) references utterance (id) on delete restrict on update restrict;

alter table utterance_option add constraint fk_utterance_option_utterance_01 foreign key (utterance_id) references utterance (id) on delete restrict on update restrict;

alter table utterance_option add constraint fk_utterance_option_option_02 foreign key (option_id) references option (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists option;

drop table if exists raw_utterance;

drop table if exists shared_transcript;

drop table if exists shared_transcript_utterance;

drop table if exists utterance;

drop table if exists utterance_option;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists option_seq;

drop sequence if exists raw_utterance_seq;

drop sequence if exists shared_transcript_seq;

drop sequence if exists utterance_seq;

