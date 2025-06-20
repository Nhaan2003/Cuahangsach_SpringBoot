// Types for the bookstore application

export interface User {
	userId: number;
	username: string;
	email: string;
	fullName?: string;
	phone?: string;
	address?: string;
	role: 'KH' | 'Nvien' | 'Qly';
	status: 'Active' | 'Lock';
	createdAt?: string;
}

export interface UserFormData {
	username: string;
	password: string;
	email: string;
	fullName: string;
	phone: string;
	address: string;
	role: 'KH' | 'Nvien' | 'Qly';
	status: 'Active' | 'Lock';
}

export interface OverviewStats {
	totalUsers: number;
	totalProducts: number;
	totalOrders: number;
}

export interface Product {
	bookId: number;
	title: string;
	author?: string;
	publisher?: string;
	publicationYear?: number;
	description?: string;
	price: number;
	stockQuantity?: number;
	categories?: Category[];
	images?: BookImage[];
}

export interface ProductFormData {
	title: string;
	author: string;
	publisher: string;
	publicationYear: number | null;
	description: string;
	price: number | null;
	stockQuantity: number;
	categoryIds: number[];
}

export interface BookImage {
	imageId: number;
	bookId?: number;
	bookTitle?: string;
	imageUrl: string;
	description?: string;
	isPrimary: boolean;
	uploadedAt?: string;
}

export interface Category {
	categoryId: number;
	categoryName: string;
}

export interface InventoryDTO {
	transactionId?: number;
	bookId: number;
	transactionType: 'Nhập' | 'Xuất';
	quantity: number;
	price: number;
	userId: number;
	transactionDate?: string;
	// Additional info for display
	bookTitle?: string;
	username?: string;
}

export interface ShoppingCartItem {
	userId: number;
	bookId: number;
	bookTitle: string;
	bookAuthor?: string;
	bookPublisher?: string;
	bookDescription?: string;
	bookPrice: number;
	stockQuantity: number;
}

export interface ApiResponse<T = any> {
	success: boolean;
	data?: T;
	message: string;
}

export interface OrderDTO {
	orderId: number;
	user?: User;
	orderDate: string;
	totalAmount: number;
	status: string;
	shippingAddress: string;
	orderDetails?: OrderDetailDTO[];
}

export interface OrderDetailDTO {
	orderDetailId?: number;
	orderId?: number;
	bookId: number;
	bookTitle?: string;
	bookAuthor?: string;
	quantity: number;
	priceAtOrder: number;
}

export interface PaymentDTO {
	paymentId?: number;
	orderId: number;
	amount: number;
	paymentMethod: string;
	paymentDate: string;
	status?: string;
}

export interface CheckoutRequestDTO {
	userId: number;
	items: CheckoutItemDTO[];
	totalAmount: number;
	shippingAddress: string;
	paymentMethod: string;
}

export interface CheckoutItemDTO {
	bookId: number;
	quantity: number;
	price: number;
	subtotal: number;
}

export interface CheckoutResponseDTO {
	success: boolean;
	message: string;
	order?: OrderDTO;
	payment?: PaymentDTO;
}
