SESSION_ID=$(curl -s -X POST http://localhost:8080/api/checkout/initiate -H "Content-Type: application/json" -H "X-User-Id: 5" -d '{"userId":5, "totalAmount":100}' | jq '.sessionId')
echo "Session: $SESSION_ID"
curl -s -X PUT "http://localhost:8080/api/checkout/$SESSION_ID/address?userId=5" -H "Content-Type: application/json" -H "X-User-Id: 5" -d '{"shippingAddress":"123 Main St", "contactName":"Gotam", "phoneNumber":"1234567890"}' > /dev/null
curl -s -X POST http://localhost:8080/api/payment/process -H "Content-Type: application/json" -H "X-User-Id: 5" -d "{\"checkoutSessionId\":$SESSION_ID, \"paymentMethod\":\"COD\"}" | jq '.'
