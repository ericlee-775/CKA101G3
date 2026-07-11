/* Farmily 管理後台 · 前端互動（純前端，不影響後端邏輯） */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {

        /* 1) 側邊欄目前頁高亮：依網址開頭比對 data-path */
        var path = window.location.pathname;
        var links = document.querySelectorAll('.admin-sidebar .nav-link[data-path]');
        var best = null, bestLen = -1;
        links.forEach(function (a) {
            var p = a.getAttribute('data-path');
            if (path === p || path.indexOf(p) === 0) {
                if (p.length > bestLen) { best = a; bestLen = p.length; }
            }
        });
        if (best) best.classList.add('active');

        /* 2) 窄螢幕側邊欄開關 */
        var toggle = document.getElementById('sidebarToggle');
        var sidebar = document.getElementById('adminSidebar');
        if (toggle && sidebar) {
            toggle.addEventListener('click', function () { sidebar.classList.toggle('open'); });
        }

        /* 3) 提示 toast 5 秒後淡出 */
        document.querySelectorAll('.app-toast').forEach(function (t) {
            setTimeout(function () {
                t.style.transition = 'opacity .4s, transform .4s';
                t.style.opacity = '0';
                t.style.transform = 'translateX(16px)';
                setTimeout(function () { t.remove(); }, 400);
            }, 5000);
        });

        /* 4) 證明文件圖片點擊放大 */
        var lightbox = document.getElementById('lightbox');
        if (lightbox) {
            var lbImg = lightbox.querySelector('img');
            document.querySelectorAll('.cert-img').forEach(function (img) {
                img.addEventListener('click', function () {
                    lbImg.src = img.src;
                    lightbox.classList.add('open');
                });
            });
            lightbox.addEventListener('click', function () { lightbox.classList.remove('open'); });
        }
    });
})();