import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExpenseService } from './expense.service';
import { Expense, EXPENSE_CATEGORIES, PAYMENT_METHODS } from './expense.models';
import { ToastService } from '../shared/toast.service';

@Component({
  selector: 'rf-expenses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expenses.component.html',
  styleUrls: ['./expenses.component.scss']
})
export class ExpensesComponent implements OnInit {
  expenseForm: FormGroup;
  expenses = signal<Expense[]>([]);
  isLoading = signal<boolean>(false);
  isSaving = signal<boolean>(false);
  editingId = signal<number | null>(null);
  showForm = signal<boolean>(false);

  categories = EXPENSE_CATEGORIES;
  paymentMethods = PAYMENT_METHODS;

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService,
    private toast: ToastService
  ) {
    this.expenseForm = this.fb.group({
      expenseDate: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      vendorName: ['', Validators.required],
      category: ['', Validators.required],
      paymentMethod: [''],
      referenceNumber: [''],
      description: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.isLoading.set(true);
    this.expenseService.getAllExpenses().subscribe({
      next: (expenses) => {
        this.expenses.set(expenses);
        this.isLoading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load expenses');
        this.isLoading.set(false);
      }
    });
  }

  showAddForm(): void {
    this.showForm.set(true);
    this.editingId.set(null);
    this.expenseForm.reset({
      expenseDate: new Date().toISOString().split('T')[0]
    });
  }

  hideForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.expenseForm.reset();
  }

  editExpense(expense: Expense): void {
    this.showForm.set(true);
    this.editingId.set(expense.id!);
    this.expenseForm.patchValue({
      expenseDate: expense.expenseDate,
      amount: expense.amount,
      vendorName: expense.vendorName,
      category: expense.category,
      paymentMethod: expense.paymentMethod,
      referenceNumber: expense.referenceNumber,
      description: expense.description,
      notes: expense.notes
    });
  }

  saveExpense(): void {
    if (this.expenseForm.invalid) {
      return;
    }

    this.isSaving.set(true);
    const expense: Expense = this.expenseForm.value;

    const operation = this.editingId()
      ? this.expenseService.updateExpense(this.editingId()!, expense)
      : this.expenseService.createExpense(expense);

    operation.subscribe({
      next: () => {
        this.loadExpenses();
        this.hideForm();
        this.isSaving.set(false);
      },
      error: () => {
        this.toast.error('Failed to save expense');
        this.isSaving.set(false);
      }
    });
  }

  deleteExpense(id: number): void {
    if (!confirm('Are you sure you want to delete this expense?')) {
      return;
    }

    this.expenseService.deleteExpense(id).subscribe({
      next: () => {
        this.loadExpenses();
      },
      error: () => {
        this.toast.error('Failed to delete expense');
      }
    });
  }

  getTotalExpenses(): number {
    return this.expenses().reduce((sum, exp) => sum + exp.amount, 0);
  }

  getCategoryLabel(value: string): string {
    return this.categories.find(c => c.value === value)?.label || value;
  }

  getPaymentMethodLabel(value: string): string {
    return this.paymentMethods.find(p => p.value === value)?.label || value;
  }
}
