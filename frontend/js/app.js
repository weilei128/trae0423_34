let currentUser = null;
let cart = {};
let allDishes = [];
let categories = [];
let currentCategoryId = 'all';
let selectedRechargeAmount = 100;

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function formatDateDisplay(dateStr) {
    const date = new Date(dateStr);
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekDay = weekDays[date.getDay()];
    return `${month}月${day}日 ${weekDay}`;
}

function showToast(message, duration = 2000) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.add('active');
    setTimeout(() => {
        toast.classList.remove('active');
    }, duration);
}

function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(pageId).classList.add('active');
}

function showLogin() {
    showPage('login-page');
}

function showRegister() {
    showPage('register-page');
}

async function handleLogin() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    
    if (!username || !password) {
        showToast('请输入用户名和密码');
        return;
    }
    
    showToast('登录中...');
    
    const result = await api.login(username, password);
    
    if (result.code === 200) {
        currentUser = result.data;
        localStorage.setItem('token', result.data.token);
        localStorage.setItem('userInfo', JSON.stringify(result.data));
        showToast('登录成功');
        setTimeout(() => {
            initMainPage();
        }, 500);
    } else {
        showToast(result.message || '登录失败');
    }
}

async function handleRegister() {
    const userData = {
        username: document.getElementById('reg-username').value.trim(),
        password: document.getElementById('reg-password').value,
        name: document.getElementById('reg-name').value.trim(),
        studentNo: document.getElementById('reg-student-no').value.trim(),
        role: document.getElementById('reg-role').value
    };
    
    if (!userData.username || !userData.password || !userData.name) {
        showToast('请填写完整信息');
        return;
    }
    
    const result = await api.register(userData);
    
    if (result.code === 200) {
        showToast('注册成功，请登录');
        setTimeout(() => {
            showLogin();
        }, 1000);
    } else {
        showToast(result.message || '注册失败');
    }
}

function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    currentUser = null;
    cart = {};
    showLogin();
    showToast('已退出登录');
}

async function initMainPage() {
    const savedUser = localStorage.getItem('userInfo');
    if (!savedUser) {
        showLogin();
        return;
    }
    
    currentUser = JSON.parse(savedUser);
    
    updateUserInfo();
    setTomorrowDate();
    showPage('main-page');
    
    await loadMenu();
    updateCartUI();
    
    if (currentUser.role === 'admin') {
        document.getElementById('admin-menu').style.display = 'flex';
        document.getElementById('report-menu').style.display = 'flex';
    }
}

function updateUserInfo() {
    document.getElementById('header-name').textContent = currentUser.name;
    document.getElementById('header-role').textContent = currentUser.role === 'student' ? '学生' : (currentUser.role === 'teacher' ? '教职工' : '管理员');
    document.getElementById('header-balance').textContent = currentUser.balance || '0.00';
    
    document.getElementById('profile-name').textContent = currentUser.name;
    document.getElementById('profile-no').textContent = `学号/工号：${currentUser.studentNo || '--'}`;
}

function setTomorrowDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    document.getElementById('tomorrow-date').textContent = formatDateDisplay(formatDate(tomorrow));
}

function switchTab(tabName) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
    
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(`${tabName}-section`).classList.add('active');
    
    if (tabName === 'orders') {
        loadOrders();
    }
}

async function loadMenu() {
    const result = await api.getMenu();
    
    if (result.code === 200) {
        categories = result.data.categories;
        renderCategories();
        
        allDishes = [];
        categories.forEach(category => {
            const categoryDishes = result.data[`category_${category.id}`] || [];
            categoryDishes.forEach(dish => {
                dish.categoryName = category.name;
                allDishes.push(dish);
            });
        });
        
        renderDishes(allDishes);
    }
}

function renderCategories() {
    const container = document.getElementById('category-tabs');
    let html = '<div class="category-tab active" onclick="selectCategory(\'all\')">全部</div>';
    
    categories.forEach(category => {
        html += `<div class="category-tab" onclick="selectCategory(${category.id})">${category.name}</div>`;
    });
    
    container.innerHTML = html;
}

function selectCategory(categoryId) {
    currentCategoryId = categoryId;
    
    document.querySelectorAll('.category-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
    
    let dishes = allDishes;
    if (categoryId !== 'all') {
        dishes = allDishes.filter(dish => dish.categoryId === categoryId);
    }
    
    renderDishes(dishes);
}

function renderDishes(dishes) {
    const container = document.getElementById('dish-list');
    
    if (dishes.length === 0) {
        container.innerHTML = '<div style="text-align:center;padding:50px;color:#999;">暂无菜品</div>';
        return;
    }
    
    let html = '';
    dishes.forEach(dish => {
        const inCart = cart[dish.id] || 0;
        const isSoldOut = dish.status === 0 || dish.stock <= 0;
        
        html += `
            <div class="dish-item ${isSoldOut ? 'sold-out' : ''}">
                <img class="dish-image" src="${dish.image}" alt="${dish.name}">
                <div class="dish-info">
                    <div>
                        <div class="dish-name">${dish.name}</div>
                        <div class="dish-desc">${dish.description || ''}</div>
                        <div class="dish-stock ${dish.stock <= 10 ? 'low' : ''}">库存：${dish.stock}份</div>
                    </div>
                    <div class="dish-bottom">
                        <div class="dish-price">¥${dish.price}</div>
                        ${isSoldOut ? '<div class="sold-out-tag">已售罄</div>' : `
                            <div class="quantity-control">
                                ${inCart > 0 ? `<button class="quantity-btn" onclick="updateQuantity(${dish.id}, -1)">-</button>` : ''}
                                ${inCart > 0 ? `<span class="quantity-value">${inCart}</span>` : ''}
                                <button class="quantity-btn" onclick="updateQuantity(${dish.id}, 1)">+</button>
                            </div>
                        `}
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function updateQuantity(dishId, delta) {
    const dish = allDishes.find(d => d.id === dishId);
    if (!dish) return;
    
    const current = cart[dishId] || 0;
    const newQuantity = current + delta;
    
    if (newQuantity < 0) return;
    if (newQuantity > dish.stock) {
        showToast('库存不足');
        return;
    }
    
    if (newQuantity === 0) {
        delete cart[dishId];
    } else {
        cart[dishId] = newQuantity;
    }
    
    updateCartUI();
    renderDishes(currentCategoryId === 'all' ? allDishes : allDishes.filter(d => d.categoryId === currentCategoryId));
}

function updateCartUI() {
    const items = Object.entries(cart);
    const totalCount = items.reduce((sum, [id, qty]) => sum + qty, 0);
    let totalAmount = 0;
    
    items.forEach(([id, qty]) => {
        const dish = allDishes.find(d => d.id == id);
        if (dish) {
            totalAmount += dish.price * qty;
        }
    });
    
    document.getElementById('cart-count').textContent = totalCount;
    document.getElementById('cart-total').textContent = totalAmount.toFixed(2);
    document.getElementById('cart-item-count').textContent = items.length;
    
    const checkoutBtn = document.getElementById('checkout-btn');
    checkoutBtn.disabled = totalCount === 0;
}

function toggleCart() {
    const popup = document.getElementById('cart-popup');
    
    if (popup.classList.contains('active')) {
        popup.classList.remove('active');
    } else {
        renderCartItems();
        popup.classList.add('active');
    }
}

function renderCartItems() {
    const container = document.getElementById('cart-items');
    const items = Object.entries(cart);
    
    if (items.length === 0) {
        container.innerHTML = '<div style="text-align:center;padding:30px;color:#999;">购物车是空的</div>';
        return;
    }
    
    let html = '';
    items.forEach(([id, qty]) => {
        const dish = allDishes.find(d => d.id == id);
        if (dish) {
            html += `
                <div class="cart-item">
                    <div class="cart-item-left">
                        <div class="cart-item-name">${dish.name}</div>
                        <div class="cart-item-price">¥${dish.price} × ${qty}</div>
                    </div>
                    <div class="quantity-control">
                        <button class="quantity-btn" onclick="updateQuantity(${dish.id}, -1);renderCartItems()">-</button>
                        <span class="quantity-value">${qty}</span>
                        <button class="quantity-btn" onclick="updateQuantity(${dish.id}, 1);renderCartItems()">+</button>
                    </div>
                </div>
            `;
        }
    });
    
    container.innerHTML = html;
}

function clearCart() {
    cart = {};
    updateCartUI();
    toggleCart();
    renderDishes(currentCategoryId === 'all' ? allDishes : allDishes.filter(d => d.categoryId === currentCategoryId));
}

async function goCheckout() {
    if (Object.keys(cart).length === 0) {
        showToast('请选择菜品');
        return;
    }
    
    const dishes = Object.entries(cart).map(([id, qty]) => ({
        dishId: parseInt(id),
        quantity: qty
    }));
    
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    const orderData = {
        orderDate: formatDate(tomorrow),
        remark: '',
        dishes: dishes
    };
    
    showToast('正在下单...');
    const result = await api.createOrder(orderData);
    
    if (result.code === 200) {
        showToast('下单成功！取餐码：' + result.data.verifyCode);
        cart = {};
        updateCartUI();
        loadMenu();
        setTimeout(() => {
            switchTab('orders');
            loadOrders();
        }, 1500);
    } else {
        showToast(result.message || '下单失败');
    }
}

async function loadOrders() {
    const result = await api.getMyOrders();
    const container = document.getElementById('order-list');
    
    if (result.code !== 200 || !result.data) {
        container.innerHTML = '<div style="text-align:center;padding:50px;color:#999;">暂无订单</div>';
        return;
    }
    
    const orders = result.data;
    
    if (orders.length === 0) {
        container.innerHTML = '<div style="text-align:center;padding:50px;color:#999;">暂无订单</div>';
        return;
    }
    
    let html = '';
    orders.forEach(order => {
        const statusMap = {
            'pending': { text: '待取餐', class: 'pending' },
            'finished': { text: '已完成', class: 'finished' },
            'refunded': { text: '已退款', class: 'refunded' },
            'cancelled': { text: '已取消', class: 'refunded' }
        };
        
        const status = statusMap[order.status] || { text: order.status, class: '' };
        
        html += `
            <div class="order-item">
                <div class="order-header">
                    <span class="order-no">订单号：${order.orderNo}</span>
                    <span class="order-status ${status.class}">${status.text}</span>
                </div>
                <div class="order-date-info">订餐日期：${formatDateDisplay(order.orderDate)}</div>
                <div class="order-items" id="order-items-${order.id}">
                    <div style="font-size:12px;color:#999;">加载中...</div>
                </div>
                ${order.status === 'pending' ? `
                    <div class="verify-code">
                        <span class="verify-code-label">取餐码：</span>
                        <span class="verify-code-value">${order.verifyCode}</span>
                    </div>
                ` : ''}
                <div class="order-footer">
                    <div class="order-total">合计：<span>¥${order.totalAmount}</span></div>
                    <div class="order-actions">
                        ${order.status === 'pending' ? `
                            <button class="btn-small btn-outline" onclick="cancelOrder(${order.id})">退餐</button>
                        ` : ''}
                        <button class="btn-small btn-primary-small" onclick="viewOrderDetail(${order.id})">详情</button>
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
    
    orders.forEach(order => {
        loadOrderItems(order.id);
    });
}

async function loadOrderItems(orderId) {
    const result = await api.getOrderDetail(orderId);
    const container = document.getElementById(`order-items-${orderId}`);
    
    if (result.code === 200 && result.data) {
        const items = result.data.items;
        let html = '';
        items.forEach(item => {
            html += `
                <div class="order-item-row">
                    <span class="order-item-name">${item.dishName} × ${item.quantity}</span>
                    <span class="order-item-price">¥${item.subtotal}</span>
                </div>
            `;
        });
        container.innerHTML = html;
    }
}

async function cancelOrder(orderId) {
    if (!confirm('确定要退餐吗？')) {
        return;
    }
    
    const result = await api.cancelOrder(orderId);
    
    if (result.code === 200) {
        showToast(result.data || '退餐成功');
        loadOrders();
    } else {
        showToast(result.message || '退餐失败');
    }
}

async function viewOrderDetail(orderId) {
    alert('查看订单详情功能');
}

function selectDate(item) {
    document.querySelectorAll('.date-item').forEach(i => i.classList.remove('active'));
    item.classList.add('active');
}

function showRecharge() {
    document.getElementById('recharge-popup').classList.add('active');
}

function selectAmount(item, amount) {
    document.querySelectorAll('.amount-item').forEach(i => i.classList.remove('active'));
    item.classList.add('active');
    selectedRechargeAmount = amount;
    document.getElementById('custom-amount').value = '';
}

function onCustomAmount() {
    const input = document.getElementById('custom-amount');
    if (input.value) {
        document.querySelectorAll('.amount-item').forEach(i => i.classList.remove('active'));
        selectedRechargeAmount = parseFloat(input.value) || 0;
    }
}

async function handleRecharge() {
    if (selectedRechargeAmount <= 0) {
        showToast('请选择或输入充值金额');
        return;
    }
    
    showToast('充值中...');
    const result = await api.recharge(selectedRechargeAmount);
    
    if (result.code === 200) {
        currentUser.balance = result.data.balance;
        localStorage.setItem('userInfo', JSON.stringify(currentUser));
        updateUserInfo();
        closePopup('recharge-popup');
        showToast('充值成功');
    } else {
        showToast(result.message || '充值失败');
    }
}

function showVerify() {
    document.getElementById('verify-popup').classList.add('active');
}

async function handleVerify() {
    const code = document.getElementById('verify-code').value.trim();
    
    if (code.length !== 6) {
        showToast('请输入6位取餐码');
        return;
    }
    
    showToast('核销中...');
    const result = await api.verifyOrder(code);
    
    if (result.code === 200) {
        closePopup('verify-popup');
        document.getElementById('verify-code').value = '';
        showToast('核销成功！');
    } else {
        showToast(result.message || '核销失败');
    }
}

async function showReport() {
    const result = await api.getDailyReport();
    
    if (result.code === 200) {
        const data = result.data;
        let message = `📊 今日报表\n\n`;
        message += `日期：${data.date}\n`;
        message += `订单总数：${data.totalOrders}\n`;
        message += `待取餐：${data.pendingCount}\n`;
        message += `已完成：${data.finishedCount}\n`;
        message += `已退款：${data.refundedCount}\n`;
        message += `营业额：¥${data.totalAmount}\n\n`;
        message += `🔥 热门菜品：\n`;
        data.topDishes.forEach((dish, index) => {
            message += `${index + 1}. ${dish.dishName} - ${dish.count}份\n`;
        });
        
        alert(message);
    } else {
        showToast(result.message || '获取报表失败');
    }
}

function showProfileEdit() {
    showToast('个人信息编辑功能');
}

function closePopup(popupId) {
    document.getElementById(popupId).classList.remove('active');
}

document.addEventListener('DOMContentLoaded', () => {
    const savedUser = localStorage.getItem('userInfo');
    if (savedUser) {
        initMainPage();
    } else {
        showLogin();
    }
});
