import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {

  resetForm!: FormGroup;

  message='';
  error='';

  constructor(private fb:FormBuilder,
              private authService:AuthService){}

  ngOnInit(){

    this.resetForm=this.fb.group({

      email:['',[Validators.required,Validators.email]],
      token:[''],

      newPassword:['',Validators.required],

      confirmPassword:['',Validators.required]

    });

  }

  resetPassword(){

    if(this.resetForm.invalid){

      this.resetForm.markAllAsTouched();
      return;

    }

    const email=this.resetForm.value.email;
    const token=this.resetForm.value.token;
    const newPassword=this.resetForm.value.newPassword;
    const confirmPassword=this.resetForm.value.confirmPassword;

    if(!token){
      this.authService.forgotPassword({ email }).subscribe({
        next:(res:any)=>{
          this.message=res || 'Reset token sent to your email';
          this.error='';
        },
        error:(err:any)=>{
          this.error = typeof err.error === 'string' ? err.error : 'Failed to send reset token';
          this.message = '';
        }
      });
      return;
    }

    if(newPassword!==confirmPassword){

      this.error="Passwords do not match";
      this.message='';
      return;

    }

    const payload={
      email:email,
      token:token,
      newPassword:newPassword
    };

    this.authService.resetPassword(payload)
    .subscribe({

      next:(res:any)=>{

        this.message=res;
        this.error='';

      },

      error: (err: any) => {

  console.log(err);

  if (err.error && typeof err.error === 'string') {
    this.error = err.error;
  } 
  else if (err.error && err.error.message) {
    this.error = err.error.message;
  } 
  else {
    this.error = "Something went wrong. Please try again.";
  }

  this.message = '';

}

    });

  }

}
