/**
 * Dino English - 3D effects (three.js) - chạy trên MỌI trang (nạp từ footer)
 * - nền 3D toàn trang (khối chữ cái trôi nổi, trượt theo cuộn chuột)
 * - mascot Dino 3D góc phải màn hình (nhìn theo chuột, bấm để chào)
 * - thẻ .card-dino nghiêng 3D theo con trỏ
 * - #hero-3d : mascot khủng long 3D + khối chữ cái bay quanh + hạt sáng
 * - #cta-3d  : nền khối chữ cái 3D trôi nổi ở banner kêu gọi
 * Nếu trình duyệt không hỗ trợ WebGL, giữ nguyên ảnh logo 2D (fallback).
 */
import * as THREE from 'three';

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/* ---------- Helpers ---------- */

function createStage(container, { fov = 35, camPos = [0, 2.2, 9], lookAt = [0, 1.5, 0] } = {}) {
    let renderer;
    try {
        renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: 'low-power' });
    } catch (e) {
        return null; // không có WebGL -> dùng ảnh fallback
    }
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.domElement.classList.add('three-canvas');
    container.appendChild(renderer.domElement);

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(fov, 1, 0.1, 100);
    camera.position.set(...camPos);
    camera.lookAt(...lookAt);

    const resize = () => {
        const w = container.clientWidth || 1;
        const h = container.clientHeight || 1;
        renderer.setSize(w, h, false);
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
    };
    new ResizeObserver(resize).observe(container);
    resize();

    // Chỉ render khi đang nằm trong màn hình
    let visible = true;
    new IntersectionObserver(([e]) => { visible = e.isIntersecting; }).observe(container);

    const clock = new THREE.Clock();
    const updaters = [];
    const loop = () => {
        requestAnimationFrame(loop);
        if (!visible || document.hidden) { clock.getDelta(); return; }
        const dt = Math.min(clock.getDelta(), 0.05);
        const t = clock.elapsedTime;
        updaters.forEach(fn => fn(t, dt));
        renderer.render(scene, camera);
    };
    loop();

    return { renderer, scene, camera, onFrame: fn => updaters.push(fn) };
}

function letterTexture(text, bg, fg = '#ffffff') {
    const c = document.createElement('canvas');
    c.width = c.height = 128;
    const g = c.getContext('2d');
    g.fillStyle = bg;
    g.fillRect(0, 0, 128, 128);
    g.strokeStyle = 'rgba(255,255,255,.55)';
    g.lineWidth = 8;
    g.strokeRect(6, 6, 116, 116);
    g.fillStyle = fg;
    g.font = '800 ' + (text.length > 1 ? 62 : 84) + 'px "Baloo 2", "Nunito", sans-serif';
    g.textAlign = 'center';
    g.textBaseline = 'middle';
    g.fillText(text, 64, 70);
    const tex = new THREE.CanvasTexture(c);
    tex.colorSpace = THREE.SRGBColorSpace;
    tex.anisotropy = 4;
    return tex;
}

function letterBlock(text, bg, size = 0.8) {
    const mat = new THREE.MeshStandardMaterial({ map: letterTexture(text, bg), roughness: 0.45, metalness: 0.05 });
    const mesh = new THREE.Mesh(new THREE.BoxGeometry(size, size, size), mat);
    return mesh;
}

function addLights(scene) {
    scene.add(new THREE.HemisphereLight(0xffffff, 0xbbf7d0, 1.1));
    const key = new THREE.DirectionalLight(0xffffff, 1.9);
    key.position.set(4, 7, 6);
    scene.add(key);
    const rim = new THREE.PointLight(0xfde047, 12, 20);
    rim.position.set(-5, 3, -3);
    scene.add(rim);
}

function blobShadowTexture() {
    const c = document.createElement('canvas');
    c.width = c.height = 128;
    const g = c.getContext('2d');
    const grad = g.createRadialGradient(64, 64, 4, 64, 64, 62);
    grad.addColorStop(0, 'rgba(21,128,61,.40)');
    grad.addColorStop(1, 'rgba(21,128,61,0)');
    g.fillStyle = grad;
    g.fillRect(0, 0, 128, 128);
    return new THREE.CanvasTexture(c);
}

/* ---------- Dino 3D model (dựng từ hình khối cơ bản) ---------- */

function buildDino() {
    const green = new THREE.MeshStandardMaterial({ color: 0x22c55e, roughness: 0.55 });
    const dark = new THREE.MeshStandardMaterial({ color: 0x16a34a, roughness: 0.6 });
    const belly = new THREE.MeshStandardMaterial({ color: 0xdcfce7, roughness: 0.7 });
    const spike = new THREE.MeshStandardMaterial({ color: 0xfacc15, roughness: 0.5 });
    const white = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.3 });
    const black = new THREE.MeshStandardMaterial({ color: 0x0f172a, roughness: 0.2 });
    const pink = new THREE.MeshStandardMaterial({ color: 0xfda4af, roughness: 0.8 });

    const dino = new THREE.Group();
    const add = (geo, mat, pos, scale, parent = dino) => {
        const m = new THREE.Mesh(geo, mat);
        m.position.set(...pos);
        if (scale) m.scale.set(...scale);
        parent.add(m);
        return m;
    };
    const sphere = new THREE.SphereGeometry(1, 40, 28);

    // Thân + bụng
    add(sphere, green, [0, 1.1, 0], [1, 0.9, 1.25]);
    add(sphere, belly, [0, 0.95, 0.35], [0.78, 0.7, 1.0]);

    // Đầu + mõm
    const head = new THREE.Group();
    head.position.set(0, 2.2, 0.9);
    dino.add(head);
    add(sphere, green, [0, 0, 0], [0.85, 0.8, 0.85], head);
    add(sphere, green, [0, -0.15, 0.6], [0.55, 0.42, 0.6], head);
    // Mắt
    [-1, 1].forEach(s => {
        add(sphere, white, [s * 0.38, 0.28, 0.5], [0.22, 0.26, 0.22], head);
        add(sphere, black, [s * 0.38, 0.28, 0.68], [0.11, 0.14, 0.1], head);
        add(sphere, white, [s * 0.34, 0.34, 0.76], [0.035, 0.035, 0.035], head);
        add(sphere, pink, [s * 0.58, -0.05, 0.4], [0.14, 0.09, 0.07], head);
        add(sphere, dark, [s * 0.12, -0.02, 1.16], [0.045, 0.045, 0.045], head); // lỗ mũi
    });
    // Miệng cười
    const smile = new THREE.Mesh(new THREE.TorusGeometry(0.26, 0.035, 8, 24, Math.PI), black);
    smile.rotation.z = Math.PI;
    smile.position.set(0, -0.2, 1.12);
    head.add(smile);

    // Gai lưng
    for (let i = 0; i < 5; i++) {
        const z = 0.75 - i * 0.55;
        const y = 1.1 + 0.9 * Math.sqrt(Math.max(0, 1 - (z / 1.25) ** 2));
        const s = add(new THREE.ConeGeometry(0.24, 0.55, 14), spike, [0, y + 0.08, z]);
        s.rotation.x = -0.25 - i * 0.08;
        s.scale.setScalar(1 - i * 0.08);
    }

    // Đuôi (pivot để vẫy)
    const tail = new THREE.Group();
    tail.position.set(0, 0.95, -1.0);
    dino.add(tail);
    const tailMesh = add(new THREE.ConeGeometry(0.62, 2.3, 24), green, [0, 0.1, -1.1], null, tail);
    tailMesh.rotation.x = -Math.PI / 2 - 0.18;

    // Chân + bàn chân
    const legGeo = new THREE.CylinderGeometry(0.3, 0.34, 0.7, 18);
    [[-0.6, 0.45], [0.6, 0.45], [-0.6, -0.65], [0.6, -0.65]].forEach(([x, z]) => {
        add(legGeo, dark, [x, 0.38, z]);
        add(sphere, dark, [x, 0.08, z + 0.12], [0.36, 0.16, 0.46]);
    });
    // Tay nhỏ
    [-1, 1].forEach(s => {
        const arm = add(sphere, green, [s * 0.95, 1.2, 0.95], [0.16, 0.16, 0.34]);
        arm.rotation.y = s * -0.4;
    });

    // Cả model đứng quay mặt chếch nhẹ để thấy khối 3D
    return { dino, head, tail };
}

/* ---------- Hero scene ---------- */

function initHero() {
    const container = document.getElementById('hero-3d');
    if (!container) return;
    const stage = createStage(container);
    if (!stage) return;
    const { scene, camera, renderer, onFrame } = stage;

    addLights(scene);

    // Bóng đổ mềm dưới chân
    const shadow = new THREE.Mesh(
        new THREE.PlaneGeometry(5, 5),
        new THREE.MeshBasicMaterial({ map: blobShadowTexture(), transparent: true, depthWrite: false })
    );
    shadow.rotation.x = -Math.PI / 2;
    shadow.position.y = 0.01;
    scene.add(shadow);

    // Dino
    const { dino, head, tail } = buildDino();
    const rig = new THREE.Group();
    rig.add(dino);
    rig.rotation.y = -0.55;
    scene.add(rig);

    // Khối chữ cái bay quanh
    const defs = [['A', '#f97316'], ['B', '#3b82f6'], ['C', '#ec4899'], ['Hi', '#8b5cf6'], ['XP', '#eab308']];
    const orbit = new THREE.Group();
    scene.add(orbit);
    const blocks = defs.map(([txt, color], i) => {
        const b = letterBlock(txt, color, 0.78);
        b.userData = { a: (i / defs.length) * Math.PI * 2, r: 3.3 + (i % 2) * 0.4, y: 1.2 + (i % 3) * 0.75, spin: 0.4 + i * 0.12 };
        orbit.add(b);
        return b;
    });

    // Hạt sáng
    const N = 60;
    const pos = new Float32Array(N * 3);
    for (let i = 0; i < N; i++) {
        pos[i * 3] = (Math.random() - 0.5) * 9;
        pos[i * 3 + 1] = Math.random() * 5;
        pos[i * 3 + 2] = (Math.random() - 0.5) * 6;
    }
    const geo = new THREE.BufferGeometry();
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    const stars = new THREE.Points(geo, new THREE.PointsMaterial({ color: 0xfacc15, size: 0.09, transparent: true, opacity: 0.85 }));
    scene.add(stars);

    // Tương tác: dino nhìn theo con trỏ, bấm vào để nhảy + chào
    const target = { x: 0, y: 0 };
    container.addEventListener('pointermove', e => {
        const r = container.getBoundingClientRect();
        target.x = ((e.clientX - r.left) / r.width - 0.5) * 2;
        target.y = ((e.clientY - r.top) / r.height - 0.5) * 2;
    });
    container.addEventListener('pointerleave', () => { target.x = target.y = 0; });

    let jump = -1; // thời điểm bắt đầu nhảy
    const ray = new THREE.Raycaster();
    const ndc = new THREE.Vector2();
    renderer.domElement.addEventListener('click', e => {
        const r = renderer.domElement.getBoundingClientRect();
        ndc.set(((e.clientX - r.left) / r.width) * 2 - 1, -((e.clientY - r.top) / r.height) * 2 + 1);
        ray.setFromCamera(ndc, camera);
        if (ray.intersectObject(dino, true).length && jump < 0) {
            jump = 0;
            if (typeof window.speakWord === 'function') window.speakWord('Hello! I am Dino!');
        }
    });

    onFrame((t, dt) => {
        const m = reduceMotion ? 0 : 1;
        // Lơ lửng nhẹ + đầu xoay theo chuột
        const bob = Math.sin(t * 1.8) * 0.06 * m;
        let jumpY = 0, squash = 1;
        if (jump >= 0) {
            jump += dt;
            const p = jump / 0.8;
            if (p >= 1) jump = -1;
            else { jumpY = Math.sin(p * Math.PI) * 1.1; squash = 1 + Math.sin(p * Math.PI) * 0.06; }
        }
        rig.position.y = bob + jumpY;
        rig.scale.set(1 / Math.sqrt(squash), squash, 1 / Math.sqrt(squash));
        shadow.scale.setScalar(1 - jumpY * 0.25);

        rig.rotation.y += ((-0.55 + target.x * 0.6) - rig.rotation.y) * Math.min(1, dt * 4);
        head.rotation.y += ((target.x * 0.35) - head.rotation.y) * Math.min(1, dt * 5);
        head.rotation.x += ((target.y * 0.25) - head.rotation.x) * Math.min(1, dt * 5);
        tail.rotation.y = Math.sin(t * 3) * 0.35 * m;

        blocks.forEach(b => {
            const d = b.userData;
            const a = d.a + t * 0.45 * m;
            b.position.set(Math.cos(a) * d.r, d.y + Math.sin(t * 1.6 + d.a) * 0.15 * m, Math.sin(a) * d.r * 0.7);
            b.rotation.x = t * d.spin * m;
            b.rotation.y = t * d.spin * 1.3 * m;
        });
        stars.rotation.y = t * 0.05 * m;
    });

    container.classList.add('ready'); // ẩn ảnh fallback
}

/* ---------- CTA banner background ---------- */

function initCta() {
    const container = document.getElementById('cta-3d');
    if (!container) return;
    const stage = createStage(container, { fov: 40, camPos: [0, 0, 10], lookAt: [0, 0, 0] });
    if (!stage) return;
    const { scene, onFrame } = stage;
    scene.add(new THREE.HemisphereLight(0xffffff, 0x14532d, 1.4));
    const key = new THREE.DirectionalLight(0xffffff, 1.6);
    key.position.set(3, 5, 6);
    scene.add(key);

    const letters = 'ABCDEFGHKMNPRSTW'.split('');
    const colors = ['#fde047', '#ffffff', '#86efac', '#fdba74', '#93c5fd'];
    const items = letters.map((ch, i) => {
        const b = letterBlock(ch, colors[i % colors.length], 0.7 + Math.random() * 0.4);
        b.material.map = letterTexture(ch, '#15803d', colors[i % colors.length]);
        b.material.transparent = true;
        b.material.opacity = 0.55;
        b.position.set((Math.random() - 0.5) * 18, (Math.random() - 0.5) * 7, -Math.random() * 4);
        b.userData = { sp: 0.15 + Math.random() * 0.35, ph: Math.random() * 6.28, y0: b.position.y };
        scene.add(b);
        return b;
    });

    onFrame(t => {
        if (reduceMotion) return;
        items.forEach(b => {
            const d = b.userData;
            b.position.y = d.y0 + Math.sin(t * d.sp * 2 + d.ph) * 0.5;
            b.rotation.x = t * d.sp;
            b.rotation.y = t * d.sp * 1.4;
        });
    });
}


/* ---------- Nền 3D toàn trang ---------- */

function initBackground() {
    const container = document.createElement('div');
    container.id = 'bg-3d';
    container.setAttribute('aria-hidden', 'true');
    document.body.prepend(container);
    const stage = createStage(container, { fov: 50, camPos: [0, 0, 12], lookAt: [0, 0, 0] });
    if (!stage) { container.remove(); return; }
    const { scene, camera, onFrame } = stage;
    document.body.classList.add('has-3d-bg');

    scene.add(new THREE.HemisphereLight(0xffffff, 0xbbf7d0, 1.5));
    const key = new THREE.DirectionalLight(0xffffff, 1.4);
    key.position.set(4, 8, 8);
    scene.add(key);

    const small = window.innerWidth < 768;
    const COUNT = small ? 18 : 38;
    const SPAN = 30; // chiều dọc thế giới tương ứng toàn bộ chiều dài trang
    const palette = [0x22c55e, 0xfacc15, 0xfb923c, 0x60a5fa, 0xf472b6, 0xa78bfa];
    const letters = 'ABCDEFGHIKLMNOPRSTUWY'.split('');
    const geos = [
        new THREE.IcosahedronGeometry(0.45, 0),
        new THREE.TorusGeometry(0.4, 0.15, 12, 28),
        new THREE.ConeGeometry(0.4, 0.8, 5),
        new THREE.OctahedronGeometry(0.5, 0)
    ];

    const items = [];
    for (let i = 0; i < COUNT; i++) {
        let mesh;
        if (i % 3 === 0) {
            const ch = letters[Math.floor(Math.random() * letters.length)];
            const hex = '#' + palette[i % palette.length].toString(16).padStart(6, '0');
            mesh = letterBlock(ch, hex, 0.7 + Math.random() * 0.35);
        } else {
            mesh = new THREE.Mesh(geos[i % geos.length],
                new THREE.MeshStandardMaterial({ color: palette[i % palette.length], roughness: 0.4 }));
        }
        mesh.material.transparent = true;
        mesh.material.opacity = 0.5;
        // Ưu tiên hai bên mép để không che nội dung
        const side = Math.random() < 0.5 ? -1 : 1;
        const edge = Math.random() < 0.75;
        const x = edge ? side * (5 + Math.random() * 7) : (Math.random() - 0.5) * 14;
        const z = -Math.random() * 7;
        const y = 4 - Math.random() * SPAN;
        mesh.position.set(x, y, z);
        mesh.rotation.set(Math.random() * 3, Math.random() * 3, 0);
        mesh.userData = { y0: y, sp: 0.2 + Math.random() * 0.5, ph: Math.random() * 6.28 };
        scene.add(mesh);
        items.push(mesh);
    }

    const mouse = { x: 0, y: 0 };
    window.addEventListener('pointermove', e => {
        mouse.x = (e.clientX / window.innerWidth - 0.5) * 2;
        mouse.y = (e.clientY / window.innerHeight - 0.5) * 2;
    }, { passive: true });

    let scrollP = 0;
    onFrame((t, dt) => {
        const max = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
        const target = Math.min(1, Math.max(0, window.scrollY / max));
        scrollP += (target - scrollP) * Math.min(1, dt * 6);
        const m = reduceMotion ? 0 : 1;
        camera.position.y = 2 - scrollP * (SPAN - 4);
        camera.position.x += (mouse.x * 1.2 * m - camera.position.x) * Math.min(1, dt * 3);
        camera.lookAt(camera.position.x * 0.3, camera.position.y - 0.5 + mouse.y * -0.5 * m, 0);
        items.forEach(o => {
            const d = o.userData;
            o.position.y = d.y0 + Math.sin(t * d.sp + d.ph) * 0.35 * m;
            o.rotation.x += dt * d.sp * 0.6 * m;
            o.rotation.y += dt * d.sp * 0.9 * m;
        });
    });
}

/* ---------- Mascot Dino 3D cố định góc màn hình ---------- */

function initMascot() {
    if (document.getElementById('hero-3d')) return; // trang chủ đã có Dino lớn
    if (window.innerWidth < 576) return;
    const container = document.createElement('div');
    container.id = 'dino-mascot';
    container.title = 'Bấm vào Dino để nghe chào nhé!';
    document.body.appendChild(container);
    const stage = createStage(container, { fov: 35, camPos: [0, 2.2, 11], lookAt: [0, 1.5, 0] });
    if (!stage) { container.remove(); return; }
    const { scene, camera, renderer, onFrame } = stage;
    addLights(scene);

    const { dino, head, tail } = buildDino();
    const rig = new THREE.Group();
    rig.add(dino);
    rig.rotation.y = -0.5;
    scene.add(rig);

    const mouse = { x: 0, y: 0 };
    window.addEventListener('pointermove', e => {
        const r = container.getBoundingClientRect();
        const cx = r.left + r.width / 2, cy = r.top + r.height / 2;
        mouse.x = Math.max(-1, Math.min(1, (e.clientX - cx) / window.innerWidth * -2.4));
        mouse.y = Math.max(-1, Math.min(1, (e.clientY - cy) / window.innerHeight * 2));
    }, { passive: true });

    const greetings = ['Hello! I am Dino!', 'Keep going!', 'Great job!', 'Let us learn English!'];
    let jump = -1, gi = 0;
    const ray = new THREE.Raycaster();
    const ndc = new THREE.Vector2();
    renderer.domElement.addEventListener('click', e => {
        const r = renderer.domElement.getBoundingClientRect();
        ndc.set(((e.clientX - r.left) / r.width) * 2 - 1, -((e.clientY - r.top) / r.height) * 2 + 1);
        ray.setFromCamera(ndc, camera);
        if (ray.intersectObject(dino, true).length && jump < 0) {
            jump = 0;
            if (typeof window.speakWord === 'function') window.speakWord(greetings[gi++ % greetings.length]);
        }
    });

    onFrame((t, dt) => {
        const m = reduceMotion ? 0 : 1;
        let jy = 0;
        if (jump >= 0) {
            jump += dt;
            const p = jump / 0.8;
            if (p >= 1) jump = -1; else jy = Math.sin(p * Math.PI) * 1.0;
        }
        rig.position.y = Math.sin(t * 1.8) * 0.06 * m + jy;
        rig.rotation.y += ((-0.5 - mouse.x * 0.5) - rig.rotation.y) * Math.min(1, dt * 4);
        head.rotation.y += ((-mouse.x * 0.4) - head.rotation.y) * Math.min(1, dt * 5);
        head.rotation.x += ((mouse.y * 0.3) - head.rotation.x) * Math.min(1, dt * 5);
        tail.rotation.y = Math.sin(t * 3) * 0.35 * m;
    });
}

/* ---------- Thẻ nghiêng 3D (không cần WebGL) ---------- */

function initTilt() {
    if (reduceMotion || !window.matchMedia('(hover: hover)').matches) return;
    document.querySelectorAll('.card-dino').forEach(card => {
        if (card.querySelector('form, input, select, textarea')) return; // không nghiêng form
        card.classList.add('tilt-3d');
        card.addEventListener('pointermove', e => {
            const r = card.getBoundingClientRect();
            const px = (e.clientX - r.left) / r.width - 0.5;
            const py = (e.clientY - r.top) / r.height - 0.5;
            card.style.transform = 'perspective(900px) rotateX(' + (-py * 9).toFixed(2) + 'deg) rotateY(' + (px * 11).toFixed(2) + 'deg) translateY(-6px)';
            card.style.setProperty('--gx', (px + 0.5) * 100 + '%');
            card.style.setProperty('--gy', (py + 0.5) * 100 + '%');
        });
        card.addEventListener('pointerleave', () => { card.style.transform = ''; });
    });
}

initTilt();
initBackground();
initMascot();
initHero();
initCta();
