let tokenUsageChart = null;
const tTextToken = window.tText || ((s) => s);

const tokenSharedFilters = {
    range: 'today',
    startDate: null,
    endDate: null,
    platform: '',
    search: '',
    platformOptions: []
};

const tokenDashboardStates = {
    cost: { page: 1, pageSize: 20, sort: 'cost', direction: 'desc' },
    usage: { page: 1, pageSize: 20, sort: 'tokens', direction: 'desc' }
};

let tokenDashboardView = 'cost';

function getEcharts() {
    if (typeof echarts !== 'undefined') {
        return echarts;
    }
    return typeof window.echarts !== 'undefined' ? window.echarts : null;
}

function optionalTokenText(value) {
    if (value == null || String(value).trim() === '') {
        return null;
    }
    return String(value).trim();
}

function pickTokenUsageUserName(row) {
    if (!row) {
        return '—';
    }
    return optionalTokenText(row.userDisplayName)
        || optionalTokenText(row.username)
        || optionalTokenText(row.userId)
        || tTextToken('未归属用户');
}

function escapeTokenUsageHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function disposeTokenUsageChart() {
    if (!tokenUsageChart) {
        return;
    }
    try {
        tokenUsageChart.dispose();
    } catch (e) {
        /* stale chart instances are harmless */
    }
    tokenUsageChart = null;
}

// Compatibility entry point for older navigation bundles.
function loadTokenUsagePage() {
    loadCostOverviewPage();
}

function loadCostOverviewPage() {
    loadTokenDashboardPage('cost');
}

function loadUsageStatisticsPage() {
    loadTokenDashboardPage('usage');
}

function loadTokenDashboardPage(view) {
    tokenDashboardView = view === 'usage' ? 'usage' : 'cost';
    tokenDashboardStates[tokenDashboardView].page = 1;
    ensureTokenFilterDefaults();
    disposeTokenUsageChart();
    $('#queryBox').hide();
    $('#footer-info').hide();
    $('#introduces').hide();
    $('#topTitle').hide();
    $('#item-content').show().css({ height: 'calc(100vh - 60px)', overflowY: 'auto' });
    hideBallDiv();
    $('#item-content').html(buildTokenDashboardHtml(tokenDashboardView));
    hydrateTokenFilters();
    bindTokenDashboardEvents();
    refreshTokenUsageData();
}

function ensureTokenFilterDefaults() {
    if (tokenSharedFilters.startDate && tokenSharedFilters.endDate) {
        return;
    }
    const today = formatTokenDateInput(new Date());
    tokenSharedFilters.startDate = today;
    tokenSharedFilters.endDate = today;
}

function buildTokenDashboardHtml(view) {
    const isUsage = view === 'usage';
    const title = isUsage ? tTextToken('用量统计') : tTextToken('费用概览');
    const subtitle = isUsage
        ? tTextToken('查看平台和用户的模型调用与 Token 消耗')
        : tTextToken('查看平台和用户的估算费用及变化趋势');
    const cards = isUsage ? `
        ${tokenMetricCard('tokenMetricModels', '调用模型数')}
        ${tokenMetricCard('tokenMetricCalls', '调用成功总数')}
        ${tokenMetricCard('tokenMetricTokens', '总 Token')}
        ${tokenMetricCard('tokenMetricAverage', '平均单次请求 Token 量')}
    ` : `
        ${tokenMetricCard('tokenMetricCurrentCost', '本期估算费用')}
        ${tokenMetricCard('tokenMetricPreviousCost', '上期估算费用')}
        ${tokenMetricCard('tokenMetricCostChange', '环比变化')}
    `;
    const tableHead = isUsage ? `
        <th>${tTextToken('用户')}</th>
        ${tokenSortableHeader('models', '调用模型数')}
        ${tokenSortableHeader('calls', '调用成功数')}
        ${tokenSortableHeader('tokens', '总 Token')}
        ${tokenSortableHeader('average', '平均单次请求 Token 量')}
    ` : `
        <th>${tTextToken('用户')}</th>
        ${tokenSortableHeader('cost', '估算费用')}
        <th>${tTextToken('费用占比')}</th>
    `;
    const chartTitle = isUsage ? tTextToken('调用与 Token 趋势') : tTextToken('费用趋势');
    const tableTitle = isUsage ? tTextToken('用户用量') : tTextToken('用户费用排行');
    const tableNote = isUsage
        ? tTextToken('仅统计成功且包含有效 Usage 的模型请求')
        : tTextToken('仅展示至少有一笔成功计价记录的用户');
    const disclaimer = isUsage
        ? tTextToken('所有指标采用相同的日期、平台和用户范围。')
        : tTextToken('费用按当前价格目录估算，可能与供应商最终账单存在差异。');

    return `
        <style id="token-usage-styles">
            #token-usage-container { min-height:100%; padding:28px 30px 42px; background:#f7f7f3; color:#172033; box-sizing:border-box; }
            .token-page-header { margin-bottom:24px; }
            .token-page-header h2 { margin:0; font-size:28px; line-height:1.25; color:#172033; }
            .token-page-subtitle { margin-top:8px; color:#64748b; font-size:14px; }
            .token-card-grid { display:grid; grid-template-columns:repeat(${isUsage ? 4 : 3},minmax(0,1fr)); gap:18px; margin-bottom:18px; }
            .token-card, .token-panel { background:#fff; border:1px solid #dbe5ef; border-radius:14px; box-shadow:0 6px 18px rgba(31,60,90,.05); }
            .token-card { min-height:128px; padding:20px 22px; box-sizing:border-box; }
            .token-card-label { color:#627c99; font-size:13px; font-weight:600; }
            .token-card-value { margin-top:10px; color:#0f3766; font-size:30px; line-height:1.1; font-weight:750; letter-spacing:-.02em; }
            .token-card-detail { min-height:20px; margin-top:10px; color:#627c99; font-size:13px; line-height:1.5; }
            .token-panel { padding:20px 22px; margin-bottom:18px; }
            .token-panel-title { color:#24364b; font-size:16px; font-weight:700; }
            .token-panel-note { margin-top:4px; color:#7b8da3; font-size:12px; }
            .token-chart-wrap { position:relative; margin-top:12px; }
            #tokenUsageChart { height:300px; }
            #tokenUsageNoData { display:none; height:300px; align-items:center; justify-content:center; color:#94a3b8; font-size:13px; }
            .token-filter-grid { display:grid; grid-template-columns:130px 150px 18px 150px 170px minmax(210px,1fr) auto; gap:10px; align-items:end; }
            .token-field label { display:block; margin-bottom:7px; color:#41556d; font-size:12px; font-weight:600; }
            .token-field input, .token-field select { width:100%; height:40px; padding:0 11px; border:1px solid #cfdce9; border-radius:9px; background:#fff; color:#172033; font-size:13px; box-sizing:border-box; outline:none; }
            .token-field input:focus, .token-field select:focus { border-color:#1d5b94; box-shadow:0 0 0 3px rgba(29,91,148,.10); }
            .token-filter-separator { height:40px; display:flex; align-items:center; justify-content:center; color:#7b8da3; }
            .token-apply-btn { height:40px; padding:0 18px; border:0; border-radius:9px; background:#1d5b94; color:#fff; font-weight:650; cursor:pointer; }
            .token-disclaimer { margin-top:12px; color:#7b8da3; font-size:12px; }
            .token-warning { color:#b45309 !important; }
            .token-table-header { display:flex; align-items:flex-end; justify-content:space-between; gap:16px; margin-bottom:12px; }
            .token-table-meta { color:#7b8da3; font-size:12px; }
            .token-table-scroll { overflow-x:auto; }
            .token-user-table { width:100%; min-width:${isUsage ? 820 : 620}px; border-collapse:separate; border-spacing:0; font-size:13px; }
            .token-user-table th { padding:12px 14px; border-top:1px solid #dbe5ef; border-bottom:1px solid #dbe5ef; background:#edf3f8; color:#33465d; text-align:left; font-weight:700; white-space:nowrap; }
            .token-user-table th:first-child { border-left:1px solid #dbe5ef; border-radius:10px 0 0 0; }
            .token-user-table th:last-child { border-right:1px solid #dbe5ef; border-radius:0 10px 0 0; }
            .token-user-table td { padding:15px 14px; border-bottom:1px solid #dbe5ef; background:#fff; color:#25384f; vertical-align:middle; }
            .token-user-table tr td:first-child { border-left:1px solid #dbe5ef; }
            .token-user-table tr td:last-child { border-right:1px solid #dbe5ef; }
            .token-user-primary { color:#153d69; font-weight:700; }
            .token-user-secondary, .token-number-secondary { margin-top:4px; color:#6f849d; font-size:12px; }
            .token-sort-btn { padding:0; border:0; background:transparent; color:inherit; font:inherit; font-weight:inherit; cursor:pointer; }
            .token-sort-arrow { margin-left:4px; color:#1d5b94; }
            .token-number-primary { color:#173e6c; font-size:15px; font-weight:700; }
            .token-pager { display:flex; justify-content:flex-end; align-items:center; gap:12px; margin-top:14px; }
            .token-pager button { height:32px; padding:0 12px; border:1px solid #cfdce9; border-radius:8px; background:#fff; color:#33465d; cursor:pointer; }
            .token-pager button:disabled { cursor:not-allowed; opacity:.45; }
            .token-pager span { color:#6f849d; font-size:12px; }
            .token-loading { opacity:.55; pointer-events:none; }
            @media (max-width:1180px) { .token-card-grid { grid-template-columns:repeat(2,minmax(0,1fr)); } .token-filter-grid { grid-template-columns:repeat(2,minmax(0,1fr)); } .token-filter-separator { display:none; } }
            @media (max-width:700px) { #token-usage-container { padding:20px 16px 32px; } .token-card-grid, .token-filter-grid { grid-template-columns:1fr; } }
        </style>
        <div id="token-usage-container" data-view="${view}">
            <div class="token-page-header"><h2>${title}</h2><div class="token-page-subtitle">${subtitle}</div></div>
            <div class="token-card-grid">${cards}</div>
            <div class="token-panel">
                <div class="token-panel-title">${chartTitle}</div>
                <div class="token-panel-note" id="tokenTrendRangeText">—</div>
                <div class="token-chart-wrap"><div id="tokenUsageChart"></div><div id="tokenUsageNoData">${tTextToken('暂无趋势数据')}</div></div>
            </div>
            <div class="token-panel">
                <div class="token-filter-grid">
                    <div class="token-field"><label>${tTextToken('时间范围')}</label><select id="tokenRangePreset">
                        <option value="today">${tTextToken('今天')}</option><option value="week">${tTextToken('本周')}</option>
                        <option value="month">${tTextToken('本月')}</option><option value="all">${tTextToken('全部时间')}</option>
                        <option value="custom">${tTextToken('自定义')}</option>
                    </select></div>
                    <div class="token-field"><label>${tTextToken('开始')}</label><input id="tokenDateStart" type="date"></div>
                    <div class="token-filter-separator">—</div>
                    <div class="token-field"><label>${tTextToken('结束')}</label><input id="tokenDateEnd" type="date"></div>
                    <div class="token-field"><label>${tTextToken('平台')}</label><select id="tokenPlatform"><option value="">${tTextToken('全部平台')}</option></select></div>
                    <div class="token-field"><label>${tTextToken('搜索用户')}</label><input id="tokenUserSearch" type="text" placeholder="${tTextToken('显示名称 / 登录名 / 用户 ID')}"></div>
                    <button type="button" class="token-apply-btn" id="tokenApplyFilter">${tTextToken('应用筛选')}</button>
                </div>
                <div class="token-disclaimer">${disclaimer}</div>
                <div class="token-disclaimer token-warning" id="tokenUnpricedWarning"></div>
            </div>
            <div class="token-panel">
                <div class="token-table-header"><div><div class="token-panel-title">${tableTitle}</div><div class="token-panel-note">${tableNote}</div></div><div class="token-table-meta" id="tokenUserCountText">—</div></div>
                <div class="token-table-scroll"><table class="token-user-table"><thead><tr>${tableHead}</tr></thead><tbody id="tokenUserTbody"></tbody></table></div>
                <div class="token-pager"><button id="tokenUsagePrev">${tTextToken('上一页')}</button><span id="tokenUsagePageInfo">—</span><button id="tokenUsageNext">${tTextToken('下一页')}</button></div>
            </div>
        </div>`;
}

function tokenMetricCard(id, label) {
    return `<div class="token-card"><div class="token-card-label">${tTextToken(label)}</div><div class="token-card-value" id="${id}">—</div><div class="token-card-detail" id="${id}Detail">—</div></div>`;
}

function tokenSortableHeader(field, label) {
    return `<th><button class="token-sort-btn" data-sort="${field}">${tTextToken(label)}<span class="token-sort-arrow" data-arrow="${field}"></span></button></th>`;
}

function hydrateTokenFilters() {
    $('#tokenRangePreset').val(tokenSharedFilters.range);
    $('#tokenDateStart').val(tokenSharedFilters.startDate);
    $('#tokenDateEnd').val(tokenSharedFilters.endDate);
    $('#tokenUserSearch').val(tokenSharedFilters.search);
    renderTokenPlatformOptions(tokenSharedFilters.platformOptions, tokenSharedFilters.platform);
    setTokenDateInputsEnabled(tokenSharedFilters.range !== 'all');
}

function bindTokenDashboardEvents() {
    $('#tokenRangePreset').on('change', function() {
        applyTokenRangePreset($(this).val());
        captureTokenSharedFilters();
    });
    $('#tokenDateStart, #tokenDateEnd').on('change', function() {
        $('#tokenRangePreset').val('custom');
        captureTokenSharedFilters();
    });
    $('#tokenPlatform').on('change', captureTokenSharedFilters);
    $('#tokenUserSearch').on('input', captureTokenSharedFilters);
    $('#tokenApplyFilter').on('click', function() {
        tokenDashboardStates[tokenDashboardView].page = 1;
        refreshTokenUsageData();
    });
    $('#tokenUserSearch').on('keydown', function(event) {
        if (event.key === 'Enter') {
            tokenDashboardStates[tokenDashboardView].page = 1;
            refreshTokenUsageData();
        }
    });
    $('.token-sort-btn').on('click', function() {
        const state = tokenDashboardStates[tokenDashboardView];
        const field = $(this).data('sort');
        if (state.sort === field) {
            state.direction = state.direction === 'desc' ? 'asc' : 'desc';
        } else {
            state.sort = field;
            state.direction = 'desc';
        }
        state.page = 1;
        refreshTokenUsageData();
    });
    $('#tokenUsagePrev').on('click', function() {
        const state = tokenDashboardStates[tokenDashboardView];
        if (state.page > 1) { state.page--; refreshTokenUsageData(); }
    });
    $('#tokenUsageNext').on('click', function() {
        tokenDashboardStates[tokenDashboardView].page++;
        refreshTokenUsageData();
    });
}

function applyTokenRangePreset(range) {
    setTokenDateInputsEnabled(range !== 'all');
    if (range === 'all' || range === 'custom') {
        return;
    }
    const end = new Date();
    end.setHours(0, 0, 0, 0);
    const start = new Date(end.getTime());
    if (range === 'week') {
        const dayOfWeek = end.getDay() || 7;
        start.setDate(end.getDate() - dayOfWeek + 1);
    } else if (range === 'month') {
        start.setDate(1);
    }
    $('#tokenDateStart').val(formatTokenDateInput(start));
    $('#tokenDateEnd').val(formatTokenDateInput(end));
}

function setTokenDateInputsEnabled(enabled) {
    $('#tokenDateStart, #tokenDateEnd').prop('disabled', !enabled);
}

function formatTokenDateInput(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function captureTokenSharedFilters() {
    tokenSharedFilters.range = $('#tokenRangePreset').val() || 'today';
    tokenSharedFilters.startDate = $('#tokenDateStart').val();
    tokenSharedFilters.endDate = $('#tokenDateEnd').val();
    tokenSharedFilters.platform = $('#tokenPlatform').val() || '';
    tokenSharedFilters.search = optionalTokenText($('#tokenUserSearch').val()) || '';
}

function buildTokenDashboardParams() {
    captureTokenSharedFilters();
    const state = tokenDashboardStates[tokenDashboardView];
    const params = {
        view: tokenDashboardView,
        page: state.page,
        pageSize: state.pageSize,
        sort: state.sort,
        direction: state.direction,
        platform: tokenSharedFilters.platform,
        search: tokenSharedFilters.search
    };
    if (tokenSharedFilters.range === 'all') {
        params.range = 'all';
        return params;
    }
    if (!tokenSharedFilters.startDate || !tokenSharedFilters.endDate) {
        showTokenUsageError(tTextToken('请选择完整的开始和结束日期'));
        return null;
    }
    if (tokenSharedFilters.startDate > tokenSharedFilters.endDate) {
        showTokenUsageError(tTextToken('开始日期不能晚于结束日期'));
        return null;
    }
    params.startDate = tokenSharedFilters.startDate;
    params.endDate = tokenSharedFilters.endDate;
    return params;
}

function refreshTokenUsageData() {
    const params = buildTokenDashboardParams();
    if (!params) {
        return;
    }
    const container = $('#token-usage-container').addClass('token-loading');
    $.getJSON('/v1/token-statistics/dashboard', params)
        .done(function(data) { renderTokenDashboard(data || {}); })
        .fail(function(xhr) {
            const message = xhr && xhr.responseJSON && (xhr.responseJSON.message || xhr.responseJSON.error)
                ? (xhr.responseJSON.message || xhr.responseJSON.error)
                : (xhr && xhr.status ? `HTTP ${xhr.status}` : tTextToken('加载失败'));
            showTokenUsageError(message);
        })
        .always(function() { container.removeClass('token-loading'); });
}

function renderTokenDashboard(data) {
    const locale = window.getCurrentLocale ? window.getCurrentLocale() : 'zh-CN';
    const summary = data.summary || {};
    const users = data.users || {};
    tokenSharedFilters.platformOptions = Array.isArray(data.platforms) ? data.platforms : tokenSharedFilters.platformOptions;
    renderTokenPlatformOptions(tokenSharedFilters.platformOptions, tokenSharedFilters.platform);
    renderTokenSummary(summary, data.previousSummary, data.comparison, locale);
    renderTokenUsageChart(data.trend || [], data.bucket || 'day', data.startDate, data.endDate, locale);
    renderTokenUserTable(users.records || [], locale, summary);

    const state = tokenDashboardStates[tokenDashboardView];
    state.page = Number(users.page || 1);
    state.pageSize = Number(users.pageSize || state.pageSize);
    state.sort = users.sort || state.sort;
    state.direction = users.direction || state.direction;
    updateTokenSortArrows();
    updateTokenUsagePager(Number(users.total || 0), state.page, state.pageSize);
    $('#tokenUserCountText').text(`${tTextToken('共')} ${Number(users.total || 0).toLocaleString(locale)} ${tTextToken('位用户')}`);
    $('#tokenTrendRangeText').text(formatTrendRangeText(data));
}

function renderTokenPlatformOptions(options, selected) {
    const select = $('#tokenPlatform');
    if (!select.length) {
        return;
    }
    select.empty().append(`<option value="">${tTextToken('全部平台')}</option>`);
    for (let i = 0; i < options.length; i++) {
        const option = options[i] || {};
        if (!optionalTokenText(option.value)) {
            continue;
        }
        select.append(`<option value="${escapeTokenUsageHtml(option.value)}">${escapeTokenUsageHtml(option.label || option.value)}</option>`);
    }
    select.val(selected || '');
}

function renderTokenSummary(summary, previousSummary, comparison, locale) {
    if (tokenDashboardView === 'usage') {
        $('#tokenMetricModels').text(Number(summary.modelCount || 0).toLocaleString(locale));
        $('#tokenMetricModelsDetail').text(tTextToken('去重后的非空模型数'));
        $('#tokenMetricCalls').text(Number(summary.recordCount || 0).toLocaleString(locale));
        $('#tokenMetricCallsDetail').text(tTextToken('成功且包含有效 Usage 的请求'));
        $('#tokenMetricTokens').text(formatCompactTokenNumber(summary.totalTokens, locale));
        $('#tokenMetricTokensDetail').text(`${Number(summary.totalTokens || 0).toLocaleString(locale)} Token`);
        $('#tokenMetricAverage').text(Number(summary.averageTokensPerRequest || 0).toLocaleString(locale));
        $('#tokenMetricAverageDetail').text(tTextToken('总 Token ÷ 调用成功总数'));
        $('#tokenUnpricedWarning').text('');
        return;
    }
    const current = tokenCostPresentation(summary, locale);
    const previous = previousSummary ? tokenCostPresentation(previousSummary, locale) : { main: '—', detail: tTextToken('全部时间不比较上期') };
    $('#tokenMetricCurrentCost').text(current.main);
    $('#tokenMetricCurrentCostDetail').text(tTextToken('所选范围内成功计价记录'));
    $('#tokenMetricPreviousCost').text(previous.main);
    $('#tokenMetricPreviousCostDetail').text(previousSummary ? tTextToken('紧邻的等长时间范围') : previous.detail);
    $('#tokenMetricCostChange').text(formatCostComparison(comparison));
    $('#tokenMetricCostChangeDetail').text(previousSummary ? tTextToken('本期相对上期') : tTextToken('全部时间不比较上期'));
    const unpriced = Number(summary.unpricedRecordCount || 0);
    $('#tokenUnpricedWarning').text(unpriced > 0
        ? `${unpriced.toLocaleString(locale)} ${tTextToken('次调用未计价，不计入费用')}` : '');
}

function tokenCostPresentation(row, locale) {
    const priced = Number(row && row.pricedRecordCount || 0);
    if (priced <= 0) {
        return { main: '—', detail: tTextToken('暂无成功计价记录') };
    }
    return { main: formatEstimatedTokenCost(row.totalCost, 'CNY', locale), detail: '' };
}

function formatCostComparison(comparison) {
    if (!comparison || comparison.type === 'unavailable') {
        return '—';
    }
    if (comparison.type === 'new') {
        return tTextToken('新增');
    }
    const percent = Number(comparison.percent || 0);
    const prefix = percent > 0 ? '+' : '';
    return `${prefix}${percent.toLocaleString(undefined, { maximumFractionDigits: 2 })}%`;
}

function formatEstimatedTokenCost(value, currency, locale) {
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
        return '—';
    }
    return `${optionalTokenText(currency) || 'CNY'} ${new Intl.NumberFormat(locale, { minimumFractionDigits: 2, maximumFractionDigits: 6 }).format(numeric)}`;
}

function formatCompactTokenNumber(value, locale) {
    const numeric = Number(value) || 0;
    if (Math.abs(numeric) < 1000) {
        return numeric.toLocaleString(locale);
    }
    const units = [{ value: 1000000000, suffix: 'b' }, { value: 1000000, suffix: 'm' }, { value: 1000, suffix: 'k' }];
    for (let i = 0; i < units.length; i++) {
        if (Math.abs(numeric) >= units[i].value) {
            const scaled = numeric / units[i].value;
            return `${scaled.toFixed(scaled >= 100 ? 0 : 1).replace(/\.0$/, '')}${units[i].suffix}`;
        }
    }
    return numeric.toLocaleString(locale);
}

function renderTokenUserTable(records, locale, summary) {
    const tbody = $('#tokenUserTbody').empty();
    const columnCount = tokenDashboardView === 'usage' ? 5 : 3;
    if (!records.length) {
        tbody.append(`<tr><td colspan="${columnCount}" style="padding:28px;text-align:center;color:#94a3b8;">${tTextToken('暂无数据')}</td></tr>`);
        return;
    }
    const totalCost = Number(summary.totalCost || 0);
    for (let i = 0; i < records.length; i++) {
        const row = records[i] || {};
        const userName = pickTokenUsageUserName(row);
        const username = optionalTokenText(row.username);
        const secondary = username && username !== userName ? username : null;
        const userCell = `<td><div class="token-user-primary">${escapeTokenUsageHtml(userName)}</div>${secondary ? `<div class="token-user-secondary">${escapeTokenUsageHtml(secondary)}</div>` : ''}</td>`;
        if (tokenDashboardView === 'usage') {
            const calls = Number(row.requestCount || 0);
            const average = calls > 0 ? Math.round(Number(row.totalTokens || 0) / calls) : 0;
            tbody.append(`<tr>${userCell}
                <td><div class="token-number-primary">${Number(row.modelCount || 0).toLocaleString(locale)}</div></td>
                <td><div class="token-number-primary">${calls.toLocaleString(locale)}</div></td>
                <td><div class="token-number-primary">${Number(row.totalTokens || 0).toLocaleString(locale)}</div></td>
                <td><div class="token-number-primary">${average.toLocaleString(locale)}</div></td></tr>`);
        } else {
            const rowCost = Number(row.totalCost || 0);
            const share = totalCost > 0 ? rowCost * 100 / totalCost : 0;
            const partial = Number(row.unpricedRecordCount || 0) > 0
                ? `<div class="token-number-secondary token-warning">${tTextToken('部分调用未计价')}</div>` : '';
            tbody.append(`<tr>${userCell}
                <td><div class="token-number-primary">${escapeTokenUsageHtml(formatEstimatedTokenCost(rowCost, 'CNY', locale))}</div>${partial}</td>
                <td><div class="token-number-primary">${share.toLocaleString(locale, { maximumFractionDigits: 1 })}%</div></td></tr>`);
        }
    }
}

function updateTokenSortArrows() {
    const state = tokenDashboardStates[tokenDashboardView];
    $('.token-sort-arrow').text('');
    $(`.token-sort-arrow[data-arrow="${state.sort}"]`).text(state.direction === 'asc' ? '↑' : '↓');
}

function updateTokenUsagePager(total, page, pageSize) {
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    $('#tokenUsagePageInfo').text(`${page} / ${totalPages}`);
    $('#tokenUsagePrev').prop('disabled', page <= 1);
    $('#tokenUsageNext').prop('disabled', page >= totalPages);
}

function formatTrendRangeText(data) {
    if (data.range === 'all') {
        return `${tTextToken('全部时间')} · ${tTextToken('按月')}`;
    }
    const labels = { hour: tTextToken('按小时'), day: tTextToken('按天'), month: tTextToken('按月') };
    return `${data.startDate || '—'} ${tTextToken('至')} ${data.endDate || '—'} · ${labels[data.bucket] || ''}`;
}

function renderTokenUsageChart(rawTrend, bucket, startDate, endDate, locale) {
    const chartDom = document.getElementById('tokenUsageChart');
    const noDataDom = document.getElementById('tokenUsageNoData');
    const ec = getEcharts();
    if (!chartDom || !noDataDom) {
        return;
    }
    if (!ec) {
        chartDom.style.display = 'none';
        noDataDom.textContent = tTextToken('图表组件未加载');
        noDataDom.style.display = 'flex';
        return;
    }
    const points = buildTokenTrendPoints(rawTrend, bucket, startDate, endDate);
    const hasData = points.some(function(point) {
        return tokenDashboardView === 'usage'
            ? point.requestCount > 0 || point.totalTokens > 0
            : point.totalCost > 0 || point.unpricedRecordCount > 0;
    });
    if (!hasData) {
        chartDom.style.display = 'none';
        noDataDom.textContent = tTextToken('暂无趋势数据');
        noDataDom.style.display = 'flex';
        disposeTokenUsageChart();
        return;
    }
    chartDom.style.display = 'block';
    noDataDom.style.display = 'none';
    disposeTokenUsageChart();
    const existing = typeof ec.getInstanceByDom === 'function' ? ec.getInstanceByDom(chartDom) : null;
    if (existing) { existing.dispose(); }
    tokenUsageChart = ec.init(chartDom);
    if (!window.__tokenUsageChartResizeBound) {
        window.__tokenUsageChartResizeBound = true;
        window.addEventListener('resize', function() { if (tokenUsageChart) { tokenUsageChart.resize(); } });
    }
    tokenUsageChart.setOption(tokenDashboardView === 'usage'
        ? buildUsageChartOption(points, locale)
        : buildCostChartOption(points, locale), true);
    setTimeout(function() { if (tokenUsageChart) { tokenUsageChart.resize(); } }, 0);
}

function buildCostChartOption(points, locale) {
    return {
        color: ['#1d5b94'],
        tooltip: { trigger: 'axis', formatter: function(params) {
            const point = points[params && params.length ? params[0].dataIndex : 0] || {};
            let text = `<strong>${escapeTokenUsageHtml(point.label || '')}</strong><br/>${tTextToken('估算费用')}: ${escapeTokenUsageHtml(formatEstimatedTokenCost(point.totalCost, 'CNY', locale))}`;
            if (point.unpricedRecordCount > 0) { text += `<br/><span style="color:#b45309">${point.unpricedRecordCount} ${tTextToken('次调用未计价')}</span>`; }
            return text;
        } },
        grid: { left: 62, right: 24, top: 28, bottom: 34 },
        xAxis: tokenChartXAxis(points),
        yAxis: { type: 'value', name: 'CNY', axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { color: '#eef2f6' } } },
        series: [{ name: tTextToken('估算费用'), type: 'line', smooth: true, symbolSize: 7, lineStyle: { width: 3 }, areaStyle: { opacity: .08 }, data: points.map(function(point) { return point.totalCost; }) }]
    };
}

function buildUsageChartOption(points, locale) {
    return {
        color: ['#78a9cf', '#1d5b94'],
        tooltip: { trigger: 'axis', formatter: function(params) {
            const point = points[params && params.length ? params[0].dataIndex : 0] || {};
            return `<strong>${escapeTokenUsageHtml(point.label || '')}</strong><br/>${tTextToken('调用成功数')}: ${point.requestCount.toLocaleString(locale)}<br/>Token: ${point.totalTokens.toLocaleString(locale)}`;
        } },
        legend: { top: 0, right: 4, data: [tTextToken('调用成功数'), 'Token'] },
        grid: { left: 56, right: 58, top: 42, bottom: 34 },
        xAxis: tokenChartXAxis(points),
        yAxis: [
            { type: 'value', name: tTextToken('调用'), minInterval: 1, axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { color: '#eef2f6' } } },
            { type: 'value', name: 'Token', axisLabel: { color: '#64748b' }, splitLine: { show: false } }
        ],
        series: [
            { name: tTextToken('调用成功数'), type: 'bar', yAxisIndex: 0, barMaxWidth: 28, data: points.map(function(point) { return point.requestCount; }), itemStyle: { borderRadius: [4, 4, 0, 0] } },
            { name: 'Token', type: 'line', yAxisIndex: 1, smooth: true, symbolSize: 6, lineStyle: { width: 2.5 }, data: points.map(function(point) { return point.totalTokens; }) }
        ]
    };
}

function tokenChartXAxis(points) {
    return { type: 'category', data: points.map(function(point) { return point.label; }), axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { color: '#64748b', hideOverlap: true } };
}

function buildTokenTrendPoints(rawTrend, bucket, startDate, endDate) {
    const byBucket = {};
    for (let i = 0; i < rawTrend.length; i++) {
        const row = rawTrend[i] || {};
        if (row.bucket) { byBucket[row.bucket] = row; }
    }
    let start;
    let end;
    if (startDate && endDate) {
        start = parseTokenLocalDate(startDate);
        end = parseTokenLocalDate(endDate);
    } else if (rawTrend.length) {
        start = parseTokenBucket(rawTrend[0].bucket);
        end = parseTokenBucket(rawTrend[rawTrend.length - 1].bucket);
    } else {
        return [];
    }
    if (!start || !end) { return []; }
    if (bucket === 'hour') {
        end = new Date(start.getTime());
        end.setHours(23, 0, 0, 0);
    } else if (bucket === 'month') {
        start.setDate(1); end.setDate(1);
    }
    const points = [];
    let cursor = new Date(start.getTime());
    while (cursor.getTime() <= end.getTime()) {
        const key = formatTokenBucket(cursor, bucket);
        const row = byBucket[key] || {};
        points.push({
            label: formatTokenBucketLabel(cursor, bucket),
            requestCount: Number(row.requestCount || 0),
            totalTokens: Number(row.totalTokens || 0),
            totalCost: Number(row.totalCost || 0),
            unpricedRecordCount: Number(row.unpricedRecordCount || 0)
        });
        if (bucket === 'hour') {
            cursor.setHours(cursor.getHours() + 1);
            if (points.length >= 24) { break; }
        } else if (bucket === 'month') {
            cursor.setMonth(cursor.getMonth() + 1);
        } else {
            cursor.setDate(cursor.getDate() + 1);
        }
    }
    return points;
}

function parseTokenLocalDate(value) {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value || '');
    return match ? new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3])) : null;
}

function parseTokenBucket(value) {
    const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):/.exec(value || '');
    return match ? new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]), Number(match[4])) : null;
}

function formatTokenBucket(date, bucket) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(bucket === 'month' ? 1 : date.getDate()).padStart(2, '0');
    const h = String(bucket === 'hour' ? date.getHours() : 0).padStart(2, '0');
    return `${y}-${m}-${d}T${h}:00:00`;
}

function formatTokenBucketLabel(date, bucket) {
    if (bucket === 'hour') { return `${String(date.getHours()).padStart(2, '0')}:00`; }
    if (bucket === 'month') { return `${date.getFullYear()}/${date.getMonth() + 1}`; }
    return `${date.getMonth() + 1}/${date.getDate()}`;
}

function showTokenUsageError(message) {
    $('.token-card-value').text('—');
    $('#tokenUserCountText').text(`${tTextToken('加载失败')}: ${message}`);
    const columns = tokenDashboardView === 'usage' ? 5 : 3;
    $('#tokenUserTbody').html(`<tr><td colspan="${columns}" style="padding:28px;text-align:center;color:#b45309;">${escapeTokenUsageHtml(message)}</td></tr>`);
    const chartDom = document.getElementById('tokenUsageChart');
    const noDataDom = document.getElementById('tokenUsageNoData');
    if (chartDom) { chartDom.style.display = 'none'; }
    if (noDataDom) { noDataDom.textContent = message; noDataDom.style.display = 'flex'; }
    disposeTokenUsageChart();
}
