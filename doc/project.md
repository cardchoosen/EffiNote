# EffiNote 工程结构

- **入口**：`MainActivity` → `EffiNoteNav`；`EffiNoteApplication` 中初始化 Room、Debug 数据、前台周期检测。
- **导航**：底部三栏（日历 / 今日 / 创建）+ 编辑任务；路由：`calendar`、`today`、`create`、`edit_task/{taskId}`。
- **数据**：`domain/model`（Task、TaskFrequency、TaskUnit）→ `data/local`（Room：TaskEntity、TaskPeriodRecordEntity、PeriodUtils、AppDatabase、TaskDao、TaskPeriodRecordDao）→ `data/repository`（TaskRepository 持久化，DefaultTaskRepository 单例）。周期更替时归档上一周期记录并重置进度（ProcessLifecycle ON_START）。
- **UI**：`ui/calendar`（月历、上下滑切月、按日完成度）、`ui/tasklist`（今日列表 + 打卡）、`ui/createtask`（新建/编辑）、`ui/theme`（Material3）。
- **Debug**：`debug/DebugInitDataLoader` 读取 `assets/debug/init_data_test.json`，仅在库空时写入测试任务；空文件跳过。
- **技术**：Kotlin、Compose、Navigation、ViewModel、Room（KSP）、Lifecycle Process。
