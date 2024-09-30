"use client";

import React, { useState } from 'react';
import axios, { AxiosError } from 'axios';
import { FaCheckCircle, FaExclamationCircle } from 'react-icons/fa';
import './Page.css';

interface ErrorResponse {
  message: string;
}

const Page: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleComputeButtonClick = async () => {
    try{
      setIsLoading(true);
      const token = sessionStorage.getItem('token');
      await axios.post(`http://localhost:8080/api/python/execute`, {},{
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setSuccess(true);
      setError(null);
    } catch(error){
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<ErrorResponse>;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        }  else if(axiosError.response?.status == 422){
          setError(axiosError.response.data.message);
        } else {
          setError('There was an error in the server side, please try again later.');
      }
      } else {
        setError('There was an error in the server side, please try again later.');
      }
      setSuccess(false);
    }
    setIsLoading(false);
  };

  return (
    <div className="content-container">
        <div className="compute-info">
          <h2 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Schedule Computation</h2>
          <p>
            Before starting the schedule computation, please ensure that the following tasks are completed:
          </p>
          <ol>
            <li>All users have added their unavailabilities.</li>
            <li>All presentation information has been added.</li>
            <li>All rooms have been created.</li>
            <li>All time slots have been created.</li>
            <li>All unavailabilities related to presentations have been identified.</li>
            <li>All presentations that need to be scheduled one after the other have been identified.</li>
          </ol>
        </div>
      
      <button className="compute-button" onClick={handleComputeButtonClick} disabled={isLoading}>
        {isLoading ? 'Computing...' : 'Compute'}
      </button>
      {success && (
        <div className="success-message">
          <FaCheckCircle className="icon" />
          The schedule creation has started. This operation should take from 10 to 20 minutes.
        </div>
      )}
      {error && (
        <div className="error-message">
          <FaExclamationCircle className="icon" />
          {error}
        </div>
      )}
    </div>
  );
};

export default Page;
