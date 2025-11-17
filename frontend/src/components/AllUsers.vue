<template>
  <div class="users-container">
    <!-- Header -->
    <div class="users-header">
      <h2>Registered Users</h2>
      <p class="users-description">
        List of all users who have made at least one booking
      </p>
      <div class="users-stats">
        <svg class="stats-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <span class="stats-count">{{ userEmails.length }}</span>
        <span class="stats-label">Total Users</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading users...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-alert">
      <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <div>
        <p class="error-title">Error Loading Users</p>
        <p class="error-message">{{ error }}</p>
        <button @click="fetchUsers" class="retry-button">Retry</button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="userEmails.length === 0" class="empty-state">
      <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
      </svg>
      <h3>No Users Found</h3>
      <p>No users have made bookings yet</p>
    </div>

    <!-- Users List -->
    <div v-else class="users-content">
      <!-- Search/Filter -->
      <div class="search-box">
        <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="Search by email..."
          class="search-input"
        />
        <span v-if="searchQuery" class="search-clear" @click="searchQuery = ''">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </span>
      </div>

      <!-- Results Info -->
      <div class="results-info">
        <span v-if="searchQuery">
          Showing {{ filteredEmails.length }} of {{ userEmails.length }} users
        </span>
        <span v-else>
          Showing all {{ userEmails.length }} users
        </span>
      </div>

      <!-- User Cards Grid -->
      <div class="users-grid">
        <div 
          v-for="(email, index) in filteredEmails" 
          :key="index" 
          class="user-card"
        >
          <div class="user-avatar">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <div class="user-info">
            <p class="user-email">{{ email }}</p>
            <button @click="copyEmail(email)" class="copy-button" title="Copy email">
              <svg v-if="!copiedEmail || copiedEmail !== email" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
              <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M5 13l4 4L19 7" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div v-if="filteredEmails.length === 0 && searchQuery" class="no-results">
        <p>No users found matching "{{ searchQuery }}"</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../plugins/axios';

export default {
  name: 'AllUsers',
  
  data() {
    return {
      userEmails: [],
      loading: true,
      error: null,
      searchQuery: '',
      copiedEmail: null
    };
  },

  computed: {
    filteredEmails() {
      if (!this.searchQuery) {
        return this.userEmails;
      }
      
      const query = this.searchQuery.toLowerCase();
      return this.userEmails.filter(email => 
        email.toLowerCase().includes(query)
      );
    }
  },

  mounted() {
    this.fetchUsers();
  },

  methods: {
    async fetchUsers() {
      this.loading = true;
      this.error = null;

      try {
        console.log('[AllUsers] Fetching registered user emails...');
        const response = await api.get('/admin/users/emails');
        
        this.userEmails = response.data;
        console.log(`[AllUsers] Loaded ${this.userEmails.length} users`);
      } catch (err) {
        console.error('[AllUsers] Error fetching users:', err);
        
        if (err.response?.status === 403) {
          this.error = 'Access denied. You need ADMIN privileges.';
        } else if (err.response?.status === 401) {
          this.error = 'Unauthorized. Please log in again.';
        } else {
          this.error = err.response?.data?.message || 'Failed to load users';
        }
      } finally {
        this.loading = false;
      }
    },

    async copyEmail(email) {
      try {
        await navigator.clipboard.writeText(email);
        this.copiedEmail = email;
        
        setTimeout(() => {
          this.copiedEmail = null;
        }, 2000);
      } catch (err) {
        console.error('Failed to copy email:', err);
      }
    }
  }
};
</script>

<style scoped>
.users-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: #fffaf4;
  min-height: 100vh;
  font-family: "Inter", -apple-system, sans-serif;
}

/* Header */
.users-header {
  margin-bottom: 2rem;
}

.users-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.users-description {
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.users-stats {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  background: #fff7f0;
  border: 1px solid rgba(255, 170, 64, 0.22);
  padding: 0.75rem 1.25rem;
  border-radius: 0.75rem;
}

.stats-icon {
  width: 24px;
  height: 24px;
  color: #ff8a00;
}

.stats-count {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ff8a00;
}

.stats-label {
  font-size: 0.875rem;
  color: #6b7280;
}

/* Loading */
.loading-state {
  text-align: center;
  padding: 3rem;
}

.spinner {
  border: 4px solid #fff7f0;
  border-top: 4px solid #ff8a00;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error */
.error-alert {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
}

.error-icon {
  width: 24px;
  height: 24px;
  color: #c33;
  flex-shrink: 0;
}

.error-title {
  font-weight: 600;
  color: #c33;
  margin-bottom: 0.25rem;
}

.error-message {
  color: #a22;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.retry-button {
  background: #c33;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
}

.retry-button:hover {
  background: #a22;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: #fff7f0;
  border: 1px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.75rem;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #ff8a00;
  margin: 0 auto 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Search */
.search-box {
  position: relative;
  margin-bottom: 1.5rem;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #6b7280;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.75rem 3rem 0.75rem 3rem;
  border: 2px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.75rem;
  font-size: 0.875rem;
  background: #fff;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #ff8a00;
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

.search-clear {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #6b7280;
  cursor: pointer;
}

.search-clear:hover {
  color: #111827;
}

/* Results Info */
.results-info {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

/* Users Grid */
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(1200px, 1fr));
  gap: 1rem;
}

.user-card {
  background: #fff;
  border: 1px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.75rem;
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.2s;
}

.user-card:hover {
  border-color: #ff8a00;
  box-shadow: 0 2px 8px rgba(255, 138, 0, 0.1);
  transform: translateY(-2px);
}

.user-avatar {
  width: 48px;
  height: 48px;
  background: #fff7f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar svg {
  width: 24px;
  height: 24px;
  color: #ff8a00;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  min-width: 0;
}

.user-email {
  font-size: 0.875rem;
  font-weight: 500;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-button {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: #6b7280;
  transition: color 0.2s;
  flex-shrink: 0;
}

.copy-button:hover {
  color: #ff8a00;
}

.copy-button svg {
  width: 18px;
  height: 18px;
}

/* No Results */
.no-results {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
  font-size: 0.875rem;
}

/* Responsive */
@media (max-width: 768px) {
  .users-container {
    padding: 1rem;
  }

  .users-grid {
    grid-template-columns: 1fr;
  }
}
</style>