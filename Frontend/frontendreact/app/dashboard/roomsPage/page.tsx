"use client";

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosError } from 'axios';
import { FaTimes, FaCheck, FaExclamationCircle } from 'react-icons/fa';
import './Page.css';

interface RoomDTO {
  id: number;
  roomNr: string;
  building: string;
}

export default function Page() {
  const [rooms, setRooms] = useState<RoomDTO[]>([]);
  const [newRoom, setNewRoom] = useState<{ roomNr: string; building: string }>({
    roomNr: '',
    building: '',
  });
  const [showAddRoom, setShowAddRoom] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{
    show: boolean;
    roomId?: number;
  }>({ show: false });
  const [errorMessage, setErrorMessage] = useState<string>('');

  useEffect(() => {
    const fetchRoomsData = async () => {
    try{
      const token = sessionStorage.getItem('token');
      const response = await axios.get<RoomDTO[]>('http://localhost:8080/api/room', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setRooms(response.data)
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
          setErrorMessage('There was an error in the server side, please try again later.');
      }
      } else {
        setErrorMessage('There was an error in the server side, please try again later.');
      }
    }
  };
  fetchRoomsData();
  }, []);

  const handleDeleteRoom = (roomId: number) => {
    setDeleteConfirmation({ show: true, roomId });
  };

  const confirmDeleteRoom = async () => {
    if (deleteConfirmation.roomId) {
      try{
        const token = sessionStorage.getItem('token');
        const response = await axios.delete(`http://localhost:8080/api/room/${deleteConfirmation.roomId}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }); 
        setRooms(prevRooms => prevRooms.filter(room => room.id !== deleteConfirmation.roomId));
        setDeleteConfirmation({ show: false });
        setErrorMessage(''); // Clear any previous error messages
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
            setErrorMessage('There was an error in the server side, please try again later.');
        }
        } else {
          setErrorMessage('There was an error in the server side, please try again later.');
        }
      }
    }
  };

  const handleAddRoom = () => {
    setShowAddRoom(prevState => !prevState);
    if (!showAddRoom) {
        setNewRoom({ roomNr: '', building: '' }); // Reset input fields when showing the add room form
        setErrorMessage(''); // Clear any previous error messages
      }
  };

  const handleSubmitRoom = async () => {
    if (!newRoom.roomNr || !newRoom.building) {
      setErrorMessage('Please enter both room number and building.');
      return;
    }
    try{
      const token = sessionStorage.getItem('token');
      const response = await axios.post('http://localhost:8080/api/room', 
        newRoom
      ,{
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      setRooms(prevRooms => [...prevRooms, response.data]);
      setNewRoom({ roomNr: '', building: '' });
      setErrorMessage(''); // Clear any previous error messages
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
        } else if(axiosError.response?.status == 500){
          setErrorMessage('There was an error with the server. Please try again later.');
        } else if(axiosError.response?.status == 409){
          setErrorMessage('The room you are attempting to add has already been added.');
        } else {
          setErrorMessage('There was an error in the server side, please try again later.');
      }
      } else {
        setErrorMessage('There was an error in the server side, please try again later.');
      }
    }
  };

  const handleOutsideClick = (event: MouseEvent) => {
    if (deleteConfirmation.show && !modalRef.current?.contains(event.target as Node)) {
      setDeleteConfirmation({ show: false });
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleOutsideClick);
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, [deleteConfirmation]);

  const modalRef = useRef<HTMLDivElement>(null);

  return (
    <div className="container">
      <h1 className="header" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Rooms</h1>
      {rooms.map(room => (
        <div className="room" key={room.id}>
          <p>Room Number: {room.roomNr} - Building: {room.building}</p>
          <button className="delete-button" onClick={() => handleDeleteRoom(room.id)}><FaTimes /></button>
        </div>
      ))}
      {deleteConfirmation.show && (
        <div className="confirm-delete-overlay">
          <div className="confirm-delete-modal" ref={modalRef}>
            <p>Are you sure you want to delete this room?</p>
            <div className="confirm-delete-buttons">
              <button className="confirm-delete-button" onClick={confirmDeleteRoom}> <FaCheck /> Yes</button>
              <button className="confirm-delete-button" onClick={() => setDeleteConfirmation({ show: false })}> <FaTimes /> No</button>
            </div>
            {errorMessage && <div className="error-message"><FaExclamationCircle /> {errorMessage}</div>}
          </div>
        </div>
      )}
      <div style={{ marginTop: '10px' }}>
        <button className="add-room-button" onClick={handleAddRoom}>
          {showAddRoom ? 'Cancel' : 'Add Room'}
        </button>
      </div>
      {showAddRoom && (
        <div className="add-room-form">
          <div className="input-row">
            <input
              className="input-field"
              type="text"
              placeholder="Room Number"
              value={newRoom.roomNr}
              onChange={e => setNewRoom({ ...newRoom, roomNr: e.target.value })}
            />
            <input
              className="input-field"
              type="text"
              placeholder="Building"
              value={newRoom.building}
              onChange={e => setNewRoom({ ...newRoom, building: e.target.value })}
            />
            <button className="submit-room-button" onClick={handleSubmitRoom}>Submit</button>
          </div>
          {errorMessage && <div className="error-message"><FaExclamationCircle className="error-icon"/> {errorMessage}</div>}
        </div>
      )}
    </div>
  );
}

