create table nsq_transaction_message(
  `id` bigint primary key auto_increment,
  `created_at` datetime not null default CURRENT_TIMESTAMP comment '创建时间',
  `updated_at` datetime not null default CURRENT_TIMESTAMP comment '修改时间',
  `sharding_id` int(11) unsigned not null default 0 comment '分片id',
  `business_key` varchar(64) not null default '' comment '业务key，比如tradeNo',
  `event_type` varchar(32) not null default '' comment '事件类型',
  `state` smallint(5) unsigned not null default 0 comment '消息投递状态, 0-CREATED, 1-PUBLISHED',
  `env` varchar(16) not null default '' comment '当前环境',
  `topic` varchar(256) not null default '' comment '消息topic',
  `payload` varchar(2048) not null default '' comment '消息体'
);

create unique index uniq_tb on nsq_transaction_message (event_type, business_key);
create index idx_sc on nsq_transaction_message (state, created_at);
