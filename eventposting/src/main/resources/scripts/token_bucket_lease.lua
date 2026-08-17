-- KEYS[1]: Rate limit key (e.g., "USER_ID:12345:PT1M")
-- ARGV[1]: Capacity (effective capacity / max burst)
-- ARGV[2]: Refill rate per millisecond (double)
-- ARGV[3]: Requested lease batch size (integer)
-- ARGV[4]: Current timestamp in milliseconds

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRatePerMs = tonumber(ARGV[2])
local requestedBatch = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

-- Read bucket state from Redis Hash
local data = redis.call('HMGET', key, 'tokens', 'lastRefill')
local tokens = tonumber(data[1])
local lastRefill = tonumber(data[2])

if not tokens then
    -- Initial state: Start full
    tokens = capacity
    lastRefill = now
else
    -- Calculate continuous refill based on elapsed time
    local elapsed = math.max(0, now - lastRefill)
    local refilled = elapsed * refillRatePerMs
    tokens = math.min(capacity, tokens + refilled)
    lastRefill = now
end

-- Determine how many tokens can be granted
local granted = 0
if tokens >= requestedBatch then
    granted = requestedBatch
    tokens = tokens - requestedBatch
elseif tokens > 0 then
    -- Grant remaining partial tokens
    granted = math.floor(tokens)
    tokens = tokens - granted
end

-- Save updated state back to Redis
redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)

-- Set TTL safety safety margin: 1 hour (or adjusted window) so abandoned keys auto-expire
redis.call('PEXPIRE', key, 3600000)

return granted