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
    
    const submitBtn = document.getElementById('submitBtn');
    const msgBox = document.getElementById('messageBox');
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = 'Registering...';
    
    const payload = {
        name: document.getElementById('name').value,
        studentId: document.getElementById('studentId').value,
        course: document.getElementById('course').value,
        semester: document.getElementById('semester').value,
        universityRollNo: document.getElementById('universityRollNo').value,
        section: document.getElementById('section').value,
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
            msgBox.className = 'md:col-span-2 p-4 rounded-xl text-sm font-medium text-center bg-teal-100 text-teal-600 border border-teal-200';
            msgBox.textContent = 'Registration successful! Redirecting to login...';
            msgBox.classList.remove('hidden');
            
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            throw new Error(data.message || 'Registration failed');
        }
    } catch (error) {
        msgBox.className = 'md:col-span-2 p-4 rounded-xl text-sm font-medium text-center bg-red-500/20 text-red-400 border border-red-500/30';
        msgBox.textContent = error.message;
        msgBox.classList.remove('hidden');
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Register Account';
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
            msgBox.textContent = 'Login successful! Redirecting...';
            msgBox.classList.remove('hidden');
            
            setTimeout(() => {
                window.location.href = 'dashboard.html';
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
 * Load dashboard data
 */
async function loadDashboard() {
    const studentDataStr = localStorage.getItem('studentData');
    if (!studentDataStr) {
        window.location.href = 'login.html';
        return;
    }

    const studentData = JSON.parse(studentDataStr);
    
    document.getElementById('dashName').textContent = studentData.name;
    document.getElementById('dashStudentId').textContent = studentData.studentId;
    document.getElementById('dashCourse').textContent = studentData.course;
    document.getElementById('dashSemester').textContent = 'Sem ' + studentData.semester;
    document.getElementById('displaySemTitle').textContent = studentData.semester;
    document.getElementById('dashRollNo').textContent = studentData.universityRollNo;
    document.getElementById('dashSection').textContent = 'Sec ' + studentData.section;

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
    document.getElementById('finalElectiveSem').textContent = studentData.semester;
    
    fetch(`${API_BASE_URL}/api/electives?semester=${studentData.semester}`)
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data) {
                const el = data.data.find(e => e.subjectName === subjectName);
                if (el) {
                    document.getElementById('finalElectiveTeacher').textContent = el.teacher || '---';
                    document.getElementById('finalElectiveCategory').textContent = el.category || '---';
                    document.getElementById('finalElectiveDiff').textContent = el.difficulty || '---';
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
        msg.className = "bg-gradient-to-br from-indigo-50 to-white border border-indigo-200 shadow-md p-6 rounded-2xl mb-8 mx-auto max-w-4xl shadow-lg";
        
        let analysisHtml = `<h3 class="text-2xl font-bold text-slate-800 mb-4 text-center">Your Analytical Priority Report</h3>`;
        analysisHtml += `<p class="text-slate-600 mb-6 text-center text-sm md:text-base">Based on your quiz performance, we've analytically ranked your dominant traits. The subjects below are prioritized accordingly.</p>`;
        
        analysisHtml += `<div class="space-y-4">`;
        currentAnalysis.forEach((cat, index) => {
            if (cat.score > 0) {
                analysisHtml += `
                    <div class="flex items-start gap-4 p-4 rounded-xl ${index === 0 ? 'bg-indigo-100 text-indigo-700 border border-indigo-200' : 'bg-slate-50/50 border border-slate-200'}">
                        <div class="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 font-bold flex items-center justify-center shrink-0 border border-indigo-200">
                            ${index + 1}
                        </div>
                        <div>
                            <h4 class="text-slate-800 font-bold text-lg mb-1">${cat.trait} <span class="text-xs text-slate-500 font-normal ml-2">(Score: ${cat.score})</span></h4>
                            <p class="text-slate-500 text-sm leading-relaxed">${cat.reason} ${index === 0 ? '<strong class="text-indigo-600">This trait defines your top priority.</strong>' : ''}</p>
                        </div>
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
        let cardClasses = 'bg-white border hover:border-indigo-400 transition-all p-6 rounded-2xl flex flex-col justify-between h-full relative ';
        cardClasses += isTopRecommend ? 'border-indigo-200 shadow-lg shadow-indigo-500/20' : 'border-slate-200';
        card.className = cardClasses;
        
        let badgeHtml = '';
        if (currentAnalysis) {
            if (isTopRecommend) {
                badgeHtml = `<span class="absolute -top-3 left-6 bg-indigo-500 text-slate-800 text-[10px] font-bold px-3 py-1 rounded-full shadow-lg border border-indigo-400">Priority #${idx + 1} • Top Match</span>`;
            } else {
                badgeHtml = `<span class="absolute -top-3 left-6 bg-slate-700 text-slate-700 text-[10px] font-bold px-3 py-1 rounded-full shadow-lg border border-slate-300">Priority #${idx + 1}</span>`;
            }
        }

        let reasonHtml = '';
        if (currentAnalysis) {
            reasonHtml = `
                <div class="bg-slate-50/50 border border-slate-200 rounded-lg p-3 mb-4">
                    <p class="text-xs text-slate-500 mb-1 font-semibold uppercase tracking-wider">Why Priority #${idx + 1}?</p>
                    <p class="text-xs text-slate-600 leading-relaxed">Matches your <span class="text-indigo-600 font-bold">${elective.mappedTrait}</span> trait. ${elective.reason}</p>
                </div>
            `;
        }

        card.innerHTML = `
            ${badgeHtml}
            <div>
                <div class="flex justify-between items-start mb-4 mt-2">
                    <div class="w-12 h-12 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-xl flex items-center justify-center text-indigo-600 border border-indigo-200">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
                    </div>
                    <label class="flex items-center gap-2 cursor-pointer bg-slate-50/50 px-3 py-1.5 rounded-lg border border-slate-200 hover:border-indigo-400 transition-colors">
                        <input type="checkbox" class="w-4 h-4 rounded text-indigo-500 focus:ring-indigo-500 focus:ring-offset-slate-900 bg-white border-slate-300" ${isChecked ? 'checked' : ''} onchange="toggleCompare('${elective.subjectName}', this.checked)">
                        <span class="text-xs font-semibold text-slate-600">Compare</span>
                    </label>
                </div>
                <h3 class="text-xl font-bold text-slate-800 mb-2">${elective.subjectName}</h3>
                <p class="text-slate-500 text-sm flex items-center gap-2 mb-3">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                    Prof. ${elective.teacher}
                </p>
                ${reasonHtml}
                <div class="mt-4 flex flex-wrap gap-2 mb-6">
                    <span class="bg-slate-100 text-slate-600 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide">${elective.category || 'N/A'}</span>
                    <span class="bg-slate-100 text-slate-600 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wide text-amber-600">${elective.difficulty || 'N/A'}</span>
                </div>
            </div>
            <button onclick="finalizeElective('${elective.subjectName}')" class="w-full py-3 bg-slate-50 hover:bg-emerald-500 border border-slate-200 hover:border-emerald-500 hover:text-white rounded-xl font-semibold transition-colors mt-auto shadow-lg hover:shadow-emerald-500/20 text-slate-800">
                Select as Final
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
                <label class="flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer hover:bg-white transition-colors">
                    <input type="radio" name="q${index}" value="${key}" class="w-4 h-4 text-indigo-500 bg-slate-50 border-slate-300 focus:ring-indigo-500">
                    <span class="text-slate-600 font-medium">${key}. ${val}</span>
                </label>
            `;
        }
        
        container.innerHTML += `
            <div class="mb-8 last:mb-0">
                <h4 class="text-lg font-bold text-slate-800 mb-3 flex gap-2">
                    <span class="text-indigo-600">Q${index + 1}.</span> ${item.q}
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 pl-6">
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
                card.className = 'bg-white border border-slate-200 shadow-sm p-5 rounded-2xl';
                const statusBadge = q.resolved 
                    ? `<span class="bg-teal-100 text-teal-600 text-xs font-bold px-2 py-1 rounded-md border border-teal-200">Answered</span>`
                    : `<span class="bg-amber-100 text-amber-600 text-xs font-bold px-2 py-1 rounded-md border border-amber-500/30">Pending</span>`;

                let answerSection = '';
                if (q.resolved && q.answer) {
                    answerSection = `
                        <div class="mt-4 p-4 bg-indigo-500/10 border border-indigo-200 rounded-xl">
                            <p class="text-xs text-indigo-600 font-bold uppercase tracking-wider mb-1">Admin's Answer</p>
                            <p class="text-slate-800">${q.answer}</p>
                        </div>
                    `;
                }

                card.innerHTML = `
                    <div class="flex justify-between items-start mb-2">
                        <p class="text-slate-500 text-xs">${new Date(q.createdAt).toLocaleDateString()}</p>
                        ${statusBadge}
                    </div>
                    <p class="text-lg font-medium text-slate-800 break-words">${q.question}</p>
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
