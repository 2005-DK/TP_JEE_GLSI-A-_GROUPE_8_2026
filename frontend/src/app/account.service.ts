import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';
import { map } from 'rxjs/operators';

const apiBase = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AccountService {
  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers() {
    const token = this.auth.getToken();
    return token ? { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) } : {};
  }

  getAccounts() {
    // Fetch clients and flatten nested accounts into a single accounts array
    return this.http.get<any[]>(`${apiBase}/api/clients`, this.headers()).pipe(
      map((clients: any[]) => clients.flatMap(c => (c.accounts || []).map((a: any) => ({ ...a, owner: { id: c.id, firstName: c.firstName, lastName: c.lastName } }))))
    );
  }

  createAccount(clientId: number, type: string) {
    return this.http.post(`${apiBase}/api/accounts`, { clientId, type }, this.headers());
  }

  deposit(accountNumber: string, amount: number) {
    return this.http.post(`${apiBase}/api/accounts/${accountNumber}/deposit`, { amount }, this.headers());
  }

  withdraw(accountNumber: string, amount: number) {
    return this.http.post(`${apiBase}/api/accounts/${accountNumber}/withdraw`, { amount }, this.headers());
  }

  transfer(from: string, to: string, amount: number) {
    return this.http.post(`${apiBase}/api/accounts/transfer`, { fromAccount: from, toAccount: to, amount }, this.headers());
  }

  getTransactions(accountNumber: string, start: string, end: string) {
    return this.http.get(`${apiBase}/api/accounts/${accountNumber}/transactions?start=${start}&end=${end}`, this.headers());
  }

  getStatementUrl(accountNumber: string, start: string, end: string) {
    const token = this.auth.getToken();
    const authParam = token ? `&auth=${token}` : '';
    return `${apiBase}/api/accounts/${accountNumber}/statement.pdf?start=${start}&end=${end}${authParam}`;
  }

  getStatementCsvUrl(accountNumber: string, start: string, end: string) {
    const token = this.auth.getToken();
    const authParam = token ? `&auth=${token}` : '';
    return `${apiBase}/api/accounts/${accountNumber}/statement?start=${start}&end=${end}${authParam}`;
  }
}
