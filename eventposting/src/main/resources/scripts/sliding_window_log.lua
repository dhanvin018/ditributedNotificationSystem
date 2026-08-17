-- KEYS[1]: Key (e.g., "USER_123:60s")
-- ARGV[1]: Limit
-- ARGV[2]: Window size in milliseconds
-- ARGV[3]: Current timestamp in milliseconds
-- ARGV[4]: Cost / Request Unique ID (timestamp + UUID)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local member_id = ARGV[4]

local window_start = now - window_ms

-- 1. Remove timestamps older than the sliding window boundary
redis.call('ZREMRANGEBYSCORE', key, 0, window_start)

-- 2. Count elements in current window
local current_requests = redis.call('ZCARD', key)

if current_requests >= limit then
    return 0 -- Denied
end

-- 3. Add current request timestamp
redis.call('ZADD', key, now, member_id)
redis.call('PEXPIRE', key, window_ms)

return 1 -- Allowed