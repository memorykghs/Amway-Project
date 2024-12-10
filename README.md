# README
## Calculator 計算機
設計一個計算機（Calculator），可以執行兩個數的加、减、乘、除計算，可以進行undo和redo操作，在實現功能的基礎上進行最佳化設計，備註：undo和redo 就是 復原和取消(上次)復原的操作。

1. 加減乘除的操作採用 Strategy Pattern 設計
   1. 如果需要新增新的計算操作，如 `%`，新增一個類別實作 `IOperation` 介面即可
   2. 同時更新 OperateEnum，不需要修改 `Calculator` 內的程式碼
2. 主要計算功能放在 `Calculator` 中，redo 和 undo 是共用的功能，實作在此類別中
   1. 使用 Stack 的特性（FILO）的特性紀錄上一次的操作
3. reset 可以清空並重新設定計算機

## Spin Lottery 轉盤抽獎
### 流程
![](/img/lottery_process.jpg)

1. 當用戶抽獎時，對 user ID 加鎖控制，避免用戶短時間內不斷抽獎
   1. 當本次抽獎還沒有結束（即 user ID 鎖還沒被釋放），用戶不可再次抽獎
2. 檢查用戶是否已經得過獎
   1. 已中獎，不可再參與抽獎
   2. 未中獎，可繼續抽獎流程
3. 執行抽獎時，透過 Math.random 產生隨機數字，判斷區間是否落在中獎的區間內
   1. 如果未落在中獎區間，為銘謝惠顧
   2. 落在中獎區間，沒有剩餘數量的話也是銘謝惠顧
   3. 落在中獎區間，且還有剩餘數量的話，對各個獎品類別加上鎖，避免多執行緒在更新時造成資料不一致
      1. 再次確認是否還有剩餘數量，如果有則更新抽獎紀錄並減少獎品數量
---
使用 Redisson Lock 的原因：
* 考量到分散式系統，需要分散式的鎖
* 可以鎖的粒度較細，分別對 user ID 和產品類別上鎖：
  * 避免 user 操作太頻繁，控制不能重複得獎
  * 在更新時才對產品類別上鎖，這樣就不需要在整個抽獎流程時就上鎖，增加讀取的效率

### 可優化方向
1. 不同的獎品可以改存在資料庫，就不需要針對不同的抽獎活動新增 Enum
   1. 可以寫一個初始化不同抽獎活動的方法，資訊來自於資料庫
   2. 每個活動有自己的 ID 以及獎品，可以根據 ID 設計 Redis key 如：`lottery:prize:PR0001`
   3. 每個獎品數量控制一樣可以透過 Redis 加鎖，設計 key 如：`lottery:prize:PR0001:golden`
2. 是否中獎以及抽獎結果也可以記錄在資料庫而不是 local memory 中