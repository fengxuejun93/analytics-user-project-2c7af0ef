const { createApp, ref, reactive, computed, onMounted, watch } = Vue;

const WMS = 'http://localhost:8081/wms';

const app = createApp({
  setup() {
    const view = ref('dashboard');
    const toast = reactive({ show: false, msg: '', type: 'success' });
    const loading = ref(false);

    // 数据
    const dash = ref({});
    const inbound = ref([]);
    const locations = ref([]);
    const batches = ref([]);
    const counts = ref([]);
    const waves = ref([]);
    const outbound = ref([]);
    const alerts = ref([]);

    // 确认面板（页面内确认层，替代浏览器 confirm）
    const confirmPanel = reactive({ show: false, title: '', rows: [], onConfirm: null, onCancel: null });

    // 弹窗（新建/填写表单）
    const modal = reactive({ show: false, title: '', fields: [], data: {}, errors: {}, onSubmit: null });

    function showToast(msg, type = 'success') {
      toast.show = true; toast.msg = msg; toast.type = type;
      setTimeout(() => { toast.show = false; }, 3000);
    }

    async function api(url, method = 'GET', body = null) {
      try {
        const opts = { method, headers: { 'Content-Type': 'application/json' } };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(WMS + url, opts);
        return await res.json();
      } catch (e) {
        showToast('网络请求失败: ' + e.message, 'error');
        return null;
      }
    }

    // ===== 数据加载 =====
    async function loadDash() { dash.value = (await api('/dashboard')) || {}; }
    async function loadInbound() { inbound.value = (await api('/inbound')) || []; }
    async function loadLocations() { locations.value = (await api('/locations')) || []; }
    async function loadBatches() { batches.value = (await api('/batches')) || []; }
    async function loadCounts() { counts.value = (await api('/counts')) || []; }
    async function loadWaves() { waves.value = (await api('/pick-waves')) || []; }
    async function loadOutbound() { outbound.value = (await api('/outbound')) || []; }
    async function loadAlerts() { alerts.value = (await api('/alerts')) || []; }

    async function refreshCurrent() {
      if (view.value === 'dashboard') await loadDash();
      else if (view.value === 'inbound') { await loadInbound(); await loadDash(); }
      else if (view.value === 'locations') { await loadLocations(); await loadDash(); }
      else if (view.value === 'batches') { await loadBatches(); await loadDash(); }
      else if (view.value === 'counts') { await loadCounts(); await loadDash(); }
      else if (view.value === 'waves') { await loadWaves(); await loadDash(); }
      else if (view.value === 'outbound') { await loadOutbound(); await loadDash(); }
      else if (view.value === 'wms-alerts') { await loadAlerts(); await loadDash(); }
    }

    function switchView(v) {
      view.value = v;
      refreshCurrent();
    }

    // ===== 确认面板 =====
    function showConfirm(title, rows, onConfirm) {
      confirmPanel.show = true;
      confirmPanel.title = title;
      confirmPanel.rows = rows;
      confirmPanel.onConfirm = onConfirm;
    }
    function cancelConfirm() {
      confirmPanel.show = false;
      confirmPanel.onConfirm = null;
    }
    async function doConfirm() {
      if (confirmPanel.onConfirm) {
        await confirmPanel.onConfirm();
      }
      confirmPanel.show = false;
    }

    // ===== 弹窗表单 =====
    function showModal(title, fields, onSubmit) {
      modal.show = true;
      modal.title = title;
      modal.fields = fields;
      modal.data = {};
      modal.errors = {};
      fields.forEach(f => { modal.data[f.key] = f.default || ''; });
      modal.onSubmit = onSubmit;
    }
    function cancelModal() { modal.show = false; modal.onSubmit = null; }
    async function submitModal() {
      modal.errors = {};
      for (const f of modal.fields) {
        if (f.required && !modal.data[f.key]) {
          modal.errors[f.key] = f.label + '不能为空';
        }
      }
      if (Object.keys(modal.errors).length > 0) return;
      if (modal.onSubmit) await modal.onSubmit(modal.data);
      modal.show = false;
    }

    // ===== 入库操作 =====
    function openCreateInbound() {
      showModal('新建入库预约', [
        { key: 'sku', label: 'SKU', required: true },
        { key: 'qty', label: '数量', type: 'number', required: true },
        { key: 'supplier', label: '供应商', required: true },
      ], async (data) => {
        const res = await api('/inbound', 'POST', { sku: data.sku, qty: parseInt(data.qty), supplier: data.supplier });
        if (res && res.id) { showToast('入库预约已创建'); refreshCurrent(); }
        else showToast(res?.msg || '创建失败', 'error');
      });
    }

    function doCheckInbound(item) {
      showConfirm('确认质检：入库单 #' + item.id, [
        { label: 'SKU', value: item.sku },
        { label: '数量', value: item.qty },
        { label: '供应商', value: item.supplier },
      ], async () => {
        const res = await api('/inbound/' + item.id + '/check', 'POST', { result: 'PASSED', checker: '当前用户' });
        if (res && res.id) { showToast('质检通过'); refreshCurrent(); }
        else showToast(res?.msg || '质检失败', 'error');
      });
    }

    function doShelfInbound(item) {
      showModal('上架确认：入库单 #' + item.id, [
        { key: 'locationId', label: '目标库位ID', type: 'number', required: true },
      ], async (data) => {
        const res = await api('/inbound/' + item.id + '/shelf', 'POST', { locationId: parseInt(data.locationId) });
        if (res && res.id) { showToast('上架成功'); refreshCurrent(); }
        else showToast(res?.msg || '上架失败', 'error');
      });
    }

    // ===== 盘点 =====
    function doExecuteCount(item) {
      showModal('执行盘点：盘点单 #' + item.id, [
        { key: 'realQty', label: '实际数量', type: 'number', required: true },
        { key: 'note', label: '备注' },
      ], async (data) => {
        const res = await api('/counts/' + item.id + '/execute', 'POST', { realQty: parseInt(data.realQty), note: data.note });
        if (res && res.id) { showToast('盘点完成'); refreshCurrent(); }
        else showToast(res?.msg || '盘点失败', 'error');
      });
    }

    // ===== 波次拣货 =====
    function doStartPicking(item) {
      showConfirm('确认开始拣货：波次 ' + item.waveNo, [
        { label: '波次号', value: item.waveNo },
        { label: '包含出库单', value: item.orderIds?.join(', ') || '-' },
      ], async () => {
        const res = await api('/pick-waves/' + item.id + '/start', 'POST');
        if (res && res.id) { showToast('开始拣货'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    function doCompletePicking(item) {
      showConfirm('确认完成拣货：波次 ' + item.waveNo, [
        { label: '波次号', value: item.waveNo },
        { label: '包含出库单', value: item.orderIds?.join(', ') || '-' },
      ], async () => {
        const res = await api('/pick-waves/' + item.id + '/complete', 'POST');
        if (res && res.id) { showToast('拣货完成'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    // ===== 出库 =====
    function doReviewOutbound(item) {
      showConfirm('确认开始复核：出库单 ' + item.orderNo, [
        { label: '出库单号', value: item.orderNo },
        { label: 'SKU', value: item.sku },
        { label: '数量', value: item.qty },
      ], async () => {
        const res = await api('/outbound/' + item.id + '/review', 'POST');
        if (res && res.id) { showToast('开始复核'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    function doCompleteOutbound(item) {
      showConfirm('确认完成出库：出库单 ' + item.orderNo, [
        { label: '出库单号', value: item.orderNo },
        { label: 'SKU', value: item.sku },
        { label: '数量', value: item.qty },
      ], async () => {
        const res = await api('/outbound/' + item.id + '/complete', 'POST');
        if (res && res.id) { showToast('出库完成，库存已扣减'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    // ===== 异常预警 =====
    function doAckAlert(item) {
      showConfirm('确认预警：' + item.content, [
        { label: '类型', value: item.type },
        { label: '内容', value: item.content },
      ], async () => {
        const res = await api('/alerts/' + item.id + '/ack', 'POST');
        if (res && res.id) { showToast('已确认'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    function doResolveAlert(item) {
      showModal('关闭预警：' + item.content, [
        { key: 'note', label: '处理说明', required: true },
      ], async (data) => {
        const res = await api('/alerts/' + item.id + '/resolve', 'POST', { note: data.note });
        if (res && res.id) { showToast('已关闭'); refreshCurrent(); }
        else showToast(res?.msg || '操作失败', 'error');
      });
    }

    // ===== 工具 =====
    function ft(str) { return str ? str.replace('T', ' ').substring(0, 16) : '-'; }
    function bclass(status) { return 'w-badge w-badge-' + status.toLowerCase(); }

    onMounted(() => { loadDash(); });

    return {
      view, toast, loading, dash, inbound, locations, batches, counts, waves, outbound, alerts,
      confirmPanel, modal,
      switchView, ft, bclass,
      openCreateInbound, doCheckInbound, doShelfInbound,
      doExecuteCount, doStartPicking, doCompletePicking,
      doReviewOutbound, doCompleteOutbound,
      doAckAlert, doResolveAlert,
      cancelConfirm, doConfirm, cancelModal, submitModal,
    };
  }
});

app.mount('#app');
