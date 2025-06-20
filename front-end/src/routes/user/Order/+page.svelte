<!-- front-end/src/routes/user/Order/+page.svelte -->
<script lang="ts">
	import { onMount } from 'svelte';
	import { authenticatedFetch, isAuthenticated } from '$lib/auth';
	import { goto } from '$app/navigation';
	
	// Order interface matching the backend DTO structure
	interface OrderDetail {
		orderDetailId: number;
		orderId: number;
		bookId: number;
		bookTitle: string;
		bookAuthor: string;
		bookImageUrl?: string;
		quantity: number;
		priceAtOrder: number;
		subtotal: number;
	}

	interface Order {
		orderId: number;
		userId: number;
		username: string;
		totalAmount: number;
		shippingAddress: string;
		paymentMethod: string;
		status: string;
		orderDate: string;
		orderDetails: OrderDetail[];
	}

	let orders: Order[] = [];
	let loading = true;
	let error = '';

	// Load orders on component mount
	onMount(async () => {
		// Check authentication
		if (!isAuthenticated()) {
			goto('/auth/login');
			return;
		}
		
		await fetchOrders();
	});

	async function fetchOrders() {
		try {
			loading = true;
			error = '';
			
			const response = await authenticatedFetch('http://localhost:8080/api/user/payment/orders', {
				method: 'GET'
			});

			const result = await response.json();
			
			if (result.success) {
				orders = result.data || [];
			} else {
				error = result.message || 'Không thể tải danh sách đơn hàng';
			}
		} catch (err) {
			console.error('Error fetching orders:', err);
			error = 'Có lỗi xảy ra khi tải danh sách đơn hàng';
		} finally {
			loading = false;
		}
	}	function getStatusColor(status: string): string {
		switch (status) {
			case 'Đã giao':
			case 'Đã giao hàng':
			case 'Delivered':
				return 'bg-green-100 text-green-800';
			case 'Đang xử lý':
			case 'Processing':
				return 'bg-yellow-100 text-yellow-800';
			case 'Đã hủy':
			case 'Cancelled':
				return 'bg-red-100 text-red-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function getStatusText(status: string): string {
		switch (status) {
			case 'Đã giao':
			case 'Đã giao hàng':
			case 'Delivered':
				return 'Đã giao';
			case 'Đang xử lý':
			case 'Processing':
				return 'Đang xử lý';
			case 'Đã hủy':
			case 'Cancelled':
				return 'Đã hủy';
			default:
				return status || 'Không xác định';
		}
	}

	function formatPrice(price: number): string {
		return new Intl.NumberFormat('vi-VN', {
			style: 'currency',
			currency: 'VND'
		}).format(price);
	}
	function formatDate(dateString: string): string {
		if (!dateString) return '';
		// Handle both "yyyy-MM-dd HH:mm:ss" and ISO date formats
		const date = new Date(dateString);
		return date.toLocaleDateString('vi-VN');
	}

	let selectedOrder: Order | null = null;

	function viewOrderDetails(order: Order) {
		selectedOrder = order;
	}

	function closeOrderDetails() {
		selectedOrder = null;
	}
	// Helper function to check if order is delivered for "buy again" functionality
	function isOrderDelivered(status: string): boolean {
		return status === 'Đã giao hàng' || status === 'Delivered' || status === 'Đã giao';
	}

	// Helper function to check if order can be cancelled
	function canCancelOrder(status: string): boolean {
		return status === 'Đang xử lý' || status === 'Processing';
	}

	// Cancel order function
	async function cancelOrder(orderId: number) {
		if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
			return;
		}

		try {
			const response = await authenticatedFetch(`http://localhost:8080/api/user/payment/orders/${orderId}/cancel`, {
				method: 'POST'
			});

			const result = await response.json();
			
			if (result.success) {
				// Update the order status in the local state
				orders = orders.map(order => 
					order.orderId === orderId 
						? { ...order, status: 'Đã hủy' }
						: order
				);
				alert('Đã hủy đơn hàng thành công!');
			} else {
				alert('Lỗi: ' + result.message);
			}
		} catch (err) {
			console.error('Error cancelling order:', err);
			alert('Có lỗi xảy ra khi hủy đơn hàng');
		}
	}
</script>

<svelte:head>
	<title>Đơn hàng - BookStore</title>
</svelte:head>

<div class="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
	<!-- Page Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-gray-900">Đơn hàng của bạn</h1>
		<p class="mt-2 text-gray-600">Theo dõi trạng thái và lịch sử đơn hàng</p>
	</div>

	{#if loading}
		<!-- Loading state -->
		<div class="text-center py-12">
			<div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
			<p class="mt-2 text-gray-600">Đang tải danh sách đơn hàng...</p>
		</div>
	{:else if error}
		<!-- Error state -->
		<div class="text-center py-12">
			<svg class="mx-auto h-12 w-12 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.464 0L4.35 16.5c-.77.833.192 2.5 1.732 2.5z"/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">Không thể tải đơn hàng</h3>
			<p class="mt-1 text-sm text-gray-500">{error}</p>
			<div class="mt-6">
				<button 
					on:click={fetchOrders}
					class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
				>
					Thử lại
				</button>
			</div>
		</div>
	{:else if orders.length === 0}
		<!-- Empty orders -->
		<div class="text-center py-12">
			<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">Chưa có đơn hàng</h3>
			<p class="mt-1 text-sm text-gray-500">Bạn chưa có đơn hàng nào. Hãy bắt đầu mua sắm!</p>
			<div class="mt-6">
				<a href="/user" class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
					Bắt đầu mua sắm
				</a>
			</div>
		</div>
	{:else}
		<!-- Orders list -->
		<div class="bg-white shadow overflow-hidden sm:rounded-md">
			<ul class="divide-y divide-gray-200">				{#each orders as order}
					<li class="px-6 py-6">
						<div class="flex items-center justify-between">
							<div class="flex-1">
								<div class="flex items-center justify-between">
									<div>
										<h3 class="text-lg font-medium text-gray-900">
											Đơn hàng #{order.orderId}
										</h3>
										<p class="text-sm text-gray-500 mt-1">
											Đặt ngày: {formatDate(order.orderDate)}
										</p>
									</div>
									<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium {getStatusColor(order.status)}">
										{getStatusText(order.status)}
									</span>
								</div>
								
								<div class="mt-3">
									<p class="text-sm text-gray-600">
										{order.orderDetails.length} sản phẩm • Tổng: {formatPrice(order.totalAmount)}
									</p>
									<div class="mt-2 text-sm text-gray-500">
										{#each order.orderDetails as detail, index}
											{detail.bookTitle}{index < order.orderDetails.length - 1 ? ', ' : ''}
										{/each}
									</div>
								</div>
							</div>							<div class="ml-6 flex flex-col space-y-2">
								<button 
									on:click={() => viewOrderDetails(order)}
									class="inline-flex items-center px-3 py-2 border border-blue-600 rounded-md text-sm font-medium text-blue-600 hover:bg-blue-50 transition-colors duration-200"
								>
									<svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
										<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
									</svg>
									Xem chi tiết
								</button>
								
								{#if canCancelOrder(order.status)}
									<button 
										on:click={() => cancelOrder(order.orderId)}
										class="inline-flex items-center px-3 py-2 border border-red-600 rounded-md text-sm font-medium text-red-600 hover:bg-red-50 transition-colors duration-200"
									>
										<svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
										</svg>
										Hủy đơn hàng
									</button>
								{/if}
								
								{#if isOrderDelivered(order.status)}
									<button class="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors duration-200">
										<svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3"/>
										</svg>
										Mua lại
									</button>
								{/if}
							</div>
						</div>
					</li>
				{/each}
			</ul>
		</div>
	{/if}
</div>

<!-- Order Details Modal -->
{#if selectedOrder}
	<div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" on:click={closeOrderDetails}>
		<div class="relative top-20 mx-auto p-5 border w-11/12 max-w-2xl shadow-lg rounded-md bg-white" on:click|stopPropagation>			<!-- Modal header -->
			<div class="flex items-center justify-between pb-4 border-b">
				<h3 class="text-lg font-medium text-gray-900">
					Chi tiết đơn hàng #{selectedOrder.orderId}
				</h3>
				<button 
					on:click={closeOrderDetails}
					class="text-gray-400 hover:text-gray-600"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
					</svg>
				</button>
			</div>

			<!-- Modal content -->
			<div class="mt-4">
				<div class="grid grid-cols-2 gap-4 mb-6">
					<div>
						<p class="text-sm font-medium text-gray-500">Ngày đặt hàng</p>
						<p class="text-sm text-gray-900">{formatDate(selectedOrder.orderDate)}</p>
					</div>
					<div>
						<p class="text-sm font-medium text-gray-500">Trạng thái</p>
						<span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium {getStatusColor(selectedOrder.status)}">
							{getStatusText(selectedOrder.status)}
						</span>
					</div>
					<div>
						<p class="text-sm font-medium text-gray-500">Phương thức thanh toán</p>
						<p class="text-sm text-gray-900">{selectedOrder.paymentMethod}</p>
					</div>
					<div>
						<p class="text-sm font-medium text-gray-500">Địa chỉ giao hàng</p>
						<p class="text-sm text-gray-900">{selectedOrder.shippingAddress}</p>
					</div>
				</div>

				<div class="mb-6">
					<h4 class="text-sm font-medium text-gray-900 mb-3">Sản phẩm đã đặt</h4>
					<div class="border rounded-lg divide-y">
						{#each selectedOrder.orderDetails as detail}
							<div class="p-4 flex justify-between items-center">
								<div>
									<p class="text-sm font-medium text-gray-900">{detail.bookTitle}</p>
									<p class="text-sm text-gray-500">Tác giả: {detail.bookAuthor || 'Không rõ'}</p>
									<p class="text-sm text-gray-500">Số lượng: {detail.quantity} x {formatPrice(detail.priceAtOrder)}</p>
								</div>
								<p class="text-sm font-medium text-gray-900">{formatPrice(detail.subtotal)}</p>
							</div>
						{/each}
					</div>
				</div>

				<div class="border-t pt-4">
					<div class="flex justify-between text-base font-medium text-gray-900">
						<span>Tổng cộng</span>
						<span>{formatPrice(selectedOrder.totalAmount)}</span>
					</div>
				</div>
			</div>

			<!-- Modal footer -->
			<div class="mt-6 flex justify-end">
				<button 
					on:click={closeOrderDetails}
					class="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors duration-200"
				>
					Đóng
				</button>
			</div>
		</div>
	</div>
{/if}
