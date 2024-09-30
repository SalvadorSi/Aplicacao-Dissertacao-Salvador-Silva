

'use client';

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosError } from 'axios';
import { FaExclamationCircle, FaCheck, FaTimes } from 'react-icons/fa';
import './Page.css';

const API_URL = 'http://localhost:8080/api';

interface Presentation {
  id: number;
  thesisTitle: string;
}

interface FollowedPresentation {
  id: number;
  userID: number;
  presentationsIDS: number[];
}

export default function Page() {
  const [presentations, setPresentations] = useState<Presentation[]>([]);
  const [selectedPresentations, setSelectedPresentations] = useState<number[]>([]);
  const [followedPresentations, setFollowedPresentations] = useState<FollowedPresentation[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{ show: boolean; slotId: number | null }>({ show: false, slotId: null });
  const [deleteConfirmationErrorMessage, setDeleteConfirmationErrorMessage] = useState<string>('');

  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    async function fetchData() {
      try {
        const token = sessionStorage.getItem('token');
        const userId = sessionStorage.getItem('id');
        const presentationsResponse = await axios.get(`${API_URL}/presentation/adviserAndArguer/${userId}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }); 
        setPresentations(presentationsResponse.data);

        const followedPresentationsResponse = await axios.get(`${API_URL}/followedPresentations/user/${userId}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }); 
        setFollowedPresentations(followedPresentationsResponse.data);
      } catch (error) {
        if (axios.isAxiosError(error)) {
          const axiosError = error as AxiosError;
          if (axiosError.response?.status === 401) {
              // Unauthorized, remove token and id, and redirect to login page
              sessionStorage.removeItem('token');
              sessionStorage.removeItem('id');
              if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
                sessionStorage.setItem('sessionExpiredAlertShown', 'true');
                alert("Your session has expired. Please log in again if you want to continue.");
            }
              window.location.href = 'http://localhost:3000';
          } else {
            setError('There was an error in the server side, please try again later.');
        }
        } else {
          setError('There was an error in the server side, please try again later.');
        }
      }
    }
    fetchData();
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleClickOutside = (event: MouseEvent) => {
    if (modalRef.current && !modalRef.current.contains(event.target as Node)) {
      setDeleteConfirmation({ show: false, slotId: null });
      setDeleteConfirmationErrorMessage('');
    }
  };  

  const handleCheckboxChange = (id: number) => {
    const selectedIndex = selectedPresentations.indexOf(id);
    if (selectedIndex === -1) {
      setSelectedPresentations([...selectedPresentations, id]);
    } else {
      setSelectedPresentations(selectedPresentations.filter(presentationId => presentationId !== id));
    }
    setError(null);
  };

  const handleSubmit = async () => {
    if (selectedPresentations.length !== 2) {
      setError('You can only choose 2 presentations.');
      return;
    }

    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      await axios.post(`${API_URL}/followedPresentations`, {
        userID: userId,
        presentationsIDS: selectedPresentations,
      },{
        headers: {
          Authorization: `Bearer ${token}`
        }
      });      
      const updatedFollowedPresentationsResponse = await axios.get(`${API_URL}/followedPresentations/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }); 
      setFollowedPresentations(updatedFollowedPresentationsResponse.data);
      setSelectedPresentations([]); // Clear selected presentations after submission
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        } else {
          setError('There was an error in the server side, please try again later.');
      }
      } else {
        setError('There was an error in the server side, please try again later.');
      }
    }
  };

  const deleteSlot = async (slotId: number) => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      await axios.delete(`${API_URL}/followedPresentations/${slotId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }); 
      const updatedFollowedPresentationsResponse = await axios.get(`${API_URL}/followedPresentations/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }); 
      setFollowedPresentations(updatedFollowedPresentationsResponse.data);
      setDeleteConfirmation({ show: false, slotId: null });
      setError(null);
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status === 401) {
            // Unauthorized, remove token and id, and redirect to login page
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('id');
            if (!sessionStorage.getItem('sessionExpiredAlertShown')) {
              sessionStorage.setItem('sessionExpiredAlertShown', 'true');
              alert("Your session has expired. Please log in again if you want to continue.");
          }
            window.location.href = 'http://localhost:3000';
        } else {
          setDeleteConfirmationErrorMessage('There was an error in the server side, please try again later.');
      }
      } else {
        setDeleteConfirmationErrorMessage('There was an error in the server side, please try again later.');
      }
    }
  };

  return (
    <div className="container">
      <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Group My Presentations</h1>
      {presentations.length === 0 && <div>You don't have any presentations.</div>}
      {presentations.length > 0 && (
        <>
          <p className='message-spacing'>Bear in mind that the program attempts to schedule presentations from the same user in consecutive time slots.</p>
          <p className='message-spacing'>Please choose the presentations you want to be assigned to followed slots.</p>
          {presentations.map(presentation => (
            <div key={presentation.id} className="presentation-item">
              <input
                type="checkbox"
                checked={selectedPresentations.includes(presentation.id)}
                onChange={() => handleCheckboxChange(presentation.id)}
                className='check-box'
              />
              <span onClick={() => handleCheckboxChange(presentation.id)}>{presentation.thesisTitle}</span>
            </div>
          ))}
          <button className="submit-button" onClick={handleSubmit}>Submit</button>
          {error && <div className="error-message"><FaExclamationCircle className="error-icon" /> {error}</div>}
          {followedPresentations.length > 0 && (
            <div>
              <hr className="hr-divider"/>
              <p className='message-spacing'>These are the presentations you have already asked to be followed:</p>
              <ul>
                {followedPresentations.map((followedPresentation) => (
                  <li key={followedPresentation.id}>
                    {followedPresentation.presentationsIDS.map((presentationId, innerIndex) => (
                      <React.Fragment key={presentationId}>
                        <span style={{ fontWeight: 'bold' }}>
                          {presentations.find((presentation) => presentation.id === presentationId)?.thesisTitle}
                        </span>
                        {innerIndex !== followedPresentation.presentationsIDS.length - 1 && " and "}
                      </React.Fragment>
                    ))}
                    <button className="delete-button" onClick={() => {setError(null);setDeleteConfirmation({ show: true, slotId: followedPresentation.id });}}><FaTimes /></button>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
      {deleteConfirmation.show && (
        <div className="confirm-delete-overlay">
          <div className="confirm-delete-modal" ref={modalRef}>
            <p>Are you sure you want to delete the link between this presentations?</p>
            <div className="confirm-delete-buttons">
              <button className="confirm-delete-button" onClick={() => deleteSlot(deleteConfirmation.slotId ?? -1)}>
                <FaCheck /> Yes
              </button>
              <button className="confirm-delete-button" onClick={() => {
                setDeleteConfirmation({ show: false, slotId: null });
                setDeleteConfirmationErrorMessage(''); // Reset the error message state when the user clicks "No"
              }}>
                <FaTimes /> No
              </button>
            </div>
            {deleteConfirmationErrorMessage && <div className="error-message"><FaExclamationCircle className="error-icon"/> {deleteConfirmationErrorMessage}</div>}
          </div>
        </div>
      )}
    </div>
  );
}

