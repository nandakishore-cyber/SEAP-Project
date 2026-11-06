// STLV4-14 Alumni Professional Portfolio
// ==========================================
async function loadPortfolio() {
    if (!isAlumni) return;
    try {
        const response = await fetch(`${API_URL}/portfolio/me`, { headers: authHeaders() });
        const result = await response.json();
        const p = result.data || {};
        document.getElementById('portfolioHeadline').value = p.headline || '';
        document.getElementById('portfolioSummary').value = p.summary || '';
        document.getElementById('portfolioExperience').value = p.experience || '';
        document.getElementById('portfolioProjects').value = p.projects || '';
        document.getElementById('portfolioCertifications').value = p.certifications || '';
        document.getElementById('portfolioWebsite').value = p.websiteUrl || '';
    } catch (error) { showAlert('portfolioMessage', 'Unable to load your portfolio.', true); }
}

document.getElementById('portfolioForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const payload = {
        headline: document.getElementById('portfolioHeadline').value.trim(),
        summary: document.getElementById('portfolioSummary').value.trim(),
        experience: document.getElementById('portfolioExperience').value.trim(),
        projects: document.getElementById('portfolioProjects').value.trim(),
        certifications: document.getElementById('portfolioCertifications').value.trim(),
        websiteUrl: document.getElementById('portfolioWebsite').value.trim()
    };
    try {
        const response = await fetch(`${API_URL}/portfolio/me`, { method: 'PUT', headers: authHeaders(), body: JSON.stringify(payload) });
        const result = await response.json();
        showAlert('portfolioMessage', result.success ? 'Professional portfolio saved.' : result.message, !result.success);
    } catch (error) { showAlert('portfolioMessage', 'Network error while saving your portfolio.', true); }
});

async function viewPortfolio(userId) {
    try {
        const response = await fetch(`${API_URL}/portfolio/${userId}`, { headers: authHeaders() });
        const result = await response.json();
        if (!result.success) { alert(result.message || 'This alumnus has not published a portfolio yet.'); return; }
        const p = result.data, safe = value => escapeHtml(value || '');
        document.getElementById('modalBody').innerHTML = `<h2>${safe(p.name)}</h2><p class="dept">${safe(p.headline)}</p>
            <h3>About</h3><p>${safe(p.summary) || 'No summary provided.'}</p><h3>Experience</h3><p>${safe(p.experience) || 'No experience provided.'}</p>
            <h3>Selected projects</h3><p>${safe(p.projects) || 'No projects provided.'}</p><h3>Certifications</h3><p>${safe(p.certifications) || 'None listed.'}</p>
            ${p.websiteUrl ? `<p><a href="${safe(p.websiteUrl)}" target="_blank" rel="noopener noreferrer">View work samples</a></p>` : ''}`;
        document.getElementById('profileModal').classList.add('show');
    } catch (error) { alert('Unable to load this portfolio.'); }
}

function previewPortfolio() {
    const value = id => document.getElementById(id).value.trim();
    const safe = value => escapeHtml(value || '');
    const website = value('portfolioWebsite');
    if (website && !/^https?:\/\//i.test(website)) {
        showAlert('portfolioMessage', 'Use a valid http or https link before previewing.', true);
        return;
    }
    document.getElementById('modalBody').innerHTML = `<h2>${escapeHtml(`${user.firstName || ''} ${user.lastName || ''}`.trim())}</h2>
        <p class="dept">${safe(value('portfolioHeadline'))}</p><h3>About</h3><p>${safe(value('portfolioSummary')) || 'No summary provided.'}</p>
        <h3>Experience</h3><p>${safe(value('portfolioExperience')) || 'No experience provided.'}</p><h3>Selected projects</h3>
        <p>${safe(value('portfolioProjects')) || 'No projects provided.'}</p><h3>Certifications</h3>
        <p>${safe(value('portfolioCertifications')) || 'None listed.'}</p>${website ? `<p><a href="${safe(website)}" target="_blank" rel="noopener noreferrer">View work samples</a></p>` : ''}`;
    document.getElementById('profileModal').classList.add('show');
}

// ==========================================
