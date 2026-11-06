// STLV4-34 Feedback and Report User
// ==========================================
function openReportFor(userId) {
    document.getElementById('reportTargetId').value = userId;
    document.getElementById('reportCategory').value = 'HARASSMENT';
    showSection('reports');
    document.getElementById('reportDetails').focus();
}

document.getElementById('reportForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    try {
        const response = await fetch(`${API_URL}/reports`, { method: 'POST', headers: authHeaders(), body: JSON.stringify({
            reportedUserId: document.getElementById('reportTargetId').value ? Number(document.getElementById('reportTargetId').value) : null,
            category: document.getElementById('reportCategory').value, details: document.getElementById('reportDetails').value.trim()
        }) });
        const result = await response.json();
        showAlert('reportMessage', result.success ? 'Report submitted to the administrator.' : result.message, !result.success);
        if (result.success) { document.getElementById('reportForm').reset(); loadReports(); }
    } catch (error) { showAlert('reportMessage', 'Network error while submitting the report.', true); }
});

async function loadReports() {
    try {
        const admin = user.role === 'ROLE_ADMIN';
        const response = await fetch(`${API_URL}/reports${admin ? '' : '/me'}`, { headers: authHeaders() });
        const result = await response.json();
        const items = result.data || [];
        const html = items.map(report => `<div class="list-item"><div class="list-item-info"><h3>${escapeHtml(report.category)} · ${escapeHtml(report.status)}</h3>${admin ? `<p>Submitted by: ${escapeHtml(report.reporterName)} (${report.reporterId})</p>` : ''}<p>${report.reportedUserId ? `Reported user: ${escapeHtml(report.reportedUserName)} (${report.reportedUserId})` : 'General feedback'}</p><p>${escapeHtml(report.details)}</p></div>${admin ? `<select aria-label="Update report status" onchange="updateReportStatus(${report.id}, this.value)"><option value="OPEN" ${report.status === 'OPEN' ? 'selected' : ''}>Open</option><option value="REVIEWING" ${report.status === 'REVIEWING' ? 'selected' : ''}>Reviewing</option><option value="RESOLVED" ${report.status === 'RESOLVED' ? 'selected' : ''}>Resolved</option><option value="DISMISSED" ${report.status === 'DISMISSED' ? 'selected' : ''}>Dismissed</option></select>` : ''}</div>`).join('');
        document.getElementById(admin ? 'adminReports' : 'myReports').innerHTML = `<h3 class="subsection-title">${admin ? 'Submitted reports' : 'Your submitted reports'}</h3>${html || '<p class="muted-text">No reports submitted yet.</p>'}`;
    } catch (error) { showAlert('reportMessage', 'Unable to load reports.', true); }
}

async function updateReportStatus(id, status) {
    try {
        const response = await fetch(`${API_URL}/reports/${id}/status?status=${status}`, { method: 'PUT', headers: authHeaders() });
        const result = await response.json();
        showAlert('reportMessage', result.success ? 'Report status updated.' : result.message, !result.success);
        if (result.success) loadReports();
    } catch (error) { showAlert('reportMessage', 'Unable to update report status.', true); }
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]);
}
