local userId = KEYS[1]
local opinionId = KEYS[2]
local flag = tonumber(ARGV[1]) -- 1：点赞 0：取消点赞
-- lua脚本保证并发情况的数据一致问题，实现点赞和取消点赞功能



if flag == 1 then
  -- 用户set添加商品并商品点赞数加1
  if redis.call('SISMEMBER', userId, opinionId) == 0 then
    redis.call('sadd', userId, opinionId)
    redis.call('incr',opinionId)
    return true
  end
else
  -- 用户set删除商品并商品点赞数减1
  redis.call('SREM', userId, opinionId)
  local oldValue = tonumber(redis.call('GET', opinionId))
  if oldValue and oldValue > 0 then
    redis.call('DECR', opinionId)
    return true
  end

end
return false

