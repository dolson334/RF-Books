import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { IncomeService } from './income.service';
import { Income, INCOME_CATEGORIES, PAYMENT_METHODS } from './income.models';

@Component({
  selector: 'rf-income',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './income.component.html',
  styleUrls: ['./income.component.scss']
})
export class IncomeComponent implements OnInit {
  incomeForm: FormGroup;
  incomes = signal<Income[]>([]);
  isLoading = signal<boolean>(false);
  isSaving = signal<boolean>(false);
  editingId = signal<number | null>(null);
  showForm = signal<boolean>(false);

  categories = INCOME_CATEGORIES;
  paymentMethods = PAYMENT_METHODS;

  constructor(
    private fb: FormBuilder,
    private incomeService: IncomeService
  ) {
    this.incomeForm = this.fb.group({
      incomeDate: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      source: ['', Validators.required],
      category: ['', Validators.required],
      paymentMethod: [''],
      referenceNumber: [''],
      description: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadIncome();
  }

  loadIncome(): void {
    this.isLoading.set(true);
    this.incomeService.getAllIncome().subscribe({
      next: (income) => {
        this.incomes.set(income);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load income', err);
        this.isLoading.set(false);
      }
    });
  }

  showAddForm(): void {
    this.showForm.set(true);
    this.editingId.set(null);
    this.incomeForm.reset({
      incomeDate: new Date().toISOString().split('T')[0]
    });
  }

  hideForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
    this.incomeForm.reset();
  }

  editIncome(income: Income): void {
    this.showForm.set(true);
    this.editingId.set(income.id!);
    this.incomeForm.patchValue({
      incomeDate: income.incomeDate,
      amount: income.amount,
      source: income.source,
      category: income.category,
      paymentMethod: income.paymentMethod,
      referenceNumber: income.referenceNumber,
      description: income.description,
      notes: income.notes
    });
  }

  saveIncome(): void {
    if (this.incomeForm.invalid) {
      return;
    }

    this.isSaving.set(true);
    const income: Income = this.incomeForm.value;

    const operation = this.editingId()
      ? this.incomeService.updateIncome(this.editingId()!, income)
      : this.incomeService.createIncome(income);

    operation.subscribe({
      next: () => {
        this.loadIncome();
        this.hideForm();
        this.isSaving.set(false);
      },
      error: (err) => {
        console.error('Failed to save income', err);
        this.isSaving.set(false);
      }
    });
  }

  deleteIncome(id: number): void {
    if (!confirm('Are you sure you want to delete this income entry?')) {
      return;
    }

    this.incomeService.deleteIncome(id).subscribe({
      next: () => {
        this.loadIncome();
      },
      error: (err) => {
        console.error('Failed to delete income', err);
      }
    });
  }

  getTotalIncome(): number {
    return this.incomes().reduce((sum, inc) => sum + inc.amount, 0);
  }

  getCategoryLabel(value: string): string {
    return this.categories.find(c => c.value === value)?.label || value;
  }

  getPaymentMethodLabel(value: string): string {
    return this.paymentMethods.find(p => p.value === value)?.label || value;
  }
}
