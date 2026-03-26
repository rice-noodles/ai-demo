# AI Demo

## 目标

- 可视化应用健康状况（可用性、错误率、延迟）
- 可视化 AI 成本（token 消耗、实时费用、24h 累计费用）

## 前置条件

1. 启动应用（默认 `8080`）并确保 `/actuator/prometheus` 可访问。
2. 配置必要环境变量：

```powershell
$env:OPENAI_API_KEY="your_api_key"
$env:AI_PRICE_INPUT_PER_1K="0.00055"
$env:AI_PRICE_OUTPUT_PER_1K="0.00219"
```

## 启动监控组件

```powershell
docker compose -f docker-compose.monitoring.yml up -d
```

## 访问地址

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- 默认账号: `admin / admin`（可通过 `GRAFANA_ADMIN_USER`、`GRAFANA_ADMIN_PASSWORD` 覆盖）

## 关键指标说明

- `ai_chat_requests_total`: AI 请求总数（标签：`status`、`model`）
- `ai_chat_request_duration_seconds_*`: AI 请求时延
- `ai_chat_tokens_total`: token 消耗（标签：`type=input|output|total`）
- `ai_chat_cost_usd_total`: AI 累计费用（美元）

