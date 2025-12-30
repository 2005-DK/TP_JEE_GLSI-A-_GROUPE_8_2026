import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div style="max-width:400px; margin:50px auto; padding:20px; border:1px solid #ccc; border-radius:5px;">
    <h2 style="text-align:center;">EGA Bank</h2>
    <h3 *ngIf="!showRegister" style="text-align:center;">Login</h3>
    <h3 *ngIf="showRegister" style="text-align:center;">Register</h3>
    
    <form *ngIf="!showRegister" (ngSubmit)="onSubmit($event)">
      <div style="margin:15px 0;">
        <label>Username</label>
        <input name="username" [(ngModel)]="username" style="width:100%; padding:8px; margin-top:5px;" required>
      </div>
      <div style="margin:15px 0;">
        <label>Password</label>
        <input name="password" type="password" [(ngModel)]="password" style="width:100%; padding:8px; margin-top:5px;" required>
      </div>
      <button type="submit" style="width:100%; padding:10px; background:#007bff; color:white; border:none; cursor:pointer; border-radius:3px;">Login</button>
      <p style="text-align:center; margin-top:15px;">
        Don't have an account? <a (click)="showRegister=true" style="cursor:pointer; color:#007bff;">Register here</a>
      </p>
    </form>

    <form *ngIf="showRegister" (ngSubmit)="onRegister($event)">
      <div style="margin:15px 0;">
        <label>Username</label>
        <input name="regUsername" [(ngModel)]="regUsername" style="width:100%; padding:8px; margin-top:5px;" required>
      </div>
      <div style="margin:15px 0;">
        <label>Password</label>
        <input name="regPassword" type="password" [(ngModel)]="regPassword" style="width:100%; padding:8px; margin-top:5px;" required>
      </div>
      <button type="submit" style="width:100%; padding:10px; background:#28a745; color:white; border:none; cursor:pointer; border-radius:3px; margin-right:10px;">Register</button>
      <button type="button" (click)="showRegister=false" style="width:45%; padding:10px; background:#6c757d; color:white; border:none; cursor:pointer; border-radius:3px; margin-top:10px;">Cancel</button>
    </form>

    <p *ngIf="error" style="color:red; margin-top:15px; text-align:center;">{{error}}</p>
    <p *ngIf="success" style="color:green; margin-top:15px; text-align:center;">{{success}}</p>
  </div>
  `
})
export class LoginComponent {
  username = '';
  password = '';
  regUsername = '';
  regPassword = '';
  error: string | null = null;
  success: string | null = null;
  showRegister = false;

  constructor(private auth: AuthService) {}

  onSubmit(e: Event) {
    e.preventDefault();
    this.error = null;
    this.auth.login(this.username, this.password).subscribe({
      next: (res: any) => {
        if (res?.token) {
          this.auth.saveToken(res.token);
          window.location.href = '/app';
        } else {
          this.error = 'Invalid response from server';
        }
      },
      error: err => this.error = err?.error?.message || 'Login failed'
    });
  }

  onRegister(e: Event) {
    e.preventDefault();
    this.error = null;
    this.success = null;
    this.auth.register(this.regUsername, this.regPassword).subscribe({
      next: () => {
        this.success = 'Registration successful! You can now login.';
        setTimeout(() => {
          this.showRegister = false;
          this.regUsername = '';
          this.regPassword = '';
          this.success = null;
        }, 1500);
      },
      error: err => this.error = err?.error || 'Registration failed'
    });
  }
}
