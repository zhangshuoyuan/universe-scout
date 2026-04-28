<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo">US</div>
        <div>
          <strong>宇宙球探</strong>
          <span>Universe Scout</span>
        </div>
      </div>
      <nav class="nav">
        <router-link to="/dashboard">总览看板</router-link>
        <router-link to="/upload">上传中心</router-link>
        <router-link to="/images">图片管理</router-link>
        <router-link to="/parse-task">解析任务</router-link>
      </nav>
    </aside>
    <div class="workspace">
      <header class="header">
        <div>
          <strong>NBA2K 模拟宇宙数据台</strong>
          <span>本地数据资产管理</span>
        </div>
        <div class="status">本地环境</div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 236px;
  flex: 0 0 236px;
  background: #102a43;
  border-right: 1px solid rgb(255 255 255 / 8%);
}

.brand {
  display: flex;
  min-height: 76px;
  align-items: center;
  gap: 12px;
  padding: 18px;
  color: #ffffff;
  border-bottom: 1px solid rgb(255 255 255 / 10%);
}

.logo {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  background: #f7c948;
  color: #102a43;
  border-radius: 8px;
  font-weight: 800;
}

.brand strong,
.brand span {
  display: block;
}

.brand strong {
  font-size: 18px;
}

.brand span {
  margin-top: 3px;
  color: #b6c8d9;
  font-size: 12px;
}

.nav {
  display: grid;
  gap: 6px;
  padding: 14px 10px;
}

.nav a {
  padding: 12px 14px;
  color: #d9e6f2;
  border-radius: 8px;
  font-weight: 600;
}

.nav a:hover {
  background: rgb(255 255 255 / 8%);
}

.nav a.router-link-active {
  color: #ffffff;
  background: #1d6fb8;
}

.workspace {
  min-width: 0;
  flex: 1;
}

.header {
  display: flex;
  min-height: 68px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 28px;
  background: #ffffff;
  border-bottom: 1px solid var(--line);
}

.header strong,
.header span {
  display: block;
}

.header strong {
  font-size: 18px;
}

.header span {
  margin-top: 4px;
  color: var(--muted);
  font-size: 13px;
}

.status {
  padding: 6px 12px;
  color: #0f766e;
  background: #dff6ef;
  border: 1px solid #b7ead8;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.content {
  padding: 26px;
}

@media (max-width: 820px) {
  .layout {
    display: block;
  }

  .sidebar {
    width: auto;
  }

  .nav {
    grid-template-columns: repeat(4, 1fr);
  }

  .content {
    padding: 16px;
  }
}
</style>
