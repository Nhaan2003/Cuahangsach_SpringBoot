<!-- front-end/src/routes/admin/+layout.svelte -->
<script lang="ts">
	import { page } from '$app/stores';
	import { logout } from '$lib/auth';
	
	function isActive(path: string): boolean {
		return $page.url.pathname === path;
	}
	
	function getLinkClass(path: string): string {
		const baseClass = "inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium";
		if (isActive(path)) {
			return `${baseClass} border-blue-500 text-blue-600`;
		}
		return `${baseClass} border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300`;
	}

	async function handleLogout() {
		await logout();
	}
</script>

<div class="min-h-screen bg-gray-100">	<!-- Header -->
	<header class="bg-white shadow-sm border-b sticky top-0 z-50">
		<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
			<div class="flex justify-between h-16">
				<div class="flex items-center">
					<h1 class="text-xl font-semibold text-gray-900">Admin Dashboard</h1>
				</div>				<nav class="flex space-x-8">
					<a href="/admin" class={getLinkClass("/admin")}>
						Tổng quan
					</a>
					<a href="/admin/accounts" class={getLinkClass("/admin/accounts")}>
						Quản lý tài khoản
					</a>
					<a href="/admin/products" class={getLinkClass("/admin/products")}>
						Quản lý sản phẩm
					</a>
					<a href="/admin/orders" class={getLinkClass("/admin/orders")}>
						Quản lý đơn hàng
					</a>
					<a href="/admin/inventory" class={getLinkClass("/admin/inventory")}>
						Báo cáo & Thống kê
					</a>
				</nav><div class="flex items-center">
					<button 
						on:click={handleLogout} 
						class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors duration-200"
					>
						<svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
						</svg>
						Đăng xuất
					</button>
				</div>
			</div>
		</div>
	</header>

	<!-- Main content -->
	<main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
		<slot />
	</main>
</div>