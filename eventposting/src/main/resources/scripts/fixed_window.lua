-- KEYS[1]: Current window key (e.g., "USER_123:FIXED:60000:28000000")
-- ARGV[1]: Limit allowed
-- ARGV[2]: Window size in milliseconds (TTL margin)
-- ARGV[3]: Cost of current request

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local windowMs = tonumber(ARGV[2])
local cost = tonumber(ARGV[3])

-- Get current counter
local current = tonumber(redis.call('GET', key) or "0")

if current + cost > limit then
    return 0 -- Rate limit exceeded
end

-- Atomically increment counter
local updated = redis.call('INCRBY', key, cost)

-- Set expiration when key is first created
if updated == cost then
    -- TTL set slightly longer than window duration to prevent premature eviction
    redis.call('PEXPIRE', key, windowMs + 1000)
end

return 1 -- Allowed