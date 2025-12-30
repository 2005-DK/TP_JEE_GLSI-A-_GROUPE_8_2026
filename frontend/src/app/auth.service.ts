import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

const apiBase = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(username: string, password: string) {
    return this.http.post<any>(`${apiBase}/api/auth/login`, { username, password });
  }

  register(username: string, password: string) {
    return this.http.post<any>(`${apiBase}/api/auth/register`, { username, password });
  }

  saveToken(token: string) {
    localStorage.setItem('ega_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('ega_token');
  }

  getAuthHeaders() {
    const token = this.getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  logout() {
    localStorage.removeItem('ega_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
