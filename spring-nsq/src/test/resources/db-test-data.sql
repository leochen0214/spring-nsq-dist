insert into nsq_transaction_message (
  id,
  created_at,
  updated_at,
  sharding_id,
  business_key,
  event_type,
  state,
  env,
  payload,
  topic) values (
    1,
    now(),
    now(),
    0,
    '123',
    'event',
    0,
    'dev',
    'payload',
    'topic1');