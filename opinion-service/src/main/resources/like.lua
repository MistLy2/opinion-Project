local userId = KEYS[1]
local opinionId = KEYS[2]
local flag = tonumber(ARGV[1]) -- 1：点赞 0：取消点赞
-- lua脚本保证并发情况的数据一致问题，实现点赞和取消点赞功能

-- 使用zset进行排序

if flag == 1 then
  -- 用户set添加商品并商品点赞数加1
  if tonumber(redis.call('hget', userId, opinionId)) ~= 1 then
    redis.call('hset', userId, opinionId,1)
    redis.call('ZINCRBY',"sortSet",1,opinionId)
    return true
  end
else
  -- 用户set删除商品并商品点赞数减1
  redis.call('hset', userId, opinionId, 0)
  local oldValue = tonumber(redis.call('zscore', "sortSet", opinionId))
  if oldValue and oldValue > 0 then
    redis.call('ZINCRBY', "sortSet", -1, opinionId)
    return true
  end

end
return false

