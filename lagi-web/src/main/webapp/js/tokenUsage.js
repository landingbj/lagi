let tokenUsageChart = null;
const tTextToken = window.tText || ((s) => s);
const tHtmlToken = window.tHtml || ((s) => s);

function getEcharts() {
    if (typeof echarts !== 'undefined') {
        return echarts;
    }
    if (typeof window.echarts !== 'undefined') {
        return window.echarts;
    }
    return null;
}

/** jQuery $.when may pass [data, status, jqXHR] or plain data. */
function firstResolvedPayload(arg) {
    if (arg == null) {
        return null;
    }
    if (Array.isArray(arg) && arg.length > 0) {
        return arg[0];
    }
    return arg;
}

function pickRowCreatedAt(row) {
    if (!row) {
        return 0;
    }
    const v = row.createdAt != null ? row.createdAt : row.created_at;
    return Number(v) || 0;
}

function pickRowTotalTokens(row) {
    if (!row) {
        return 0;
    }
    const v = row.totalTokens != null ? row.totalTokens : row.total_tokens;
    return Number(v) || 0;
}

function pickRowCost(row) {
    if (!row || row.totalCost == null || row.totalCost === '') {
        return null;
    }
    const value = Number(row.totalCost);
    return Number.isFinite(value) ? value : null;
}

function formatTokenCost(value, currency) {
    if (value == null || value === '') {
        return 'CNY 0';
    }
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
        return 'CNY 0';
    }
    const text = numeric.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return 'CNY ' + text;
}

function formatOverviewCost(overview) {
    if (overview && overview.totalCost != null) {
        return formatTokenCost(overview.totalCost, overview.currency);
    }
    const costs = overview && Array.isArray(overview.costsByCurrency) ? overview.costsByCurrency : [];
    if (!costs.length) {
        return 'CNY 0';
    }
    return costs.map(function(item) {
        return formatTokenCost(item.totalCost, item.currency);
    }).join(' / ');
}

function disposeTokenUsageChart() {
    if (!tokenUsageChart) {
        return;
    }
    try {
        tokenUsageChart.dispose();
    } catch (e) {
        /* ignore */
    }
    tokenUsageChart = null;
}

/** Active API range from quick buttons; cleared when dates are edited manually. */
window.__tokenUsageApiRange = 'today';
window.tokenUsageListPage = 1;
window.tokenUsageListPageSize = 20;

function loadTokenUsagePage() {
    disposeTokenUsageChart();

    $('#queryBox').hide();
    $('#footer-info').hide();
    $('#introduces').hide();
    $('#topTitle').hide();
    $('#item-content').show();
    $('#item-content').css('height', 'calc(100vh - 60px)');
    $('#item-content').css('overflow-y', 'auto');
    hideBallDiv();

    window.__tokenUsageApiRange = 'today';
    window.tokenUsageListPage = 1;
    window.tokenUsageListPageSize = 20;

    const today = formatTokenDateInput(new Date());
    const html = `
        <div id="token-usage-container" style="padding: 20px; min-height: 100%; background: #fff;">
            <div style="margin-bottom: 14px;">
                <h2 style="margin: 0; font-size: 24px;">${tTextToken('使用情况')}</h2>
                <div style="margin-top: 6px; color: #6b7280; font-size: 13px;">${tTextToken('查看服务端持久化的 LLM Token 用量与趋势（与聊天会话字符估算无关）。')}</div>
            </div>

            <div style="background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px; margin-bottom: 14px;">
                <div style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center;">
                    <div style="font-size: 13px; color: #374151; margin-right: 6px;">${tTextToken('时间范围')}</div>
                    <button type="button" class="token-range-btn" data-range="today" data-active="1" style="${tokenRangeBtnStyle(true)}">${tTextToken('今天')}</button>
                    <button type="button" class="token-range-btn" data-range="week" data-active="0" style="${tokenRangeBtnStyle(false)}">${tTextToken('本周')}</button>
                    <button type="button" class="token-range-btn" data-range="month" data-active="0" style="${tokenRangeBtnStyle(false)}">${tTextToken('本月')}</button>
                    <button type="button" class="token-range-btn" data-range="all" data-active="0" style="${tokenRangeBtnStyle(false)}">${tTextToken('全部')}</button>
                    <input id="tokenDateStart" type="date" value="${today}" style="padding: 6px 8px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 12px;" />
                    <span style="color: #9ca3af;">${tTextToken('至')}</span>
                    <input id="tokenDateEnd" type="date" value="${today}" style="padding: 6px 8px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 12px;" />
                    <select id="tokenMetricSelect" style="padding: 6px 8px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 12px;">
                        <option value="tokens">Tokens</option>
                        <option value="cost">${tTextToken('费用')}</option>
                    </select>
                    <button type="button" id="tokenRefreshBtn" style="padding: 6px 12px; border: none; border-radius: 999px; background: #1296db; color: #fff; font-size: 12px; cursor: pointer;">${tTextToken('刷新')}</button>
                </div>
                <div style="margin-top: 8px; color: #9ca3af; font-size: 12px;">
                    ${tTextToken('费用统一按人民币计算；未收录模型按同类模型价格估算。')}
                </div>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin-bottom: 14px;">
                ${tokenStatCardHtml(tTextToken('总 Tokens'), 'tokenTotalTokens', '—')}
                ${tokenStatCardHtml(tTextToken('模型成本'), 'tokenTotalCost', '—')}
                ${tokenStatCardHtml(tTextToken('调用记录'), 'tokenRecordCount', '—')}
                ${tokenStatCardHtml(tTextToken('已计费记录'), 'tokenPricedRecordCount', '—')}
            </div>

            <div style="background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px; margin-bottom: 14px;">
                <div style="font-size: 16px; font-weight: 600; margin-bottom: 10px;">${tTextToken('按时间趋势')}</div>
                <div id="tokenUsageChart" style="height: 260px;"></div>
                <div id="tokenUsageNoData" style="display: none; text-align: center; color: #9ca3af; padding: 50px 0;">${tTextToken('暂无趋势数据')}</div>
            </div>

            <div style="background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <div style="font-size: 16px; font-weight: 600;">${tTextToken('用量明细')}</div>
                    <div id="tokenSessionCountText" style="font-size: 12px; color: #6b7280;">—</div>
                </div>
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
                        <thead>
                            <tr style="background: #f9fafb;">
                                <th style="${tokenThStyle()}">${tTextToken('时间')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('用户')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('模型')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('Prompt')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('Completion')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('Total')}</th>
                                <th style="${tokenThStyle()}">${tTextToken('费用')}</th>
                            </tr>
                        </thead>
                        <tbody id="tokenSessionTbody"></tbody>
                    </table>
                </div>
                <div id="tokenUsagePager" style="display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-top: 12px;">
                    <button type="button" id="tokenUsagePrev" style="padding: 4px 10px; border-radius: 6px; border: 1px solid #e5e7eb; background: #fff; cursor: pointer;">${tTextToken('上一页')}</button>
                    <span id="tokenUsagePageInfo" style="font-size: 12px; color: #6b7280;">—</span>
                    <button type="button" id="tokenUsageNext" style="padding: 4px 10px; border-radius: 6px; border: 1px solid #e5e7eb; background: #fff; cursor: pointer;">${tTextToken('下一页')}</button>
                </div>
            </div>
        </div>
    `;
    $('#item-content').html(tHtmlToken(html));
    bindTokenUsageEvents();
    refreshTokenUsageData();
}

function tokenRangeBtnStyle(active) {
    if (active) {
        return 'padding:6px 10px;border-radius:6px;border:1px solid #1296db;background:#1296db;color:#fff;font-size:12px;cursor:pointer;';
    }
    return 'padding:6px 10px;border-radius:6px;border:1px solid #e5e7eb;background:#fff;color:#374151;font-size:12px;cursor:pointer;';
}

function tokenStatCardHtml(title, valueId, value) {
    return `
        <div style="background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:14px;">
            <div style="font-size:12px;color:#6b7280;">${title}</div>
            <div id="${valueId}" style="margin-top:8px;font-size:26px;font-weight:700;color:#111827;">${value}</div>
        </div>
    `;
}

function tokenThStyle() {
    return 'padding:10px;border-bottom:1px solid #e5e7eb;text-align:left;color:#6b7280;font-weight:600;';
}

function bindTokenUsageEvents() {
    $('.token-range-btn').on('click', function() {
        $('.token-range-btn').each(function() {
            $(this).attr('style', tokenRangeBtnStyle(false));
            $(this).attr('data-active', '0');
        });
        $(this).attr('style', tokenRangeBtnStyle(true));
        $(this).attr('data-active', '1');
        window.__tokenUsageApiRange = $(this).data('range');
        applyTokenRange($(this).data('range'));
        window.tokenUsageListPage = 1;
        refreshTokenUsageData();
    });

    $('#tokenDateStart').on('change', function() {
        clearTokenQuickRangeSelection();
        window.__tokenUsageApiRange = null;
        window.tokenUsageListPage = 1;
        refreshTokenUsageData();
    });
    $('#tokenDateEnd').on('change', function() {
        clearTokenQuickRangeSelection();
        window.__tokenUsageApiRange = null;
        window.tokenUsageListPage = 1;
        refreshTokenUsageData();
    });
    $('#tokenMetricSelect').on('change', function() {
        refreshTokenUsageData();
    });
    $('#tokenRefreshBtn').on('click', function() {
        refreshTokenUsageData();
    });
    $('#tokenUsagePrev').on('click', function() {
        if (window.tokenUsageListPage > 1) {
            window.tokenUsageListPage--;
            refreshTokenUsageData();
        }
    });
    $('#tokenUsageNext').on('click', function() {
        window.tokenUsageListPage++;
        refreshTokenUsageData();
    });
}

function clearTokenQuickRangeSelection() {
    $('.token-range-btn').each(function() {
        $(this).attr('style', tokenRangeBtnStyle(false));
        $(this).attr('data-active', '0');
    });
}

function applyTokenRange(range) {
    const now = new Date();
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    let start = new Date(end);
    if (range === 'week') {
        const dayOfWeek = end.getDay() || 7;
        start.setDate(end.getDate() - dayOfWeek + 1);
    } else if (range === 'month') {
        start.setDate(1);
    } else if (range === 'all') {
        start = new Date(2020, 0, 1);
    }
    $('#tokenDateStart').val(formatTokenDateInput(start));
    $('#tokenDateEnd').val(formatTokenDateInput(end));
}

function formatTokenDateInput(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

/**
 * Maps UI to API range query: quick buttons win; otherwise derive from date span.
 */
function resolveTokenApiRange() {
    if (window.__tokenUsageApiRange) {
        return window.__tokenUsageApiRange;
    }
    return deriveTokenApiRangeFromDateInputs();
}

function deriveTokenApiRangeFromDateInputs() {
    const startStr = $('#tokenDateStart').val();
    const endStr = $('#tokenDateEnd').val();
    const today = formatTokenDateInput(new Date());
    if (startStr === endStr && startStr === today) {
        return 'today';
    }
    const s = new Date(startStr + 'T00:00:00');
    const e = new Date(endStr + 'T00:00:00');
    const weekStart = new Date(e);
    const endDayOfWeek = weekStart.getDay() || 7;
    weekStart.setDate(weekStart.getDate() - endDayOfWeek + 1);
    const monthStart = new Date(e.getFullYear(), e.getMonth(), 1);
    if (s.getTime() === weekStart.getTime()) {
        return 'week';
    }
    if (s.getTime() === monthStart.getTime()) {
        return 'month';
    }
    const diff = Math.round((e.getTime() - s.getTime()) / 86400000);
    if (diff < 0) {
        return 'all';
    }
    return 'all';
}

function refreshTokenUsageData() {
    const metric = $('#tokenMetricSelect').val() || 'tokens';
    const range = resolveTokenApiRange();
    const page = window.tokenUsageListPage || 1;
    const pageSize = window.tokenUsageListPageSize || 20;
    const locale = window.getCurrentLocale ? window.getCurrentLocale() : 'zh-CN';

    $.when(
        $.getJSON('/v1/token-statistics/overview', { range: range }),
        $.getJSON('/v1/token-statistics/details', { range: range, page: page, pageSize: pageSize })
    ).done(function(ovPack, detPack) {
        const ov = firstResolvedPayload(ovPack) || {};
        const det = firstResolvedPayload(detPack) || {};
        const totalTokens = Number(ov.totalTokens != null ? ov.totalTokens : 0);
        const recordCount = Number(ov.recordCount != null ? ov.recordCount : 0);
        const pricedRecordCount = Number(ov.pricedRecordCount != null ? ov.pricedRecordCount : 0);

        $('#tokenTotalTokens').text(totalTokens.toLocaleString(locale));
        $('#tokenTotalCost').text(formatOverviewCost(ov));
        $('#tokenRecordCount').text(recordCount.toLocaleString(locale));
        $('#tokenPricedRecordCount').text(pricedRecordCount.toLocaleString(locale));

        const totalRows = Number(det.total != null ? det.total : 0);
        const ps = Number(det.pageSize != null ? det.pageSize : pageSize);
        const totalPages = Math.max(1, Math.ceil(totalRows / ps));
        let srvPage = Number(det.page != null ? det.page : page);
        if (totalRows > 0 && srvPage > totalPages) {
            window.tokenUsageListPage = totalPages;
            refreshTokenUsageData();
            return;
        }
        $('#tokenSessionCountText').text(`${tTextToken('共')} ${totalRows} ${tTextToken('条记录')} · API ${range}`);

        window.tokenUsageListPage = srvPage;
        updateTokenUsagePager(totalRows, srvPage, ps);

        renderTokenSessionTableFromApi(det.records || [], locale);
        fetchTokenChartRows(range, function(rows) {
            const { start, end } = getTokenRangeTime();
            renderTokenUsageChart(rows, start, end, metric);
        });
    }).fail(function(xhr) {
        const msg = (xhr && xhr.status) ? ('HTTP ' + xhr.status) : 'error';
        $('#tokenTotalTokens').text('—');
        $('#tokenSessionCountText').text(tTextToken('加载失败') + ': ' + msg);
    });
}

function updateTokenUsagePager(total, page, pageSize) {
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    if (page > totalPages) {
        window.tokenUsageListPage = totalPages;
        page = totalPages;
    }
    $('#tokenUsagePageInfo').text(`${page} / ${totalPages} (${total})`);
    $('#tokenUsagePrev').prop('disabled', page <= 1);
    $('#tokenUsageNext').prop('disabled', page >= totalPages);
}

/**
 * Paginates detail API to aggregate chart buckets (capped).
 */
function fetchTokenChartRows(range, done) {
    const pageSize = 200;
    let page = 1;
    const maxPages = 40;
    const all = [];

    function next() {
        $.getJSON('/v1/token-statistics/details', { range: range, page: page, pageSize: pageSize })
            .done(function(data) {
                const recs = data.records || [];
                for (let i = 0; i < recs.length; i++) {
                    all.push(recs[i]);
                }
                if (recs.length < pageSize || page >= maxPages) {
                    done(all);
                } else {
                    page++;
                    next();
                }
            })
            .fail(function() {
                done(all);
            });
    }
    next();
}

function getTokenRangeTime() {
    const startStr = $('#tokenDateStart').val();
    const endStr = $('#tokenDateEnd').val();
    const start = startStr ? new Date(`${startStr}T00:00:00`) : new Date(0);
    const end = endStr ? new Date(`${endStr}T23:59:59`) : new Date();
    if (start > end) {
        return { start: end, end: start };
    }
    return { start, end };
}

function renderTokenSessionTableFromApi(records, locale) {
    const tbody = $('#tokenSessionTbody');
    tbody.empty();
    if (!records.length) {
        tbody.append(`<tr><td colspan="7" style="padding: 24px; text-align: center; color: #9ca3af;">${tTextToken('暂无数据')}</td></tr>`);
        return;
    }
    for (let i = 0; i < records.length; i++) {
        const row = records[i] || {};
        const createdAt = pickRowCreatedAt(row);
        const dateLabel = createdAt ? new Date(createdAt).toLocaleString(locale) : '—';
        const userId = row.userId != null ? row.userId : '—';
        const provider = row.provider || '—';
        const model = row.model || '—';
        const pt = Number(row.promptTokens != null ? row.promptTokens : 0);
        const ct = Number(row.completionTokens != null ? row.completionTokens : 0);
        const tt = pickRowTotalTokens(row);
        const cost = formatTokenCost(pickRowCost(row), row.currency);
        const tr = `
            <tr>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${dateLabel}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${userId}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${provider} / ${model}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${pt.toLocaleString(locale)}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${ct.toLocaleString(locale)}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${tt.toLocaleString(locale)}</td>
                <td style="padding:10px;border-bottom:1px solid #f3f4f6;">${cost}</td>
            </tr>
        `;
        tbody.append(tr);
    }
}

function formatDayKeyFromMs(ms) {
    const d = new Date(ms);
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return d.getFullYear() + '-' + m + '-' + day;
}

function renderTokenUsageChart(rows, start, end, metric) {
    const chartDom = document.getElementById('tokenUsageChart');
    const noDataDom = document.getElementById('tokenUsageNoData');
    const ec = getEcharts();
    if (!chartDom || !ec) {
        return;
    }

    const sumsByDay = {};
    const costsByDay = {};
    const costCurrencies = new Set();
    for (let r = 0; r < rows.length; r++) {
        const ca = pickRowCreatedAt(rows[r]);
        if (!ca) {
            continue;
        }
        const key = formatDayKeyFromMs(ca);
        const tt = pickRowTotalTokens(rows[r]);
        sumsByDay[key] = (sumsByDay[key] || 0) + tt;
        const cost = pickRowCost(rows[r]);
        if (cost != null) {
            costsByDay[key] = (costsByDay[key] || 0) + cost;
            if (rows[r].currency) {
                costCurrencies.add(String(rows[r].currency));
            }
        }
    }

    if (metric === 'cost' && costCurrencies.size > 1) {
        chartDom.style.display = 'none';
        if (noDataDom) {
            noDataDom.textContent = tTextToken('存在多种币种，请查看上方成本汇总');
            noDataDom.style.display = 'block';
        }
        disposeTokenUsageChart();
        return;
    }

    const points = [];
    const dayMs = 24 * 3600 * 1000;
    let cursor = new Date(start.getFullYear(), start.getMonth(), start.getDate()).getTime();
    const endTs = new Date(end.getFullYear(), end.getMonth(), end.getDate()).getTime();
    const spanDays = Math.floor((endTs - cursor) / dayMs) + 1;
    if (spanDays > 90) {
        cursor = endTs - 89 * dayMs;
    }
    for (let ts = cursor; ts <= endTs; ts += dayMs) {
        const d = new Date(ts);
        const label = `${d.getMonth() + 1}/${d.getDate()}`;
        const key = formatDayKeyFromMs(ts);
        const dayTokens = sumsByDay[key] || 0;
        points.push({
            label: label,
            tokens: dayTokens,
            cost: Number((costsByDay[key] || 0).toFixed(6))
        });
    }

    const hasData = points.some(function(item) { return item.tokens > 0 || item.cost > 0; });
    if (!hasData) {
        chartDom.style.display = 'none';
        if (noDataDom) {
            noDataDom.style.display = 'block';
        }
        disposeTokenUsageChart();
        return;
    }

    chartDom.style.display = 'block';
    if (noDataDom) {
        noDataDom.textContent = tTextToken('暂无趋势数据');
        noDataDom.style.display = 'none';
    }

    disposeTokenUsageChart();
    if (typeof ec.getInstanceByDom === 'function') {
        const existing = ec.getInstanceByDom(chartDom);
        if (existing) {
            try {
                existing.dispose();
            } catch (e) {
                /* ignore */
            }
        }
    }
    tokenUsageChart = ec.init(chartDom);
    if (!window.__tokenUsageChartResizeBound) {
        window.__tokenUsageChartResizeBound = true;
        window.addEventListener('resize', function() {
            if (tokenUsageChart) {
                tokenUsageChart.resize();
            }
        });
    }

    const isCost = metric === 'cost';
    const option = {
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 20, bottom: 35 },
        xAxis: {
            type: 'category',
            data: points.map(function(item) { return item.label; }),
            axisLine: { lineStyle: { color: '#d1d5db' } }
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            splitLine: { lineStyle: { color: '#eef2f7' } }
        },
        series: [{
            data: points.map(function(item) { return isCost ? item.cost : item.tokens; }),
            type: 'line',
            smooth: true,
            lineStyle: { color: '#ef4444', width: 2 },
            itemStyle: { color: '#ef4444' },
            areaStyle: { color: 'rgba(239,68,68,0.12)' }
        }]
    };
    tokenUsageChart.setOption(option, true);
    setTimeout(function() {
        if (tokenUsageChart) {
            tokenUsageChart.resize();
        }
    }, 0);
}
