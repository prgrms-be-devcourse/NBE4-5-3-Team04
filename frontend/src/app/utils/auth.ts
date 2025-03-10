// JWT 디코딩 함수
function decodeJWT(token: string) {
  try {
    const base64Payload = token
      .split(".")[1]
      .replace(/-/g, "+")
      .replace(/_/g, "/");
    const decodedPayload = JSON.parse(atob(base64Payload));
    return decodedPayload;
  } catch (error) {
    console.error("JWT 디코딩 실패:", error);
    return null;
  }
}

// 쿠키에서 Access Token 가져오기
export function getAccessTokenFromCookie(): string | null {
  return (
    document.cookie
      .split("; ")
      .find((row) => row.startsWith("accessToken="))
      ?.split("=")[1] || null
  );
}

// Access Token에서 id 추출하기 (쿠키 기반)
export function getUserIdFromToken(): string | null {
  const token = getAccessTokenFromCookie(); // 쿠키에서 가져오기
  if (!token) return null;

  const payload = decodeJWT(token);
  return payload?.id || null; // JWT payload에 있는 id 반환
}

// Access Token을 sessionStorage에 저장
export function saveAccessTokenFromCookie() {
  const accessToken = getAccessTokenFromCookie();
  if (accessToken) {
    sessionStorage.setItem("accessToken", accessToken);
  }
}

// accessToken 불러오기
export function getAccessToken(): string | null {
  return typeof window !== "undefined"
    ? sessionStorage.getItem("accessToken")
    : null;
}

// Access Token 삭제 (sessionStorage에서 제거)
export function removeAccessToken() {
  sessionStorage.removeItem("accessToken");
}
