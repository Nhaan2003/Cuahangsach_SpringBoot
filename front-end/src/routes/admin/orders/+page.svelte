<!-- front-end/src/routes/admin/orders/+page.svelte -->
<script lang="ts">	import { onMount } from 'svelte';
	import { writable, derived } from 'svelte/store';
	import { goto } from '$app/navigation';
	import { authenticatedFetch } from '$lib/auth';
	import type { OrderDTO, ApiResponse } from '$lib/types';

	// Stores
	const orders = writable<OrderDTO[]>([]);
	const loading = writable<boolean>(false);
	const error = writable<string>('');
	const statistics = writable<any>({});

	// Filter states
	const searchTerm = writable<string>('');
	const selectedStatus = writable<string>('');
	const fromDate = writable<string>('');
	const toDate = writable<string>('');
	const showFilters = writable<boolean>(false);

	// Selected orders for bulk operations
	const selectedOrders = writable<Set<number>>(new Set());

	// API URLs
	const ORDERS_API = 'http://localhost:8080/api/admin/orders';

	// Derived store for filtered orders
	const filteredOrders = derived(
		[orders, searchTerm, selectedStatus, fromDate, toDate],
		([$orders, $searchTerm, $selectedStatus, $fromDate, $toDate]) => {
			let filtered = [...$orders];

			// Filter by search term (customer name, order ID)
			if ($searchTerm.trim()) {
				const term = $searchTerm.toLowerCase().trim();
				filtered = filtered.filter(order => 
					order.orderId.toString().includes(term) ||
					(order.user && order.user.fullName && order.user.fullName.toLowerCase().includes(term)) ||
					(order.user && order.user.username && order.user.username.toLowerCase().includes(term))
				);
			}

			// Filter by status
			if ($selectedStatus) {
				filtered = filtered.filter(order => order.status === $selectedStatus);
			}

			// Filter by date range
			if ($fromDate) {
				const fromDateTime = new Date($fromDate);
				filtered = filtered.filter(order => {
					if (!order.orderDate) return false;
					const orderDateTime = new Date(order.orderDate);
					return orderDateTime >= fromDateTime;
				});
			}

			if ($toDate) {
				const toDateTime = new Date($toDate);
				toDateTime.setHours(23, 59, 59, 999);
				filtered = filtered.filter(order => {
					if (!order.orderDate) return false;
					const orderDateTime = new Date(order.orderDate);
					return orderDateTime <= toDateTime;
				});
			}

			// Sort by order date (newest first)
			filtered.sort((a, b) => {
				if (!a.orderDate) return 1;
				if (!b.orderDate) return -1;
				return new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime();
			});

			return filtered;
		}
	);
	// Order status options
	const statusOptions = [
		{ value: 'Đang xử lý', label: 'Đang xử lý', color: 'bg-yellow-100 text-yellow-800' },
		{ value: 'Đã giao', label: 'Đã giao', color: 'bg-green-100 text-green-800' },
		{ value: 'Đã hủy', label: 'Đã hủy', color: 'bg-red-100 text-red-800' }
	];

	// Functions
	async function fetchOrders(): Promise<void> {
		loading.set(true);
		try {
			const response = await authenticatedFetch(ORDERS_API);
			if (response.ok) {
				const result = await response.json();
				if (result.success && result.data) {
					orders.set(result.data);
					error.set('');
				} else {
					error.set(result.message || 'Không thể tải danh sách đơn hàng');
				}
			} else {
				error.set('Lỗi kết nối đến server');
			}
		} catch (err) {
			console.error('Fetch error:', err);
			error.set('Lỗi kết nối: ' + (err as Error).message);
		} finally {
			loading.set(false);
		}
	}

	async function fetchStatistics(): Promise<void> {
		try {
			const response = await authenticatedFetch(`${ORDERS_API}/statistics`);
			if (response.ok) {
				const result = await response.json();
				if (result.success) {
					statistics.set(result.data);
				}
			}
		} catch (err) {
			console.error('Statistics fetch error:', err);
		}
	}

	async function updateOrderStatus(orderId: number, newStatus: string): Promise<void> {
		try {
			const response = await authenticatedFetch(`${ORDERS_API}/${orderId}/status`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({ status: newStatus })
			});

			if (response.ok) {
				const result = await response.json();
				if (result.success) {
					// Update the order in the local state
					orders.update(orderList => 
						orderList.map(order => 
							order.orderId === orderId 
								? { ...order, status: newStatus }
								: order
						)
					);
					alert('Cập nhật trạng thái đơn hàng thành công!');
					await fetchStatistics(); // Refresh statistics
				} else {
					alert('Lỗi: ' + result.message);
				}
			} else {
				alert('Lỗi kết nối khi cập nhật trạng thái');
			}
		} catch (err) {
			console.error('Update status error:', err);
			alert('Lỗi khi cập nhật trạng thái: ' + (err as Error).message);
		}
	}

	function formatDate(dateString: string): string {
		return new Date(dateString).toLocaleString('vi-VN');
	}

	function formatPrice(price: number): string {
		return new Intl.NumberFormat('vi-VN', {
			style: 'currency',
			currency: 'VND'
		}).format(price);
	}

	function getStatusColor(status: string): string {
		const statusOption = statusOptions.find(option => option.value === status);
		return statusOption ? statusOption.color : 'bg-gray-100 text-gray-800';
	}

	function toggleOrderSelection(orderId: number): void {
		selectedOrders.update(selected => {
			const newSelected = new Set(selected);
			if (newSelected.has(orderId)) {
				newSelected.delete(orderId);
			} else {
				newSelected.add(orderId);
			}
			return newSelected;
		});
	}

	function selectAllOrders(): void {
		selectedOrders.set(new Set($filteredOrders.map(order => order.orderId)));
	}

	function clearSelection(): void {
		selectedOrders.set(new Set());
	}

	function clearFilters(): void {
		searchTerm.set('');
		selectedStatus.set('');
		fromDate.set('');
		toDate.set('');
	}

	function toggleFilters(): void {
		showFilters.update(show => !show);
	}

	function getFilterSummary(): string {
		let summary = [];
		if ($searchTerm) summary.push(`Tìm kiếm: "${$searchTerm}"`);
		if ($selectedStatus) summary.push(`Trạng thái: ${$selectedStatus}`);
		if ($fromDate) summary.push(`Từ: ${new Date($fromDate).toLocaleDateString('vi-VN')}`);
		if ($toDate) summary.push(`Đến: ${new Date($toDate).toLocaleDateString('vi-VN')}`);
		
		return summary.length > 0 ? summary.join(' • ') : 'Không có bộ lọc';
	}

	// Clear error after 5 seconds
	$: if ($error) {
		setTimeout(() => error.set(''), 5000);
	}

	onMount(() => {
		fetchOrders();
		fetchStatistics();
	});
</script>

<div class="p-6">
	<!-- Page Header -->
	<div class="mb-6">
		<div class="flex justify-between items-center">
			<div>
				<h2 class="text-2xl font-bold text-gray-900 mb-2">Quản lý Đơn hàng</h2>
				<p class="text-gray-600">Duyệt và quản lý các đơn hàng của khách hàng</p>
			</div>
			<div class="flex items-center space-x-4">
				<span class="text-sm text-gray-500">
					{$filteredOrders.length} / {$orders.length} đơn hàng
				</span>
				<button
					on:click={toggleFilters}
					class="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
				>
					<svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"/>
					</svg>
					{$showFilters ? 'Ẩn bộ lọc' : 'Hiện bộ lọc'}
				</button>
				<button 
					on:click={fetchOrders}
					class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
					disabled={$loading}
				>
					{$loading ? 'Đang tải...' : 'Tải lại'}
				</button>
			</div>
		</div>
	</div>

	<!-- Statistics Cards -->
	{#if Object.keys($statistics).length > 0}
		<div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
			<div class="bg-white p-4 rounded-lg shadow">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
							<svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
								<path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
							</svg>
						</div>
					</div>
					<div class="ml-3">
						<p class="text-sm font-medium text-gray-500">Tổng đơn hàng</p>
						<p class="text-lg font-semibold text-gray-900">{$statistics.totalOrders || 0}</p>
					</div>
				</div>
			</div>
			
			<div class="bg-white p-4 rounded-lg shadow">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
							<svg class="w-4 h-4 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
								<path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
								<path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"/>
							</svg>
						</div>
					</div>					<div class="ml-3">
						<p class="text-sm font-medium text-gray-500">Chờ xử lý</p>
						<p class="text-lg font-semibold text-gray-900">{$statistics.pendingOrders || 0}</p>
					</div>
				</div>
			</div>
			
			<div class="bg-white p-4 rounded-lg shadow">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
							<svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
								<path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
							</svg>
						</div>
					</div>
					<div class="ml-3">
						<p class="text-sm font-medium text-gray-500">Đã giao</p>
						<p class="text-lg font-semibold text-gray-900">{$statistics.completedOrders || 0}</p>
					</div>
				</div>
			</div>
			
			<div class="bg-white p-4 rounded-lg shadow">
				<div class="flex items-center">
					<div class="flex-shrink-0">
						<div class="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
							<svg class="w-4 h-4 text-red-600" fill="currentColor" viewBox="0 0 20 20">
								<path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
							</svg>
						</div>
					</div>
					<div class="ml-3">
						<p class="text-sm font-medium text-gray-500">Đã hủy</p>
						<p class="text-lg font-semibold text-gray-900">{$statistics.cancelledOrders || 0}</p>
					</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- Filters Panel -->
	{#if $showFilters}
		<div class="mb-6 bg-white rounded-lg shadow p-4">
			<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
				<!-- Search -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">Tìm kiếm</label>
					<input
						bind:value={$searchTerm}
						type="text"
						placeholder="Mã đơn hàng, tên khách hàng..."
						class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
					/>
				</div>

				<!-- Status Filter -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">Trạng thái</label>
					<select
						bind:value={$selectedStatus}
						class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
					>
						<option value="">Tất cả trạng thái</option>
						{#each statusOptions as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
				</div>

				<!-- From Date -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">Từ ngày</label>
					<input
						bind:value={$fromDate}
						type="date"
						class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
					/>
				</div>

				<!-- To Date -->
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">Đến ngày</label>
					<input
						bind:value={$toDate}
						type="date"
						class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
					/>
				</div>
			</div>

			<!-- Filter Summary and Clear -->
			<div class="flex items-center justify-between pt-3 border-t border-gray-200">
				<div class="text-sm text-gray-600">
					<span class="font-medium">Bộ lọc hiện tại:</span>
					<span>{getFilterSummary()}</span>
				</div>
				<button
					on:click={clearFilters}
					class="inline-flex items-center px-3 py-1 text-xs font-medium text-gray-600 hover:text-gray-800 border border-gray-300 rounded-md hover:bg-gray-100"
					disabled={!$searchTerm && !$selectedStatus && !$fromDate && !$toDate}
				>
					<svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
					</svg>
					Xóa bộ lọc
				</button>
			</div>
		</div>
	{/if}

	<!-- Alert Messages -->
	{#if $error}
		<div class="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
			{$error}
		</div>
	{/if}

	<!-- Orders Table -->
	{#if $loading}
		<div class="flex justify-center items-center py-8">
			<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
			<span class="ml-2 text-gray-600">Đang tải dữ liệu...</span>
		</div>
	{:else if $orders.length === 0}
		<div class="text-center py-8">
			<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">Không có đơn hàng</h3>
			<p class="mt-1 text-sm text-gray-500">Chưa có đơn hàng nào trong hệ thống.</p>
		</div>
	{:else if $filteredOrders.length === 0}
		<div class="text-center py-8">
			<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">Không tìm thấy đơn hàng</h3>
			<p class="mt-1 text-sm text-gray-500">Không có đơn hàng nào phù hợp với bộ lọc hiện tại.</p>
			<div class="mt-6">
				<button
					on:click={clearFilters}
					class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
				>
					Xóa bộ lọc
				</button>
			</div>
		</div>
	{:else}
		<div class="bg-white shadow overflow-hidden sm:rounded-md">
			<div class="px-4 py-5 sm:p-6">
				<div class="mb-4 flex items-center justify-between">
					<h3 class="text-lg leading-6 font-medium text-gray-900">
						Hiển thị {$filteredOrders.length} / {$orders.length} đơn hàng
					</h3>
					{#if $selectedOrders.size > 0}
						<div class="flex items-center space-x-2">
							<span class="text-sm text-gray-600">Đã chọn: {$selectedOrders.size}</span>
							<button
								on:click={clearSelection}
								class="text-sm text-blue-600 hover:text-blue-800"
							>
								Bỏ chọn
							</button>
						</div>
					{/if}
				</div>
				
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									<input
										type="checkbox"
										on:change={selectAllOrders}
										class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
									/>
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Mã đơn hàng
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Khách hàng
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Tổng tiền
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Trạng thái
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Ngày đặt
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									Thao tác
								</th>
							</tr>
						</thead>
						<tbody class="bg-white divide-y divide-gray-200">
							{#each $filteredOrders as order}
								<tr class="hover:bg-gray-50">
									<td class="px-6 py-4 whitespace-nowrap">
										<input
											type="checkbox"
											checked={$selectedOrders.has(order.orderId)}
											on:change={() => toggleOrderSelection(order.orderId)}
											class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
										/>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">
											#{order.orderId}
										</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">
											{order.user?.fullName || order.user?.username || 'N/A'}
										</div>
										<div class="text-sm text-gray-500">
											{order.user?.email || ''}
										</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">
											{formatPrice(order.totalAmount)}
										</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {getStatusColor(order.status)}">
											{order.status}
										</span>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm text-gray-900">
											{order.orderDate ? formatDate(order.orderDate) : 'N/A'}
										</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
										<div class="flex items-center space-x-2">
											<select
												value={order.status}
												on:change={(e) => updateOrderStatus(order.orderId, e.target.value)}
												class="text-sm border border-gray-300 rounded px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-500"
											>
												{#each statusOptions as option}
													<option value={option.value}>{option.label}</option>
												{/each}
											</select>											<button
												class="text-blue-600 hover:text-blue-900"
												on:click={() => goto(`/admin/orders/${order.orderId}`)}
											>
												Chi tiết
											</button>
										</div>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			</div>
		</div>
	{/if}
</div>
