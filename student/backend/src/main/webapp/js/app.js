// Use an empty string so the browser resolves to the current origin (e.g. Render domain)
const API_BASE_URL = '';

let globalElectives = [];
let selectedForCompare = [];
let currentAnalysis = null;

/**
 * Handle student registration
 */
async function handleRegister(e) {
    e.preventDefault();
    
    const submitBtn = document.querySelector('button[type="submit"]');
    const msgBox = document.querySelector('.msg-box') || document.createElement('div');
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = 'Creating Account...';
    
    const payload = {
        name: document.getElementById('studentName').value,
        studentId: document.getElementById('studentId').value,
        password: document.getElementById('password').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            msgBox.className = 'p-4 rounded-xl text-sm font-medium text-center bg-teal-100 text-teal-600 border border-teal-200';
            msgBox.textContent = 'Account created! Redirecting to login...';
            msgBox.classList.remove('hidden');
            
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            throw new Error(data.message || 'Registration failed');
        }
    } catch (error) {
        msgBox.className = 'p-4 rounded-xl text-sm font-medium text-center bg-red-500/20 text-red-400 border border-red-500/30';
        msgBox.textContent = error.message;
        msgBox.classList.remove('hidden');
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Create Account';
    }
}

/**
 * Handle student login
 */
async function handleLogin(e) {
    e.preventDefault();
    
    const submitBtn = document.getElementById('loginBtn');
    const msgBox = document.getElementById('loginMessageBox');
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = 'Signing In...';

    const payload = {
        studentId: document.getElementById('loginStudentId').value,
        password: document.getElementById('loginPassword').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            localStorage.setItem('studentData', JSON.stringify(data.data));
            
            msgBox.className = 'p-4 rounded-xl text-sm font-medium text-center bg-teal-100 text-teal-600 border border-teal-200';
            msgBox.textContent = 'Login successful! Verifying profile...';
            msgBox.classList.remove('hidden');
            
            setTimeout(() => {
                // Check if profile is incomplete (e.g. college is missing)
                if (!data.data.college || !data.data.branch) {
                    window.location.href = 'setup-profile.html';
                } else {
                    window.location.href = 'dashboard.html';
                }
            }, 1000);
        } else {
            throw new Error(data.message || 'Login failed');
        }
    } catch (error) {
        msgBox.className = 'p-4 rounded-xl text-sm font-medium text-center bg-red-500/20 text-red-400 border border-red-500/30';
        msgBox.textContent = error.message;
        msgBox.classList.remove('hidden');
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Sign In';
    }
}

/**
 * Handle Profile Update (Comprehensive)
 */
async function handleUpdateProfile(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('saveProfileBtn');
    const msgBox = document.getElementById('setupMessageBox');
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = 'Syncing Identity...';

    const studentData = JSON.parse(localStorage.getItem('studentData'));
    const payload = {
        studentId: studentData.studentId,
        fatherName: document.getElementById('fatherName').value,
        motherName: document.getElementById('motherName').value,
        dob: document.getElementById('dob').value,
        personalEmail: document.getElementById('personalEmail').value,
        officialEmail: document.getElementById('officialEmail').value,
        phone: document.getElementById('phone').value,
        college: document.getElementById('college').value,
        course: document.getElementById('course').value,
        branch: document.getElementById('branch').value,
        specialization: document.getElementById('specialization').value,
        semester: document.getElementById('semester').value,
        section: document.getElementById('section').value,
        classRollNo: document.getElementById('classRollNo').value,
        enrollNo: document.getElementById('enrollNo').value,
        universityRollNo: document.getElementById('universityRollNo').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/update-profile`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (response.ok && data.success) {
            localStorage.setItem('studentData', JSON.stringify(data.data));
            msgBox.className = 'lg:col-span-3 p-4 rounded-xl text-sm font-medium text-center bg-teal-100 text-teal-600 border border-teal-200';
            msgBox.textContent = 'Identity Synced! Launching Dashboard...';
            msgBox.classList.remove('hidden');
            setTimeout(() => { window.location.href = 'dashboard.html'; }, 1500);
        } else {
            throw new Error(data.message || 'Update failed');
        }
    } catch (error) {
        msgBox.className = 'lg:col-span-3 p-4 rounded-xl text-sm font-medium text-center bg-red-500/20 text-red-400 border border-red-500/30';
        msgBox.textContent = error.message;
        msgBox.classList.remove('hidden');
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Finalize Clinical Identity';
    }
}

/**
 * Load dashboard data
 */
async function loadDashboard() {
    const studentDataStr = localStorage.getItem('studentData');
    if (!studentDataStr) {
        window.location.href = 'login.html';
        return;
    }

    const studentData = JSON.parse(studentDataStr);
    
    // Redirect if profile still incomplete
    if (!studentData.college || !studentData.fatherName) {
        window.location.href = 'setup-profile.html';
        return;
    }

    document.getElementById('dashName').textContent = 'Welcome, ' + (studentData.name || 'Scholar');
    document.getElementById('displaySemTitle').textContent = studentData.semester;

    // Render Vertical Matrix
    const matrix = document.getElementById('profileMatrix');
    const fields = [
        { label: "Father Name", key: "fatherName" },
        { label: "Mother Name", key: "motherName" },
        { label: "D.O.B.", key: "dob" },
        { label: "Personal Email", key: "personalEmail" },
        { label: "Official Email", key: "officialEmail" },
        { label: "Phone", key: "phone" },
        { label: "College", key: "college" },
        { label: "Course", key: "course" },
        { label: "Branch", key: "branch" },
        { label: "Specialization", key: "specialization" },
        { label: "Year/Sem", key: "semester", format: (v) => v ? 'Sem ' + v : '---' },
        { label: "Section", key: "section" },
        { label: "Class Roll No.", key: "classRollNo" },
        { label: "Enroll No.", key: "enrollNo" },
        { label: "University Roll No.", key: "universityRollNo" }
    ];

    matrix.innerHTML = fields.map(f => `
        <div class="flex items-center justify-between py-5 border-b border-indigo-50/50 hover:bg-white transition-colors px-4 rounded-xl group relative overflow-hidden">
            <div class="absolute left-0 top-0 w-1 h-full bg-indigo-500 scale-y-0 group-hover:scale-y-100 transition-transform origin-center"></div>
            <span class="text-xs font-black text-slate-600 uppercase tracking-widest">${f.label}</span>
            <span class="text-sm font-black text-slate-900 tracking-tight group-hover:text-indigo-600 transition-colors">
                ${f.format ? f.format(studentData[f.key]) : (studentData[f.key] || '---')}
            </span>
        </div>
    `).join('');

    // View State Management
    if (studentData.selectedElective && studentData.selectedElective.trim() !== '') {
        document.getElementById('preSelectionView').classList.add('hidden');
        document.getElementById('compareStickyBar').classList.add('hidden');
        renderPostSelectionView(studentData.selectedElective);
    } else {
        document.getElementById('postSelectionView').classList.add('hidden');
        // Fetch Electives
        try {
            const response = await fetch(`${API_BASE_URL}/api/electives?semester=${studentData.semester}`);
            const data = await response.json();
            
            document.getElementById('electivesLoader').classList.add('hidden');

            if (response.ok && data.success && data.data && data.data.length > 0) {
                globalElectives = data.data;
                renderElectivesGrid();
            } else {
                document.getElementById('noElectivesMessage').classList.remove('hidden');
            }
        } catch (error) {
            document.getElementById('electivesLoader').classList.add('hidden');
            console.error("Error fetching electives:", error);
        }
    }

    await loadQueries(studentData);
}

function renderPostSelectionView(subjectName) {
    document.getElementById('postSelectionView').classList.remove('hidden');
    document.getElementById('finalElectiveName').textContent = subjectName;

    const studentData = JSON.parse(localStorage.getItem('studentData'));
    
    fetch(`${API_BASE_URL}/api/electives?semester=${studentData.semester}`)
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data) {
                const el = data.data.find(e => e.subjectName === subjectName);
                if (el) {
                    document.getElementById('finalElectiveTeacher').textContent = el.teacher || '---';
                    document.getElementById('finalElectiveCategory').textContent = el.category || '---';
                    
                    // Add Subject ID and Syllabus to post selection view if elements exist
                    const idSpan = document.createElement('span');
                    idSpan.className = 'text-xs text-indigo-400 font-black block mt-4 uppercase tracking-[0.2em] opacity-60';
                    idSpan.textContent = `Registry ID: ${el.subjectId || 'N/A'}`;
                    document.getElementById('finalElectiveName').parentNode.insertBefore(idSpan, document.getElementById('finalElectiveName').nextSibling);

                    if (el.syllabus) {
                        const sylBtn = document.createElement('button');
                        sylBtn.className = 'mt-10 bg-white/5 hover:bg-white/10 text-white text-xs font-black py-4 px-8 rounded-2xl flex items-center gap-3 transition-all border border-white/10 group';
                        sylBtn.innerHTML = `<svg class="w-5 h-5 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path></svg> Repository Syllabus`;
                        sylBtn.onclick = () => downloadPDF(el.syllabus, el.subjectName);
                        document.getElementById('finalElectiveName').parentNode.appendChild(sylBtn);
                    }
                }
            }
        });
}

function getElectiveTrait(elective) {
    const cat = (elective.category || '').toLowerCase();
    const name = (elective.subjectName || '').toLowerCase();

    if (cat.includes('program') || name.includes('machine learning') || name.includes('data science') || name.includes('artificial intelligence') || name.includes('c++') || name.includes('java')) return 'Programming';
    if (cat.includes('develop') || name.includes('web') || name.includes('app') || name.includes('software')) return 'Development';
    if (cat.includes('manage') || name.includes('project') || name.includes('business') || name.includes('mba') || name.includes('cloud')) return 'Management';
    if (cat.includes('research') || cat.includes('security') || name.includes('cyber') || name.includes('blockchain') || name.includes('data analysis')) return 'Research';
    
    const mapping = {
        'Programming': ['Machine Learning', 'Data Science', 'Artificial Intelligence', 'Programming'],
        'Development': ['Web Development', 'App Development', 'Software Engineering', 'Development'],
        'Management': ['Project Management', 'Business Analytics', 'MBA Basics', 'Management', 'Cloud'],
        'Research': ['Cyber Security', 'Blockchain', 'Data Analysis', 'Data Analysis with R', 'Research']
    };
    
    for (const [trait, subjects] of Object.entries(mapping)) {
        if (subjects.find(s => s.toLowerCase() === name || s.toLowerCase() === cat)) {
            return trait;
        }
    }

    return 'Programming';
}

function renderElectivesGrid() {
    const grid = document.getElementById('electivesGrid');
    grid.innerHTML = '';
    grid.classList.remove('hidden');

    const msgBox = document.getElementById('analysisMsgBox');
    if (msgBox) msgBox.remove();
    
    let electivesToRender = [...globalElectives];

    if (currentAnalysis) {
        const msg = document.createElement('div');
        msg.id = 'analysisMsgBox';
        msg.className = "glass-card p-10 rounded-[3rem] mb-12 relative overflow-hidden bg-gradient-to-br from-indigo-500/10 to-transparent";
        
        let analysisHtml = `<h3 class="text-3xl font-black text-white mb-6 text-center tracking-tight">Analytical Priority Report</h3>`;
        analysisHtml += `<p class="text-slate-400 mb-10 text-center font-medium">Synthesized based on your categorical responses and behavioral quiz results.</p>`;
        
        analysisHtml += `<div class="grid grid-cols-1 md:grid-cols-2 gap-6">`;
        currentAnalysis.forEach((cat, index) => {
            if (cat.score > 0) {
                analysisHtml += `
                    <div class="p-6 rounded-3xl ${index === 0 ? 'bg-indigo-500/10 border border-indigo-500/20' : 'bg-slate-50 border border-slate-100'}">
                        <div class="flex items-center gap-4 mb-3">
                            <div class="w-8 h-8 rounded-full bg-indigo-500/20 text-indigo-400 font-black flex items-center justify-center text-xs border border-indigo-500/30">
                                ${index + 1}
                            </div>
                            <h4 class="text-indigo-950 font-extrabold text-lg tracking-tight">${cat.trait}</h4>
                        </div>
                        <p class="text-slate-500 text-sm leading-relaxed font-medium">${cat.reason}</p>
                    </div>
                `;
            }
        });
        analysisHtml += `</div>`;
        msg.innerHTML = analysisHtml;
        grid.parentNode.insertBefore(msg, grid);

        electivesToRender = electivesToRender.map(elective => {
            const catName = getElectiveTrait(elective);
            const traitDetail = currentAnalysis.find(c => c.trait === catName);
            const score = traitDetail ? traitDetail.score : 0;
            const rank = currentAnalysis.findIndex(c => c.trait === catName) + 1;
            return { ...elective, priorityScore: score, mappedTrait: catName, rank: rank, reason: traitDetail ? traitDetail.reason : '' };
        });

        electivesToRender.sort((a, b) => b.priorityScore - a.priorityScore);
    }
    
    electivesToRender.forEach((elective, idx) => {
        const isTopRecommend = currentAnalysis ? elective.rank === 1 : false;
        const isChecked = selectedForCompare.includes(elective.subjectName);

        const card = document.createElement('div');
        let cardClasses = 'glass-card p-8 rounded-[2.5rem] flex flex-col h-full relative group ';
        if (isTopRecommend) cardClasses += 'border-indigo-500/50 bg-indigo-500/5 shadow-[0_20px_40px_rgba(99,102,241,0.1)]';
        card.className = cardClasses;
        
        let badgeHtml = '';
        if (currentAnalysis) {
            badgeHtml = `<span class="absolute -top-3 left-8 ${isTopRecommend ? 'bg-indigo-500' : 'bg-slate-800'} text-white text-[10px] font-black px-4 py-1.5 rounded-full shadow-xl z-10 tracking-widest uppercase italic">Ranked #${idx + 1}</span>`;
        }

        let reasonHtml = '';
        if (currentAnalysis) {
            reasonHtml = `
                <div class="bg-indigo-500/5 border border-indigo-500/20 rounded-2xl p-4 mb-6">
                    <p class="text-[9px] text-indigo-500 mb-1 font-black uppercase tracking-[0.2em]">Match Logic</p>
                    <p class="text-xs text-slate-500 leading-relaxed font-medium">Aligned with <span class="text-indigo-900 font-bold">${elective.mappedTrait}</span> path.</p>
                </div>
            `;
        }

        card.innerHTML = `
            ${badgeHtml}
            <div class="flex justify-between items-start mb-8">
                <div class="w-14 h-14 bg-indigo-50 border border-indigo-100 rounded-2xl flex items-center justify-center text-indigo-600 group-hover:scale-110 transition-transform duration-500">
                    <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
                </div>
                <label class="flex items-center gap-3 cursor-pointer bg-slate-50 px-4 py-2 rounded-xl border border-slate-200 hover:border-indigo-500/50 transition-all">
                    <input type="checkbox" class="w-4 h-4 rounded-lg text-indigo-500 bg-white/5 border-white/10 focus:ring-indigo-500 focus:ring-offset-0" ${isChecked ? 'checked' : ''} onchange="toggleCompare('${elective.subjectName}', this.checked)">
                    <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Compare</span>
                </label>
            </div>

            <div class="mb-4">
                <h3 class="text-2xl font-black text-indigo-950 tracking-tight mb-1">${elective.subjectName}</h3>
                <p class="text-[10px] text-indigo-400 font-black uppercase tracking-[0.2em] mb-4">${elective.subjectId || 'REGO-N/A'}</p>
                <div class="flex items-center gap-2 text-slate-400 text-sm font-semibold mb-6">
                    <div class="w-6 h-6 rounded-full bg-white/10 flex items-center justify-center">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                    </div>
                    Prof. <span class="text-slate-700">${elective.teacher}</span>
                </div>
            </div>

            ${reasonHtml}

            <div class="flex flex-wrap gap-3 mb-10">
                <span class="px-3 py-1 rounded-lg bg-orange-50 border border-orange-100 text-[9px] font-black uppercase tracking-widest text-orange-600">${elective.difficulty || 'N/A'}</span>
                ${elective.syllabus ? `<button onclick="downloadPDF('${elective.syllabus}', '${elective.subjectName}')" class="px-3 py-1 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-[9px] font-black uppercase tracking-widest text-indigo-400 flex items-center gap-2 hover:bg-indigo-500 hover:text-white transition-all">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path></svg>
                    PDF
                </button>` : ''}
            </div>

            <button onclick="finalizeElective('${elective.subjectName}')" class="w-full py-4 rounded-2xl bg-indigo-50 hover:bg-indigo-600 text-indigo-600 hover:text-white font-black text-sm uppercase tracking-widest border border-indigo-100 transition-all mt-auto group">
                Finalize Registry
            </button>
        `;
        
        grid.appendChild(card);
    });
}


// ----------------- COMPARE LOGIC -----------------

function toggleCompare(subjectName, isChecked) {
    if (isChecked) {
        if (selectedForCompare.length >= 3) {
            alert("You can only compare up to 3 electives at a time.");
            renderElectivesGrid();
            return;
        }
        if (!selectedForCompare.includes(subjectName)) {
            selectedForCompare.push(subjectName);
        }
    } else {
        selectedForCompare = selectedForCompare.filter(s => s !== subjectName);
    }
    
    updateStickyBar();
}

function updateStickyBar() {
    const bar = document.getElementById('compareStickyBar');
    const displayCount = document.getElementById('compareCount');
    const compareBtn = document.getElementById('compareBtn');
    
    displayCount.textContent = selectedForCompare.length;
    
    if (selectedForCompare.length >= 2) {
        bar.classList.remove('translate-y-full');
        compareBtn.disabled = false;
        compareBtn.className = "bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-2 rounded-lg font-bold transition-colors shadow-lg shadow-indigo-200";
    } else if (selectedForCompare.length > 0) {
        bar.classList.remove('translate-y-full');
        compareBtn.disabled = true;
        compareBtn.className = "bg-slate-700 text-slate-500 px-6 py-2 rounded-lg font-bold cursor-not-allowed";
        compareBtn.innerText = "Select at least 2";
    } else {
        bar.classList.add('translate-y-full');
    }
}

function clearComparison() {
    selectedForCompare = [];
    updateStickyBar();
    renderElectivesGrid();
}

function openCompareModal() {
    if (selectedForCompare.length < 2) return;
    
    const modal = document.getElementById('compareModal');
    const grid = document.getElementById('compareGrid');
    grid.innerHTML = '';
    
    // Set grid columns based on count
    grid.className = `grid gap-6 grid-cols-1 md:grid-cols-${selectedForCompare.length}`;
    
    selectedForCompare.forEach(subjectName => {
        const el = globalElectives.find(e => e.subjectName === subjectName);
        if (!el) return;
        
        const col = document.createElement('div');
        col.className = 'bg-white/90 shadow-sm backdrop-blur-md border border-slate-200 p-6 rounded-2xl flex flex-col';
        col.innerHTML = `
            <h4 class="text-xl font-bold text-slate-800 mb-4 border-b border-slate-200 pb-3">${el.subjectName}</h4>
            
            <div class="space-y-4 mb-8 flex-grow">
                <div>
                    <span class="text-xs text-slate-500 font-bold uppercase tracking-wider block mb-1">Category</span>
                    <span class="text-indigo-600 font-medium">${el.category || 'N/A'}</span>
                </div>
                <div>
                    <span class="text-xs text-slate-500 font-bold uppercase tracking-wider block mb-1">Difficulty</span>
                    <span class="text-amber-600 font-medium">${el.difficulty || 'N/A'}</span>
                </div>
                <div>
                    <span class="text-xs text-slate-500 font-bold uppercase tracking-wider block mb-1">Skills</span>
                    <span class="text-slate-700">${el.skills || 'N/A'}</span>
                </div>
                <div>
                    <span class="text-xs text-slate-500 font-bold uppercase tracking-wider block mb-1">Career Scope</span>
                    <span class="text-teal-600 font-medium">${el.scope || 'N/A'}</span>
                </div>
            </div>
            
            <button onclick="finalizeElective('${el.subjectName}')" class="w-full py-3 bg-emerald-600 hover:bg-emerald-500 rounded-xl font-bold text-white transition-colors mt-auto shadow-lg shadow-emerald-500/20">
                Finalize ${el.subjectName}
            </button>
        `;
        grid.appendChild(col);
    });
    
    modal.classList.remove('hidden');
}

function closeCompareModal() {
    document.getElementById('compareModal').classList.add('hidden');
}

// ----------------- QUIZ LOGIC -----------------

const quizQuestions = [
    { q: "Do you enjoy coding?", options: { A: "Yes", B: "Sometimes", C: "No" } },
    { q: "Which subject do you like most?", options: { A: "Programming", B: "Business", C: "Theory", D: "Logical" } },
    { q: "Your career goal?", options: { A: "Software Engineer", B: "Manager", C: "Researcher", D: "Developer" } },
    { q: "How comfortable are you with math?", options: { A: "High", B: "Medium", C: "Low" } },
    { q: "Do you like solving problems?", options: { A: "Yes", B: "Sometimes", C: "No" } },
    { q: "What type of work do you prefer?", options: { A: "Coding", B: "Managing", C: "Studying" } },
    { q: "Do you like building projects?", options: { A: "Yes", B: "Sometimes", C: "No" } },
    { q: "Which skill do you want?", options: { A: "Technical", B: "Leadership", C: "Research" } },
    { q: "Preferred work environment?", options: { A: "Tech company", B: "Business firm", C: "Lab/Research" } },
    { q: "How do you handle challenges?", options: { A: "Solve logically", B: "Manage team", C: "Analyze deeply" } }
];

function openQuizModal() {
    const modal = document.getElementById('quizModal');
    const container = document.getElementById('quizContainer');
    container.innerHTML = '';
    
    quizQuestions.forEach((item, index) => {
        let optionsHtml = '';
        for (const [key, val] of Object.entries(item.options)) {
            optionsHtml += `
                <label class="flex items-center gap-4 p-5 rounded-2xl border border-slate-200 bg-slate-50 hover:border-indigo-500/50 hover:bg-indigo-50 cursor-pointer transition-all group">
                    <input type="radio" name="q${index}" value="${key}" class="w-5 h-5 text-indigo-500 bg-white border-slate-300 focus:ring-indigo-500 focus:ring-offset-0">
                    <span class="text-slate-600 group-hover:text-indigo-700 font-bold text-sm tracking-tight">${val}</span>
                </label>
            `;
        }
        
        container.innerHTML += `
            <div class="mb-12 last:mb-0">
                <h4 class="text-xl font-black text-slate-900 mb-6 flex gap-4">
                    <span class="text-indigo-600">Q${index + 1}.</span> ${item.q}
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    ${optionsHtml}
                </div>
            </div>
        `;
    });
    
    modal.classList.remove('hidden');
}

function closeQuizModal() {
    document.getElementById('quizModal').classList.add('hidden');
}

function calculateQuizResults() {
    let scores = { programmingScore: 0, managementScore: 0, researchScore: 0, developmentScore: 0 };
    
    for (let i = 0; i < quizQuestions.length; i++) {
        const selected = document.querySelector(`input[name="q${i}"]:checked`);
        if (!selected) {
            alert("Please answer all questions to get the best recommendation!");
            return;
        }
        const ans = selected.value;
        
        if (ans === 'A') {
            scores.programmingScore += 2;
            scores.developmentScore += 1;
        } else if (ans === 'B') {
            scores.managementScore += 2;
            scores.developmentScore += 1;
        } else if (ans === 'C') {
            scores.researchScore += 2;
        } else if (ans === 'D') {
            scores.developmentScore += 2;
            scores.programmingScore += 1;
        }
    }
    
    let categoryDetails = [
        {
            trait: 'Programming',
            score: scores.programmingScore,
            reason: 'Your answers heavily favor coding, logical problem-solving, and building technical foundations.'
        },
        {
            trait: 'Development',
            score: scores.developmentScore,
            reason: 'You showed a keen interest in building projects, practical applications, and software engineering.'
        },
        {
            trait: 'Management',
            score: scores.managementScore,
            reason: 'You prefer taking leadership roles, focusing on business goals, and managing teams.'
        },
        {
            trait: 'Research',
            score: scores.researchScore,
            reason: 'Your choices indicate a preference for theoretical depth, exploration, and specialized analytical fields.'
        }
    ];

    // Sort descending by score
    categoryDetails.sort((a, b) => b.score - a.score);
    
    closeQuizModal();
    
    // Store globally to use inside renderElectivesGrid
    currentAnalysis = categoryDetails;
    
    // Scroll and show results
    window.scrollTo({ top: document.getElementById('preSelectionView').offsetTop - 50, behavior: 'smooth' });
    renderElectivesGrid();
}

// ----------------- FINAL SELECTION LOGIC -----------------

async function finalizeElective(subjectName) {
    if (!confirm(`Are you absolutely sure you want to finalize "${subjectName}"? This action cannot be easily undone.`)) return;
    
    const studentDataStr = localStorage.getItem('studentData');
    if (!studentDataStr) return;
    const studentData = JSON.parse(studentDataStr);

    try {
        const response = await fetch(`${API_BASE_URL}/api/electives/select`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId: studentData.studentId, subjectName: subjectName })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            studentData.selectedElective = subjectName;
            localStorage.setItem('studentData', JSON.stringify(studentData));
            
            closeCompareModal();
            document.getElementById('preSelectionView').classList.add('hidden');
            document.getElementById('compareStickyBar').classList.add('hidden');
            
            renderPostSelectionView(subjectName);
            window.scrollTo({ top: 0, behavior: 'smooth' });
            
            alert("Elective finalized successfully! Your dashboard has been updated.");
        } else {
            throw new Error(data.message || 'Failed to finalize elective.');
        }
    } catch (error) {
        alert(error.message);
    }
}

// ----------------- QUERIES LOGIC -----------------

async function submitQuery() {
    const studentDataStr = localStorage.getItem('studentData');
    if (!studentDataStr) return;
    const studentData = JSON.parse(studentDataStr);
    
    const queryText = document.getElementById('queryText').value.trim();
    const submitBtn = document.getElementById('submitQueryBtn');

    if (!queryText) {
        showQueryMsg('Please enter a question.', 'error');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = 'Submitting...';

    const payload = {
        studentId: studentData.studentId,
        studentName: studentData.name,
        semester: studentData.semester,
        question: queryText
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/queries`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (response.ok && data.success) {
            showQueryMsg('Question submitted successfully!', 'success');
            document.getElementById('queryText').value = '';
            loadQueries(studentData);
        } else {
            throw new Error(data.message || 'Failed to submit question.');
        }
    } catch (error) {
        showQueryMsg(error.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Submit Question';
    }
}

function showQueryMsg(message, type) {
    const msgBox = document.getElementById('queryMsg');
    msgBox.classList.remove('hidden');
    msgBox.className = type === 'success' 
        ? 'mt-4 p-3 rounded-lg text-sm font-medium bg-teal-100 text-teal-600 border border-teal-200'
        : 'mt-4 p-3 rounded-lg text-sm font-medium bg-red-500/20 text-red-400 border border-red-500/30';
    msgBox.textContent = message;
    setTimeout(() => { msgBox.classList.add('hidden'); }, 5000);
}

async function loadQueries(studentData) {
    const loader = document.getElementById('queriesLoader');
    const grid = document.getElementById('queriesGrid');
    const noMsg = document.getElementById('noQueriesMsg');
    
    loader.classList.remove('hidden');
    grid.innerHTML = '';
    noMsg.classList.add('hidden');

    try {
        const response = await fetch(`${API_BASE_URL}/api/queries?studentId=${studentData.studentId}`);
        const data = await response.json();
        loader.classList.add('hidden');

        if (response.ok && data.success && data.data && data.data.length > 0) {
            data.data.forEach(q => {
                const card = document.createElement('div');
                card.className = 'glass-card p-6 rounded-[2rem] border border-white/5 bg-white/2';
                const statusBadge = q.resolved 
                    ? `<span class="bg-teal-500/10 text-teal-400 text-[10px] font-black px-3 py-1 rounded-full border border-teal-500/20 uppercase tracking-widest">Answered</span>`
                    : `<span class="bg-amber-500/10 text-amber-400 text-[10px] font-black px-3 py-1 rounded-full border border-amber-500/20 uppercase tracking-widest">Pending Review</span>`;

                let answerSection = '';
                if (q.resolved && q.answer) {
                    answerSection = `
                        <div class="mt-6 p-6 bg-indigo-500/10 border border-indigo-500/20 rounded-2xl relative overflow-hidden">
                            <div class="absolute top-0 left-0 w-1 h-full bg-indigo-500"></div>
                            <p class="text-[10px] text-indigo-400 font-black uppercase tracking-[0.2em] mb-3">Administrator Verdict</p>
                            <p class="text-white font-medium leading-relaxed">${q.answer}</p>
                        </div>
                    `;
                }

                card.innerHTML = `
                    <div class="flex justify-between items-start mb-4">
                        <p class="text-slate-500 text-[10px] font-bold uppercase tracking-widest">${new Date(q.createdAt).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })}</p>
                        ${statusBadge}
                    </div>
                    <p class="text-lg font-extrabold text-white tracking-tight break-words">${q.question}</p>
                    ${answerSection}
                `;
                grid.appendChild(card);
            });
        } else {
            noMsg.classList.remove('hidden');
        }
    } catch (error) {
        loader.classList.add('hidden');
        console.error("Error fetching queries:", error);
    }
}

function logout() {
    localStorage.removeItem('studentData');
    window.location.href = 'login.html';
}

/**
 * Handle Base64 PDF download
 */
function downloadPDF(base64Data, filename) {
    try {
        const linkSource = `data:application/pdf;base64,${base64Data}`;
        const downloadLink = document.createElement("a");
        const fileName = `${filename.replace(/ /g, "_")}_Syllabus.pdf`;
        downloadLink.href = linkSource;
        downloadLink.download = fileName;
        downloadLink.click();
    } catch (e) {
        console.error("PDF Download failed", e);
        alert("Failed to download PDF. The file might be corrupted or too large.");
    }
}
