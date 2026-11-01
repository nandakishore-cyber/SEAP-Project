const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');
const user = JSON.parse(localStorage.getItem('user') || '{}');
let currentAlumniProfileId = null;
const isStudent = user.role === 'ROLE_STUDENT';
const isAlumni = user.role === 'ROLE_ALUMNI';

// Auth check
if (!token) {
    window.location.href = 'index.html';
}

// Init Dashboard
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('userNameDisplay').textContent = `Welcome, ${user.firstName}`;
    document.getElementById('dashboardTitle').textContent = isStudent
        ? 'Student Dashboard'
        : user.role === 'ROLE_ADMIN' ? 'Administrator Dashboard' : 'Alumni Dashboard';
    document.getElementById('dashboardSubtitle').textContent = isStudent
        ? 'Manage your academic identity, interests, and career goals.'
        : 'Manage your profile, professional journey, and alumni network.';
    document.getElementById('dashboardOverview').innerHTML = isStudent
        ? `<div class="overview-card"><span class="overview-label">STUDENT SPACE</span><strong>Shape your next opportunity</strong><p>Keep your academic profile ready for alumni mentors and career conversations.</p></div>
           <div class="overview-card"><span class="overview-label">NEXT STEP</span><strong>Complete your Student Details</strong><p>Add your program, graduation year, interests, and skills.</p></div>`
        : `<div class="overview-card"><span class="overview-label">ALUMNI NETWORK</span><strong>Share your journey</strong><p>Help students learn from your academic and professional experience.</p></div>
           <div class="overview-card"><span class="overview-label">NEXT STEP</span><strong>Submit your Alumni Details</strong><p>Add your career information to join the verified alumni directory.</p></div>`;
    if (isStudent) {
        document.getElementById('studentNav').classList.remove('hidden');
        document.getElementById('mentorshipNav').classList.remove('hidden');
        document.getElementById('eventsNav').classList.remove('hidden');
        document.getElementById('alumniNav').classList.add('hidden');
        document.getElementById('directoryNav').querySelector('button').textContent = 'Find Alumni Mentors';
        document.getElementById('studentProfile').classList.remove('hidden');
        document.querySelector('.certificate-panel')?.classList.add('hidden');
    }
    if (isAlumni) {
        document.getElementById('mentorshipNav').classList.remove('hidden');
        document.getElementById('receivedMentorshipRequests').classList.remove('hidden');
        document.getElementById('eventCreationPanel').classList.remove('hidden');
    }
    
    // Show admin tab if admin
    if (user.role === 'ROLE_ADMIN') {
        document.getElementById('adminNav').classList.remove('hidden');
    }
    
    // Load initial section
    loadProfile();
    loadNotificationCount();
});

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}

function showSection(sectionId) {
    // UI updates
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.dashboard-section').forEach(sec => sec.classList.remove('active'));
    
    document.getElementById(`nav-${sectionId}`).classList.add('active');
    document.getElementById(`section-${sectionId}`).classList.add('active');
    
    // Load data based on section
    if (sectionId === 'profile') loadProfile();
    if (sectionId === 'studentProfile') loadProfile();
    if (sectionId === 'alumniProfile') loadAlumniProfile();
    if (sectionId === 'directory') loadDirectory();
    if (sectionId === 'mentorship') loadMentorship();
    if (sectionId === 'events') loadEvents();
    if (sectionId === 'notifications') loadNotifications();
    if (sectionId === 'verification') loadVerifications();
}

// --- Auth Headers Helper ---
const authHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
});

function showAlert(elementId, message, isError = false) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.className = `alert ${isError ? 'error' : ''}`;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 5000);
}

// ==========================================
// 1. Basic Profile (STLV4-79)
// ==========================================
async function loadProfile() {
    try {
        const res = await fetch(`${API_URL}/profiles/me`, { headers: authHeaders() });
        const data = await res.json();
        
        if (data.success && data.data) {
            const p = data.data;
            document.getElementById('profPhone').value = p.phone || '';
            document.getElementById('profDob').value = p.dateOfBirth || '';
            document.getElementById('profCity').value = p.city || '';
            document.getElementById('profCountry').value = p.country || '';
            document.getElementById('profBio').value = p.bio || '';
            document.getElementById('profLinkedin').value = p.linkedinUrl || '';
            document.getElementById('studentId').value = p.studentId || '';
            document.getElementById('studentProgram').value = p.program || '';
            document.getElementById('studentDepartment').value = p.department || '';
            document.getElementById('studentGraduationYear').value = p.expectedGraduationYear || '';
            document.getElementById('studentInterests').value = p.interests || '';
            document.getElementById('studentSkills').value = p.studentSkills || '';
        }
    } catch (e) { console.error("Profile not created yet or error"); }
}

document.getElementById('profileForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        phone: document.getElementById('profPhone').value,
        dateOfBirth: document.getElementById('profDob').value || null,
        city: document.getElementById('profCity').value,
        country: document.getElementById('profCountry').value,
        bio: document.getElementById('profBio').value,
        linkedinUrl: document.getElementById('profLinkedin').value
    };
    if (isStudent) Object.assign(payload, getStudentFields());

    try {
        // Try PUT first, if 404 (doesn't exist), fallback to POST
        let res = await fetch(`${API_URL}/profiles/me`, {
            method: 'PUT', headers: authHeaders(), body: JSON.stringify(payload)
        });

        function getStudentFields() {
            return {
                studentId: document.getElementById('studentId').value,
                program: document.getElementById('studentProgram').value,
                department: document.getElementById('studentDepartment').value,
                expectedGraduationYear: parseInt(document.getElementById('studentGraduationYear').value) || null,
                interests: document.getElementById('studentInterests').value,
                studentSkills: document.getElementById('studentSkills').value
            };
        }

        document.getElementById('studentForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                let res = await fetch(`${API_URL}/profiles/me`, {
                    method: 'PUT',
                    headers: authHeaders(),
                    body: JSON.stringify(getStudentFields())
                });
                if (res.status === 404) {
                    res = await fetch(`${API_URL}/profiles`, {
                        method: 'POST',
                        headers: authHeaders(),
                        body: JSON.stringify(getStudentFields())
                    });
                }
                const data = await res.json();
                showAlert('profileMessage', data.success ? 'Student details saved successfully.' : data.message || 'Failed to save student details.', !data.success);
            } catch (error) {
                showAlert('profileMessage', 'Network error while saving student details.', true);
            }
        });
        
        if (res.status === 404) {
            res = await fetch(`${API_URL}/profiles`, {
                method: 'POST', headers: authHeaders(), body: JSON.stringify(payload)
            });
        }
        
        const data = await res.json();
        if (data.success || res.status === 201 || res.status === 200) {
            showAlert('profileMessage', 'Profile saved successfully!');
        } else {
            showAlert('profileMessage', data.message || 'Failed to save', true);
        }
    } catch (e) {
        showAlert('profileMessage', 'Network error', true);
    }
});

// ==========================================
// 2. Alumni Profile (STLV4-61 & STLV4-17)
// ==========================================
async function loadAlumniProfile() {
    try {
        const res = await fetch(`${API_URL}/alumni/me`, { headers: authHeaders() });
        const data = await res.json();
        
        if (data.success && data.data) {
            const a = data.data;
            currentAlumniProfileId = a.id;
            document.getElementById('alumCollege').value = a.collegeName || '';
            document.getElementById('alumDept').value = a.department || '';
            document.getElementById('alumDegree').value = a.degree || '';
            document.getElementById('alumYear').value = a.graduationYear || '';
            document.getElementById('alumCompany').value = a.currentCompany || '';
            document.getElementById('alumDesignation').value = a.currentDesignation || '';
            document.getElementById('alumExp').value = a.yearsOfExperience || 0;
            document.getElementById('alumSkills').value = a.skills || '';
            
            // Badge
            const badge = document.getElementById('verificationBadge');
            badge.textContent = a.verificationStatus;
            badge.className = `badge ${a.verificationStatus.toLowerCase()}`;
            loadBonafideStatus();
            
            // Disable form if verified or pending
            if(a.verificationStatus === 'VERIFIED' || a.verificationStatus === 'PENDING') {
                document.getElementById('btnAlumniSave').classList.add('hidden');
                document.querySelectorAll('#alumniForm input').forEach(el => el.disabled = true);
            }
        }
    } catch (e) { console.error("No alumni profile yet"); }
}

document.getElementById('alumniForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    if(currentAlumniProfileId) return; // Prevent double creation
    
    const payload = {
        collegeName: document.getElementById('alumCollege').value,
        department: document.getElementById('alumDept').value,
        degree: document.getElementById('alumDegree').value,
        graduationYear: parseInt(document.getElementById('alumYear').value),
        currentCompany: document.getElementById('alumCompany').value,
        currentDesignation: document.getElementById('alumDesignation').value,
        yearsOfExperience: parseInt(document.getElementById('alumExp').value) || 0,
        skills: document.getElementById('alumSkills').value
    };

    try {
        const res = await fetch(`${API_URL}/alumni`, {
            method: 'POST', headers: authHeaders(), body: JSON.stringify(payload)
        });

        async function loadBonafideStatus() {
            if (!isAlumni) return;
            try {
                const response = await fetch(`${API_URL}/alumni/me/bonafide`, { headers: authHeaders() });
                const data = await response.json();
                const status = document.getElementById('certificateStatus');
                if (data.success && data.data) {
                    status.textContent = `Uploaded: ${data.data.originalFileName}`;
                    status.className = 'status-chip success';
                } else {
                    status.textContent = 'Not uploaded';
                    status.className = 'status-chip warning';
                }
            } catch (error) {
                console.error('Unable to load bonafide certificate status', error);
            }
        }
        const data = await res.json();
        if (data.success || res.status === 201) {
            showAlert('alumniMessage', 'Alumni Profile submitted for verification!');
            loadAlumniProfile(); // Reload to show badge and disable form
        } else {
            showAlert('alumniMessage', data.message || 'Validation error', true);
        }
    } catch (e) {
        showAlert('alumniMessage', 'Network error', true);
    }
});

// ==========================================
// 3. Directory / View Alumni (STLV4-17)
// ==========================================
async function loadDirectory() {
    const grid = document.getElementById('directoryGrid');
    const dept = document.getElementById('searchDept').value;
    const company = document.getElementById('searchCompany').value;
    
    let url = `${API_URL}/alumni?`;
    if(dept) url += `department=${encodeURIComponent(dept)}&`;
    if(company) url += `company=${encodeURIComponent(company)}`;
    
    grid.innerHTML = '<div class="loader-container"><div class="loader"></div></div>';
    
    try {
        const res = await fetch(url); // public endpoint
        const data = await res.json();
        
        grid.innerHTML = '';
        if(data.data.content.length === 0) {
            grid.innerHTML = '<p style="color:var(--clr-text-muted); grid-column:1/-1;">No verified alumni found matching criteria.</p>';
            return;
        }
        
        data.data.content.forEach(a => {
            const card = document.createElement('div');
            card.className = 'alumni-card glass-panel';
            card.onclick = () => viewFullProfile(a.id);
            card.innerHTML = `
                <h3>${a.firstName} ${a.lastName}</h3>
                <p class="dept">${a.degree} - ${a.department} (${a.graduationYear})</p>
                <p class="company">${a.currentDesignation || 'Alumni'} @ ${a.currentCompany || 'Not specified'}</p>
            `;
            grid.appendChild(card);
        });
    } catch (e) {
        grid.innerHTML = '<p class="error-msg">Failed to load directory</p>';
    }
}

async function viewFullProfile(id) {
    try {
        const res = await fetch(`${API_URL}/alumni/${id}`);
        const data = await res.json();
        const a = data.data;
        
        const modalBody = document.getElementById('modalBody');
        modalBody.innerHTML = `
            <h2 style="margin-bottom: 0.5rem">${a.firstName} ${a.lastName}</h2>
            <div style="margin-bottom: 1.5rem">
                <span class="badge verified">${a.verificationStatus}</span>
            </div>
            
            <h3 style="border-bottom: 1px solid var(--clr-border); padding-bottom: 0.5rem; margin-bottom:1rem;">Academic</h3>
            <div class="modal-detail"><label>College:</label> <span>${a.collegeName}</span></div>
            <div class="modal-detail"><label>Degree:</label> <span>${a.degree} in ${a.department}</span></div>
            <div class="modal-detail"><label>Batch:</label> <span>${a.graduationYear}</span></div>
            
            <h3 style="border-bottom: 1px solid var(--clr-border); padding-bottom: 0.5rem; margin-top:2rem; margin-bottom:1rem;">Professional</h3>
            <div class="modal-detail"><label>Company:</label> <span>${a.currentCompany || 'N/A'}</span></div>
            <div class="modal-detail"><label>Role:</label> <span>${a.currentDesignation || 'N/A'}</span></div>
            <div class="modal-detail"><label>Experience:</label> <span>${a.yearsOfExperience} Years</span></div>
            <div class="modal-detail"><label>Skills:</label> <span>${a.skills || 'N/A'}</span></div>
        `;
        document.getElementById('profileModal').classList.add('show');
    } catch (e) { alert("Failed to load profile details."); }
}

function closeModal() {
    document.getElementById('profileModal').classList.remove('show');
}

// ==========================================
// 4. Admin Verification (STLV4-24)
// ==========================================
async function loadVerifications() {
    if(user.role !== 'ROLE_ADMIN') return;
    
    const list = document.getElementById('verificationList');
    list.innerHTML = '<div class="loader-container"><div class="loader"></div></div>';
    
    try {
        const res = await fetch(`${API_URL}/alumni/pending`, { headers: authHeaders() });
        const data = await res.json();
        
        list.innerHTML = '';
        if(data.data.content.length === 0) {
            list.innerHTML = '<p style="color:var(--clr-text-muted);">No pending verifications.</p>';
            return;
        }
        
        data.data.content.forEach(a => {
            const item = document.createElement('div');
            item.className = 'list-item';
            item.innerHTML = `
                <div class="list-item-info">
                    <h3>${a.firstName} ${a.lastName}</h3>
                    <p>${a.collegeName} | ${a.degree} - ${a.department} (${a.graduationYear})</p>
                    <p>Current: ${a.currentCompany || 'N/A'}</p>
                </div>
                <div class="list-item-actions">
                    <button class="btn btn-success btn-sm" onclick="processVerification(${a.id}, true)">Approve</button>
                    <button class="btn btn-danger btn-sm" onclick="processVerification(${a.id}, false)">Reject</button>
                </div>
            `;
            list.appendChild(item);
        });
    } catch (e) {
        list.innerHTML = '<p class="error-msg">Failed to load verifications</p>';
    }
}

async function processVerification(id, isApproved) {
    const remarks = isApproved ? "Profile verified successfully by admin." : prompt("Enter reason for rejection:");
    if(!isApproved && !remarks) return; // cancelled prompt
    
    try {
        const res = await fetch(`${API_URL}/alumni/${id}/verify`, {
            method: 'PUT',
            headers: authHeaders(),
            body: JSON.stringify({ approved: isApproved, remarks: remarks })
        });
        
        const data = await res.json();
        if(data.success || res.status === 200) {
            alert(isApproved ? "Profile Verified!" : "Profile Rejected");
            loadVerifications(); // refresh list
        }
    } catch (e) { alert("Action failed."); }
}

// ==========================================
// 5. Mentorship Requests (STLV4-82)
// ==========================================
async function loadMentorship() {
    if (isAlumni) {
        await loadReceivedMentorshipRequests();
        return;
    }
    if (!isStudent) return;
    const grid = document.getElementById('mentorGrid');
    const requests = document.getElementById('mentorshipRequests');
    grid.innerHTML = '<div class="loader-container"><div class="loader"></div></div>';
    try {
        const [mentorResponse, requestResponse] = await Promise.all([
            fetch(`${API_URL}/mentorship/mentors`, { headers: authHeaders() }),
            fetch(`${API_URL}/mentorship/requests/me`, { headers: authHeaders() })
        ]);
        const mentorData = await mentorResponse.json();
        const requestData = await requestResponse.json();
        grid.innerHTML = '';
        (mentorData.data || []).forEach(mentor => {
            const card = document.createElement('div');
            card.className = 'alumni-card glass-panel';
            card.innerHTML = `
                <h3>${mentor.firstName} ${mentor.lastName}</h3>
                <p class="dept">${mentor.currentDesignation || 'Alumni mentor'} @ ${mentor.currentCompany || 'Professional network'}</p>
                <p>${mentor.department} · ${mentor.graduationYear}</p>
                <button class="btn btn-primary btn-sm" onclick="openMentorshipRequest(${mentor.id})">Request mentorship</button>
            `;
            grid.appendChild(card);
        });
        if (!grid.children.length) {
            grid.innerHTML = '<p class="error-msg">No verified alumni mentors are available yet.</p>';
        }
        renderMentorshipRequests(requests, requestData.data || []);
    } catch (error) {
        grid.innerHTML = '<p class="error-msg">Unable to load mentors right now.</p>';
        requests.innerHTML = '';
    }
}

function renderMentorshipRequests(container, items) {
    container.innerHTML = items.length
        ? `<h3 class="subsection-title">My requests</h3>${items.map(item => `
            <div class="list-item">
                <div class="list-item-info">
                    <h3>${item.alumniName}</h3>
                    <p>${item.message}</p>
                    <small>Status: ${item.status}</small>
                </div>
            </div>`).join('')}`
        : '<p style="color:var(--clr-text-muted);">You have not sent a mentorship request yet.</p>';
}

function openMentorshipRequest(mentorId) {
    document.getElementById('selectedMentorId').value = mentorId;
    document.getElementById('mentorshipRequestPanel').classList.remove('hidden');
}

function closeMentorshipRequest() {
    document.getElementById('mentorshipRequestPanel').classList.add('hidden');
    document.getElementById('mentorshipRequestForm').reset();
}

document.getElementById('mentorshipRequestForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        const response = await fetch(`${API_URL}/mentorship/requests`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({
                alumniProfileId: Number(document.getElementById('selectedMentorId').value),
                message: document.getElementById('mentorshipMessageInput').value,
                goals: document.getElementById('mentorshipGoals').value
            })
        });
        const data = await response.json();
        showAlert('mentorshipMessage', data.success ? 'Mentorship request sent.' : data.message, !data.success);
        if (data.success) {
            closeMentorshipRequest();
            loadMentorship();
        }
    } catch (error) {
        showAlert('mentorshipMessage', 'Network error while sending the request.', true);
    }
});

async function loadReceivedMentorshipRequests() {
    if (!isAlumni) return;
    const container = document.getElementById('receivedMentorshipRequests');
    try {
        const response = await fetch(`${API_URL}/mentorship/requests/received`, { headers: authHeaders() });
        const data = await response.json();
        const items = data.data || [];
        container.innerHTML = `<h3 class="subsection-title">Requests from students</h3>` + (items.length
            ? items.map(item => `
                <div class="list-item">
                    <div class="list-item-info">
                        <h3>${item.studentName}</h3>
                        <p>${item.message}</p>
                        <small>Status: ${item.status}${item.goals ? ` · Goals: ${item.goals}` : ''}</small>
                    </div>
                    ${item.status === 'PENDING' ? `<div class="list-item-actions">
                        <button class="btn btn-primary btn-sm" onclick="decideMentorship(${item.id}, 'ACCEPTED')">Accept</button>
                        <button class="btn btn-outline btn-sm" onclick="decideMentorship(${item.id}, 'REJECTED')">Reject</button>
                    </div>` : ''}
                </div>`).join('')
            : '<p class="muted-text">No students have requested mentorship yet.</p>');
    } catch (error) {
        container.innerHTML = '<p class="error-msg">Unable to load received mentorship requests.</p>';
    }
}

async function decideMentorship(requestId, status) {
    const responseMessage = window.prompt('Optional response message:', '') || '';
    try {
        const response = await fetch(`${API_URL}/mentorship/requests/${requestId}`, {
            method: 'PUT',
            headers: authHeaders(),
            body: JSON.stringify({ status, responseMessage })
        });
        const data = await response.json();
        showAlert('mentorshipMessage', data.success ? `Request ${status.toLowerCase()}.` : data.message, !data.success);
        if (data.success) {
            loadReceivedMentorshipRequests();
            loadNotificationCount();
        }
    } catch (error) {
        showAlert('mentorshipMessage', 'Network error while updating the request.', true);
    }
}

// ==========================================
// 6. Alumni Events (STLV4-84)
// ==========================================
async function loadEvents() {
    if (!isStudent && !isAlumni) return;
    const grid = document.getElementById('eventGrid');
    const registrations = document.getElementById('eventRegistrations');
    grid.innerHTML = '<div class="loader-container"><div class="loader"></div></div>';
    try {
        const eventResponse = await fetch(`${API_URL}/events`, { headers: authHeaders() });
        const registrationResponse = isStudent
            ? await fetch(`${API_URL}/events/registrations/me`, { headers: authHeaders() })
            : null;
        const eventData = await eventResponse.json();
        const registrationData = registrationResponse ? await registrationResponse.json() : { data: [] };
        grid.innerHTML = '';
        (eventData.data || []).forEach(event => {
            const card = document.createElement('div');
            card.className = 'alumni-card glass-panel';
            const remaining = event.capacity - event.registeredCount;
            card.innerHTML = `
                <h3>${event.title}</h3>
                <p>${event.description}</p>
                <p class="dept">${new Date(event.eventDate).toLocaleString()} · ${event.mode}</p>
                <p>${event.location} · ${remaining} seats left</p>
                ${isStudent ? `<button class="btn btn-primary btn-sm" ${event.registered || remaining <= 0 ? 'disabled' : ''}
                    onclick="openEventRegistration(${event.id})">
                    ${event.registered ? 'Registered' : remaining <= 0 ? 'Full' : 'Register'}
                </button>` : ''}
            `;
            grid.appendChild(card);
        });
        if (!grid.children.length) {
            grid.innerHTML = '<p class="error-msg">No upcoming alumni events are available.</p>';
        }
        registrations.innerHTML = (registrationData.data || []).length
            ? `<h3 class="subsection-title">My event registrations</h3>${registrationData.data.map(item => `
                <div class="list-item"><div class="list-item-info">
                    <h3>${item.eventTitle}</h3><p>${item.attendanceMode} · ${item.currentSemester}</p>
                </div></div>`).join('')}`
            : '<p style="color:var(--clr-text-muted);">You have no event registrations yet.</p>';
    } catch (error) {
        grid.innerHTML = '<p class="error-msg">Unable to load alumni events right now.</p>';
        registrations.innerHTML = '';
    }
}

function openEventRegistration(eventId) {
    document.getElementById('selectedEventId').value = eventId;
    document.getElementById('eventRegistrationPanel').classList.remove('hidden');
}

function closeEventRegistration() {
    document.getElementById('eventRegistrationPanel').classList.add('hidden');
    document.getElementById('eventRegistrationForm').reset();
}

document.getElementById('eventRegistrationForm').addEventListener('submit', async event => {
    event.preventDefault();
    const eventId = document.getElementById('selectedEventId').value;
    try {
        const response = await fetch(`${API_URL}/events/${eventId}/registrations`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({
                attendanceMode: document.getElementById('eventAttendanceMode').value,
                currentSemester: document.getElementById('eventSemester').value,
                expectations: document.getElementById('eventExpectations').value,
                dietaryPreference: document.getElementById('eventDietaryPreference').value
            })
        });
        const data = await response.json();
        showAlert('eventsMessage', data.success ? 'Event registration completed.' : data.message, !data.success);
        if (data.success) {
            closeEventRegistration();
            loadEvents();
        }
    } catch (error) {
        showAlert('eventsMessage', 'Network error while registering for the event.', true);
    }
});

document.getElementById('eventCreationForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        const response = await fetch(`${API_URL}/events`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({
                title: document.getElementById('newEventTitle').value.trim(),
                description: document.getElementById('newEventDescription').value.trim(),
                eventDate: document.getElementById('newEventDate').value,
                location: document.getElementById('newEventLocation').value.trim(),
                mode: document.getElementById('newEventMode').value,
                capacity: Number(document.getElementById('newEventCapacity').value),
                registrationDeadline: document.getElementById('newEventDeadline').value
            })
        });
        const data = await response.json();
        showAlert('eventsMessage', data.success ? 'Event published and students notified.' : data.message, !data.success);
        if (data.success) {
            document.getElementById('eventCreationForm').reset();
            loadEvents();
        }
    } catch (error) {
        showAlert('eventsMessage', 'Network error while creating the event.', true);
    }
});

document.getElementById('certificateForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const file = document.getElementById('bonafideFile').files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
        const response = await fetch(`${API_URL}/alumni/me/bonafide`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });
        const data = await response.json();
        showAlert('certificateMessage', data.success ? 'Certificate uploaded successfully.' : data.message, !data.success);
        if (data.success) {
            document.getElementById('certificateForm').reset();
            loadBonafideStatus();
        }
    } catch (error) {
        showAlert('certificateMessage', 'Network error while uploading the certificate.', true);
    }
});

async function loadNotificationCount() {
    try {
        const response = await fetch(`${API_URL}/notifications/unread-count`, { headers: authHeaders() });
        const data = await response.json();
        const count = document.getElementById('notificationCount');
        count.textContent = data.data || 0;
        count.classList.toggle('hidden', !(data.data > 0));
    } catch (error) {
        console.error('Unable to load notification count', error);
    }
}

async function loadNotifications() {
    const list = document.getElementById('notificationList');
    list.innerHTML = '<div class="loader-container"><div class="loader"></div></div>';
    try {
        const response = await fetch(`${API_URL}/notifications`, { headers: authHeaders() });
        const data = await response.json();
        const notifications = data.data || [];
        list.innerHTML = notifications.length ? notifications.map(item => `
            <div class="list-item notification-item ${item.read ? '' : 'unread'}" onclick="markNotificationRead(${item.id})">
                <div class="list-item-info"><h3>${item.title}</h3><p>${item.message}</p><small>${new Date(item.createdAt).toLocaleString()}</small></div>
                ${item.read ? '' : '<span class="notification-dot"></span>'}
            </div>`).join('') : '<p class="muted-text">You are all caught up.</p>';
    } catch (error) {
        list.innerHTML = '<p class="error-msg">Unable to load notifications.</p>';
    }
}

async function markNotificationRead(notificationId) {
    await fetch(`${API_URL}/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: authHeaders()
    });
    loadNotifications();
    loadNotificationCount();
}

async function markAllNotificationsRead() {
    await fetch(`${API_URL}/notifications/read-all`, {
        method: 'PUT',
        headers: authHeaders()
    });
    loadNotifications();
    loadNotificationCount();
}

setInterval(loadNotificationCount, 30000);
