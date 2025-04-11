"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { Notification, NotificationService } from "@/lib/service/NotificationService";
import { getUserIdFromToken } from "@/app/utils/auth";

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  addNotification: (notification: Notification) => void;
  markAsRead: (notificationId: number) => Promise<void>;
  fetchNotifications: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error("useNotification must be used within a NotificationProvider");
  }
  return context;
};

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const notificationService = NotificationService.getInstance();

  // 알림 추가
  const addNotification = (notification: Notification) => {
    setNotifications((prev) => {
      // 중복 알림 방지
      const isDuplicate = notification.id && prev.some(item => item.id === notification.id);
      if (isDuplicate) {
        return prev;
      }
      return [notification, ...prev];
    });
  };

  // 알림 읽음 처리
  const markAsRead = async (notificationId: number) => {
    const success = await notificationService.markAsRead(notificationId);
    if (success) {
      setNotifications((prev) =>
        prev.map((notification) =>
          notification.id === notificationId
            ? { ...notification, isRead: true }
            : notification
        )
      );
    }
  };

  // 읽지 않은 알림 개수
  const unreadCount = notifications.filter((notification) => !notification.isRead).length;

  // 알림 목록 가져오기
  const fetchNotifications = async () => {
    try {
      const unreadNotifications = await notificationService.getUnreadNotifications();
      setNotifications(unreadNotifications);
    } catch (error) {
      console.error('알림 목록 가져오기 오류:', error);
    }
  };

  // 초기 설정 및 SSE 연결
  useEffect(() => {
    const userId = getUserIdFromToken();
    if (userId) {

      // 알림 목록 가져오기
      fetchNotifications();
      
      // 주기적으로 알림 목록 갱신 (1분마다)
      const intervalId = setInterval(() => {
        fetchNotifications();
      }, 60000);

      // SSE 연결 설정
      notificationService.connect(Number(userId));
      
      // 알림 리스너 등록
      const handleNotification = (notification: Notification) => {
        // 중복 알림 방지
        setNotifications(prev => {
          // 이미 동일한 ID의 알림이 있는지 확인
          const isDuplicate = notification.id && prev.some(item => item.id === notification.id);
          if (isDuplicate) {
            return prev;
          }
          return [notification, ...prev];
        });
      };

      notificationService.addListener(handleNotification);

      return () => {
        clearInterval(intervalId);
        notificationService.removeListener(handleNotification);
        notificationService.disconnect();
      };
    }
  }, [getUserIdFromToken, notificationService]); // userId 변경 감지를 위해 getUserIdFromToken 추가

  const value = {
    notifications,
    unreadCount,
    addNotification,
    markAsRead,
    fetchNotifications,
  };

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};
