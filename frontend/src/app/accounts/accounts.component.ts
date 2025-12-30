import { Component, OnInit } from '@angular/core';
import { AccountService } from '../account.service';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div style="padding:20px;">
    <h2>EGA Bank - My Accounts</h2>
    <button (click)="logout()" style="float:right; padding:8px 16px; background:#dc3545; color:white; border:none; cursor:pointer;">Logout</button>
    <div style="clear:both;"></div>

    <div *ngIf="accounts?.length===0" style="background:#f8f9fa; padding:20px; text-align:center;">
      <p>No accounts found. <a (click)="createNewAccount()" style="cursor:pointer; color:#007bff;">Create one</a></p>
    </div>

    <div *ngFor="let a of accounts" style="border:1px solid #ddd; margin:15px 0; padding:15px; border-radius:5px; max-width:600px;">
      <h4>Account: {{a.accountNumber}}</h4>
      <p><strong>Type:</strong> {{a.type}}</p>
      <p><strong>Balance:</strong> {{a.balance | currency}}</p>
      <p><strong>Owner:</strong> {{a.owner?.firstName}} {{a.owner?.lastName}}</p>

      <div style="margin-top:15px;">
        <button (click)="depositPrompt(a.accountNumber)" style="padding:8px 12px; margin-right:5px; background:#28a745; color:white; border:none; cursor:pointer; border-radius:3px;">Deposit</button>
        <button (click)="withdrawPrompt(a.accountNumber)" style="padding:8px 12px; margin-right:5px; background:#ffc107; color:black; border:none; cursor:pointer; border-radius:3px;">Withdraw</button>
        <button (click)="showTransferForm(a.accountNumber)" style="padding:8px 12px; margin-right:5px; background:#17a2b8; color:white; border:none; cursor:pointer; border-radius:3px;">Transfer</button>
        <button (click)="viewTransactionHistory(a.accountNumber)" style="padding:8px 12px; background:#007bff; color:white; border:none; cursor:pointer; border-radius:3px;">History</button>
      </div>

      <div *ngIf="selectedAccount === a.accountNumber && showTransfer" style="margin-top:15px; padding:15px; background:#f0f0f0; border-radius:3px;">
        <h5>Transfer Funds</h5>
        <div style="margin:10px 0;">
          <label>To Account Number</label>
          <input name="toAccount" [(ngModel)]="transferTo" style="width:100%; padding:8px; margin-top:5px;">
        </div>
        <div style="margin:10px 0;">
          <label>Amount</label>
          <input name="transferAmount" type="number" [(ngModel)]="transferAmount" style="width:100%; padding:8px; margin-top:5px;">
        </div>
        <button (click)="executeTransfer(a.accountNumber)" style="padding:8px 12px; background:#17a2b8; color:white; border:none; cursor:pointer; margin-right:5px;">Send</button>
        <button (click)="showTransfer=false" style="padding:8px 12px; background:#6c757d; color:white; border:none; cursor:pointer;">Cancel</button>
      </div>

      <div *ngIf="selectedAccount === a.accountNumber && showHistory" style="margin-top:15px; padding:15px; background:#f0f0f0; border-radius:3px; max-height:400px; overflow-y:auto;">
        <h5>Transaction History</h5>
        <div style="margin-bottom:10px;">
          <label>From Date</label>
          <input type="datetime-local" [(ngModel)]="historyStartDate" style="width:100%; padding:8px; margin-top:5px;">
          <label style="margin-top:10px;">To Date</label>
          <input type="datetime-local" [(ngModel)]="historyEndDate" style="width:100%; padding:8px; margin-top:5px;">
          <button (click)="loadTransactions(a.accountNumber)" style="padding:8px 12px; background:#007bff; color:white; border:none; cursor:pointer; margin-top:10px;">Load</button>
          <button (click)="downloadStatement(a.accountNumber, 'pdf')" style="padding:8px 12px; background:#6c757d; color:white; border:none; cursor:pointer; margin-left:5px; margin-top:10px;">Download PDF</button>
          <button (click)="downloadStatement(a.accountNumber, 'csv')" style="padding:8px 12px; background:#6c757d; color:white; border:none; cursor:pointer; margin-left:5px; margin-top:10px;">Download CSV</button>
        </div>
        <table style="width:100%; border-collapse:collapse; font-size:12px;">
          <thead>
            <tr style="background:#e9ecef;">
              <th style="border:1px solid #ddd; padding:8px;">ID</th>
              <th style="border:1px solid #ddd; padding:8px;">Type</th>
              <th style="border:1px solid #ddd; padding:8px;">Amount</th>
              <th style="border:1px solid #ddd; padding:8px;">Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let tx of transactions" style="background:#fff;">
              <td style="border:1px solid #ddd; padding:8px;">{{tx.id}}</td>
              <td style="border:1px solid #ddd; padding:8px;">{{tx.type}}</td>
              <td style="border:1px solid #ddd; padding:8px;">{{tx.amount | currency}}</td>
              <td style="border:1px solid #ddd; padding:8px;">{{tx.timestamp | date:'short'}}</td>
            </tr>
          </tbody>
        </table>
        <p *ngIf="transactions.length === 0" style="margin-top:10px; text-align:center; color:#6c757d;">No transactions found</p>
        <button (click)="showHistory=false" style="padding:8px 12px; background:#6c757d; color:white; border:none; cursor:pointer; margin-top:10px;">Close</button>
      </div>
    </div>

    <p *ngIf="error" style="color:red; margin-top:20px;">{{error}}</p>
  </div>
  `,
  styles: []
})
export class AccountsComponent implements OnInit {
  accounts: any[] = [];
  transactions: any[] = [];
  selectedAccount: string | null = null;
  showTransfer = false;
  showHistory = false;
  transferTo = '';
  transferAmount = 0;
  historyStartDate = '';
  historyEndDate = '';
  error: string | null = null;

  constructor(private svc: AccountService, private auth: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.svc.getAccounts().subscribe({
      next: (res: any) => this.accounts = res,
      error: (err) => {
        this.error = 'Failed to load accounts';
        this.accounts = [];
      }
    });
  }

  depositPrompt(acc: string) {
    const amount = Number(prompt('Amount to deposit') || '0');
    if (amount > 0) {
      this.svc.deposit(acc, amount).subscribe({
        next: () => {
          alert('Deposit successful');
          this.load();
        },
        error: (e) => alert(e?.error?.message || 'Deposit failed')
      });
    }
  }

  withdrawPrompt(acc: string) {
    const amount = Number(prompt('Amount to withdraw') || '0');
    if (amount > 0) {
      this.svc.withdraw(acc, amount).subscribe({
        next: () => {
          alert('Withdrawal successful');
          this.load();
        },
        error: (e) => alert(e?.error?.message || 'Withdrawal failed')
      });
    }
  }

  showTransferForm(acc: string) {
    this.selectedAccount = acc;
    this.showTransfer = true;
    this.showHistory = false;
  }

  executeTransfer(from: string) {
    if (!this.transferTo || this.transferAmount <= 0) {
      alert('Please enter valid destination and amount');
      return;
    }
    this.svc.transfer(from, this.transferTo, this.transferAmount).subscribe({
      next: () => {
        alert('Transfer successful');
        this.showTransfer = false;
        this.transferTo = '';
        this.transferAmount = 0;
        this.load();
      },
      error: (e) => alert(e?.error?.message || 'Transfer failed')
    });
  }

  viewTransactionHistory(acc: string) {
    this.selectedAccount = acc;
    this.showHistory = true;
    this.showTransfer = false;
    const now = new Date();
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    this.historyStartDate = thirtyDaysAgo.toISOString().slice(0, 16);
    this.historyEndDate = now.toISOString().slice(0, 16);
    this.loadTransactions(acc);
  }

  loadTransactions(acc: string) {
    if (!this.historyStartDate || !this.historyEndDate) {
      alert('Please select date range');
      return;
    }
    const start = new Date(this.historyStartDate).toISOString();
    const end = new Date(this.historyEndDate).toISOString();
    this.svc.getTransactions(acc, start, end).subscribe({
      next: (res: any) => this.transactions = res,
      error: (e) => alert(e?.error?.message || 'Failed to load transactions')
    });
  }

  downloadStatement(acc: string, format: 'pdf' | 'csv') {
    const start = new Date(this.historyStartDate).toISOString();
    const end = new Date(this.historyEndDate).toISOString();
    const url = format === 'pdf' 
      ? this.svc.getStatementUrl(acc, start, end) 
      : this.svc.getStatementCsvUrl(acc, start, end);
    window.open(url, '_blank');
  }

  createNewAccount() {
    alert('Create account feature coming soon');
  }

  logout() {
    this.auth.logout();
    window.location.href = '/';
  }
}

