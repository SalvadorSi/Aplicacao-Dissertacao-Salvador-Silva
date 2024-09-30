"use client";

import React, { useState, useEffect } from 'react';
import axios, { AxiosError } from 'axios';
import './Page.css';

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

const Page: React.FC = () => {
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [userUnavailabilitySlots, setUserUnavailabilitySlots] = useState<number[]>([]);
  const [message, setMessage] = useState<string>(
    "Here you can add your restrictions by clicking on the slots you are not available.\nGreen Slot -> You are available.\nRed Slot -> You are unavailable."
  );

  useEffect(() => {
    fetchData();
    fetchUserUnavailabilitySlots();
  }, []);

  const fetchData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<SlotDTO[]>(`http://localhost:8080/api/slot`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = response.data;
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

  const fetchUserUnavailabilitySlots = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
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

  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };

  const toggleSlotAvailability = async (slotID: string) => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      if (userUnavailabilitySlots.includes(parseInt(slotID))) {
        await axios.delete('http://localhost:8080/api/unavailability', {
          data: { slotID, userID: userId } ,
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      } else {
        await axios.post('http://localhost:8080/api/unavailability', {
          slotID,
          userID: userId
      },{
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      }
      fetchUserUnavailabilitySlots(); // Refresh user unavailability slots after toggle
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

  const formatDate = (date: string): string => {
    const [year, month, day] = date.split('-');
    return `${day}-${month}-${year}`;
  };

  return (
    <div className="container">
      <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>My Unavailabilities</h1>
      {slots.length === 0 ? (
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                Wait for the organizer to add the available slots.
            </div>
        ) : (
            <>
              <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                {message}
              </div>
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
                            const isUserUnavailabilitySlot = userUnavailabilitySlots.includes(parseInt(slot?.id ?? '0'));
                            return (
                              <td key={`${date}-${hour}`} className={slot ? (isUserUnavailabilitySlot ? 'slot-unavailable' : 'slot-exists') : 'slot-not-exists'}
                              onClick={() => {
                                if (slot) {
                                  toggleSlotAvailability(slot.id);
                                }
                              }}>
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
};

export default Page;
