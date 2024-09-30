"use client";

import React, { useState, useEffect, useRef } from 'react';
import axios, { AxiosError } from 'axios';

import './Page.css'; 

interface User {
    id: number;
    name: string;
    email: string;
    role: string;
}

interface SlotDTO {
    id: string;
    date: string;
    startingHour: string;
}

interface UnavailabilityDTO {
    id: number;
    slotsID: number[];
    userID: number;
}

export default function Page() {
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
    const [slots, setSlots] = useState<SlotDTO[]>([]);
    const [dates, setDates] = useState<string[]>([]);
    const [hours, setHours] = useState<string[]>([]);
    const [userUnavailabilitySlots, setUserUnavailabilitySlots] = useState<number[]>([]);

    useEffect(() => {
        fetchData();
      }, []);
    
    const fetchData = async () => {
        try {
        const token = sessionStorage.getItem('token');
        const userResponse = await axios.get<User[]>('http://localhost:8080/api/user', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        setUsers(userResponse.data);
        const slotResponse = await axios.get<SlotDTO[]>(`http://localhost:8080/api/slot`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        const data = slotResponse.data;
        setSlots(data);
        const uniqueDates = Array.from(new Set(data.map(slot => slot.date)));
        const uniqueHours = Array.from(new Set(data.map(slot => slot.startingHour)));
        setDates(uniqueDates);
        setHours(uniqueHours.sort());
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
                  //setErrorMessage('There was an error in the server side, please try again later.');
                }
              } else {
                //setErrorMessage('There was an error in the server side, please try again later.');
              }
        }
    };

    const fetchUserUnavailabilitySlots = async (userId: number) => {
        try {
          const token = sessionStorage.getItem('token');
          const response = await axios.get<UnavailabilityDTO>(`http://localhost:8080/api/unavailability/user/${userId}`, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }); 
          setUserUnavailabilitySlots(response.data.slotsID);
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
              //setErrorMessage('There was an error in the server side, please try again later.');
          }
          } else {
            //setErrorMessage('There was an error in the server side, please try again later.');
          }
        }
      };
    
    const handleUserChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedValue = parseInt(event.target.value);
        setSelectedUserId(selectedValue === 0 ? null : selectedValue);
        if (selectedValue !== null && selectedValue !== 0) {
            fetchUserUnavailabilitySlots(selectedValue);
        }
    };

    const calculateFinishHour = (startingHour: string): string => {
        const [hour, minute] = startingHour.split(':').map(parseFloat);
        const finishHour = hour + (minute + 45) / 60;
        const finishHourHour = Math.floor(finishHour);
        const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
        return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
    };

    const formatDate = (date: string): string => {
        const [year, month, day] = date.split('-');
        return `${day}-${month}-${year}`;
    };

    return (
        <div className="container">
            <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>Unavailabilities Manager</h1>
            <select id="userSelect" className="input-field" value={selectedUserId || 0} onChange={handleUserChange}>
                <option value="0">Select one user</option>
                {users.sort((a, b) => a.name.localeCompare(b.name)).map(user => (
                <option key={user.id} value={user.id}>
                    {user.name}
                </option>
                ))}
            </select>
            {slots.length === 0 ? (
                <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                    There are no slots.
                </div>
            ) : (
            <>
              <div className="table-container" style={{ overflowX: 'auto' }}>
                <table className="slots-table">
                  <thead>
                    <tr>
                      <th className="hour-cell"></th>
                      {dates.sort().map(date => (
                        <th key={date} className="date-cell">{formatDate(date)}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                  {hours.map(hour => {
                    const startHourParts = hour.split(':');
                    const startHour = `${startHourParts[0]}:${startHourParts[1]}`;
                    const finishHour = calculateFinishHour(hour);
                    return (
                        <tr key={hour}>
                            <td className="hour-cell">{startHour} - {finishHour}</td>
                            {dates.map(date => {
                                const slot = slots.find(s => s.date === date && s.startingHour === hour);
                                let slotClassName = 'slot-not-exists'; // Default to slot-not-exists
                                if (slot) {
                                    // Slot exists, apply user unavailability logic if a user is selected
                                    if (selectedUserId && userUnavailabilitySlots.includes(parseInt(slot.id))) {
                                        slotClassName = 'slot-unavailable';
                                    } else {
                                        slotClassName = 'slot-exists';
                                    }
                                }
                                return (
                                    <td key={`${date}-${hour}`} className={slotClassName}>
                                    </td>
                                );
                            })}
                        </tr>
                    );
                })}

                  </tbody>
                </table>
              </div>
            </>
        )}
        </div>
    );
}
