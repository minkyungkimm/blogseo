let trendChart   = null;
let allTitles    = [];   // 전체 복사용
let allHashtags  = [];   // 해시태그 전체 복사용

// ── 검색 진입점 ───────────────────────────────────────────────────────
function handleSearch() {
    const keyword = document.getElementById('keywordInput').value.trim();
    if (!keyword) { document.getElementById('keywordInput').focus(); return; }
    runAnalysis(keyword);
}

async function runAnalysis(keyword) {
    setLoading(true);
    hideError();
    hideResults();

    try {
        // 키워드 분석 API
        const res  = await fetch('/api/keyword/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ keyword })
        });
        const data = await res.json();
        if (!res.ok) { showError(data.message || '분석 중 오류가 발생했습니다.'); return; }

        renderAnalysis(data);
        showResults();

        // 해시태그 + 제목 추천 (병렬 호출)
        fetchHashtags(data.keyword);
        fetchTitles(data.keyword, data.relatedKeywords.map(k => k.keyword));

    } catch (e) {
        showError('서버 연결에 실패했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
        setLoading(false);
    }
}

async function fetchTitles(keyword, relatedKeywords) {
    showTitleSkeleton();
    try {
        const res  = await fetch('/api/title/recommend', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ keyword, relatedKeywords })
        });
        const data = await res.json();
        if (!res.ok) { renderTitlesError(); return; }
        renderTitles(data.titles);
    } catch (e) {
        renderTitlesError();
    }
}

// ── 분석 결과 렌더링 ─────────────────────────────────────────────────
function renderAnalysis(data) {
    renderSeoScore(data.seoScore);
    renderStatCards(data);
    renderTrendChart(data.trends);
    renderRelatedKeywords(data.relatedKeywords);
}

function renderSeoScore(s) {
    if (!s) return;
    document.getElementById('seoScoreValue').textContent = s.score;
    document.getElementById('seoScoreStars').textContent = stars(s.stars);

    document.getElementById('searchVolumeStars').textContent = stars(scoreToStars(s.searchVolumeScore));
    document.getElementById('searchVolumeLevel').textContent = s.searchVolumeLevel;

    document.getElementById('competitionStars').textContent = stars(scoreToStars(100 - s.competitionScore));
    document.getElementById('competitionLevel').textContent = s.competitionLevel;

    document.getElementById('recommendStars').textContent = stars(scoreToStars(s.recommendScore));
    document.getElementById('recommendLevel').textContent = s.recommendLevel;
}

function stars(n) {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
}

function scoreToStars(score) {
    if (score >= 80) return 5;
    if (score >= 60) return 4;
    if (score >= 40) return 3;
    if (score >= 20) return 2;
    return 1;
}

function renderStatCards(data) {
    document.getElementById('currentRatioValue').textContent = data.currentRatio.toFixed(1);
    const badge = document.getElementById('popularityBadge');
    badge.textContent  = data.popularityLevel;
    badge.className    = 'level-badge ' + levelClass(data.popularityLevel);

    document.getElementById('peakPeriodValue').textContent = data.peakPeriod;
    document.getElementById('peakRatioValue').textContent  = '인기도 ' + data.peakRatio.toFixed(1) + '점';

    const trendEl = document.getElementById('trendBadge');
    trendEl.textContent = data.trendSummary;
    trendEl.className   = 'trend-badge ' + trendClass(data.trendSummary);

    const descMap = { '상승세': '검색량이 증가하는 추세입니다', '하락세': '검색량이 감소하는 추세입니다', '안정적': '검색량이 안정적입니다' };
    document.getElementById('trendDesc').textContent = descMap[data.trendSummary] || '';
}

function renderTrendChart(trends) {
    if (trendChart) { trendChart.destroy(); trendChart = null; }
    const ctx = document.getElementById('trendChart').getContext('2d');
    trendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: trends.map(t => t.period),
            datasets: [{
                label: '검색 인기도',
                data: trends.map(t => t.ratio),
                borderColor: '#10a37f',
                backgroundColor: 'rgba(16,163,127,0.07)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#10a37f',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { min: 0, max: 100, grid: { color: '#f3f4f6' }, ticks: { color: '#9ca3af', font: { size: 11 } } },
                x: { grid: { display: false }, ticks: { color: '#9ca3af', font: { size: 11 } } }
            }
        }
    });
}

function renderRelatedKeywords(keywords) {
    const container = document.getElementById('relatedKeywordsList');
    if (!keywords || keywords.length === 0) {
        container.innerHTML = '<p class="no-data">연관 키워드 데이터가 없습니다.</p>';
        return;
    }
    const maxRatio = Math.max(...keywords.map(k => k.ratio), 1);
    container.innerHTML = keywords.map(k => `
        <div class="keyword-item">
            <span class="keyword-name">${k.keyword}</span>
            <div class="keyword-bar-wrap">
                <div class="keyword-bar" style="width:${(k.ratio / maxRatio * 100).toFixed(1)}%"></div>
            </div>
            <span class="keyword-ratio">${k.ratio.toFixed(1)}</span>
            <span class="keyword-level"><span class="level-badge ${levelClass(k.popularityLevel)}">${k.popularityLevel}</span></span>
        </div>
    `).join('');
}

// ── 해시태그 API + 렌더링 ────────────────────────────────────────────
async function fetchHashtags(keyword) {
    const area = document.getElementById('hashtagArea');
    area.innerHTML = '<div class="skeleton-row"></div><div class="skeleton-row"></div>';

    try {
        const res  = await fetch('/api/hashtag/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ keyword })
        });
        const data = await res.json();
        if (!res.ok) { area.innerHTML = '<p class="no-data">해시태그 생성 중 오류가 발생했습니다.</p>'; return; }
        renderHashtags(data.hashtags);
    } catch (e) {
        area.innerHTML = '<p class="no-data">해시태그 생성 중 오류가 발생했습니다.</p>';
    }
}

function renderHashtags(hashtags) {
    allHashtags = hashtags;
    const area  = document.getElementById('hashtagArea');

    if (!hashtags || hashtags.length === 0) {
        area.innerHTML = '<p class="no-data">해시태그 데이터가 없습니다.</p>';
        return;
    }

    area.innerHTML = hashtags.map(tag => `
        <span class="hashtag-tag" onclick="copyHashtag(this, '${escapeAttr(tag)}')">${tag}</span>
    `).join('');
}

function copyHashtag(el, tag) {
    navigator.clipboard.writeText(tag).then(() => {
        el.classList.add('copied');
        setTimeout(() => el.classList.remove('copied'), 1200);
    });
}

function copyHashtags() {
    if (!allHashtags.length) return;
    navigator.clipboard.writeText(allHashtags.join(' ')).then(() => {
        const btn = document.querySelector('.hashtag-box .copy-all-btn');
        btn.textContent = '복사 완료!';
        setTimeout(() => { btn.textContent = '전체 복사'; }, 1500);
    });
}

// ── 제목 렌더링 ───────────────────────────────────────────────────────
function renderTitles(titles) {
    allTitles = titles.map(t => t.title);
    const container = document.getElementById('titlesList');
    container.className = '';

    if (!titles || titles.length === 0) {
        container.innerHTML = '<p class="no-data">제목 데이터가 없습니다.</p>';
        return;
    }

    container.innerHTML = titles.map((t, i) => `
        <div class="title-item">
            <span class="title-rank ${i < 3 ? 'rank-top' : ''}">${i + 1}</span>
            <span class="title-text">${t.title}</span>
            <div class="title-meta">
                <span class="traffic-badge traffic-${t.trafficLevel}">예상유입 ${t.trafficLevel}</span>
                <span class="pattern-badge pattern-${t.pattern}">${t.pattern}</span>
                <button class="copy-btn" onclick="copyTitle(this, '${escapeAttr(t.title)}')">복사</button>
            </div>
        </div>
    `).join('');
}

function showTitleSkeleton() {
    const container = document.getElementById('titlesList');
    container.className = 'titles-loading';
    container.innerHTML = Array(5).fill('<div class="skeleton-row"></div>').join('');
}

function renderTitlesError() {
    document.getElementById('titlesList').innerHTML = '<p class="no-data">제목 생성 중 오류가 발생했습니다.</p>';
}

// ── 복사 기능 ─────────────────────────────────────────────────────────
function copyTitle(btn, title) {
    navigator.clipboard.writeText(title).then(() => {
        btn.textContent = '완료!';
        btn.classList.add('copied');
        setTimeout(() => { btn.textContent = '복사'; btn.classList.remove('copied'); }, 1500);
    });
}

function copyAllTitles() {
    if (!allTitles.length) return;
    const text = allTitles.map((t, i) => `${i + 1}. ${t}`).join('\n');
    navigator.clipboard.writeText(text).then(() => {
        const btn = document.querySelector('.copy-all-btn');
        btn.textContent = '복사 완료!';
        setTimeout(() => { btn.textContent = '전체 복사'; }, 1500);
    });
}

// ── UI 헬퍼 ──────────────────────────────────────────────────────────
function setLoading(on) {
    const btn = document.getElementById('searchButton');
    btn.disabled    = on;
    btn.textContent = on ? '분석 중...' : '분석하기';
}
function showResults()  { document.getElementById('resultsSection').style.display = 'flex'; }
function hideResults()  { document.getElementById('resultsSection').style.display = 'none'; }
function showError(msg) {
    document.getElementById('errorMessage').textContent = msg;
    document.getElementById('errorBox').style.display   = 'block';
}
function hideError() { document.getElementById('errorBox').style.display = 'none'; }

function fillKeyword(keyword) {
    document.getElementById('keywordInput').value = keyword;
    document.getElementById('keywordInput').focus();
}

function levelClass(level) {
    const map = { '매우 높음': 'level-very-high', '높음': 'level-high', '보통': 'level-medium', '낮음': 'level-low', '매우 낮음': 'level-very-low' };
    return map[level] || 'level-medium';
}
function trendClass(s) {
    if (s === '상승세') return 'trend-up';
    if (s === '하락세') return 'trend-down';
    return 'trend-stable';
}

// onclick 속성 내 따옴표 이스케이프
function escapeAttr(str) {
    return str.replace(/'/g, "\\'");
}

// Enter 키
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('keywordInput').addEventListener('keydown', e => {
        if (e.key === 'Enter') handleSearch();
    });
});
