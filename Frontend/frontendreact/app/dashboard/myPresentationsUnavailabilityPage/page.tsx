"use client";

import React, { useState, useEffect } from 'react';
import axios, { AxiosError } from 'axios';
import './Page.css';

interface PresentationDTO {
  id: number;
  thesisTitle: string;
}

interface SlotDTO {
  id: string;
  date: string;
  startingHour: string;
}

interface UnavailabilityDTO {
  id: number;
  slotsID: string[];
  presentationID: number;
  userID: number;
}

const Page: React.FC = () => {
  const [presentations, setPresentations] = useState<PresentationDTO[]>([]);
  const [selectedPresentation, setSelectedPresentation] = useState<number | null>(null);
  const [slots, setSlots] = useState<SlotDTO[]>([]);
  const [dates, setDates] = useState<string[]>([]);
  const [hours, setHours] = useState<string[]>([]);
  const [unavailability, setUnavailability] = useState<UnavailabilityDTO | null>(null);
  const [message, setMessage] = useState<string>(
    "Here you can add restrictions to a single presentation.\n Please choose a presentation and click on the slots below.\nGreen Slot -> You are available.\nRed Slot -> You are unavailable."
  );

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const userId = sessionStorage.getItem('id');
      const presentationResponse = await axios.get<PresentationDTO[]>(`http://localhost:8080/api/presentation/adviser/${userId}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const presentationData = presentationResponse.data;
      setPresentations(presentationData);
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

  const handlePresentationChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPresentation(event.target.value ? Number(event.target.value) : null);
  };

  useEffect(() => {
    if (selectedPresentation !== null) {
      fetchSlots();
      fetchUnavailability();
    } else {
      setSlots([]);
      setDates([]);
      setHours([]);
      setUnavailability(null);
    }
  }, [selectedPresentation]);

  const fetchSlots = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<SlotDTO[]>('http://localhost:8080/api/slot', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const slotData = response.data;
      setSlots(slotData);

      const uniqueDates = Array.from(new Set(slotData.map(slot => slot.date)));
      const uniqueHours = Array.from(new Set(slotData.map(slot => slot.startingHour)));
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

  const fetchUnavailability = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const response = await axios.get<UnavailabilityDTO>(`http://localhost:8080/api/unavailability/presentation/${selectedPresentation}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const unavailabilityData = response.data;
      setUnavailability(unavailabilityData);
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

  const calculateFinishHour = (startingHour: string): string => {
    const [hour, minute] = startingHour.split(':').map(parseFloat);
    const finishHour = hour + (minute + 45) / 60;
    const finishHourHour = Math.floor(finishHour);
    const finishHourMinute = Math.round((finishHour - finishHourHour) * 60);
    return `${finishHourHour.toString().padStart(2, '0')}:${finishHourMinute.toString().padStart(2, '0')}`;
  };

  const isSlotUnavailable = (slotId: string): boolean => {
    return unavailability?.slotsID.includes(slotId) ?? false;
  };

  const handleSlotClick = async (slotId: string) => {
    if (isSlotUnavailable(slotId)) {
      await removeUnavailability(slotId);
    } else {
      await addUnavailability(slotId);
    }
  };

  const addUnavailability = async (slotId: string) => {
    try {
      const token = sessionStorage.getItem('token');
      await axios.post('http://localhost:8080/api/unavailability/presentation', {
        slotID: slotId,
        presentationID: selectedPresentation,
        userID: 1 
    },{
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
      await fetchUnavailability(); // Refresh unavailability data after adding
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

  const removeUnavailability = async (slotId: string) => {
    try {
      const token = sessionStorage.getItem('token');
      await axios.delete('http://localhost:8080/api/unavailability/presentation', {
        data: {
          slotID: slotId,
          presentationID: selectedPresentation,
          userID: 1 
        }, 
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      await fetchUnavailability(); // Refresh unavailability data after removing
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

  return (
    <div className="container">
      <div className="header">
        <h1 className="title" style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333', marginBottom: '1rem', textAlign: 'center' }}>My Presentations Unavailabilities</h1>
        <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem' }}>
          {message}
        </div>
        <select value={selectedPresentation ?? ''} className="input-field" onChange={handlePresentationChange}>
          <option value="">Select Your Presentation</option>
          {presentations.map(presentation => (
            <option key={presentation.id} value={presentation.id}>{presentation.thesisTitle}</option>
            ))}
          </select>
        </div>
        {selectedPresentation && (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            {slots.length === 0 ? (
            <div className="message" style={{ whiteSpace: 'pre-line', marginBottom: '1rem'}}>
                Wait for the organizer to add the available slots.
            </div>
        ) : (
            <>
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
                            const slotId = slot?.id ?? '';
                            let slotClassName = '';
                            if (slot) {
                            slotClassName = isSlotUnavailable(slotId) ? 'slot-unavailable' : 'slot-exists';
                            } else {
                            slotClassName = 'slot-not-exists';
                            }
                            return (
                            <td
                                key={`${date}-${hour}`}
                                className={slotClassName}
                                onClick={slot ? () => handleSlotClick(slotId) : undefined} // Handle slot click
                            >
                                {slot && <div className="slot-content"></div>}
                            </td>
                        );
                    })}
                    </tr>
                );
                  })}
                </tbody>
              </table>
            </>
        )}
          </div>
        )}
      </div>
    );
  };
  
  export default Page;
  


