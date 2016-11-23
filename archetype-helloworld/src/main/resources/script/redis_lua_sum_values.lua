local sum = 0
local matches = redis.call('KEYS', '192*')

for _,key in ipairs(matches) do
    local val = redis.call('GET', key)
    sum = sum + tonumber(val)
end

return sum