<template>
  <div class="users-container" @click="handleOutsideClick">
    <!-- Header -->
    <div class="users-header">
      <h2>Registered Users</h2>
      <p class="users-description">
        List of all users registered
      </p>
      <div class="users-stats">
        <svg class="stats-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <span class="stats-count">{{ users.length }}</span>
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
    <div v-else-if="users.length === 0" class="empty-state">
      <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
      </svg>
      <h3>No Users Found</h3>
      <p>No users have made bookings yet</p>
    </div>

    <!-- Filters -->
    <div v-else class="filters">
      <!-- Search Bar -->
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

      <!-- Role Filter -->
      <select v-model="selectedRole" class="role-filter">
        <option value="">All Roles</option>
        <option v-for="role in uniqueRoles" :key="role" :value="role">{{ role }}</option>
      </select>
    </div>

    <!-- Users Grid -->
    <div v-if="!loading && !error && users.length > 0" class="users-grid">
      <div 
        v-for="(user, index) in filteredUsers" 
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
          <span class="user-email">{{ user.email }}</span>
          <span class="user-role">{{ user.role }}</span>
        </div>

        <!-- User actions wrapper -->
        <div class="user-actions">
          <button 
            @click.stop="toggleMenu(index)" 
            class="more-options-btn"
          >
            ⋮
          </button>

          <div v-if="openMenu === index" class="menu-dropdown" @click.stop>
            <div @click="copyEmail(user, index)">Copy email</div>
            <div @click="openRoleModal(user)">Change role</div>
          </div>

          <!-- Local notification tooltip for copy -->
          <transition name="tooltip-fade">
            <div v-if="copiedIndex === index" class="copy-tooltip">
              <svg class="tooltip-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Copied successfully!
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- No Results -->
    <div v-if="!loading && !error && users.length > 0 && filteredUsers.length === 0" class="no-results">
      <p>No users found.</p>
    </div>

    <!-- ROLE MODAL -->
    <transition name="modal-fade">
      <div v-if="showRoleModal" class="popup-overlay" @click.self="closeRoleModal">
        <div class="popup-window" @click.stop>
          <h2 class="popup-title">Change Role</h2>
          <p class="popup-subtitle">{{ selectedUser.email }}</p>

          <!-- Inline notification in modal -->
          <transition name="slide-down">
            <div v-if="modalNotification" :class="['modal-notification', `notification-${modalNotification.type}`]">
              <svg v-if="modalNotification.type === 'success'" class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <svg v-else-if="modalNotification.type === 'error'" class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              <svg v-else class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>{{ modalNotification.message }}</span>
            </div>
          </transition>

          <div class="popup-body">
            <label>Role:</label>
            <select v-model="selectedUser.role" class="popup-select">
              <option value="ADMIN">ADMIN</option>
              <option value="USER">USER</option>
            </select>
          </div>

          <div class="popup-actions">
            <button 
              class="popup-btn save" 
              @click="updateRole"
              :disabled="isRoleUnchanged"
            >
              Save
            </button>
            <button class="popup-btn cancel" @click="closeRoleModal">Cancel</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import api from '../plugins/axios';

export default {
  data() {
    return {
      users: [],
      loading: true,
      error: null,
      searchQuery: '',
      selectedRole: '',
      
      openMenu: null,
      selectedUser: null,
      showRoleModal: false,
      originalRole: null,
      
      copiedIndex: null,
      copiedTimeout: null,
      
      modalNotification: null,
      modalNotificationTimeout: null,
    };
  },
  
  computed: {
    filteredUsers() {
      return this.users.filter(u => {
        const matchesEmail = u.email.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesRole = !this.selectedRole || u.role === this.selectedRole;
        return matchesEmail && matchesRole;
      });
    },
    
    uniqueRoles() {
      const roles = this.users.map(u => u.role);
      return [...new Set(roles)];
    },
    
    isRoleUnchanged() {
      return this.selectedUser && this.selectedUser.role === this.originalRole;
    }
  },

  mounted() {
    this.fetchUsers();
    document.addEventListener('keydown', this.handleEscKey);
  },
  
  beforeUnmount() {
    document.removeEventListener('keydown', this.handleEscKey);
    if (this.copiedTimeout) clearTimeout(this.copiedTimeout);
    if (this.modalNotificationTimeout) clearTimeout(this.modalNotificationTimeout);
  },

  methods: {
    async fetchUsers() {
      this.loading = true;
      this.error = null;

      try {
        const response = await api.get('/admin/users/emails');
        this.users = response.data;
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load users';
      } finally {
        this.loading = false;
      }
    },
    
    async copyEmail(user, index) {
      try {
        await navigator.clipboard.writeText(user.email);
        
        if (this.copiedTimeout) clearTimeout(this.copiedTimeout);

        this.copiedIndex = index;
        
        this.copiedTimeout = setTimeout(() => {
          this.copiedIndex = null;
        }, 1500);
        
      } catch (err) {
        console.error('Failed to copy email:', err);
      }
      
      this.openMenu = null;
    },
    
    showModalNotification(message, type = 'success') {
      if (this.modalNotificationTimeout) {
        clearTimeout(this.modalNotificationTimeout);
      }
      
      this.modalNotification = { message, type };
  
      if (type === 'success') {
        this.modalNotificationTimeout = setTimeout(() => {
          this.modalNotification = null;
          setTimeout(() => {
            this.closeRoleModal();
          }, 500);
        }, 2000);
      }
    },
    
    toggleMenu(index) {
      this.openMenu = this.openMenu === index ? null : index;
    },
    
    openRoleModal(user) {
      this.selectedUser = { ...user };
      this.originalRole = user.role;
      this.showRoleModal = true;
      this.openMenu = null;
      this.modalNotification = null;
    },
    
    closeRoleModal() {
      this.showRoleModal = false;
      this.selectedUser = null;
      this.originalRole = null;
      this.modalNotification = null;
      if (this.modalNotificationTimeout) {
        clearTimeout(this.modalNotificationTimeout);
      }
    },
    
    async updateRole() {
      if (this.isRoleUnchanged) {
        this.showModalNotification("User already has the chosen role.", 'warning');
        return;
      }
      
      try {
        await api.patch('/admin/users/role', {
          email: this.selectedUser.email,
          role: this.selectedUser.role
        });
        
        this.showModalNotification("User role changed successfully!", 'success');
        this.fetchUsers();
        
      } catch (err) {
        this.showModalNotification(
          err.response?.data?.message || "Failed to update user role.", 
          'error'
        );
      }
    },
    
    handleOutsideClick(event) {
      if (this.openMenu !== null) {
        this.openMenu = null;
      }
    },
    
    handleEscKey(event) {
      if (event.key === 'Escape') {
        if (this.showRoleModal) {
          this.closeRoleModal();
        }
      }
    }
  }
};
</script>

<style scoped>
.users-container {
  width: 100%;         
  max-width: 1200px;  
  margin: 0 auto;
  padding: 2rem;
  background: #fffaf4;
  min-height: 100vh;
  font-family: "Inter", -apple-system, sans-serif;
  box-sizing: border-box; 
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

/* Filters */
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

/* Search */
.search-box {
  position: relative;
  flex: 1;
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

/* Role Filter */
.role-filter {
  padding: 0.75rem 1.5rem;
  border: 2px solid rgba(255, 170, 64, 0.22);
  border-radius: 0.75rem;
  background-color: #fff;
  color: #111827;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.role-filter:hover {
  background-color: #fff7f0;
  border-color: #ff8a00;
}

.role-filter:focus {
  outline: none;
  border-color: #ff8a00;
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

/* Users Grid */
.users-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  border-radius: 6px;
  transition: background 0.2s;
  cursor: pointer;
  width: 100%; 
  box-sizing: border-box;
}

.user-card:hover {
  background-color: #f0f0f0;
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
  display: flex;
  justify-content: space-between;
  flex: 1;
  margin-left: 15px;
  min-width: 0; 
}

.user-email {
  flex: 1; 
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  color: #111827;
}

.user-role {
  flex-shrink: 0;
  margin-left: 15px;
  text-align: right;
  white-space: nowrap;
  color: #6b7280;
  font-size: 0.875rem;
}

/* User Actions */
.user-actions {
  position: relative; 
  display: inline-block;
}

.more-options-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 5px 10px;
  color: #6b7280;
  transition: color 0.2s;
}

.more-options-btn:hover {
  color: #ff8a00;
}

.menu-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 5px;
  background: white;
  border: 1px solid rgba(255, 170, 64, 0.22);
  border-radius: 8px;
  padding: 5px 0;
  min-width: 140px;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.menu-dropdown div {
  padding: 10px 15px;
  cursor: pointer;
  font-size: 0.875rem;
  color: #111827;
  transition: background 0.2s;
}

.menu-dropdown div:hover {
  background: #fff7f0;
  color: #ff8a00;
}

/* Copy Tooltip - Local notification */
.copy-tooltip {
  position: absolute;
  right: 100%;
  top: 50%;
  transform: translateY(-50%);
  margin-right: 10px;
  background: #ff8a00;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.813rem;
  font-weight: 500;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(255, 138, 0, 0.3);
  z-index: 200;
}

.copy-tooltip::after {
  content: '';
  position: absolute;
  right: -5px;
  top: 50%;
  transform: translateY(-50%);
  border-left: 5px solid #ff8a00;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
}

.tooltip-icon {
  width: 16px;
  height: 16px;
}

.tooltip-fade-enter-active, .tooltip-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.tooltip-fade-enter-from, .tooltip-fade-leave-to {
  opacity: 0;
  transform: translateY(-50%) translateX(10px);
}

/* No Results */
.no-results {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
  font-size: 0.875rem;
}

/* Modal Overlay */
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

/* Modal Window */
.popup-window {
  background: white;
  width: 90%;
  max-width: 400px;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.25);
}

.modal-fade-enter-active {
  animation: modalFadeIn 0.3s ease-out;
}

.modal-fade-leave-active {
  animation: modalFadeOut 0.2s ease-in;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes modalFadeOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.9);
  }
}

.popup-title {
  font-size: 1.25rem;
  margin-bottom: 8px;
  font-weight: 600;
  color: #111827;
}

.popup-subtitle {
  margin-bottom: 20px;
  color: #6b7280;
  font-size: 0.875rem;
}

/* Modal Inline Notification */
.modal-notification {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  background: #fff7f0;
  border: 1px solid rgba(255, 170, 64, 0.3);
  color: #111827;
  font-size: 0.875rem;
}

.notification-icon {
  width: 20px;
  height: 20px;
  color: #ff8a00;
  flex-shrink: 0;
}

.notification-success {
  background: #fff7f0;
  border-color: rgba(255, 170, 64, 0.3);
}

.notification-error {
  background: #fff7f0;
  border-color: rgba(255, 170, 64, 0.3);
}

.notification-warning {
  background: #fff7f0;
  border-color: rgba(255, 170, 64, 0.3);
}

.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

.popup-body label {
  font-weight: 600;
  margin-bottom: 8px;
  display: block;
  color: #111827;
  font-size: 0.875rem;
}

.popup-select {
  width: 100%;
  padding: 10px 12px;
  font-size: 0.875rem;
  border: 2px solid rgba(255, 170, 64, 0.22);
  border-radius: 8px;
  margin-bottom: 20px;
  background: #fff;
  color: #111827;
  transition: all 0.2s;
}

.popup-select:focus {
  outline: none;
  border-color: #ff8a00;
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

.popup-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.popup-btn {
  padding: 10px 20px;
  font-size: 0.875rem;
  border-radius: 8px;
  cursor: pointer;
  border: none;
  font-weight: 600;
  transition: all 0.2s;
}

.popup-btn.save {
  background: #ff8a00;
  color: white;
}

.popup-btn.save:hover:not(:disabled) {
  background: #e67a00;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 138, 0, 0.3);
}

.popup-btn.save:disabled {
  background: #ddd;
  color: #999;
  cursor: not-allowed;
}

.popup-btn.cancel {
  background: #f3f4f6;
  color: #111827;
}

.popup-btn.cancel:hover {
  background: #e5e7eb;
}
/* Responsive */
@media (max-width: 768px) {
  .users-container {
    padding: 1rem;
  }

  .users-header h2 {
    font-size: 1.5rem;
  }

  .filters {
    flex-direction: column;
  }

  .role-filter {
    width: 100%;
  }

  .user-card {
    padding: 10px 15px;
  }

  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .user-role {
    margin-left: 0;
    font-size: 0.75rem;
  }

  .copy-tooltip {
    right: auto;
    left: 50%;
    top: auto;
    bottom: 100%;
    transform: translateX(-50%);
    margin-right: 0;
    margin-bottom: 10px;
  }

  .copy-tooltip::after {
    right: auto;
    left: 50%;
    top: 100%;
    transform: translateX(-50%);
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid #ff8a00;
    border-bottom: none;
  }

  .popup-window {
    width: 95%;
    padding: 20px;
  }

  .popup-actions {
    flex-direction: column-reverse;
  }

  .popup-btn {
    width: 100%;
  }
}
</style>