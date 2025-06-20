<!-- front-end/src/routes/admin/orders/[id]/+page.svelte -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { authenticatedFetch } from '$lib/auth';
	import type { OrderDTO, OrderDetailDTO } from '$lib/types';

	// Get order ID from route parameters
	$: orderId = $page.params.id;

	// Component state
	let order: OrderDTO | null = null;
	let loading = true;
	let error = '';
	let updating = false;

	// Order status options
	const statusOptions = [
		{ value: 'Đang xử lý', label: 'Đang xử lý', color: 'bg-yellow-100 text-yellow-800' },
		{ value: 'Đã giao', label: 'Đã giao', color: 'bg-green-100 text-green-800' },
		{ value: 'Đã hủy', label: 'Đã hủy', color: 'bg-red-100 text-red-800' }
	];

	// API URL
	const ORDERS_API = 'http://localhost:8080/api/admin/orders';

	onMount(() => {
		if (orderId) {
			fetchOrderDetails();
		}
	});

	async function fetchOrderDetails(): Promise<void> {
		loading = true;
		try {
			const response = await authenticatedFetch(`${ORDERS_API}/${orderId}`);
			if (response.ok) {
				const result = await response.json();
				if (result.success && result.data) {
					order = result.data;
					error = '';
				} else {
					error = result.message || 'Không thể tải thông tin đơn hàng';
				}
			} else {
				error = 'Lỗi kết nối đến server';
			}
		} catch (err) {
			console.error('Fetch error:', err);
			error = 'Lỗi kết nối: ' + (err as Error).message;
		} finally {
			loading = false;
		}
	}

	async function updateOrderStatus(newStatus: string): Promise<void> {
		if (!order) return;
		
		const confirmed = confirm(`Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng thành "${newStatus}"?`);
		if (!confirmed) return;

		updating = true;
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
					order = { ...order, status: newStatus };
					alert('Cập nhật trạng thái đơn hàng thành công!');
				} else {
					alert('Lỗi: ' + result.message);
				}
			} else {
				alert('Lỗi kết nối khi cập nhật trạng thái');
			}
		} catch (err) {
			console.error('Update status error:', err);
			alert('Lỗi khi cập nhật trạng thái: ' + (err as Error).message);
		} finally {
			updating = false;
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

	function calculateSubtotal(quantity: number, price: number): number {
		return quantity * price;
	}

	function goBack(): void {
		goto('/admin/orders');
	}
</script>

<svelte:head>
	<title>Chi tiết đơn hàng #{orderId} - Admin</title>
</svelte:head>

<div class="p-6">
	<!-- Breadcrumb -->
	<nav class="flex mb-4" aria-label="Breadcrumb">
		<ol class="inline-flex items-center space-x-1 md:space-x-3">
			<li class="inline-flex items-center">
				<button
					on:click={goBack}
					class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600"
				>
					<svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
						<path fill-rule="evenodd" d="M7.707 14.707a1 1 0 01-1.414 0L2.586 11H17a1 1 0 110 2H2.586l3.707 3.707a1 1 0 01-1.414 1.414l-5.414-5.414a1 1 0 010-1.414L4.879 6.879a1 1 0 011.414 1.414L2.586 11z"/>
					</svg>
					Quản lý đơn hàng
				</button>
			</li>
			<li>
				<div class="flex items-center">
					<svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
						<path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"/>
					</svg>
					<span class="ml-1 text-sm font-medium text-gray-500 md:ml-2">Đơn hàng #{orderId}</span>
				</div>
			</li>
		</ol>
	</nav>

	<!-- Page Header -->
	<div class="mb-6">
		<div class="flex justify-between items-center">
			<div>
				<h2 class="text-2xl font-bold text-gray-900 mb-2">Chi tiết đơn hàng #{orderId}</h2>
				<p class="text-gray-600">Xem thông tin chi tiết và quản lý trạng thái đơn hàng</p>
			</div>
			<div class="flex items-center space-x-4">
				<button 
					on:click={fetchOrderDetails}
					class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
					disabled={loading}
				>
					{loading ? 'Đang tải...' : 'Tải lại'}
				</button>
			</div>
		</div>
	</div>

	<!-- Error Message -->
	{#if error}
		<div class="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
			{error}
		</div>
	{/if}

	<!-- Loading State -->
	{#if loading}
		<div class="flex justify-center items-center py-8">
			<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
			<span class="ml-2 text-gray-600">Đang tải thông tin đơn hàng...</span>
		</div>
	{:else if order}
		<!-- Order Details -->
		<div class="bg-white shadow overflow-hidden sm:rounded-lg">
			<!-- Order Information -->
			<div class="px-4 py-5 sm:px-6 border-b border-gray-200">
				<h3 class="text-lg leading-6 font-medium text-gray-900">Thông tin đơn hàng</h3>
				<p class="mt-1 max-w-2xl text-sm text-gray-500">Chi tiết và trạng thái đơn hàng</p>
			</div>
			
			<div class="px-4 py-5 sm:p-6">
				<dl class="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
					<div>
						<dt class="text-sm font-medium text-gray-500">Mã đơn hàng</dt>
						<dd class="mt-1 text-sm text-gray-900">#{order.orderId}</dd>
					</div>
					
					<div>
						<dt class="text-sm font-medium text-gray-500">Ngày đặt hàng</dt>
						<dd class="mt-1 text-sm text-gray-900">{order.orderDate ? formatDate(order.orderDate) : 'N/A'}</dd>
					</div>
					
					<div>
						<dt class="text-sm font-medium text-gray-500">Khách hàng</dt>
						<dd class="mt-1 text-sm text-gray-900">
							<div>
								<p class="font-medium">{order.user?.fullName || order.user?.username || 'N/A'}</p>
								<p class="text-gray-500">{order.user?.email || ''}</p>
								<p class="text-gray-500">{order.user?.phone || ''}</p>
							</div>
						</dd>
					</div>
					
					<div>
						<dt class="text-sm font-medium text-gray-500">Trạng thái hiện tại</dt>
						<dd class="mt-1">
							<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {getStatusColor(order.status)}">
								{order.status}
							</span>
						</dd>
					</div>
					
					<div>
						<dt class="text-sm font-medium text-gray-500">Tổng tiền</dt>
						<dd class="mt-1 text-sm text-gray-900 font-semibold">{formatPrice(order.totalAmount)}</dd>
					</div>
					
					<div>
						<dt class="text-sm font-medium text-gray-500">Địa chỉ giao hàng</dt>
						<dd class="mt-1 text-sm text-gray-900">{order.shippingAddress}</dd>
					</div>
				</dl>
			</div>
		</div>

		<!-- Status Update Section -->
		<div class="mt-6 bg-white shadow sm:rounded-lg">
			<div class="px-4 py-5 sm:px-6 border-b border-gray-200">
				<h3 class="text-lg leading-6 font-medium text-gray-900">Cập nhật trạng thái</h3>
				<p class="mt-1 max-w-2xl text-sm text-gray-500">Thay đổi trạng thái đơn hàng</p>
			</div>
			
			<div class="px-4 py-5 sm:p-6">
				<div class="flex items-center space-x-4">
					<label for="status-select" class="text-sm font-medium text-gray-700">Trạng thái mới:</label>
					<select
						id="status-select"
						bind:value={order.status}
						on:change={(e) => updateOrderStatus(e.target.value)}
						disabled={updating}
						class="flex-1 max-w-xs border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
					>
						{#each statusOptions as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
					{#if updating}
						<div class="flex items-center text-sm text-gray-500">
							<div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
							Đang cập nhật...
						</div>
					{/if}
				</div>
				
				<div class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-md">
					<div class="flex">
						<svg class="flex-shrink-0 w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
							<path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/>
						</svg>
						<div class="ml-3">
							<h3 class="text-sm font-medium text-yellow-800">Lưu ý quan trọng</h3>
							<div class="mt-2 text-sm text-yellow-700">
								<ul class="list-disc pl-5 space-y-1">
									<li>Khi chuyển trạng thái thành <strong>"Đã giao"</strong>, hệ thống sẽ tự động cập nhật kho và tạo giao dịch xuất hàng</li>
									<li>Đơn hàng đã giao hoặc đã hủy không thể thay đổi trạng thái</li>
									<li>Thao tác này không thể hoàn tác</li>
								</ul>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Order Details -->
		{#if order.orderDetails && order.orderDetails.length > 0}
			<div class="mt-6 bg-white shadow overflow-hidden sm:rounded-lg">
				<div class="px-4 py-5 sm:px-6 border-b border-gray-200">
					<h3 class="text-lg leading-6 font-medium text-gray-900">Chi tiết sản phẩm</h3>
					<p class="mt-1 max-w-2xl text-sm text-gray-500">Danh sách sản phẩm trong đơn hàng</p>
				</div>
				
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sản phẩm</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Giá</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số lượng</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thành tiền</th>
							</tr>
						</thead>
						<tbody class="bg-white divide-y divide-gray-200">
							{#each order.orderDetails as detail}
								<tr>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">{detail.bookTitle || `Book ID: ${detail.bookId}`}</div>
										<div class="text-sm text-gray-500">{detail.bookAuthor || ''}</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm text-gray-900">{formatPrice(detail.priceAtOrder)}</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm text-gray-900">{detail.quantity}</div>
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="text-sm font-medium text-gray-900">{formatPrice(calculateSubtotal(detail.quantity, detail.priceAtOrder))}</div>
									</td>
								</tr>
							{/each}
						</tbody>
						<tfoot class="bg-gray-50">
							<tr>
								<td colspan="3" class="px-6 py-4 text-right text-sm font-medium text-gray-900">Tổng cộng:</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="text-sm font-bold text-gray-900">{formatPrice(order.totalAmount)}</div>
								</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>
		{/if}
	{:else}
		<!-- Order Not Found -->
		<div class="text-center py-12">
			<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">Không tìm thấy đơn hàng</h3>
			<p class="mt-1 text-sm text-gray-500">Đơn hàng với ID #{orderId} không tồn tại hoặc đã bị xóa.</p>
			<div class="mt-6">
				<button
					on:click={goBack}
					class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
				>
					Quay lại danh sách
				</button>
			</div>
		</div>
	{/if}
</div>
