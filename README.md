Excel 数据库
```
将excel作为数据库

实现方法类似redis：实际上是内存的操作，再由异步任务将内存的数据持久化到excel
```

### 使用
1. 配置config.properties指定excel文件位置。
2. 创建bean，第一个字段必须为id，所有的字段都必须为string类型。
3. 创建beanDao，继承ExcelDbDao，可用方法有insert，query。
4. 初始化excel数据。sheet即表名，与类名一致；列与bean的属性一一对应。excel第一行数据作为列名，可自行设置。详见demo.xlsx