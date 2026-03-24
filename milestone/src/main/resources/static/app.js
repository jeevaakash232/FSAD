const API_URL = '/api/events';
let stompClient = null;
let charts = {};
let isInitialized = false;

// Initialize WebSocket
function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // suppress console noise
    stompClient.connect({}, function () {
        console.log('✅ WebSocket Connected');
        stompClient.subscribe('/topic/events', function (event) {
            displayRealtimeEvent(JSON.parse(event.body));
        });
    }, function (err) {
        console.warn('WebSocket error, retrying in 5s...', err);
        setTimeout(connect, 5000);
    });
}

// Initialize charts
function initializeCharts() {
    if (isInitialized) return;
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded yet, retrying...');
        setTimeout(initializeCharts, 500);
        return;
    }

    const systemConfigs = {
        'smart-home':      { min: 0, max: 50,  color: '#667eea' },
        'industrial':      { min: 0, max: 250, color: '#f59e0b' },
        'environmental':   { min: 0, max: 200, color: '#10b981' },
        'vehicle':         { min: 0, max: 200, color: '#ef4444' }
    };

    ['smart-home', 'industrial', 'environmental', 'vehicle'].forEach(system => {
        const canvas = document.getElementById(`chart-${system}`);
        if (!canvas || charts[system]) return;

        const config = systemConfigs[system];
        charts[system] = new Chart(canvas, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Value',
                    data: [],
                    borderColor: config.color,
                    backgroundColor: config.color + '20',
                    tension: 0.3,
                    fill: true,
                    borderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: config.color,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: true, backgroundColor: 'rgba(0,0,0,0.8)', padding: 10, displayColors: false }
                },
                scales: {
                    x: {
                        display: true,
                        grid: { display: false },
                        ticks: { font: { size: 10 }, maxTicksLimit: 5, maxRotation: 0, color: '#6b7280' }
                    },
                    y: {
                        display: true,
                        min: config.min,
                        max: config.max,
                        ticks: { font: { size: 10 }, maxTicksLimit: 5, color: '#6b7280', callback: v => v.toFixed(0) },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    }
                }
            }
        });
    });

    isInitialized = true;
    console.log('✅ Charts initialized');
}

function updateChart(systemId, value, timestamp) {
    const chart = charts[systemId];
    if (!chart) return;

    const label = new Date(timestamp).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    chart.data.labels.push(label);
    chart.data.datasets[0].data.push(value);

    if (chart.data.labels.length > 8) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
    }
    chart.update('none');
}

const systemIdMap = {
    'Smart-Home-Monitor':       'smart-home',
    'Industrial-Sensor-Network':'industrial',
    'Environmental-Monitoring': 'environmental',
    'Vehicle-Tracking-System':  'vehicle'
};

async function loadSystemEvents(systemName) {
    try {
        const res = await fetch(`${API_URL}/system/${systemName}`);
        if (!res.ok) return;
        const events = await res.json();
        const systemId = systemIdMap[systemName];
        if (!systemId) return;

        // count
        const countEl = document.getElementById(`count-${systemId}`);
        if (countEl) countEl.textContent = events.length;

        // chart — load last 8 events oldest-first
        const chart = charts[systemId];
        if (chart && events.length > 0) {
            chart.data.labels = [];
            chart.data.datasets[0].data = [];
            [...events].reverse().slice(-8).forEach(e => {
                chart.data.labels.push(new Date(e.timestamp).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
                chart.data.datasets[0].data.push(e.eventValue);
            });
            chart.update('none');
        }

        // last event time
        const healthEl = document.getElementById(`health-${systemId}`);
        if (healthEl && events.length > 0) {
            const secs = Math.floor((new Date() - new Date(events[0].timestamp)) / 1000);
            healthEl.textContent = secs < 60 ? `${secs}s ago` : secs < 3600 ? `${Math.floor(secs / 60)}m ago` : `${Math.floor(secs / 3600)}h ago`;
        }

        // event list
        const listEl = document.getElementById(`events-${systemId}`);
        if (listEl) {
            listEl.innerHTML = events.slice(0, 5).map(e => `
                <div class="event-item ${(e.status || 'normal').toLowerCase()}">
                    <strong>${e.eventType}</strong>: ${e.eventValue}
                    <div class="timestamp">${new Date(e.timestamp).toLocaleString()}</div>
                </div>`).join('');
        }
    } catch (err) {
        console.error(`Error loading ${systemName}:`, err);
    }
}

function displayRealtimeEvent(event) {
    const systemId = systemIdMap[event.systemName];
    if (systemId) {
        updateChart(systemId, event.eventValue, event.timestamp);

        const countEl = document.getElementById(`count-${systemId}`);
        if (countEl) countEl.textContent = parseInt(countEl.textContent || '0') + 1;

        const healthEl = document.getElementById(`health-${systemId}`);
        if (healthEl) healthEl.textContent = 'Just now';
    }

    const allList = document.getElementById('all-events');
    if (allList) {
        const div = document.createElement('div');
        div.className = `event-item ${(event.status || 'normal').toLowerCase()}`;
        div.innerHTML = `
            <strong>${event.systemName}</strong> — ${event.eventType}: ${event.eventValue}
            ${['ALERT','CRITICAL'].includes(event.status) ? ' ⚠️' : ''}
            <div class="timestamp">${new Date(event.timestamp).toLocaleString()}</div>`;
        allList.insertBefore(div, allList.firstChild);
        while (allList.children.length > 20) allList.removeChild(allList.lastChild);
    }

    if (!window.statsUpdatePending) {
        window.statsUpdatePending = true;
        setTimeout(() => { updateStatistics(); window.statsUpdatePending = false; }, 1000);
    }
}

async function loadAllEvents() {
    try {
        const res = await fetch(API_URL);
        if (!res.ok) return;
        const events = await res.json();
        const allList = document.getElementById('all-events');
        if (allList) {
            allList.innerHTML = events.slice(0, 20).map(e => `
                <div class="event-item ${(e.status || 'normal').toLowerCase()}">
                    <strong>${e.systemName}</strong> — ${e.eventType}: ${e.eventValue}
                    ${['ALERT','CRITICAL'].includes(e.status) ? ' ⚠️' : ''}
                    <div class="timestamp">${new Date(e.timestamp).toLocaleString()}</div>
                </div>`).join('');
        }
    } catch (err) {
        console.error('Error loading all events:', err);
    }
}

async function updateStatistics() {
    try {
        const res = await fetch(`${API_URL}/statistics`);
        if (!res.ok) return;
        const stats = await res.json();
        const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val || 0; };
        set('stat-info',     stats.infoCount);
        set('stat-warning',  stats.warningCount);
        set('stat-alert',    stats.alertCount);
        set('stat-critical', stats.criticalCount);
    } catch (err) {
        console.error('Error loading statistics:', err);
    }
}

window.onload = function () {
    console.log('🚀 Real-Time Event Synchronization Engine starting...');

    initializeCharts();

    // Load data after charts are ready
    const waitForCharts = setInterval(() => {
        if (isInitialized) {
            clearInterval(waitForCharts);
            loadAllEvents();
            Object.keys(systemIdMap).forEach(loadSystemEvents);
            updateStatistics();
            connect();
            setInterval(updateStatistics, 10000);
            console.log('✅ System ready');
        }
    }, 100);
};
