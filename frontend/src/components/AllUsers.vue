<template>
  <div class="users-container" @click="handleOutsideClick">
    <div class="users-card">
      <!-- Header -->
      <div class="users-header">
        <div class="header-title-section">
          <div class="header-label">ADMIN PANEL</div>
          <h2 class="header-title">All Users</h2>
          <span class="header-subtitle">{{ filteredUsers.length }} total users</span>
        </div>
        <div class="header-controls">
          <v-text-field
              v-model="searchQuery"
              density="compact"
              variant="outlined"
              hide-details
              clearable
              placeholder="Search by email..."
              style="max-width: 250px"
              :disabled="loading"
              class="control-input"
          />

          <v-select
              v-model="selectedRole"
              :items="roleOptions"
              item-title="title"
              item-value="value"
              density="compact"
              variant="outlined"
              style="max-width: 180px"
              :disabled="loading"
              :clearable="false"
              hide-details
              label="Filter by role"
              class="control-select"
          />

          <v-btn
              color="#171717"
              variant="flat"
              @click="resetFilters"
              class="control-button"
          >
            Reset Filters
          </v-btn>
        </div>
      </div>

      <!-- Error Alert -->
      <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
          closable
      >
        {{ error }}
      </v-alert>

      <div v-if="loading" class="loading-container">
        <v-progress-circular
            indeterminate
            size="48"
            width="4"
            color="#171717"
        />
        <p class="loading-message mt-3">Loading users...</p>
      </div>

      <template v-else>
        <v-data-table
            :headers="headers"
            :items="filteredUsers"
            item-key="email"
            density="compact"
            class="users-table"
            :items-per-page="15"
            fixed-header
            height="70vh"
        >
          <template #item.email="{ item }">
            <div class="email-with-icon" @click="copyEmail(item)">
              <v-icon size="16" color="#737373" class="email-icon">
                mdi-account-circle
              </v-icon>

              <span class="table-email-clickable">
                  {{ item.email }}
              </span>
            </div>
          </template>


          <template #item.role="{ item }">
            <v-chip
                size="x-small"
                :color="getRoleColor(item.role)"
                variant="flat"
                class="table-chip"
            >
              {{ item.role }}
            </v-chip>
          </template>

          <template #item.actions="{ item }">
            <v-menu :close-on-content-click="false" location="bottom end">
              <template #activator="{ props }">
                <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="#171717"
                    class="table-action-button"
                >
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </template>
              <v-list density="compact" class="table-action-menu">
                <v-list-item
                    prepend-icon="mdi-shield-crown"
                    title="Set as ADMIN"
                    @click="updateUserRole(item, 'ADMIN')"
                />

                <v-list-item
                    prepend-icon="mdi-account"
                    title="Set as USER"
                    @click="updateUserRole(item, 'USER')"
                />
                <v-list-item
                    prepend-icon="mdi-delete"
                    title="Delete USER"
                    class="text-red"
                    @click="deleteUser(item)"
                />
              </v-list>
            </v-menu>
          </template>

          <template #no-data>
            <div class="table-empty-state">
              <v-icon size="48" color="#a3a3a3" class="mb-3">mdi-account-group</v-icon>
              <div class="table-empty-title">No users found</div>
              <div class="table-empty-subtitle">
                Try adjusting your filters or check back later.
              </div>
            </div>
          </template>
        </v-data-table>
      </template>
    </div>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="showDeleteDialog" max-width="500">
      <v-card class="delete-dialog">
        <v-card-title class="dialog-title">
          <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon>
          Delete User
        </v-card-title>
        <v-card-text class="dialog-text">
          <p class="mb-4">
            Are you sure you want to delete user <strong>{{ selectedUser?.email }}</strong>?
          </p>
          <p class="text-warning">
            This action cannot be undone.
          </p>
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-spacer></v-spacer>
          <v-btn
              variant="text"
              @click="closeDeleteDialog"
              :disabled="deletingUser"
          >
            Cancel
          </v-btn>
          <v-btn
              color="error"
              variant="flat"
              @click="confirmDeleteUser"
              :loading="deletingUser"
          >
            Yes, Delete User
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success Snackbar -->
    <v-snackbar
        v-model="showSuccessSnackbar"
        :timeout="2000"
        color="success"
        location="top"
    >
      <div style="display: flex; align-items: center; gap: 8px;">
        <v-icon>mdi-check-circle</v-icon>
        {{ successMessage }}
      </div>
    </v-snackbar>
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
      selectedRole: 'ALL',

      selectedUser: null,
      showRoleModal: false,
      originalRole: null,

      showDeleteDialog: false,
      deletingUser: false,

      showSuccessSnackbar: false,
      successMessage: '',

      modalNotification: null,
      modalNotificationTimeout: null,

      headers: [
        { title: 'Email', key: 'email', width: '60%', align: 'start', sortable: true },
        { title: 'Role', key: 'role', width: '20%', align: 'center', sortable: true },
        { title: 'Actions', key: 'actions', width: '20%', align: 'center', sortable: false },
      ],
    };
  },

  computed: {
    filteredUsers() {
      return this.users.filter(u => {
        const matchesEmail = u.email.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesRole = this.selectedRole === 'ALL' || u.role === this.selectedRole;
        return matchesEmail && matchesRole;
      });
    },

    roleOptions() {
      const roles = [...new Set(this.users.map(u => u.role))];
      return [
        { title: 'All Roles', value: 'ALL' },
        ...roles.map(role => ({ title: role, value: role }))
      ];
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

    async copyEmail(user) {
      try {
        await navigator.clipboard.writeText(user.email);
        this.successMessage = 'Email copied to clipboard';
        this.showSuccessSnackbar = true;
      } catch (err) {
        console.error('Failed to copy email:', err);
        this.error = 'Failed to copy email';
      }
    },
    async updateUserRole(user, role) {
      try {
        await api.patch('/admin/users/role', {
          email: user.email,
          role
        });

        this.successMessage = `Role changed to ${role}`;
        this.showSuccessSnackbar = true;

        this.fetchUsers();
      } catch (err) {
        console.error(err);
        this.error = err.response?.data?.message || "Failed to update role.";
      }
    },

    deleteUser(user) {
      if (!user || !user.email) {
        this.error = 'Invalid user';
        return;
      }
      this.selectedUser = user;
      this.showDeleteDialog = true;
    },

    closeDeleteDialog() {
      this.showDeleteDialog = false;
      this.selectedUser = null;
    },

    async confirmDeleteUser() {
      if (!this.selectedUser?.email) return;

      try {
        this.deletingUser = true;
        this.error = null;

        const payload = { email: this.selectedUser.email };
        await api.delete(`/users`, { data: payload });
        
        this.successMessage = 'User deleted successfully';
        this.showSuccessSnackbar = true;
        this.closeDeleteDialog();
        await this.fetchUsers();
      } catch (err) {
        console.error('Delete error:', err.response);
        this.error = err.response?.data?.message || "Failed to delete user.";
        this.showDeleteDialog = false;
      } finally {
        this.deletingUser = false;
      }
    },

    getRoleColor(role) {
      return role === 'ADMIN' ? '#171717' : '#737373';
    },

    resetFilters() {
      this.selectedRole = 'ALL';
      this.searchQuery = '';
    },

    showModalNotification(message, type = 'success') {
      if (this.modalNotificationTimeout) {
        clearTimeout(this.modalNotificationTimeout);
      }

      this.modalNotification = { message, type };

      if (type === 'success') {
        this.modalFNotificationTimeout = setTimeout(() => {
          this.modalNotification = null;
          setTimeout(() => {
            this.closeRoleModal();
          }, 500);
        }, 2000);
      }
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

    handleOutsideClick() {
      // Handle any outside clicks if needed
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
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

* {
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.users-container {
  padding: 28px;
  background: #fafafa;
  min-height: 100vh;
}

.users-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.users-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f5f5f5;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-label {
  font-size: 11px;
  font-weight: 700;
  color: #737373;
  letter-spacing: 1.5px;
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  color: #171717;
  margin: 0;
  letter-spacing: -0.5px;
  line-height: 1;
}

.header-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-input :deep(.v-field),
.control-select :deep(.v-field) {
  border-radius: 12px !important;
  border: 2px solid #e5e5e5 !important;
  font-weight: 600;
}

.control-input :deep(.v-field:hover),
.control-select :deep(.v-field:hover) {
  border-color: #a3a3a3 !important;
}

.control-input :deep(.v-field--focused),
.control-select :deep(.v-field--focused) {
  border-color: #171717 !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.control-button {
  font-weight: 600 !important;
  text-transform: none !important;
  letter-spacing: 0.3px;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
}

.control-button:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
  transform: translateY(-1px);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-message {
  font-size: 15px;
  font-weight: 600;
  color: #737373;
  letter-spacing: 0.3px;
}

.users-table {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f5f5f5;
}

.users-table :deep(table) {
  border-collapse: separate !important;
  border-spacing: 0;
}

.users-table :deep(thead th) {
  position: sticky;
  top: 0;
  background: #fafafa !important;
  z-index: 2;
  font-weight: 700 !important;
  color: #171717 !important;
  font-size: 13px !important;
  letter-spacing: 0.3px;
  border-bottom: 2px solid #f5f5f5 !important;
}

.users-table :deep(tbody tr) {
  height: 55px;
  transition: all 0.2s ease;
}

.users-table :deep(tbody tr:hover) {
  background: #fafafa !important;
}

.users-table :deep(tbody td) {
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5 !important;
}

.table-text-bold {
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.2px;
}

.table-chip {
  font-weight: 700 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 11px !important;
}

.table-action-button {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.table-action-button:hover {
  background-color: #f5f5f5 !important;
  transform: rotate(90deg);
}

.table-action-menu {
  border-radius: 12px !important;
  border: 1px solid #e5e5e5 !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.table-action-menu :deep(.v-list-item) {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.table-empty-state {
  text-align: center;
  padding: 60px 0;
  color: #737373;
}

.table-empty-title {
  font-weight: 700;
  color: #171717;
  font-size: 18px;
  margin-bottom: 8px;
  letter-spacing: -0.3px;
}

.table-empty-subtitle {
  font-weight: 600;
  color: #737373;
  font-size: 14px;
  letter-spacing: 0.3px;
}

/* Role Dialog */
.role-dialog {
  border-radius: 16px !important;
}

.dialog-title {
  font-weight: 700;
  font-size: 20px;
  color: #171717;
  padding: 24px 24px 8px;
}

.dialog-subtitle {
  font-size: 14px;
  color: #737373;
  padding: 0 24px 16px;
  font-weight: 600;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

.role-select :deep(.v-field) {
  border-radius: 20px !important;
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

/* Responsive */
@media (max-width: 768px) {
  .users-container {
    padding: 16px;
  }

  .users-card {
    padding: 20px;
  }

  .users-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-controls {
    width: 100%;
    flex-direction: column;
  }

  .control-input,
  .control-select,
  .control-button {
    width: 100%;
    max-width: none !important;
  }

  .header-title {
    font-size: 24px;
  }
}
.email-with-icon {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.email-icon {
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.email-with-icon:hover .email-icon {
  opacity: 0.9;
}

.table-email-clickable {
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.2px;
  transition: color 0.2s ease, text-decoration 0.2s ease;
}

.table-email-clickable:hover {
  color: #000000;
  text-decoration: underline;
}

</style>