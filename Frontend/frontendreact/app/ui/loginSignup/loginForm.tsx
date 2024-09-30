import React, { useState } from 'react';
import { Container, Typography, TextField, Button, Box, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import axios from 'axios';

function getSubjectFromToken(token: string): string {
  const [, payloadBase64] = token.split('.');
  const payload = JSON.parse(atob(payloadBase64));
  return payload.sub; // Return the value of the 'sub' claim
}

const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [emailError, setEmailError] = useState('');
  const [loginError, setLoginError] = useState('');
  const [forgotPasswordOpen, setForgotPasswordOpen] = useState(false);
  const [emailSent, setEmailSent] = useState(false);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');


  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        password,
      });
      sessionStorage.clear();
      sessionStorage.setItem('token', response.data.token);
      sessionStorage.setItem('id', getSubjectFromToken(response.data.token));
      // Redirect to dashboard after successful login
      window.location.href = `/dashboard`;
    } catch (error) {
      console.error('Login failed:', error);
      setLoginError('Your email or password is incorrect.');
    }
  };

  const validateEmail = (email: string) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newEmail = e.target.value;
    setEmail(newEmail);
    setLoginError('');
    if (!validateEmail(newEmail)) {
      setEmailError('Invalid email format');
    } else {
      setEmailError('');
    }
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
    setLoginError('');
  };

  const handleForgotPasswordClick = () => {
    setForgotPasswordOpen(true);
    setEmailSent(false); // Reset emailSent state when opening the dialog
  };

  const handleForgotPasswordClose = () => {
    setForgotPasswordOpen(false);
  };

  const handleForgotPasswordSubmit = async () => {
    setEmailSent(true); // Set emailSent state to true immediately after clicking "Send"
    try {
      setForgotPasswordOpen(false);
      await axios.post('http://localhost:8080/api/auth/forgotMyPassword', { email: forgotPasswordEmail });
      // Optionally, show success message to user
      //alert('Password reset email sent successfully!');
      console.log("Email sent")
    } catch (error) {
      console.error('Error sending password reset email:', error);
      // Optionally, show error message to user
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Typography component="h1" variant="h5">
        Log in
      </Typography>
      <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
        <TextField
          margin="normal"
          required
          fullWidth
          id="email"
          label="Email Address"
          name="email"
          autoComplete="email"
          autoFocus
          value={email}
          onChange={handleEmailChange}
          error={!!emailError}
          helperText={emailError}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="password"
          label="Password"
          type="password"
          id="password"
          autoComplete="current-password"
          value={password}
          onChange={handlePasswordChange}
        />
        {loginError && (
          <Typography variant="body2" color="error" gutterBottom>
            {loginError}
          </Typography>
        )}
        <Button
          type="submit"
          fullWidth
          variant="contained"
          sx={{
            mt: 3,
            mb: 2,
            bgcolor: 'primary.main',
            '&:hover': {
              bgcolor: 'primary.light',
            },
            '&:active': {
              bgcolor: 'primary.dark',
            },
            color: 'white',
            '&:not(:hover)': {
              bgcolor: 'primary.main',
            },
          }}
        >
          Log In
        </Button>
        <Button onClick={handleForgotPasswordClick} fullWidth>
          Forgot my password?
        </Button>
      </Box>

      {/* Forgot Password Dialog */}
      <Dialog open={forgotPasswordOpen} onClose={handleForgotPasswordClose}>
        <DialogTitle>Forgot My Password?</DialogTitle>
        <DialogContent sx={{ minWidth: '300px' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="forgotPasswordEmail"
            label="Email Address"
            name="forgotPasswordEmail"
            autoComplete="email"
            value={forgotPasswordEmail}
            onChange={(e) => setForgotPasswordEmail(e.target.value)}
            error={!!emailError}
            helperText={emailError}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleForgotPasswordClose} color="secondary">
            Cancel
          </Button>
          <Button onClick={handleForgotPasswordSubmit} color="primary">
            Send
          </Button>
        </DialogActions>
      </Dialog>

      {/* Email Sent Message */}
      <Dialog open={emailSent} onClose={() => setEmailSent(false)}>
        <DialogTitle>Forgot my password?</DialogTitle>
        <DialogContent>
          <Typography>
            If the email you provided is associated with an account in our application, you will receive a password reset email shortly. Please check your inbox.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEmailSent(false)} color="primary">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default LoginForm;
