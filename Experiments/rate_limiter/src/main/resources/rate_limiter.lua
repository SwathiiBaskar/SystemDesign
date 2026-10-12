local tokens = tonumber(redis.call('HGET', KEYS[1], 'tokens'))
local lastRefill = tonumber(redis.call('HGET', KEYS[1], 'lastRefill'))

local now = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local refillRate = tonumber(ARGV[3])

if tokens == nil then
    tokens = capacity
end

if lastRefill == nil then
    lastRefill = now
end

local elapsed = (now - lastRefill) / 1000
local newTokens = elapsed * refillRate

tokens = math.min(capacity, tokens + newTokens)

local allowed = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call('HSET', KEYS[1], 'tokens', tokens)
redis.call('HSET', KEYS[1], 'lastRefill', now)

redis.call('EXPIRE', KEYS[1], 3600)

return {allowed, tokens}