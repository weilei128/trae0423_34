const api = {
    request: async (url, options = {}) => {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        try {
            const response = await fetch(`${API_BASE_URL}${url}`, {
                ...options,
                headers
            });
            
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API请求失败:', error);
            return { code: 500, message: '网络错误，请稍后重试' };
        }
    },

    get: (url) => api.request(url, { method: 'GET' }),

    post: (url, data) => api.request(url, {
        method: 'POST',
        body: JSON.stringify(data)
    }),

    login: (username, password) => api.post('/user/login', { username, password }),

    register: (userData) => api.post('/user/register', userData),

    getUserInfo: () => api.get('/user/info'),

    recharge: (amount) => api.post('/user/recharge', { amount }),

    updateUser: (userData) => api.post('/user/update', userData),

    getTodayDishes: () => api.get('/dish/today'),

    getMenu: () => api.get('/dish/menu'),

    getDishesByCategory: (categoryId) => api.get(`/dish/category/${categoryId}`),

    getCategories: () => api.get('/dish/categories'),

    createOrder: (orderData) => api.post('/order/create', orderData),

    getMyOrders: () => api.get('/order/my'),

    getOrderDetail: (orderId) => api.get(`/order/detail/${orderId}`),

    cancelOrder: (orderId) => api.post(`/order/cancel/${orderId}`),

    verifyOrder: (verifyCode) => api.post('/order/verify', { verifyCode }),

    getDailyReport: (date) => {
        const url = date ? `/report/daily?date=${date}` : '/report/daily';
        return api.get(url);
    },

    getRechargeReport: (startDate, endDate) => {
        let url = '/report/recharge';
        if (startDate && endDate) {
            url += `?startDate=${startDate}&endDate=${endDate}`;
        }
        return api.get(url);
    }
};
