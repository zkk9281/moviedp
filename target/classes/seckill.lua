local voucherId = ARGV[1]
local userId = ARGV[2]
--local orderId = ARGV[3]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

if(tonumber(redis.call('get', stockKey)) <= 0) then
    return 1
end

if(redis.call('sismember', orderKey, userId) == 1) then
    return 2
end

-- 扣库存
redis.call('incrby', stockKey, -1)
-- 下单，保存用户到redis
redis.call('sadd', orderKey, userId)
-- 发送消息到redis队列
--redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0