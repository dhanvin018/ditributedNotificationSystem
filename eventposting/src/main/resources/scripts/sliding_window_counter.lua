-- KEYS[1]: Base key for current window (e.g., "rate:USER:123:60:1700000100")
-- KEYS[2]: Base key for previous window
-- ARGV[1]: Limit allowed
-- ARGV[2]: Window size in milliseconds (e.g., 60000)
-- ARGV[3]: Current timestamp in milliseconds
-- ARGV[4]: Cost of current request

local curr_key = KEYS[1]
local prev_key = KEYS[2]

local limit = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local cost = tonumber(ARGV[4])

-- Get current counts from Redis
local curr_count = tonumber(redis.call('GET', curr_key) or "0")
local prev_count = tonumber(redis.call('GET', prev_key) or "0")

-- Calculate how far we are into the current window (0.0 to 1.0)
local time_into_curr_window = now % window_ms
local weight = (window_ms - time_into_curr_window) / window_ms

-- Estimate total requests in the sliding 60-second frame
local estimated_count = math.floor(prev_count * weight) + curr_count

if estimated_count + cost > limit then
    return 0 -- Rate limit exceeded
end

-- Increment current window counter
redis.call('INCRBY', curr_key, cost)

-- Maintain TTL: keep window key alive for 2x window duration so it transitions to prev_key cleanly
redis.call('PEXPIRE', curr_key, window_ms * 2)

return 1 -- Allowed