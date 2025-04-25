```java
<div className="lmn-container">
  <div className="lmn-row lmn-align-items-start">
    <div className="lmn-col-2">
      Start Time:
    </div>
    <div className="lmn-col-4">
      {dayjs(data.startTime).format('DD MMM YYYY, HH:mm:ss.SSS')}
    </div>
  </div>

  <div className="lmn-row lmn-align-items-start">
    <div className="lmn-col-2">
      End Time:
    </div>
    <div className="lmn-col-4">
      {dayjs(data.endTime).format('DD MMM YYYY, HH:mm:ss.SSS')}
    </div>
  </div>

  <div className="lmn-row lmn-align-items-start">
    <div className="lmn-col-2">
      Total Test Time:
    </div>
    <div className="lmn-col-4">
      {data.totalTestTime.toLocaleString()} ms
    </div>
  </div>
</div>
```
