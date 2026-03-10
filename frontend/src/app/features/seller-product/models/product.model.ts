export interface Product {

    id?: number;
    name: string;
    description: string;
    price: number;
    mrp: number;
    discountPercentage?: number;
    category: any;        // API returns {id, name} object
    categoryName?: string; // API also returns this as a flat string
    quantity: number;
    sellerId: number;
    active?: boolean;
    stockThreshold: number;
    categoryId:number
}
