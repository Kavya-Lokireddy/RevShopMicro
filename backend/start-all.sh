#!/bin/bash
echo "Starting all RevShop services locally..."

BASE_DIR="/Users/gotamsingh/Desktop/Archive_and_Misc/Project_P3_DesktopFolder/RevShop_P3"

# Start Discovery Server first
echo "Starting Discovery Server (Eureka)..."
java -jar "$BASE_DIR/discovery-server/target/discovery-server-1.0.0.jar" > /tmp/discovery.log 2>&1 &
echo "PID: $!"

# Wait for Eureka to be ready
echo "Waiting for Eureka to start..."
until curl -s http://localhost:8761/actuator/health > /dev/null 2>&1; do sleep 2; done
echo "Eureka is UP!"

# Start all other services in parallel
echo "Starting API Gateway..."
java -jar "$BASE_DIR/api-gateway/target/api-gateway-1.0.0.jar" > /tmp/api-gateway.log 2>&1 &
echo "PID: $!"

echo "Starting Auth Service..."
java -jar "$BASE_DIR/auth-service/target/auth-service-1.0.0.jar" > /tmp/auth-service.log 2>&1 &
echo "PID: $!"

echo "Starting Product Service..."
java -jar "$BASE_DIR/product-service/target/product-service-1.0.0.jar" > /tmp/product-service.log 2>&1 &
echo "PID: $!"

echo "Starting Cart Service..."
java -jar "$BASE_DIR/cart-service/target/cart-service-1.0.0.jar" > /tmp/cart-service.log 2>&1 &
echo "PID: $!"

echo "Starting Order Service..."  
java -jar "$BASE_DIR/order-service/target/order-service-1.0.0.jar" > /tmp/order-service.log 2>&1 &
echo "PID: $!"

echo "Starting Checkout Service..."
java -jar "$BASE_DIR/checkout-service/target/checkout-service-1.0.0.jar" > /tmp/checkout-service.log 2>&1 &
echo "PID: $!"

echo ""
echo "All services launched! Waiting for them to be ready..."
sleep 15

echo ""
echo "=== Service Status ==="
for port in 8761 8080 8081 8082 8083 8084 8085; do
  if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
    echo "Port $port: UP"
  else
    echo "Port $port: STARTING..."
  fi
done

echo ""
echo "Done! Services are starting up. Check logs in /tmp/*.log"
