const { createApp, ref, computed, onMounted, watch } = Vue;

const app = createApp({
  setup() {
    // ===== 状态 =====
    const currentView = ref('dashboard');
    const dashboard = ref({});
    const alerts = ref([]);
    const logs = ref([]);
    const users = ref([]);
    const loading = ref(false);
    const toast = ref({ show: false, msg: '', type: 'success' });

    // 筛选
    const alertFilter = ref('');  // status filter
    const alertTypeFilter = ref('');
    const logUserFilter = ref('');
    const logActionFilter = ref('');

    // 弹窗
    const dispatchModal = ref({ show: false, alertId: null });
    const resolveModal = ref({ show: false, alertId: null, note: '', error: '' });
    const dispatchAssignee = ref('');
    const dispatchError = ref('');

    // ===== API 调用 =====
    const BASE = '';
    async function apiGet(url) {
      const res = await fetch(BASE + url);
      return res.json();
    }
    async function apiPost(url, body) {
      const res = await fetch(BASE + url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      return res.json();
    }

    function showToast(msg, type = 'success') {
      toast.value = { show: true, msg, type };
      setTimeout(() => { toast.value.show = false; }, 3000);
    }

    // ===== 数据加载 =====
    async function loadDashboard() {
      try {
        dashboard.value = await apiGet('/api/dashboard');
      } catch (e) {
        showToast('加载看板失败', 'error');
      }
    }

    async function loadAlerts() {
      loading.value = true;
      try {
        let url = '/api/alerts?';
        if (alertFilter.value) url += 'status=' + alertFilter.value + '&';
        if (alertTypeFilter.value) url += 'type=' + alertTypeFilter.value;
        alerts.value = await apiGet(url);
      } catch (e) {
        showToast('加载告警失败', 'error');
      }
      loading.value = false;
    }

    async function loadLogs() {
      loading.value = true;
      try {
        let url = '/api/audit-logs?';
        if (logUserFilter.value) url += 'userId=' + logUserFilter.value + '&';
        if (logActionFilter.value) url += 'action=' + logActionFilter.value;
        logs.value = await apiGet(url);
      } catch (e) {
        showToast('加载审计日志失败', 'error');
      }
      loading.value = false;
    }

    async function loadUsers() {
      try {
        users.value = await apiGet('/api/users');
      } catch (e) { /* ignore */ }
    }

    // ===== 告警操作 =====
    function openDispatch(alertId) {
      dispatchAssignee.value = '';
      dispatchError.value = '';
      dispatchModal.value = { show: true, alertId };
    }

    async function confirmDispatch() {
      if (!dispatchAssignee.value) {
        dispatchError.value = '请选择派发对象';
        return;
      }
      const res = await apiPost('/api/alerts/' + dispatchModal.value.alertId + '/dispatch', {
        assigneeId: Number(dispatchAssignee.value)
      });
      if (res.ok) {
        showToast('已派发');
        dispatchModal.value.show = false;
        loadAlerts();
        loadDashboard();
      } else {
        dispatchError.value = res.msg || '派发失败';
      }
    }

    function openResolve(alertId) {
      resolveModal.value = { show: true, alertId, note: '', error: '' };
    }

    async function confirmResolve() {
      if (!resolveModal.value.note.trim()) {
        resolveModal.value.error = '请填写关闭说明';
        return;
      }
      const res = await apiPost('/api/alerts/' + resolveModal.value.alertId + '/resolve', {
        note: resolveModal.value.note
      });
      if (res.ok) {
        showToast('已关闭');
        resolveModal.value.show = false;
        loadAlerts();
        loadDashboard();
      } else {
        resolveModal.value.error = res.msg || '关闭失败';
      }
    }

    // ===== 格式化 =====
    function fmtTime(str) {
      if (!str) return '-';
      return str.replace('T', ' ').substring(0, 16);
    }
    function statusBadgeClass(status) {
      return 'badge badge-' + status.toLowerCase();
    }
    function typeBadgeClass(type) {
      return 'type-badge type-' + type;
    }

    // ===== 看板告警列表 =====
    const recentAlerts = computed(() => dashboard.value.recentAlerts || []);

    // ===== 视图切换 =====
    function switchView(view) {
      currentView.value = view;
      if (view === 'dashboard') loadDashboard();
      else if (view === 'alerts') loadAlerts();
      else if (view === 'logs') loadLogs();
    }

    // ===== 筛选监听 =====
    watch(alertFilter, () => { if (currentView.value === 'alerts') loadAlerts(); });
    watch(alertTypeFilter, () => { if (currentView.value === 'alerts') loadAlerts(); });
    watch(logUserFilter, () => { if (currentView.value === 'logs') loadLogs(); });
    watch(logActionFilter, () => { if (currentView.value === 'logs') loadLogs(); });

    onMounted(() => {
      loadDashboard();
      loadUsers();
    });

    return {
      currentView, dashboard, alerts, logs, users, loading, toast,
      alertFilter, alertTypeFilter, logUserFilter, logActionFilter,
      dispatchModal, resolveModal, dispatchAssignee, dispatchError,
      recentAlerts,
      switchView, openDispatch, confirmDispatch, openResolve, confirmResolve,
      fmtTime, statusBadgeClass, typeBadgeClass, loadAlerts, loadLogs
    };
  }
});

app.mount('#app');
