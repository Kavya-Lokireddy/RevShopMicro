import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Notification {
    id: number;
    userId: number;
    message: string;
    read: boolean;
    createdAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class NotificationService {

    private baseUrl = '/api/notifications';

    constructor(private http: HttpClient) { }

    getUserNotifications(): Observable<Notification[]> {
        return this.http.get<any[]>(this.baseUrl).pipe(
            map((notifications) => notifications.map((notification) => this.normalizeNotification(notification)))
        );
    }

    getUnreadNotifications(): Observable<Notification[]> {
        return this.http.get<any[]>(`${this.baseUrl}/unread`).pipe(
            map((notifications) => notifications.map((notification) => this.normalizeNotification(notification)))
        );
    }

    markAsRead(id: number): Observable<any> {
        return this.http.put(`${this.baseUrl}/${id}/read`, {}, { responseType: 'text' });
    }

    private normalizeNotification(notification: any): Notification {
        return {
            id: notification.id,
            userId: notification.userId,
            message: notification.message,
            read: Boolean(notification.read ?? notification.isRead),
            createdAt: notification.createdAt
        };
    }
}

