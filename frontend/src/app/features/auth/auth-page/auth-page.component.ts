import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

@Component({
  selector: 'app-auth-page',
  templateUrl: './auth-page.component.html',
  styleUrls: ['./auth-page.component.css']
})
export class AuthPageComponent implements OnInit {

  isLogin = true;

  loginForm!: FormGroup;
  registerForm!: FormGroup;

  message = '';
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {

    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.pattern('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$')]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email, Validators.pattern('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$')]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['BUYER', Validators.required],
      businessName: ['']
    });
  }

  switchTab(val: boolean) {
    this.isLogin = val;
    this.message = '';
    this.error = '';
  }

  login() {

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.authService.login(this.loginForm.value)
      .subscribe({
        next: (response) => {
          this.message = "Login successful";

          const decoded: any = jwtDecode(response.token);
          const role = decoded.role || 'BUYER';

          if (role === 'SELLER') {
            this.router.navigateByUrl('/seller/dashboard');
          } else {
            this.router.navigateByUrl('/buyer/dashboard');
          }
        },
        error: (err: any) => {
          this.error = err?.error?.message || (typeof err.error === 'string' ? err.error : "Login failed");
          this.message = '';
        }
      });
  }

  register() {

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.authService.register(this.registerForm.value)
      .subscribe({
        next: (res: any) => {
          this.message = res || 'Registered successfully';
          this.error = '';
          this.isLogin = true;
        },
        error: (err: any) => {
          this.error = err?.error?.message || (typeof err.error === 'string' ? err.error : `Registration failed (${err.status || 'unknown'})`);
          this.message = '';
        }
      });
  }
}
