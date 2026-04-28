<template>
  <section class="dashboard">
    <div class="page-title">
      <div>
        <h2>总览看板</h2>
        <p>查看当前本地数据链路的基础状态</p>
      </div>
    </div>

    <div class="stats">
      <article>
        <span>赛季数据</span>
        <strong>1</strong>
        <small>已初始化测试赛季</small>
      </article>
      <article>
        <span>解析模板</span>
        <strong>1</strong>
        <small>首发名单模板已就绪</small>
      </article>
      <article>
        <span>系统状态</span>
        <strong>就绪</strong>
        <small>后续可进入上传中心</small>
      </article>
    </div>

    <section class="panel">
      <h3>Day 2 工作流</h3>
      <div class="steps">
        <div>选择图片或 zip</div>
        <div>后端保存本地文件</div>
        <div>写入上传批次</div>
        <div>查看文件记录</div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: 18px;
}

.page-title h2,
.page-title p,
h3 {
  margin: 0;
}

.page-title h2 {
  font-size: 26px;
}

.page-title p {
  margin-top: 6px;
  color: var(--muted);
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

article,
.panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgb(15 23 42 / 5%);
}

article {
  display: grid;
  gap: 10px;
  padding: 20px;
}

article span {
  color: var(--muted);
  font-weight: 700;
}

article strong {
  color: #102a43;
  font-size: 28px;
}

article small {
  color: #7890a8;
}

.panel {
  display: grid;
  gap: 16px;
  padding: 20px;
}

.steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.steps div {
  padding: 14px;
  color: #12344d;
  background: #eef7ff;
  border: 1px solid #cbe4f9;
  border-radius: 8px;
  font-weight: 700;
}

@media (max-width: 980px) {
  .stats,
  .steps {
    grid-template-columns: 1fr;
  }
}
</style>
