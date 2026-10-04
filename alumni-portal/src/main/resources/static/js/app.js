const API_URL = 'http://localhost:8080/api';

// Redirect if already logged in
if (localStorage.getItem('token')) {
    window.location.href = 'dashboard.html';
}

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(form => form.classList.remove('active'));
    
    if (tab === 'login') {
        document.querySelectorAll('.tab-btn')[0].classList.add('active');
        document.getElementById('loginForm').classList.add('active');
    } else {
        document.querySelectorAll('.tab-btn')[1].classList.add('active');
        document.getElementById('registerForm').classList.add('active');
    }
}

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const loader = btn.querySelector('.loader');
    const btnText = btn.querySelector('span');
    const errorDiv = document.getElementById('loginError');
    
    btnText.classList.add('hidden');
    loader.classList.remove('hidden');
    btn.disabled = true;
    errorDiv.textContent = '';

    const payload = {
        email: document.getElementById('loginEmail').value,
        password: document.getElementById('loginPassword').value
    };

    try {
        const res = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        const data = await res.json();
        if (data.success) {
            localStorage.setItem('token', data.data.accessToken);
            localStorage.setItem('user', JSON.stringify(data.data));
            window.location.href = 'dashboard.html';
        } else {
            errorDiv.textContent = data.message || 'Login failed';
        }
    } catch (err) {
        errorDiv.textContent = 'Network error. Make sure backend is running.';
    } finally {
        btnText.classList.remove('hidden');
        loader.classList.add('hidden');
        btn.disabled = false;
    }
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const loader = btn.querySelector('.loader');
    const btnText = btn.querySelector('span');
    const errorDiv = document.getElementById('regError');
    
    btnText.classList.add('hidden');
    loader.classList.remove('hidden');
    btn.disabled = true;
    errorDiv.textContent = '';

    const payload = {
        firstName: document.getElementById('regFirstName').value,
        lastName: document.getElementById('regLastName').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value,
        role: "ROLE_ALUMNI" // Default role
    };

    try {
        const res = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        const data = await res.json();
        if (data.success || res.status === 201) {
            localStorage.setItem('token', data.data.accessToken);
            localStorage.setItem('user', JSON.stringify(data.data));
            window.location.href = 'dashboard.html';
        } else {
            // Handle validation errors from backend
            if (data.data && typeof data.data === 'object') {
                errorDiv.innerHTML = Object.values(data.data).join('<br>');
            } else {
                errorDiv.textContent = data.message || 'Registration failed';
            }
        }
    } catch (err) {
        errorDiv.textContent = 'Network error. Make sure backend is running.';
    } finally {
        btnText.classList.remove('hidden');
        loader.classList.add('hidden');
        btn.disabled = false;
    }
});
