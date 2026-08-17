# 1. Save your token
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInRpZXIiOiJGUkVFIiwiaWF0IjoxNzg2NTQ5MDczLCJleHAiOjE3ODY2MzU0NzN9.a4FMhMuSUp6auy_2-LgkrRYFtqlMxTIDLs-BtjZnlo4"

# 2. Fire 15 rapid requests to a protected endpoint
for i in {1..15}; do
  echo -n "Request $i: "
  curl -s -o /dev/null -w "%{http_code}\n" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/events/postEvent
done