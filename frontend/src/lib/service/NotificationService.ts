import {client} from "@/lib/backend/client";
import {EventSourcePolyfill} from 'event-source-polyfill';

export interface Notification {
    id: number;
    content: string;
    type: "NEW_COMMENT" | "NEW_REPLY" | "NEW_POST" | "NEW_FOLLOWER" | string;
    isRead: boolean;
    createdAt: string;
    senderId: number;
    senderNickname: string;
    relatedId: number;
}

// 서버에서 받는 알림 이벤트 DTO 인터페이스
export interface NotificationEventDTO {
    id?: number;
    senderId: number;
    senderNickname: string;
    receiverId: number;
    type: string;
    content: string;
    relatedId: number;
    isRead: boolean;
    createdAt?: string;
}

export class NotificationService {
    private static instance: NotificationService;
    private eventSource: any = null;
    private listeners: ((notification: Notification) => void)[] = [];

    private constructor() {
    }

    public static getInstance(): NotificationService {
        if (!NotificationService.instance) {
            NotificationService.instance = new NotificationService();
        }
        return NotificationService.instance;
    }

    // SSE 연결 설정
    public connect(memberId: number): void {
        if (this.eventSource) {
            console.log('기존 SSE 연결 종료 중...');
            this.disconnect();
        }

        const url = `${process.env.NEXT_PUBLIC_BASE_URL}/api/notifications/subscribe?memberId=${memberId}`;

        // EventSourcePolyfill 설정 개선
        const options = {
            withCredentials: true,
            heartbeatTimeout: 60000, // 60초
            connectionTimeout: 60000, // 60초
            headers: {
                'Cache-Control': 'no-cache',
                'X-Requested-With': 'XMLHttpRequest'
            }
        };

        console.log('SSE 연결 시도:', url, options);
        this.eventSource = new EventSourcePolyfill(url, options);

        this.eventSource.onmessage = (event) => {
            try {
                // 이벤트 타입 확인
                const eventType = event.type || 'message';
                const eventName = event.lastEventId || '';

                console.log(`SSE 이벤트 수신: ${eventType}, 이름: ${eventName}, 데이터: ${event.data}`);

                // 하트비트 이벤트 처리
                if (event.data && (event.data.includes('keep-alive') || event.data.includes('연결 성공'))) {
                    console.log('하트비트 또는 연결 확인 메시지 수신:', event.data);
                    return;
                }

                // 알림 이벤트 처리
                const eventData = JSON.parse(event.data);
                console.log('파싱된 알림 데이터:', eventData);

                // NotificationEventDTO 형식인지 확인
                if (eventData.senderId && eventData.receiverId) {
                    // DTO를 Notification 형식으로 변환
                    const notification: Notification = {
                        id: eventData.id || 0,
                        content: eventData.content,
                        type: eventData.type,
                        isRead: eventData.isRead || false,
                        createdAt: eventData.createdAt || new Date().toISOString(),
                        senderId: eventData.senderId,
                        senderNickname: eventData.senderNickname,
                        relatedId: eventData.relatedId
                    };
                    console.log('변환된 알림 객체:', notification);
                    this.notifyListeners(notification);
                } else {
                    // 기존 Notification 형식인 경우
                    const notification = eventData as Notification;
                    this.notifyListeners(notification);
                }
            } catch (error) {
                console.error("알림 데이터 파싱 오류:", error);
                console.error("원본 데이터:", event.data);
            }
        };

        this.eventSource.onerror = (error) => {
            // 오류 정보 로깅
            console.log("SSE 연결 오류 발생", error);

            // 연결 상태 확인 및 재연결 시도
            if (this.eventSource) {
                const state = this.eventSource.readyState;
                console.log(`현재 연결 상태: ${state}`);

                // 연결이 끊어졌거나 연결 중이 아닌 경우
                if (state !== 0) { // 0 = CONNECTING
                    this.disconnect();
                    console.log("SSE 연결 재시도 중...");
                    // 3초 후 재연결 시도 (시간 더 단축)
                    console.log(`3초 후 ${memberId} 사용자에 대한 SSE 재연결 시도`);
                    setTimeout(() => this.connect(Number(memberId)), 3000);
                }
            }
        };

        // 연결 상태 모니터링
        const checkConnection = () => {
            if (this.eventSource && this.eventSource.readyState === 1) { // 1 = OPEN
                console.log('SSE 연결 상태 양호');
            } else if (this.eventSource) {
                console.log(`SSE 연결 상태 비정상: ${this.eventSource.readyState}`);
                this.disconnect();
                console.log(`자동 재연결 시도: ${memberId}`);
                setTimeout(() => this.connect(Number(memberId)), 1000);
            }
        };

        // 30초마다 연결 상태 확인
        const connectionCheckInterval = setInterval(checkConnection, 30000);

        // 연결 종료 시 인터벌 제거
        this.eventSource.onclose = () => {
            clearInterval(connectionCheckInterval);
            console.log('SSE 연결 종료');
        };

        // 특정 이벤트 리스너 추가
        this.eventSource.addEventListener('connect', (event) => {
            console.log('초기 연결 성공:', event.data);
        });

        this.eventSource.addEventListener('heartbeat', (event) => {
            console.log('하트비트 수신:', event.data);
        });
    }

    // SSE 연결 해제
    public disconnect(): void {
        if (this.eventSource) {
            try {
                this.eventSource.close();
                console.log('SSE 연결 종료 성공');
            } catch (e) {
                console.error('SSE 연결 종료 오류:', e);
            } finally {
                this.eventSource = null;
            }
        }
    }

    // 알림 리스너 등록
    public addListener(listener: (notification: Notification) => void): void {
        this.listeners.push(listener);
    }

    // 알림 리스너 제거
    public removeListener(listener: (notification: Notification) => void): void {
        this.listeners = this.listeners.filter((l) => l !== listener);
    }

    // 리스너들에게 알림
    private notifyListeners(notification: Notification): void {
        this.listeners.forEach((listener) => listener(notification));
    }

    // 읽지 않은 알림 목록 가져오기
    public async getUnreadNotifications(): Promise<Notification[]> {
        try {
            console.log('알림 목록 요청 시작');
            const response = await client.GET("/api/notifications", {
                credentials: "include"
            });

            console.log('알림 목록 응답:', response);
            if (response.data && response.data.data) {
                const notifications = response.data.data;
                console.log('받아온 알림 목록:', notifications);
                return notifications;
            }
            console.log('알림 데이터가 없습니다');
            return [];
        } catch (error) {
            console.error("알림 목록 가져오기 실패:", error);
            return [];
        }
    }

    // 알림 읽음 표시
    public async markAsRead(notificationId: number): Promise<boolean> {
        try {
            await client.PATCH(`/api/notifications/{notificationId}/read`, {
                params: {
                    path: {notificationId}
                },
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: "include"
            });
            return true;
        } catch (error) {
            console.error("알림 읽음 표시 실패:", error);
            return false;
        }
    }
}
