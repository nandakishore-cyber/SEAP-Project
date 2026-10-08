const API_URL = 'http://localhost:8080/api';
const pageRole = document.body.dataset.role;

if (localStorage.getItem('token')) {
    window.location.href = 'dashboard.html';
}

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(button => {
        button.classList.toggle('active', button.dataset.tab === tab);
    });
    document.querySelectorAll('.auth-form').forEach(form => {
        form.classList.toggle('active', form.id === `${tab}Form`);
    });
}

document.querySelectorAll('.tab-btn').forEach(button => {
    button.addEventListener('click', () => switchTab(button.dataset.tab));
});

function setLoading(form, loading) {
    const button = form.querySelector('button[type="submit"]');
    button.disabled = loading;
    button.querySelector('span').classList.toggle('hidden', loading);
    button.querySelector('.loader').classList.toggle('hidden', !loading);
}

async function readResponse(response) {
    try {
        return await response.json();
    } catch (error) {
        return { success: false, message: 'The server returned an invalid response.' };
    }
}

function showValidationError(errorDiv, data, fallback) {
    if (data.data && typeof data.data === 'object') {
        errorDiv.textContent = Object.values(data.data).join(' ');
    } else {
        errorDiv.textContent = data.message || fallback;
    }
}

async function authenticate(url, payload, form, errorDiv, fallback, redirect = true) {
    setLoading(form, true);
    errorDiv.textContent = '';

    try {
        const response = await fetch(`${API_URL}${url}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await readResponse(response);

        if (!response.ok || !data.success || !data.data) {
            showValidationError(errorDiv, data, fallback);
            return;
        }

        localStorage.setItem('token', data.data.accessToken);
        localStorage.setItem('user', JSON.stringify(data.data));
        if (redirect) {
            window.location.href = 'dashboard.html';
        }
        return data;
    } catch (error) {
        errorDiv.textContent = 'Network error. Make sure the backend is running.';
    } finally {
        setLoading(form, false);
    }
}

function registrationDetails() {
    if (pageRole === 'ROLE_STUDENT') {
        return {
            endpoint: '/profiles',
            payload: {
                studentId: document.getElementById('regStudentId').value.trim(),
                program: document.getElementById('regProgram').value.trim(),
                department: document.getElementById('regDepartment').value.trim(),
                expectedGraduationYear: parseInt(document.getElementById('regGraduationYear').value),
                interests: document.getElementById('regInterests').value.trim(),
                studentSkills: document.getElementById('regSkills').value.trim()
            }
        };
    }

    return {
        endpoint: '/alumni',
        payload: {
            collegeName: document.getElementById('regCollege').value.trim(),
            department: document.getElementById('regDepartment').value.trim(),
            degree: document.getElementById('regDegree').value.trim(),
            graduationYear: parseInt(document.getElementById('regGraduationYear').value),
            currentCompany: document.getElementById('regCompany').value.trim(),
            currentDesignation: document.getElementById('regDesignation').value.trim(),
            yearsOfExperience: parseInt(document.getElementById('regExperience').value) || 0,
            skills: document.getElementById('regSkills').value.trim()
        }
    };
}

async function createRoleDetails(token) {
    const details = registrationDetails();
    const response = await fetch(`${API_URL}${details.endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(details.payload)
    });
    const data = await readResponse(response);
    if (!response.ok || !data.success) {
        throw new Error(data.message || 'Your account was created, but the profile details could not be saved.');
    }
}

function showRegistrationSuccess(email) {
    switchTab('login');
    document.getElementById('loginEmail').value = email;
    const message = document.getElementById('loginError');
    message.textContent = 'Account created successfully. Sign in to open your dashboard.';
    message.className = 'success-msg';
}

document.getElementById('loginForm').addEventListener('submit', event => {
    event.preventDefault();
    authenticate('/auth/login', {
        email: document.getElementById('loginEmail').value.trim(),
        password: document.getElementById('loginPassword').value,
        role: pageRole
    }, event.target, document.getElementById('loginError'), 'Login failed');
});

document.getElementById('registerForm').addEventListener('submit', event => {
    event.preventDefault();
    const email = document.getElementById('regEmail').value.trim();
    const form = event.target;
    const errorDiv = document.getElementById('regError');
    authenticate('/auth/register', {
        firstName: document.getElementById('regFirstName').value.trim(),
        lastName: document.getElementById('regLastName').value.trim(),
        email,
        password: document.getElementById('regPassword').value,
        role: pageRole
    }, form, errorDiv, 'Registration failed', false).then(async data => {
        if (!data || !data.data) return;
        try {
            await createRoleDetails(data.data.accessToken);
            localStorage.clear();
            showRegistrationSuccess(email);
        } catch (error) {
            localStorage.clear();
            errorDiv.textContent = error.message;
        }
    });
});
