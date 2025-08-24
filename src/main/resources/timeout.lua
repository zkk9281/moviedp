local voucherId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 库存
redis.call('incrby', stockKey, 1)
-- 存用户
redis.call('SREM', orderKey, userId)
return 0