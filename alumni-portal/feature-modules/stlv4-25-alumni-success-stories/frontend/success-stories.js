// STLV4-25 Alumni Success Stories
// ==========================================
var storyCacheById = {};
async function loadStories() {
    try {
        const [publicResponse, mineResponse, pendingResponse] = await Promise.all([
            fetch(`${API_URL}/stories`, { headers: authHeaders() }),
            isAlumni ? fetch(`${API_URL}/stories/me`, { headers: authHeaders() }) : Promise.resolve(null),
            user.role === 'ROLE_ADMIN' ? fetch(`${API_URL}/stories/pending`, { headers: authHeaders() }) : Promise.resolve(null)
        ]);
        const publicData = await publicResponse.json();
        const grid = document.getElementById('storyGrid');
        grid.innerHTML = (publicData.data || []).map(story => `<article class="alumni-card glass-panel"><h3>${escapeHtml(story.title)}</h3><p class="dept">${escapeHtml(story.achievement || 'Alumni journey')} · ${escapeHtml(story.authorName)}</p><p>${escapeHtml(story.content)}</p></article>`).join('') || '<p class="muted-text">No published stories yet.</p>';
        if (mineResponse) {
            const mine = await mineResponse.json();
            const myStories = mine.data || [];
            storyCacheById = Object.fromEntries(myStories.map(story => [story.id, story]));
            document.getElementById('myStories').innerHTML = `<h3 class="subsection-title">Your submissions</h3>` + (myStories.map(story => `<div class="list-item"><div class="list-item-info"><h3>${escapeHtml(story.title)}</h3><p>Status: ${escapeHtml(story.status)}</p></div><div class="list-item-actions">${story.status === 'PENDING' ? `<button class="btn btn-outline btn-sm" onclick="editStory(${story.id})">Edit</button>` : ''}<button class="btn btn-outline btn-sm" onclick="deleteStory(${story.id})">Delete</button></div></div>`).join('') || '<p class="muted-text">You have not submitted a story yet.</p>');
        }
        if (pendingResponse) {
            const pending = await pendingResponse.json();
            document.getElementById('pendingStories').innerHTML = `<h3 class="subsection-title">Stories awaiting review</h3>` + ((pending.data || []).map(story => `<div class="list-item"><div class="list-item-info"><h3>${escapeHtml(story.title)}</h3><p>${escapeHtml(story.authorName)} · ${escapeHtml(story.content)}</p></div><div class="list-item-actions"><button class="btn btn-primary btn-sm" onclick="moderateStory(${story.id}, 'PUBLISHED')">Publish</button><button class="btn btn-outline btn-sm" onclick="moderateStory(${story.id}, 'REJECTED')">Reject</button></div></div>`).join('') || '<p class="muted-text">No stories awaiting review.</p>');
        }
    } catch (error) { showAlert('storiesMessage', 'Unable to load success stories.', true); }
}

document.getElementById('storyForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    try {
        const storyId = document.getElementById('editingStoryId').value;
        const response = await fetch(`${API_URL}/stories${storyId ? `/${storyId}` : ''}`, { method: storyId ? 'PUT' : 'POST', headers: authHeaders(), body: JSON.stringify({
            title: document.getElementById('storyTitle').value.trim(), achievement: document.getElementById('storyAchievement').value.trim(), content: document.getElementById('storyContent').value.trim()
        }) });
        const result = await response.json();
        showAlert('storiesMessage', result.success ? (storyId ? 'Story updated and returned for review.' : 'Story submitted. It will appear after administrator review.') : result.message, !result.success);
        if (result.success) { cancelStoryEdit(); loadStories(); }
    } catch (error) { showAlert('storiesMessage', 'Network error while submitting your story.', true); }
});

function editStory(id) {
    const story = storyCacheById[id];
    if (!story || story.status !== 'PENDING') return;
    document.getElementById('editingStoryId').value = id;
    document.getElementById('storyTitle').value = story.title;
    document.getElementById('storyAchievement').value = story.achievement || '';
    document.getElementById('storyContent').value = story.content;
    document.getElementById('storyFormHeading').textContent = 'Edit pending story';
    document.getElementById('storySubmitButton').textContent = 'Save and resubmit';
    document.getElementById('cancelStoryEdit').classList.remove('hidden');
    document.getElementById('storyForm').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function cancelStoryEdit() {
    document.getElementById('storyForm').reset();
    document.getElementById('editingStoryId').value = '';
    document.getElementById('storyFormHeading').textContent = 'Share your story';
    document.getElementById('storySubmitButton').textContent = 'Submit for review';
    document.getElementById('cancelStoryEdit').classList.add('hidden');
}

async function moderateStory(id, status) {
    try {
        const response = await fetch(`${API_URL}/stories/${id}/moderation?status=${status}`, { method: 'PUT', headers: authHeaders() });
        const result = await response.json();
        showAlert('storiesMessage', result.success ? `Story ${status.toLowerCase()}.` : result.message, !result.success);
        if (result.success) loadStories();
    } catch (error) { showAlert('storiesMessage', 'Unable to update story moderation.', true); }
}

async function deleteStory(id) {
    if (!confirm('Delete this story?')) return;
    try {
        const response = await fetch(`${API_URL}/stories/${id}`, { method: 'DELETE', headers: authHeaders() });
        const result = await response.json();
        showAlert('storiesMessage', result.success ? 'Story deleted.' : result.message, !result.success);
        if (result.success) loadStories();
    } catch (error) { showAlert('storiesMessage', 'Unable to delete this story.', true); }
}

// ==========================================
