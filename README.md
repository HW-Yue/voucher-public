# 秒杀业务ddd改造与对接订单服务/支付服务

## 已经做的工作有，
### 秒杀模块改造ddd架构，
### 新增优惠卷接口和下单接口已经实现，
由于对接了springSecurity，所以将添加优惠卷这个接口加了角色限制

### 接口测试的接口记得加
Authorization Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NjQ3NDc1NjYsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlcyI6WyIxMTEiLCJjbGFzc18xX3N0dWRlbnQiLCJjbGFzc18xX3RlYWNoZXIiLCJlZGl0b3IiLCLlhoXlrrnnvJYiXSwidXNlcklkIjoxMTd9.RcLRovycgPj28CI565hBTKviNDIAM2pGbzPL62S37ic
这个jwt是管理员了，权限系统的

### 实现了从jwt中提取用户id的功能，服务一人一单
auth领域负责提取用户id， 后期还想加入人群标签，也会在这个领域在实现，

添加秒杀卷的请求样例： http://localhost:8091/voucher/seckill
```json
{
  "shopId": 1,
  "title": "100元代金券",
  "subTitle": "周一至周五均可使用",
  "rules": "全场通用\\n无需预约\\n可无限叠加\\n不兑现、不找零\\n仅限堂食",
  "payValue": 8000,
  "actualValue": 10000,
  "type": 1,
  "stock": 100,
  "status":1,
  "beginTime": "2025-01-26T10:09:17",
  "endTime": "2026-09-23T23:09:04"
}
```
下单的接口样例 ：http://localhost:8080/api/voucher-order/seckill/7

### 当前的业务流程
1.用户下单，lua脚本验证库存与一人一单，然后进行库存扣减，使用redis实现的消息队列把订单异步写入数据库
2.目前写入的时候，订单状态是空白的，只是创建了order_id

### 为什么我单独弄一个鉴权服务？
因为后期要将秒杀服务和拼团服务作为微服务，供其他服务调用，所有服务拥有统一的一个鉴权服务去管理，比较方便。
鉴权服务是个半成品，只是实现了登录注册，还有角色权限管理，生成携带用户消息的jwt的简单ddd结构，后期会完善上传。
鉴权服务下一步要结合微信公众号登录，就是把微信的那个id和服务里面username绑定，方便统一管理用户。做更多拓展的登录方式。

## 后期计划

## 1. 改造业务流程
1. 在redis中用两个变量表示库存，一个是可售库存，一个是真实库存， 订单下单时进行可售库存扣减，支付成功后进行真实库存扣减
2. 用户支付后，同时进行订单异步写入数据库， http调用订单服务，调用支付宝，返回支付二维码
3. 支付成功后，回调秒杀服务，数据库订单状态为已付款，redis真实库存-1
4. 支付失败/超时，可售库存要加回去

## 2. 接入一个RAG项目里面，当作商城模块，进行秒杀token的功能。